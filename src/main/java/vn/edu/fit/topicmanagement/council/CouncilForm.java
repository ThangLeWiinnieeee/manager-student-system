package vn.edu.fit.topicmanagement.council;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class CouncilForm {

    @NotBlank(message = "Tên hội đồng không được để trống")
    @Size(max = 150, message = "Tên hội đồng tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long roundId;

    @Size(max = 200, message = "Địa điểm tối đa 200 ký tự")
    private String location;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime reportDate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
}
