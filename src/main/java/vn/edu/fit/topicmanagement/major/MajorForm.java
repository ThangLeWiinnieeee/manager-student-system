package vn.edu.fit.topicmanagement.major;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MajorForm {
    @NotBlank(message = "Mã chuyên ngành không được để trống")
    @Size(max = 20, message = "Mã chuyên ngành tối đa 20 ký tự")
    @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Mã chuyên ngành chỉ gồm chữ, số, gạch dưới hoặc gạch ngang")
    private String code;

    @NotBlank(message = "Tên chuyên ngành không được để trống")
    @Size(max = 150, message = "Tên chuyên ngành tối đa 150 ký tự")
    private String name;

    private boolean enabled = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
