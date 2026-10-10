package vn.edu.fit.topicmanagement.registrationperiod;

public enum RoundType {
    MON_HOC("Đề tài môn học"),
    NCKH("Nghiên cứu khoa học"),
    TLCN("Tiểu luận chuyên ngành"),
    KLTN("Khóa luận tốt nghiệp");

    private final String displayName;

    RoundType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean requiresGvpbDeadline() {
        return this == TLCN || this == KLTN;
    }

    public boolean requiresCouncilReportDate() {
        return this == KLTN;
    }
}