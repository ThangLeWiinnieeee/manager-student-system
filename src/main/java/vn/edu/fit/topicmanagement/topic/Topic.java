package vn.edu.fit.topicmanagement.topic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import vn.edu.fit.topicmanagement.department.Department;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration;
import vn.edu.fit.topicmanagement.user.UserAccount;

/**
 * Đề tài: có thể phân loại theo bộ môn, thuộc một đợt đăng ký,
 * do một giảng viên tạo, do một hoặc hai giảng viên hướng dẫn (ràng buộc ở TopicService).
 */
@Entity
@Table(name = "topics")
public class Topic {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TopicStatus status = TopicStatus.DRAFT;

    /** Lý do khoa từ chối - chỉ có giá trị khi status = REJECTED. */
    @Column(length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "round_id")
    private CourseRegistration round;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "proposed_group_id")
    private Long proposedGroupId;

    public Long getProposedGroupId() { return proposedGroupId; }
    public void setProposedGroupId(Long value) { proposedGroupId = value; }

    /** Giảng viên tạo đề tài (chủ sở hữu bản nháp). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by_id")
    private UserAccount createdBy;

    /** 1 - 2 giảng viên hướng dẫn qua bảng nối topic_advisors. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "topic_advisors",
            joinColumns = @JoinColumn(name = "topic_id"),
            inverseJoinColumns = @JoinColumn(name = "advisor_id"))
    private Set<UserAccount> advisors = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TopicStatus getStatus() { return status; }
    public void setStatus(TopicStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public CourseRegistration getRound() { return round; }
    public void setRound(CourseRegistration round) { this.round = round; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public UserAccount getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserAccount createdBy) { this.createdBy = createdBy; }
    public Set<UserAccount> getAdvisors() { return advisors; }
    public void setAdvisors(Set<UserAccount> advisors) { this.advisors = advisors; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isManagedBy(String email) {
        UserAccount manager = round.getLecturer() == null ? createdBy : round.getLecturer();
        return proposedGroupId == null && manager.getEmail().equalsIgnoreCase(email);
    }

    public boolean isEditableStatus() {
        return status == TopicStatus.DRAFT || status == TopicStatus.REJECTED;
    }

    public String getUpdatedAtText() {
        return updatedAt == null ? "" : updatedAt.atZone(ZONE).format(DISPLAY_FORMAT);
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = Instant.now();
    }
}
