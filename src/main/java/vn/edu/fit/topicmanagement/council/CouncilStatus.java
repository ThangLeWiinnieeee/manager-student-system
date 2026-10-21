package vn.edu.fit.topicmanagement.council;

public enum CouncilStatus {
    DRAFT("Đang thành lập"),
    ACTIVE("Hoạt động"),
    COMPLETED("Đã hoàn thành");

    private final String displayName;

    CouncilStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
