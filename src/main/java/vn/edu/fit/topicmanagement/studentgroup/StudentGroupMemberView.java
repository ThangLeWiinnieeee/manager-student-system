
package vn.edu.fit.topicmanagement.studentgroup;

import java.time.Instant;

public class StudentGroupMemberView {

    private final Long studentId;
    private final String fullName;
    private final String email;
    private final boolean leader;
    private final Instant joinedAt;

    public StudentGroupMemberView(
            Long studentId,
            String fullName,
            String email,
            boolean leader,
            Instant joinedAt) {

        this.studentId = studentId;
        this.fullName = fullName;
        this.email = email;
        this.leader = leader;
        this.joinedAt = joinedAt;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isLeader() {
        return leader;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
