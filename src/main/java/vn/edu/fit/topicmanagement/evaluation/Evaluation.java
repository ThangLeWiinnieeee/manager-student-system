package vn.edu.fit.topicmanagement.evaluation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "evaluations")
public class Evaluation {

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
    @JoinColumn(name = "evaluator_id")
    private UserAccount evaluator;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false, length = 20)
    private EvaluationType evaluationType;

    @Column(name = "score_content", nullable = false, precision = 4, scale = 2)
    private BigDecimal scoreContent = BigDecimal.ZERO;

    @Column(name = "score_presentation", nullable = false, precision = 4, scale = 2)
    private BigDecimal scorePresentation = BigDecimal.ZERO;

    @Column(name = "score_qa", nullable = false, precision = 4, scale = 2)
    private BigDecimal scoreQa = BigDecimal.ZERO;

    @Column(name = "total_score", nullable = false, precision = 4, scale = 2)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Column(length = 2000)
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EvaluationStatus status = EvaluationStatus.DRAFT;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public UserAccount getEvaluator() { return evaluator; }
    public void setEvaluator(UserAccount evaluator) { this.evaluator = evaluator; }
    public EvaluationType getEvaluationType() { return evaluationType; }
    public void setEvaluationType(EvaluationType evaluationType) { this.evaluationType = evaluationType; }
    public BigDecimal getScoreContent() { return scoreContent; }
    public void setScoreContent(BigDecimal scoreContent) { this.scoreContent = scoreContent; }
    public BigDecimal getScorePresentation() { return scorePresentation; }
    public void setScorePresentation(BigDecimal scorePresentation) { this.scorePresentation = scorePresentation; }
    public BigDecimal getScoreQa() { return scoreQa; }
    public void setScoreQa(BigDecimal scoreQa) { this.scoreQa = scoreQa; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public EvaluationStatus getStatus() { return status; }
    public void setStatus(EvaluationStatus status) { this.status = status; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public String getSubmittedAtText() {
        return submittedAt == null ? "-" : submittedAt.atZone(ZONE).format(DISPLAY_FORMAT);
    }

    public void calculateTotal() {
        BigDecimal c = scoreContent == null ? BigDecimal.ZERO : scoreContent;
        BigDecimal p = scorePresentation == null ? BigDecimal.ZERO : scorePresentation;
        BigDecimal q = scoreQa == null ? BigDecimal.ZERO : scoreQa;
        this.totalScore = c.add(p).add(q).setScale(2, RoundingMode.HALF_UP);
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
