package vn.edu.fit.topicmanagement.council;

import jakarta.persistence.*;
import java.time.Instant;
import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "council_members")
public class CouncilMember {

    @EmbeddedId
    private CouncilMemberId id = new CouncilMemberId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("councilId")
    @JoinColumn(name = "council_id")
    private Council council;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("lecturerId")
    @JoinColumn(name = "lecturer_id")
    private UserAccount lecturer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouncilRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    public CouncilMember() {}

    public CouncilMember(Council council, UserAccount lecturer, CouncilRole role) {
        this.council = council;
        this.lecturer = lecturer;
        this.role = role;
        this.id = new CouncilMemberId(council.getId(), lecturer.getId());
    }

    public CouncilMemberId getId() { return id; }
    public void setId(CouncilMemberId id) { this.id = id; }
    public Council getCouncil() { return council; }
    public void setCouncil(Council council) { this.council = council; }
    public UserAccount getLecturer() { return lecturer; }
    public void setLecturer(UserAccount lecturer) { this.lecturer = lecturer; }
    public CouncilRole getRole() { return role; }
    public void setRole(CouncilRole role) { this.role = role; }
    public Instant getJoinedAt() { return joinedAt; }

    @PrePersist
    void initJoinedAt() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}
