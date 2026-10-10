package vn.edu.fit.topicmanagement.registrationperiod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class RegistrationPeriodForm {
    @NotBlank(message = "Tên đợt đăng ký không được để trống")
    @Size(max = 200, message = "Tên đợt đăng ký tối đa 200 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn loại đợt đăng ký")
    private RoundType type;

    @NotNull(message = "Vui lòng chọn thời gian bắt đầu cho GV đăng ký")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime gvStart;

    @NotNull(message = "Vui lòng chọn thời gian kết thúc cho GV đăng ký")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime gvEnd;

    @NotNull(message = "Vui lòng chọn thời gian bắt đầu cho SV đăng ký")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime svStart;

    @NotNull(message = "Vui lòng chọn thời gian kết thúc cho SV đăng ký")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime svEnd;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime gvpbDeadline;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime councilReportDate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RoundType getType() { return type; }
    public void setType(RoundType type) { this.type = type; }
    public LocalDateTime getGvStart() { return gvStart; }
    public void setGvStart(LocalDateTime gvStart) { this.gvStart = gvStart; }
    public LocalDateTime getGvEnd() { return gvEnd; }
    public void setGvEnd(LocalDateTime gvEnd) { this.gvEnd = gvEnd; }
    public LocalDateTime getSvStart() { return svStart; }
    public void setSvStart(LocalDateTime svStart) { this.svStart = svStart; }
    public LocalDateTime getSvEnd() { return svEnd; }
    public void setSvEnd(LocalDateTime svEnd) { this.svEnd = svEnd; }
    public LocalDateTime getGvpbDeadline() { return gvpbDeadline; }
    public void setGvpbDeadline(LocalDateTime gvpbDeadline) { this.gvpbDeadline = gvpbDeadline; }
    public LocalDateTime getCouncilReportDate() { return councilReportDate; }
    public void setCouncilReportDate(LocalDateTime councilReportDate) { this.councilReportDate = councilReportDate; }
}