package vn.edu.fit.topicmanagement.user;

import java.nio.charset.StandardCharsets;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static void validate(String password) {
        if (password == null || password.isBlank() || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessRuleException("Mật khẩu cần ít nhất 8 ký tự và tối đa 72 byte UTF-8.");
        }
    }
}
