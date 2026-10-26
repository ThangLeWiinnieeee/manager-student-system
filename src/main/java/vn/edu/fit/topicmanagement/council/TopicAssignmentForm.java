package vn.edu.fit.topicmanagement.council;

import jakarta.validation.constraints.NotNull;

public class TopicAssignmentForm {

    @NotNull(message = "Vui lòng chọn đề tài")
    private Long registrationId;

    private Long councilId;

    private Long reviewerId;
    private Long supporterId;
    public Long getSupporterId() { return supporterId; }
    public void setSupporterId(Long value) { supporterId = value; }

    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
    public Long getCouncilId() { return councilId; }
    public void setCouncilId(Long councilId) { this.councilId = councilId; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
}
