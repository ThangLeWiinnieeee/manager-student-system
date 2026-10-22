package vn.edu.fit.topicmanagement.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.user.PasswordResetForm;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;

@SpringBootTest(properties = {
        "app.mail.from=noreply@example.test",
        "spring.datasource.url=jdbc:h2:mem:recovery_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE",
        "debug=false"
})
@AutoConfigureMockMvc
class PasswordRecoveryTest {
    @Autowired PasswordRecoveryService recovery;
    @Autowired UserAccountRepository users;
    @Autowired JdbcTemplate db;
    @Autowired PasswordEncoder encoder;
    @Autowired MockMvc mvc;
    @MockitoBean JavaMailSender mail;
    private String flow;
    private Long userId;

    @BeforeEach
    void setup() {
        db.update("DELETE FROM password_recovery");
        db.update("DELETE FROM users");
        reset(mail);
        UserAccount account = new UserAccount();
        account.setEmail("student@example.test");
        account.setFullName("Sinh viên kiểm thử");
        account.setRole(Role.STUDENT);
        account.setPasswordHash(encoder.encode("OldPassword123!"));
        userId = users.saveAndFlush(account).getId();
        flow = PasswordRecoveryService.newFlow();
    }

    @Test
    void completeFlowCanResumeAndResetRetryNeverChangesPasswordAgain() {
        String sendId = id();
        recovery.send(flow, "STUDENT@example.test", sendId);
        String otp = sentOtp();
        recovery.send(flow, "student@example.test", sendId);
        verify(mail, times(1)).send(any(SimpleMailMessage.class));
        String stored = db.queryForObject("SELECT otp_hash FROM password_recovery", String.class);
        assertNotEquals(otp, stored);
        assertTrue(encoder.matches(otp, stored));
        assertEquals("OTP", recovery.status(flow));
        recovery.verify(flow, otp, id());
        recovery.verify(flow, otp, id()); // lost verification response
        assertEquals("PASSWORD", recovery.status(flow));
        recovery.reset(flow, password("NewPassword123!"));
        recovery.reset(flow, password("DoNotApply123!")); // lost final response
        assertEquals("DONE", recovery.status(flow));
        String hash = users.findById(userId).orElseThrow().getPasswordHash();
        assertTrue(encoder.matches("NewPassword123!", hash));
        assertFalse(encoder.matches("OldPassword123!", hash));
        assertFalse(encoder.matches("DoNotApply123!", hash));
    }

    @Test
    void wrongAttemptsAreCommittedAndReplaysDoNotConsumeAnotherAttempt() {
        recovery.send(flow, "student@example.test", id());
        String otp = sentOtp();
        String wrong = otp.equals("000000") ? "111111" : "000000";
        String attemptId = id();
        assertThrows(BusinessRuleException.class, () -> recovery.verify(flow, wrong, attemptId));
        assertThrows(BusinessRuleException.class, () -> recovery.verify(flow, wrong, attemptId));
        assertEquals(1, db.queryForObject("SELECT attempts FROM password_recovery", Integer.class));
        for (int i = 0; i < 4; i++) assertThrows(BusinessRuleException.class, () -> recovery.verify(flow, wrong, id()));
        assertThrows(BusinessRuleException.class, () -> recovery.verify(flow, otp, id()));
        assertEquals("EXPIRED", recovery.status(flow));
    }

    @Test
    void expirationCannotBeExtendedByReloadingAndResetNeedsVerification() {
        recovery.send(flow, "student@example.test", id());
        String otp = sentOtp();
        assertThrows(BusinessRuleException.class, () -> recovery.reset(flow, password("NewPassword123!")));
        db.update("UPDATE password_recovery SET expires_at = ?", Timestamp.from(Instant.now().minusSeconds(1)));
        assertThrows(BusinessRuleException.class, () -> recovery.verify(flow, otp, id()));
        assertEquals("EXPIRED", recovery.status(flow));
    }

    @Test
    void resendHasCooldownHourlyLimitAndInvalidatesPreviousFlow() {
        recovery.send(flow, "student@example.test", id());
        String oldOtp = sentOtp();
        recovery.send(PasswordRecoveryService.newFlow(), "student@example.test", id());
        verify(mail, times(1)).send(any(SimpleMailMessage.class));
        for (int i = 0; i < 4; i++) {
            db.update("UPDATE password_recovery SET last_sent_at = ?", Timestamp.from(Instant.now().minusSeconds(61)));
            recovery.send(PasswordRecoveryService.newFlow(), "student@example.test", id());
        }
        verify(mail, times(3)).send(any(SimpleMailMessage.class));
        assertThrows(BusinessRuleException.class, () -> recovery.verify(flow, oldOtp, id()));
    }

    @Test
    void smtpFailureDoesNotLeaveUsableOtpAndDoesNotRollbackRateLimit() {
        doThrow(new MailSendException("test timeout")).when(mail).send(any(SimpleMailMessage.class));
        recovery.send(flow, "student@example.test", id());
        assertNull(db.queryForObject("SELECT otp_hash FROM password_recovery", String.class));
        assertEquals(1, db.queryForObject("SELECT send_count FROM password_recovery", Integer.class));
        assertEquals("EXPIRED", recovery.status(flow));
    }

