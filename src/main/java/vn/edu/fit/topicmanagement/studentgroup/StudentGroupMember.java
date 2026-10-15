
package vn.edu.fit.topicmanagement.studentgroup;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.Instant;

import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "student_group_members")
public class StudentGroupMember {

    @EmbeddedId
    private StudentGroupMemberId id = new StudentGroupMemberId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("studentId")
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    public StudentGroupMember() {
    }

    public StudentGroupMember(
            StudentGroup group,
            UserAccount student) {
        this.group = group;
        this.student = student;
    }

    public StudentGroupMemberId getId() {
        return id;
    }

    public void setId(StudentGroupMemberId id) {
        this.id = id;
    }

    public StudentGroup getGroup() {
        return group;
    }

    public void setGroup(StudentGroup group) {
        this.group = group;
    }

    public UserAccount getStudent() {
        return student;
    }

    public void setStudent(UserAccount student) {
        this.student = student;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    @PrePersist
    void initializeJoinedAt() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}
