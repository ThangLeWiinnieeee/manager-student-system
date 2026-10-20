package vn.edu.fit.topicmanagement.notification;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "notifications")
public class Notification {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", nullable = false, length = 20)
    private NotificationTarget targetRole = NotificationTarget.ALL;

    @Column(nullable = false)
    private boolean pinned = false;

    @Column(nullable = false)
    private boolean published = true;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by_id")
    private UserAccount createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public NotificationTarget getTargetRole() { return targetRole; }
    public void setTargetRole(NotificationTarget targetRole) { this.targetRole = targetRole; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public UserAccount getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserAccount createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public String getCreatedAtText() {
        return createdAt == null ? "" : createdAt.atZone(ZONE).format(DISPLAY_FORMAT);
    }

    public String getUpdatedAtText() {
        return updatedAt == null ? "" : updatedAt.atZone(ZONE).format(DISPLAY_FORMAT);
    }

    @PrePersist
    void initCreatedAt() {
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