    @Test
    void unknownAndDisabledAccountsNeverReceiveMail() {
        recovery.send(flow, "unknown@example.test", id());
        db.update("UPDATE users SET enabled = FALSE WHERE id = ?", userId);
        recovery.send(flow, "student@example.test", id());
        verifyNoInteractions(mail);
        assertThrows(BusinessRuleException.class, () -> recovery.verify(flow, "123456", id()));
    }

    @Test
    void changedAccountPasswordInvalidatesExistingVerification() {
        recovery.send(flow, "student@example.test", id());
        recovery.verify(flow, sentOtp(), id());
        db.update("UPDATE users SET password_hash = ? WHERE id = ?", encoder.encode("AdminChanged123!"), userId);
        assertThrows(BusinessRuleException.class, () -> recovery.reset(flow, password("NewPassword123!")));
        assertTrue(encoder.matches("AdminChanged123!", users.findById(userId).orElseThrow().getPasswordHash()));
    }

    @Test
    void mismatchAndExpiredVerificationDoNotChangePassword() {
        recovery.send(flow, "student@example.test", id());
        recovery.verify(flow, sentOtp(), id());
        PasswordResetForm form = password("NewPassword123!");
        form.setConfirmPassword("Mismatch123!");
        assertThrows(BusinessRuleException.class, () -> recovery.reset(flow, form));
        db.update("UPDATE password_recovery SET verified_until = ?", Timestamp.from(Instant.now().minusSeconds(1)));
        assertThrows(BusinessRuleException.class, () -> recovery.reset(flow, password("NewPassword123!")));
        assertTrue(encoder.matches("OldPassword123!", users.findById(userId).orElseThrow().getPasswordHash()));
    }

    @Test
    void concurrentSubmissionsOnlyApplyOnePassword() throws Exception {
        recovery.send(flow, "student@example.test", id());
        recovery.verify(flow, sentOtp(), id());
        CountDownLatch start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> { start.await(); recovery.reset(flow, password("FirstPassword123!")); return true; });
            var second = pool.submit(() -> { start.await(); recovery.reset(flow, password("SecondPassword123!")); return true; });
            start.countDown();
            assertTrue(first.get());
            assertTrue(second.get());
        }
        assertEquals("DONE", recovery.status(flow));
        String hash = users.findById(userId).orElseThrow().getPasswordHash();
        assertTrue(encoder.matches("FirstPassword123!", hash) ^ encoder.matches("SecondPassword123!", hash));
    }

    @Test
    void httpFlowResumesAfterLostResponsesAndNewPasswordCanLogin() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(get("/forgot-password"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession();
        mvc.perform(post("/forgot-password/send").session(session).with(csrf()).contentType("application/json")
                .content("{\"email\":\"student@example.test\",\"requestId\":\"" + id() + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/forgot-password/verify").session(session).with(csrf()).contentType("application/json")
                .content("{\"otp\":\"" + sentOtp() + "\",\"requestId\":\"" + id() + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/forgot-password/status").session(session))
                .andExpect(jsonPath("$.state").value("PASSWORD"));
        mvc.perform(post("/forgot-password/reset").session(session).with(csrf()).contentType("application/json")
                .content("{\"newPassword\":\"NewPassword123!\",\"confirmPassword\":\"NewPassword123!\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/forgot-password/status").session(session))
                .andExpect(jsonPath("$.state").value("DONE"));
        mvc.perform(post("/login").with(csrf()).param("email", "student@example.test")
                .param("password", "NewPassword123!"))
                .andExpect(org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated());
        mvc.perform(post("/login").with(csrf()).param("email", "student@example.test")
                .param("password", "OldPassword123!"))
                .andExpect(org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated());
    }

    @Test
    void publicEndpointsRequireCsrfAndSessionVerification() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(get("/forgot-password"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession();
        mvc.perform(post("/forgot-password/send").session(session).contentType("application/json")
                .content("{\"email\":\"student@example.test\",\"requestId\":\"" + id() + "\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/forgot-password/reset").session(session).with(csrf()).contentType("application/json")
                .content("{\"newPassword\":\"NewPassword123!\",\"confirmPassword\":\"NewPassword123!\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/forgot-password/send").session(session).with(csrf()).contentType("application/json")
                .content("{\"email\":\"invalid\",\"requestId\":\"" + id() + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/forgot-password/status").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("EMAIL"));
    }

    private String sentOtp() {
        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail, atLeastOnce()).send(captor.capture());
        var matcher = java.util.regex.Pattern.compile("\\b[0-9]{6}\\b").matcher(captor.getValue().getText());
        assertTrue(matcher.find());
        return matcher.group();
    }

    private static String id() { return UUID.randomUUID().toString(); }
    private static PasswordResetForm password(String value) {
        PasswordResetForm form = new PasswordResetForm();
        form.setNewPassword(value);
        form.setConfirmPassword(value);
        return form;
    }
}
