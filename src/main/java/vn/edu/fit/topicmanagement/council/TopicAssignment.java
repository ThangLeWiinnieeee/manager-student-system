package vn.edu.fit.topicmanagement.council;

import jakarta.persistence.*;
import java.time.Instant;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "topic_assignments")
public class TopicAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "topic_id", unique = true)
    private Topic topic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "council_id")
    private Council council;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    private UserAccount reviewer;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    public Long getId() { return id; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public Council getCouncil() { return council; }
    public void setCouncil(Council council) { this.council = council; }
    public UserAccount getReviewer() { return reviewer; }
    public void setReviewer(UserAccount reviewer) { this.reviewer = reviewer; }
    public Instant getAssignedAt() { return assignedAt; }

    @PrePersist
    void initAssignedAt() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}
