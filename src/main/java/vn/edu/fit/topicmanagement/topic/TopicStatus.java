package vn.edu.fit.topicmanagement.topic;

/**
 * Vòng đời trạng thái đề tài:
 *
 * DRAFT ──submit──▶ PENDING ──approve──▶ APPROVED ──publish──▶ PUBLISHED
 *   ▲                  │
 *   └── sửa & gửi lại ─┴── reject (kèm lý do) ──▶ REJECTED
 *
 * Mọi phép chuyển trạng thái phải đi qua canTransitionTo() để chặn nhảy trạng thái sai.
 */
public enum TopicStatus {
    DRAFT("Nháp"),
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Bị từ chối"),
    PUBLISHED("Đã công bố");

    private final String displayName;

    TopicStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(TopicStatus target) {
        return switch (this) {
            case DRAFT, REJECTED -> target == PENDING;
            case PENDING -> target == APPROVED || target == REJECTED;
            case APPROVED -> target == PUBLISHED;
            case PUBLISHED -> false;
        };
    }
}