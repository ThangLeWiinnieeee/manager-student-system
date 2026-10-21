package vn.edu.fit.topicmanagement.council;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CouncilMemberId implements Serializable {

    @Column(name = "council_id")
    private Long councilId;

    @Column(name = "lecturer_id")
    private Long lecturerId;

    public CouncilMemberId() {}

    public CouncilMemberId(Long councilId, Long lecturerId) {
        this.councilId = councilId;
        this.lecturerId = lecturerId;
    }

    public Long getCouncilId() { return councilId; }
    public void setCouncilId(Long councilId) { this.councilId = councilId; }
    public Long getLecturerId() { return lecturerId; }
    public void setLecturerId(Long lecturerId) { this.lecturerId = lecturerId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CouncilMemberId that)) return false;
        return Objects.equals(councilId, that.councilId) && Objects.equals(lecturerId, that.lecturerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(councilId, lecturerId);
    }
}
