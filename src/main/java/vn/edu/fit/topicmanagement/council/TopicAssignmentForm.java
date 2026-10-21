package vn.edu.fit.topicmanagement.council;

import jakarta.validation.constraints.NotNull;

public class TopicAssignmentForm {

    @NotNull(message = "Vui lòng chọn đề tài")
    private Long topicId;

    private Long councilId;

    private Long reviewerId;

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public Long getCouncilId() { return councilId; }
    public void setCouncilId(Long councilId) { this.councilId = councilId; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
}
