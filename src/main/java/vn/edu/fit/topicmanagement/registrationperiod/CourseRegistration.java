package vn.edu.fit.topicmanagement.registrationperiod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;

/** Môn/lớp do GV đăng ký. Giữ bảng và ID cũ để bảo toàn các liên kết nhóm/điểm. */
@Entity
@Table(name = "registration_periods")
public class CourseRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.EAGER)
    @jakarta.persistence.JoinColumn(name = "period_id", nullable = false)
    private RegistrationPeriod period;
    public RegistrationPeriod getPeriod() { return period; }
    public void setPeriod(RegistrationPeriod value) { period = value; }

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Column(name = "course_name", length = 150)
    private String courseName;
    @Column(name = "class_code", length = 100)
    private String classCode;
    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @jakarta.persistence.JoinColumn(name = "lecturer_id")
    private vn.edu.fit.topicmanagement.user.UserAccount lecturer;
    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @jakarta.persistence.JoinColumn(name = "department_id")
    private vn.edu.fit.topicmanagement.department.Department department;
    @Enumerated(EnumType.STRING)
    @Column(name = "registration_mode", nullable = false)
    private RegistrationMode registrationMode = RegistrationMode.SHARED;
    @Column(name = "max_members", nullable = false)
    private int maxMembers = 5;

    public String getCourseName() { return courseName; }
    public void setCourseName(String value) { courseName = value; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String value) { classCode = value; }
    public vn.edu.fit.topicmanagement.user.UserAccount getLecturer() { return lecturer; }
    public void setLecturer(vn.edu.fit.topicmanagement.user.UserAccount value) { lecturer = value; }
    public vn.edu.fit.topicmanagement.department.Department getDepartment() { return department; }
    public void setDepartment(vn.edu.fit.topicmanagement.department.Department value) { department = value; }
    public RegistrationMode getRegistrationMode() { return registrationMode; }
    public void setRegistrationMode(RegistrationMode value) { registrationMode = value; }
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int value) { maxMembers = value; }
    public String getCourseLabel() {
        return (courseName == null ? "Chưa thiết lập" : courseName) + (classCode == null ? "" : " / " + classCode);
    }
    public String getName() { return getCourseLabel(); }
    public String getSelectionLabel() { return getCourseLabel() + " — " + period.getName() + " (#" + period.getId() + ")"; }
    public RoundType getType() { return period.getType(); }
    public LocalDateTime getGvStart() { return period.getGvStart(); }
    public LocalDateTime getGvEnd() { return period.getGvEnd(); }
    public LocalDateTime getSvStart() { return period.getSvStart(); }
    public LocalDateTime getSvEnd() { return period.getSvEnd(); }
    public LocalDateTime getGvpbDeadline() { return period.getGvpbDeadline(); }
    public LocalDateTime getCouncilReportDate() { return period.getCouncilReportDate(); }
    public String getGvStartText() { return period.getGvStartText(); }
    public String getGvEndText() { return period.getGvEndText(); }
    public String getSvStartText() { return period.getSvStartText(); }
    public String getSvEndText() { return period.getSvEndText(); }
    public String getGvpbDeadlineText() { return period.getGvpbDeadlineText(); }
    public String getCouncilReportDateText() { return period.getCouncilReportDateText(); }
    public String getStage() { return period.getStage(); }
    public boolean isGvWindowOpen(LocalDateTime now) { return period.isGvWindowOpen(now); }
    public boolean isSvWindowOpen(LocalDateTime now) { return period.isSvWindowOpen(now); }
    public boolean isEnded() { return period.isEnded(); }

    @Column(name = "join_code", unique = true, length = 32)
    private String joinCode;
    @Column(name = "joining_enabled", nullable = false)
    private boolean joiningEnabled = true;
    public String getJoinCode() { return joinCode; }
    public void renewJoinCode() { joinCode = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(java.util.Locale.ROOT); }
    public boolean isJoiningEnabled() { return joiningEnabled; }
    public void setJoiningEnabled(boolean value) { joiningEnabled = value; }

    @PrePersist
    void initializeCreatedAt() {
        if (joinCode == null) renewJoinCode();
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
