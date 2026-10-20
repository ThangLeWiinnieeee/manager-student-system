package vn.edu.fit.topicmanagement.evaluation;

public enum EvaluationStatus {
    DRAFT("Lưu nháp"),
    SUBMITTED("Đã xác nhận nộp");

    private final String displayName;

    EvaluationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
