package vn.edu.fit.topicmanagement.council;

import jakarta.persistence.*;
import java.time.Instant;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "group_assignments")
public class TopicAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "registration_id", nullable = false)
    private vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration registration;

    public vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration getRegistration() { return registration; }
    public void setRegistration(vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration value) {
        registration = value;
        topic = value.getTopic();
    }


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "council_id")
    private Council council;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    private UserAccount reviewer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supporter_id")
    private UserAccount supporter;
    public UserAccount getSupporter() { return supporter; }
    public void setSupporter(UserAccount value) { supporter = value; }

    @Column(name = "legacy_assessment", nullable = false)
    private boolean legacyAssessment;
    public boolean isLegacyAssessment() { return legacyAssessment; }
    public void setLegacyAssessment(boolean value) { legacyAssessment = value; }

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
