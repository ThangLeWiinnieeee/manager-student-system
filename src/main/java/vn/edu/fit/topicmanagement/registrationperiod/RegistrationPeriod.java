package vn.edu.fit.topicmanagement.registrationperiod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Đợt đăng ký đề tài: chia hai giai đoạn riêng biệt.
 * Giai đoạn 1 (gvStart - gvEnd): giảng viên đăng ký đề tài.
 * Giai đoạn 2 (svStart - svEnd): nhóm sinh viên đăng ký đề tài trong danh sách đã công bố.
 */
@Entity
@Table(name = "registration_periods")
public class RegistrationPeriod {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false, length = 20)
    private RoundType type;

    @Column(name = "gv_start", nullable = false)
    private LocalDateTime gvStart;

    @Column(name = "gv_end", nullable = false)
    private LocalDateTime gvEnd;

    @Column(name = "sv_start", nullable = false)
    private LocalDateTime svStart;

    @Column(name = "sv_end", nullable = false)
    private LocalDateTime svEnd;

    /** Hạn chót GVPB nộp điểm về Khoa - chỉ bắt buộc với TLCN/KLTN. */
    @Column(name = "gvpb_deadline")
    private LocalDateTime gvpbDeadline;

    /** Ngày báo cáo hội đồng - chỉ bắt buộc với KLTN. */
    @Column(name = "council_report_date")
    private LocalDateTime councilReportDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
        public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RoundType getType() { return type; }
    public void setType(RoundType type) { this.type = type; }
    public LocalDateTime getGvStart() { return gvStart; }
    public void setGvStart(LocalDateTime gvStart) { this.gvStart = gvStart; }
    public LocalDateTime getGvEnd() { return gvEnd; }
    public void setGvEnd(LocalDateTime gvEnd) { this.gvEnd = gvEnd; }
    public LocalDateTime getSvStart() { return svStart; }
    public void setSvStart(LocalDateTime svStart) { this.svStart = svStart; }
    public LocalDateTime getSvEnd() { return svEnd; }
    public void setSvEnd(LocalDateTime svEnd) { this.svEnd = svEnd; }
    public LocalDateTime getGvpbDeadline() { return gvpbDeadline; }
    public void setGvpbDeadline(LocalDateTime gvpbDeadline) { this.gvpbDeadline = gvpbDeadline; }
    public LocalDateTime getCouncilReportDate() { return councilReportDate; }
    public void setCouncilReportDate(LocalDateTime councilReportDate) { this.councilReportDate = councilReportDate; }
    public Instant getCreatedAt() { return createdAt; }
        /** Cửa sổ thời gian giảng viên đăng ký đề tài đang mở? (dùng chung cho TopicService) */
    public boolean isGvWindowOpen(LocalDateTime now) {
        return !now.isBefore(gvStart) && !now.isAfter(gvEnd);
    }

    /** Cửa sổ thời gian sinh viên đăng ký đề tài đang mở? (TV3 sẽ dùng) */
    public boolean isSvWindowOpen(LocalDateTime now) {
        return !now.isBefore(svStart) && !now.isAfter(svEnd);
    }

    /** Nhãn giai đoạn hiện tại của đợt để hiển thị trên giao diện. */
    public String getStage() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(gvStart)) {
            return "Sắp mở";
        }
        if (!now.isAfter(gvEnd)) {
            return "GV đang đăng ký";
        }
        if (now.isBefore(svStart)) {
            return "Chờ giai đoạn SV";
        }
        if (!now.isAfter(svEnd)) {
            return "SV đang đăng ký";
        }
        return "Đã kết thúc";
    }

    public String getGvStartText() { return gvStart.format(DISPLAY_FORMAT); }
    public String getGvEndText() { return gvEnd.format(DISPLAY_FORMAT); }
    public String getSvStartText() { return svStart.format(DISPLAY_FORMAT); }
    public String getSvEndText() { return svEnd.format(DISPLAY_FORMAT); }
    public String getGvpbDeadlineText() { return gvpbDeadline == null ? "-" : gvpbDeadline.format(DISPLAY_FORMAT); }
    public String getCouncilReportDateText() { return councilReportDate == null ? "-" : councilReportDate.format(DISPLAY_FORMAT); }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}