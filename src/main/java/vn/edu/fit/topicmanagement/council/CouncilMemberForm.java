package vn.edu.fit.topicmanagement.council;

import jakarta.validation.constraints.NotNull;

public class CouncilMemberForm {

    @NotNull(message = "Vui lòng chọn giảng viên")
    private Long lecturerId;

    @NotNull(message = "Vui lòng chọn vai trò trong hội đồng")
    private CouncilRole role;

    public Long getLecturerId() { return lecturerId; }
    public void setLecturerId(Long lecturerId) { this.lecturerId = lecturerId; }
    public CouncilRole getRole() { return role; }
    public void setRole(CouncilRole role) { this.role = role; }
}
