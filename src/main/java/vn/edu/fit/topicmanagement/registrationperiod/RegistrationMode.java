package vn.edu.fit.topicmanagement.registrationperiod;

public enum RegistrationMode {
    EXCLUSIVE("Đề tài có sẵn — một nhóm nhận"),
    SELF_PROPOSED("Nhóm tự đề xuất đề tài"),
    SHARED("Đề tài có sẵn — nhiều nhóm thực hiện");

    private final String displayName;
    RegistrationMode(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
