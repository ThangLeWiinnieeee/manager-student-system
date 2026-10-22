package vn.edu.fit.topicmanagement.user;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordForm extends PasswordResetForm {
    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
}
