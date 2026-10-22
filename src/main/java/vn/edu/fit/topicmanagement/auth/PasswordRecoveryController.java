package vn.edu.fit.topicmanagement.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.user.PasswordResetForm;

@Controller
@RequestMapping("/forgot-password")
public class PasswordRecoveryController {
    private static final String FLOW = "passwordRecoveryFlow";
    private static final String EMAIL = "passwordRecoveryEmail";
    private final PasswordRecoveryService recovery;

    public PasswordRecoveryController(PasswordRecoveryService recovery) {
        this.recovery = recovery;
    }

    @GetMapping
    String page(HttpSession session) {
        synchronized (session) {
            if (session.getAttribute(FLOW) == null) session.setAttribute(FLOW, PasswordRecoveryService.newFlow());
        }
        return "auth/forgot-password";
    }

    @GetMapping("/status")
    @ResponseBody
    Map<String, String> status(HttpSession session) {
        synchronized (session) {
            return state(session);
        }
    }

    @PostMapping("/send")
    @ResponseBody
    Map<String, String> send(@Valid @RequestBody EmailRequest request, HttpSession session) {
        synchronized (session) {
            flow(session);
            String email = request.email().trim().toLowerCase(Locale.ROOT);
            if (!email.equals(session.getAttribute(EMAIL))) {
                session.setAttribute(FLOW, PasswordRecoveryService.newFlow());
                session.setAttribute(EMAIL, email);
            }
            recovery.send(flow(session), email, request.requestId());
            return Map.of("state", "OTP", "message", "Nếu email thuộc tài khoản đang hoạt động và chưa vượt giới hạn gửi, bạn sẽ nhận được mã OTP. Kiểm tra cả thư rác; chờ ít nhất 60 giây trước khi gửi lại.");
        }
    }

    @PostMapping("/verify")
    @ResponseBody
    Map<String, String> verify(@Valid @RequestBody OtpRequest request, HttpSession session) {
        synchronized (session) {
            recovery.verify(flow(session), request.otp(), request.requestId());
            return state(session);
        }
    }

    @PostMapping("/reset")
    @ResponseBody
    Map<String, String> reset(@Valid @RequestBody PasswordResetForm form, HttpSession session) {
        synchronized (session) {
            recovery.reset(flow(session), form);
            return state(session);
        }
    }

    private Map<String, String> state(HttpSession session) {
        String token = flow(session);
        String email = (String) session.getAttribute(EMAIL);
        return Map.of("state", email == null ? "EMAIL" : recovery.status(token), "email", email == null ? "" : email);
    }

    private String flow(HttpSession session) {
        String token = (String) session.getAttribute(FLOW);
        if (token == null) throw new BusinessRuleException("Phiên đã hết hạn. Tải lại trang và yêu cầu OTP mới.");
        return token;
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseBody
    ResponseEntity<Map<String, String>> businessError(BusinessRuleException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    ResponseEntity<Map<String, String>> invalidForm(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("message", message == null ? "Thông tin không hợp lệ." : message));
    }

    public record EmailRequest(
            @NotBlank(message = "Vui lòng nhập email.") @Email(message = "Email không hợp lệ.") @Size(max = 150) String email,
            @NotBlank @Pattern(regexp = "[a-fA-F0-9-]{36}") String requestId) {}

    public record OtpRequest(
            @NotBlank @Pattern(regexp = "[0-9]{6}", message = "OTP gồm 6 chữ số.") String otp,
            @NotBlank @Pattern(regexp = "[a-fA-F0-9-]{36}") String requestId) {}
}
