package vn.edu.fit.topicmanagement.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserCreateForm extends UserForm {
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự")
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
