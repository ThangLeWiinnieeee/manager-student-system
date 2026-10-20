package vn.edu.fit.topicmanagement.evaluation;

public enum EvaluationType {
    REVIEWER("Giảng viên phản biện"),
    COUNCIL("Thành viên hội đồng");

    private final String displayName;

    EvaluationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
