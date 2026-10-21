package vn.edu.fit.topicmanagement.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NotificationForm {

    @NotBlank(message = "Tiêu đề thông báo không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    @Size(max = 4000, message = "Nội dung tối đa 4000 ký tự")
    private String content;

    @NotNull(message = "Vui lòng chọn đối tượng nhận thông báo")
    private NotificationTarget targetRole = NotificationTarget.ALL;

    private boolean pinned = false;

    private boolean published = true;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public NotificationTarget getTargetRole() { return targetRole; }
    public void setTargetRole(NotificationTarget targetRole) { this.targetRole = targetRole; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}
