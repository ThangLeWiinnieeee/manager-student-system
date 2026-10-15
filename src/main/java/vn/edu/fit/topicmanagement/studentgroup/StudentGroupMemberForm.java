
package vn.edu.fit.topicmanagement.studentgroup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class StudentGroupMemberForm {

    @NotBlank(message = "Email sinh viên không được để trống")
    @Email(message = "Email sinh viên không hợp lệ")
    private String email;

    public StudentGroupMemberForm() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
