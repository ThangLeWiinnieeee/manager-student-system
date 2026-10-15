
package vn.edu.fit.topicmanagement.studentgroup;

public enum GroupTopicRegistrationStatus {

    PENDING("Chờ duyệt"),

    APPROVED("Đã chấp thuận"),

    REJECTED("Bị từ chối");

    private final String displayName;

    GroupTopicRegistrationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
