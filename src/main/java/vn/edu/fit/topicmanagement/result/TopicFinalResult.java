package vn.edu.fit.topicmanagement.result;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import vn.edu.fit.topicmanagement.council.Council;
import vn.edu.fit.topicmanagement.topic.Topic;

@Entity
@Table(name = "group_results")
public class TopicFinalResult {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

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

    @Column(name = "supporter_score", precision = 4, scale = 2)
    private BigDecimal supporterScore;
    @Column(name = "defense_allowed")
    private Boolean defenseAllowed;
    public BigDecimal getSupporterScore() { return supporterScore; }
    public void setSupporterScore(BigDecimal value) { supporterScore = value; }
    public Boolean getDefenseAllowed() { return defenseAllowed; }
    public void setDefenseAllowed(Boolean value) { defenseAllowed = value; }

    @Column(name = "reviewer_score", precision = 4, scale = 2)
    private BigDecimal reviewerScore;

    @Column(name = "council_avg_score", precision = 4, scale = 2)
    private BigDecimal councilAvgScore;

    @Column(name = "final_score", precision = 4, scale = 2)
    private BigDecimal finalScore;

    @Column(length = 30)
    private String grade;

    @Column(length = 1000)
    private String conclusion;

    @Column(name = "president_confirmed", nullable = false)
    private boolean presidentConfirmed = false;

    @Column(name = "president_confirmed_at")
    private Instant presidentConfirmedAt;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public Council getCouncil() { return council; }
    public void setCouncil(Council council) { this.council = council; }
    public BigDecimal getReviewerScore() { return reviewerScore; }
    public void setReviewerScore(BigDecimal reviewerScore) { this.reviewerScore = reviewerScore; }
    public BigDecimal getCouncilAvgScore() { return councilAvgScore; }
    public void setCouncilAvgScore(BigDecimal councilAvgScore) { this.councilAvgScore = councilAvgScore; }
    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public boolean isPresidentConfirmed() { return presidentConfirmed; }
    public void setPresidentConfirmed(boolean presidentConfirmed) { this.presidentConfirmed = presidentConfirmed; }
    public Instant getPresidentConfirmedAt() { return presidentConfirmedAt; }
    public void setPresidentConfirmedAt(Instant presidentConfirmedAt) { this.presidentConfirmedAt = presidentConfirmedAt; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public String getPresidentConfirmedAtText() {
        return presidentConfirmedAt == null ? "-" : presidentConfirmedAt.atZone(ZONE).format(DISPLAY_FORMAT);
    }

    public String getPublishedAtText() {
        return publishedAt == null ? "-" : publishedAt.atZone(ZONE).format(DISPLAY_FORMAT);
    }

    @PrePersist
    void initTimestamps() {
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
