
package vn.edu.fit.topicmanagement.studentgroup;

import jakarta.persistence.*;
import java.time.Instant;
import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "student_groups")
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "round_id", nullable = false)
    private Long roundId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leader_id")
    private UserAccount leader;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public StudentGroup() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public UserAccount getLeader() {
        return leader;
    }

    public void setLeader(UserAccount leader) {
        this.leader = leader;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
