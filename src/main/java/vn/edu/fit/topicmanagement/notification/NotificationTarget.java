package vn.edu.fit.topicmanagement.notification;

public enum NotificationTarget {
    ALL("Tất cả"),
    LECTURER("Giảng viên"),
    STUDENT("Sinh viên");

    private final String displayName;

    NotificationTarget(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
