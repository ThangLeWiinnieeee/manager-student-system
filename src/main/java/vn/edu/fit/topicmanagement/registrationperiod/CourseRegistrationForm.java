package vn.edu.fit.topicmanagement.registrationperiod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CourseRegistrationForm {
    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long periodId;
    public Long getPeriodId() { return periodId; }
    public void setPeriodId(Long value) { periodId = value; }

    @NotBlank(message = "Vui lòng nhập môn học hoặc hoạt động")
    @Size(max = 150)
    private String courseName;
    @NotBlank(message = "Vui lòng nhập mã lớp hoặc mã đợt")
    @Size(max = 100)
    private String classCode;
    private Long lecturerId;
    @NotNull(message = "Vui lòng chọn bộ môn")
    private Long departmentId;
    @NotNull
    private RegistrationMode registrationMode = RegistrationMode.EXCLUSIVE;
    @jakarta.validation.constraints.Min(1)
    @jakarta.validation.constraints.Max(5)
    private int maxMembers = 5;

    public String getCourseName() { return courseName; }
    public void setCourseName(String value) { courseName = value; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String value) { classCode = value; }
    public Long getLecturerId() { return lecturerId; }
    public void setLecturerId(Long value) { lecturerId = value; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long value) { departmentId = value; }
    public RegistrationMode getRegistrationMode() { return registrationMode; }
    public void setRegistrationMode(RegistrationMode value) { registrationMode = value; }
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int value) { maxMembers = value; }
}
