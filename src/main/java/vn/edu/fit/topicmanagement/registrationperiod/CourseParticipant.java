package vn.edu.fit.topicmanagement.registrationperiod;

import jakarta.persistence.*;
import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "course_participants", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_id"}))
public class CourseParticipant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "course_id")
    private CourseRegistration course;
    @ManyToOne(optional = false) @JoinColumn(name = "student_id")
    private UserAccount student;

    protected CourseParticipant() {}
    public CourseParticipant(CourseRegistration course, UserAccount student) {
        this.course = course;
        this.student = student;
    }
    public CourseRegistration getCourse() { return course; }
    public UserAccount getStudent() { return student; }
}
