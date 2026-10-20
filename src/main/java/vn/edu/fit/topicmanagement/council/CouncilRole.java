package vn.edu.fit.topicmanagement.council;

public enum CouncilRole {
    PRESIDENT("Chủ tịch"),
    SECRETARY("Thư ký"),
    MEMBER("Ủy viên");

    private final String displayName;

    CouncilRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
