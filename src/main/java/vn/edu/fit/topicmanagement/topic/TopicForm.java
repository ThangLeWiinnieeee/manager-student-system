package vn.edu.fit.topicmanagement.topic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Form đăng ký đề tài (DTO). Không bind trực tiếp entity vào form (quy tắc nhóm).
 * GVHD chọn 1-2 giảng viên: ràng buộc được kiểm cả ở annotation lẫn TopicService.
 */
public class TopicForm {
    @NotBlank(message = "Tên đề tài không được để trống")
    @Size(max = 200, message = "Tên đề tài tối đa 200 ký tự")
    private String title;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long roundId;

    @NotNull(message = "Vui lòng chọn bộ môn")
    private Long departmentId;

    @NotEmpty(message = "Đề tài phải có ít nhất một giảng viên hướng dẫn")
    @Size(max = 2, message = "Đề tài chỉ được hướng dẫn bởi tối đa hai giảng viên")
    private List<Long> advisorIds;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public List<Long> getAdvisorIds() { return advisorIds; }
    public void setAdvisorIds(List<Long> advisorIds) { this.advisorIds = advisorIds; }
}