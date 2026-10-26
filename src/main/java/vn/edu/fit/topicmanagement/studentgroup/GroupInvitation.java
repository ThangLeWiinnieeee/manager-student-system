package vn.edu.fit.topicmanagement.studentgroup;

import jakarta.persistence.*;
import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "group_invitations")
public class GroupInvitation {
    @EmbeddedId private StudentGroupMemberId id = new StudentGroupMemberId();
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("groupId") @JoinColumn(name = "group_id")
    private StudentGroup group;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("studentId") @JoinColumn(name = "student_id")
    private UserAccount student;

    protected GroupInvitation() {}
    public GroupInvitation(StudentGroup group, UserAccount student) {
        this.group = group;
        this.student = student;
        this.id = new StudentGroupMemberId(group.getId(), student.getId());
    }
    public StudentGroup getGroup() { return group; }
    public UserAccount getStudent() { return student; }
}
