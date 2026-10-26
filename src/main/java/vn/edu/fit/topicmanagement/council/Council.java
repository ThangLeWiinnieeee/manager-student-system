package vn.edu.fit.topicmanagement.council;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration;

@Entity
@Table(name = "councils")
public class Council {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "round_id")
    private CourseRegistration round;

    @Column(length = 200)
    private String location;

    @Column(name = "report_date")
    private LocalDateTime reportDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouncilStatus status = CouncilStatus.DRAFT;

    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CouncilMember> members = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CourseRegistration getRound() { return round; }
    public void setRound(CourseRegistration round) { this.round = round; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    public CouncilStatus getStatus() { return status; }
    public void setStatus(CouncilStatus status) { this.status = status; }
    public List<CouncilMember> getMembers() { return members; }
    public void setMembers(List<CouncilMember> members) { this.members = members; }
    public Instant getCreatedAt() { return createdAt; }

    public String getReportDateText() {
        return reportDate == null ? "-" : reportDate.format(DISPLAY_FORMAT);
    }

    public boolean isEligible() {
        if (members.size() < 3 || members.size() > 5) {
            return false;
        }
        long presidentCount = members.stream().filter(m -> m.getRole() == CouncilRole.PRESIDENT).count();
        long secretaryCount = members.stream().filter(m -> m.getRole() == CouncilRole.SECRETARY).count();
        return presidentCount == 1 && secretaryCount == 1;
    }

    @PrePersist
    void initCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
