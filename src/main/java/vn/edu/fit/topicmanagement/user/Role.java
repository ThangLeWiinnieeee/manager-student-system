package vn.edu.fit.topicmanagement.user;

public enum Role {
    ADMIN("Quản trị viên"),
    DEAN("Trưởng khoa"),
    LECTURER("Giảng viên"),
    STUDENT("Sinh viên");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
