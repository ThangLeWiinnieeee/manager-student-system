
package vn.edu.fit.topicmanagement.studentgroup;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StudentGroupMemberId implements Serializable {

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "student_id")
    private Long studentId;

    public StudentGroupMemberId() {
    }

    public StudentGroupMemberId(Long groupId, Long studentId) {
        this.groupId = groupId;
        this.studentId = studentId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof StudentGroupMemberId that)) {
            return false;
        }

        return Objects.equals(groupId, that.groupId)
                && Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, studentId);
    }
}
