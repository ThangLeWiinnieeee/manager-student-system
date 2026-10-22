package vn.edu.fit.topicmanagement.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.user.PasswordResetForm;

/** One bounded recovery record per account; no OTP or password is stored in plaintext. */
@Service
@Transactional(noRollbackFor = BusinessRuleException.class)
public class PasswordRecoveryService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Logger LOG = LoggerFactory.getLogger(PasswordRecoveryService.class);
    private static final String INVALID = "Mã không hợp lệ, đã hết hạn hoặc đã bị thay thế. Vui lòng yêu cầu mã mới nếu cần.";
    private final JdbcTemplate db;
    private final PasswordEncoder encoder;
    private final JavaMailSender mail;
    private final String from;

    public PasswordRecoveryService(JdbcTemplate db, PasswordEncoder encoder,
            JavaMailSender mail, @Value("${app.mail.from:}") String from) {
        this.db = db;
        this.encoder = encoder;
        this.mail = mail;
        this.from = from;
    }

    public static String newFlow() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public void send(String flow, String email, String requestId) {
        if (from.isBlank()) {
            throw new BusinessRuleException("Chức năng gửi email chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        var accounts = db.query("SELECT id, email, password_hash, enabled FROM users WHERE LOWER(email) = ? FOR UPDATE",
                (rs, n) -> new Account(rs.getLong("id"), rs.getString("email"),
                        rs.getString("password_hash"), rs.getBoolean("enabled")), email);
        // Same public response for unknown, disabled, throttled and accepted accounts.
        if (accounts.size() != 1 || !accounts.getFirst().enabled()) return;
        Account account = accounts.getFirst();
        Instant now = Instant.now();
        var existing = db.query("SELECT * FROM password_recovery WHERE user_id = ?", this::read, account.id());
        Recovery previous = existing.isEmpty() ? null : existing.getFirst();
        String flowHash = hash(flow);
        if (previous != null && previous.flowHash().equals(flowHash)
                && previous.sendRequestId().equals(requestId)) return;
        boolean sameWindow = previous != null && previous.windowStart().plusSeconds(3600).isAfter(now);
        if (previous != null && (previous.lastSent().plusSeconds(60).isAfter(now)
                || (sameWindow && previous.sendCount() >= 3))) return;

        String otp = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        String otpHash = encoder.encode(otp);
        Instant window = sameWindow ? previous.windowStart() : now;
        int count = sameWindow ? previous.sendCount() + 1 : 1;
        if (previous != null) db.update("DELETE FROM password_recovery WHERE user_id = ?", account.id());
        db.update("""
                INSERT INTO password_recovery
                (user_id, flow_hash, email, otp_hash, password_snapshot, expires_at,
                 last_sent_at, window_started_at, send_count, send_request_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, account.id(), flowHash, account.email(), otpHash, account.password(),
                Timestamp.from(now.plusSeconds(300)), Timestamp.from(now), Timestamp.from(window), count, requestId);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(account.email());
        message.setSubject("Mã xác minh đặt lại mật khẩu - Quản lý đề tài");
        message.setText("Mã OTP của bạn: " + otp + "\nMã có hiệu lực 5 phút. Không chia sẻ mã này với bất kỳ ai."
                + "\nNếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.");
        // shortcut: synchronous SMTP holds the transaction; use an outbox if mail volume grows.
        try {
            mail.send(message);
        } catch (MailException ex) {
            // SMTP can accept a message before timing out. Invalidate that uncertain code.
            db.update("UPDATE password_recovery SET otp_hash = NULL WHERE user_id = ?", account.id());
            LOG.warn("Password recovery email could not be confirmed ({}).", ex.getClass().getSimpleName());
        }
    }

    public void verify(String flow, String otp, String requestId) {
        Recovery recovery = lock(flow);
        Instant now = Instant.now();
        if (recovery == null) throw new BusinessRuleException(INVALID);
        String state = state(recovery, now);
        if (state.equals("PASSWORD") || state.equals("DONE")) return;
        if (!state.equals("OTP")) throw new BusinessRuleException(INVALID);
        if (requestId.equals(recovery.verifyRequestId())) throw new BusinessRuleException(INVALID);
        db.update("UPDATE password_recovery SET verify_request_id = ? WHERE user_id = ?", requestId, recovery.userId());
        if (!encoder.matches(otp, recovery.otpHash())) {
            // BusinessRuleException must NOT roll this increment back.
            db.update("UPDATE password_recovery SET attempts = attempts + 1 WHERE user_id = ?", recovery.userId());
            throw new BusinessRuleException(INVALID);
        }
        db.update("UPDATE password_recovery SET verified_until = ?, otp_hash = NULL WHERE user_id = ?",
                Timestamp.from(now.plusSeconds(600)), recovery.userId());
    }

    public void reset(String flow, PasswordResetForm form) {
        Recovery recovery = lock(flow);
        String state = recovery == null ? "EXPIRED" : state(recovery, Instant.now());
        // A lost response can be retried: never apply a second password change.
        if (state.equals("DONE")) return;
        if (!state.equals("PASSWORD")) throw new BusinessRuleException("Phiên xác minh đã hết hạn. Vui lòng yêu cầu OTP mới.");
        form.validate();
        int updated = db.update("""
                UPDATE users SET password_hash = ?
                WHERE id = ? AND enabled = TRUE AND email = ? AND password_hash = ?
                """, encoder.encode(form.getNewPassword()), recovery.userId(), recovery.email(), recovery.passwordSnapshot());
        if (updated != 1) throw new BusinessRuleException("Thông tin tài khoản đã thay đổi. Vui lòng yêu cầu OTP mới.");
        db.update("UPDATE password_recovery SET completed_at = ?, otp_hash = NULL WHERE user_id = ?",
                Timestamp.from(Instant.now()), recovery.userId());
    }

    @Transactional(readOnly = true)
    public String status(String flow) {
        var rows = db.query("SELECT * FROM password_recovery WHERE flow_hash = ?", this::read, hash(flow));
        return rows.isEmpty() ? "OTP" : state(rows.getFirst(), Instant.now());
    }

    private Recovery lock(String flow) {
        String flowHash = hash(flow);
        var ids = db.queryForList("SELECT user_id FROM password_recovery WHERE flow_hash = ?", Long.class, flowHash);
        if (ids.isEmpty()) return null;
        // All writers acquire the account lock first, then the recovery row (same lock order).
        db.queryForList("SELECT id FROM users WHERE id = ? FOR UPDATE", Long.class, ids.getFirst());
        var rows = db.query("SELECT * FROM password_recovery WHERE flow_hash = ? FOR UPDATE", this::read, flowHash);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private String state(Recovery recovery, Instant now) {
        if (recovery.completed() != null) return recovery.completed().plusSeconds(900).isAfter(now) ? "DONE" : "EXPIRED";
        if (recovery.verifiedUntil() != null) return recovery.verifiedUntil().isAfter(now) ? "PASSWORD" : "EXPIRED";
        return recovery.otpHash() != null && recovery.expires().isAfter(now) && recovery.attempts() < 5 ? "OTP" : "EXPIRED";
    }

    private Recovery read(ResultSet rs, int row) throws SQLException {
        return new Recovery(rs.getLong("user_id"), rs.getString("flow_hash"), rs.getString("email"),
                rs.getString("otp_hash"), rs.getString("password_snapshot"), rs.getTimestamp("expires_at").toInstant(),
                instant(rs, "verified_until"), instant(rs, "completed_at"), rs.getInt("attempts"),
                rs.getTimestamp("last_sent_at").toInstant(), rs.getTimestamp("window_started_at").toInstant(),
                rs.getInt("send_count"), rs.getString("send_request_id"), rs.getString("verify_request_id"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private record Account(long id, String email, String password, boolean enabled) {}
    private record Recovery(long userId, String flowHash, String email, String otpHash, String passwordSnapshot,
            Instant expires, Instant verifiedUntil, Instant completed, int attempts, Instant lastSent,
            Instant windowStart, int sendCount, String sendRequestId, String verifyRequestId) {}
}
