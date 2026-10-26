package vn.edu.fit.topicmanagement.registrationperiod;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Lịch chung do Khoa mở; các môn/lớp tham gia dùng trực tiếp lịch này. */
@Entity
@Table(name = "registration_windows")
public class RegistrationPeriod {
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String name;
    @Enumerated(EnumType.STRING) @Column(name = "round_type", nullable = false, length = 20)
    private RoundType type;
    @Column(name = "gv_start", nullable = false) private LocalDateTime gvStart;
    @Column(name = "gv_end", nullable = false) private LocalDateTime gvEnd;
    @Column(name = "sv_start", nullable = false) private LocalDateTime svStart;
    @Column(name = "sv_end", nullable = false) private LocalDateTime svEnd;
    @Column(name = "gvpb_deadline") private LocalDateTime gvpbDeadline;
    @Column(name = "council_report_date") private LocalDateTime councilReportDate;
    @Column(name = "gv_notice_sent", nullable = false) private boolean gvNoticeSent;
    @Column(name = "sv_notice_sent", nullable = false) private boolean svNoticeSent;

    public boolean isGvNoticeSent() { return gvNoticeSent; }
    public void setGvNoticeSent(boolean value) { gvNoticeSent = value; }
    public boolean isSvNoticeSent() { return svNoticeSent; }
    public void setSvNoticeSent(boolean value) { svNoticeSent = value; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RoundType getType() { return type; }
    public void setType(RoundType type) { this.type = type; }
    public LocalDateTime getGvStart() { return gvStart; }
    public void setGvStart(LocalDateTime value) { gvStart = value; }
    public LocalDateTime getGvEnd() { return gvEnd; }
    public void setGvEnd(LocalDateTime value) { gvEnd = value; }
    public LocalDateTime getSvStart() { return svStart; }
    public void setSvStart(LocalDateTime value) { svStart = value; }
    public LocalDateTime getSvEnd() { return svEnd; }
    public void setSvEnd(LocalDateTime value) { svEnd = value; }
    public LocalDateTime getGvpbDeadline() { return gvpbDeadline; }
    public void setGvpbDeadline(LocalDateTime value) { gvpbDeadline = value; }
    public LocalDateTime getCouncilReportDate() { return councilReportDate; }
    public void setCouncilReportDate(LocalDateTime value) { councilReportDate = value; }
    public boolean isGvWindowOpen(LocalDateTime now) { return !now.isBefore(gvStart) && now.isBefore(gvEnd); }
    public boolean isSvWindowOpen(LocalDateTime now) { return !now.isBefore(svStart) && !now.isAfter(svEnd); }
    public boolean isGvOpen() { return isGvWindowOpen(LocalDateTime.now()); }
    public boolean isEnded() { return LocalDateTime.now().isAfter(svEnd); }
    public boolean isReviewerGradingOpen() {
        LocalDateTime now = LocalDateTime.now();
        return type != null && type.requiresGvpbDeadline() && gvpbDeadline != null
                && now.isAfter(svEnd) && !now.isAfter(gvpbDeadline);
    }
    public String getStage() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(gvStart)) return "Sắp mở";
        if (isGvWindowOpen(now)) return "GV đang đăng ký";
        if (now.isBefore(svStart)) return "Chờ giai đoạn SV";
        if (!now.isAfter(svEnd)) return "SV đang đăng ký";
        if (isReviewerGradingOpen()) return "Đang chấm phản biện";
        if (type != null && type.requiresGvpbDeadline() && gvpbDeadline != null)
            return "Đã hết hạn chấm phản biện";
        return "Đã kết thúc";
    }
    public String getGvStartText() { return gvStart.format(DISPLAY); }
    public String getGvEndText() { return gvEnd.format(DISPLAY); }
    public String getSvStartText() { return svStart.format(DISPLAY); }
    public String getSvEndText() { return svEnd.format(DISPLAY); }
    public String getGvpbDeadlineText() { return gvpbDeadline == null ? "-" : gvpbDeadline.format(DISPLAY); }
    public String getCouncilReportDateText() { return councilReportDate == null ? "-" : councilReportDate.format(DISPLAY); }
}
