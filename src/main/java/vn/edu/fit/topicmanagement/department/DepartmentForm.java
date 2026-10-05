package vn.edu.fit.topicmanagement.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DepartmentForm {
    @NotBlank(message = "Mã bộ môn không được để trống")
    @Size(max = 20, message = "Mã bộ môn tối đa 20 ký tự")
    @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Mã bộ môn chỉ gồm chữ, số, gạch dưới hoặc gạch ngang")
    private String code;

    @NotBlank(message = "Tên bộ môn không được để trống")
    @Size(max = 150, message = "Tên bộ môn tối đa 150 ký tự")
    private String name;

    private boolean enabled = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
