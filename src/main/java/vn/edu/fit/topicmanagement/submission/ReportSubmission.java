
package vn.edu.fit.topicmanagement.submission;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import vn.edu.fit.topicmanagement.studentgroup.StudentGroup;
import vn.edu.fit.topicmanagement.user.UserAccount;

@Entity
@Table(name = "report_submissions")
public class ReportSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private UserAccount uploadedBy;

    @Column(name = "original_filename",
            nullable = false,
            length = 255)
    private String originalFilename;

    @Column(name = "stored_filename",
            nullable = false,
            unique = true,
            length = 80)
    private String storedFilename;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "content_type",
            nullable = false,
            length = 100)
    private String contentType = "application/pdf";

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @PrePersist
    void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public StudentGroup getGroup() {
        return group;
    }

    public void setGroup(StudentGroup group) {
        this.group = group;
    }

    public UserAccount getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UserAccount uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public String getUploadedAtText() {
        if (uploadedAt == null) {
            return "";
        }

        return uploadedAt
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy HH:mm"));
    }
}
