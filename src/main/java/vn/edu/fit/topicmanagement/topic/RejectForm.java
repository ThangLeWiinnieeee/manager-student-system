package vn.edu.fit.topicmanagement.topic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Form từ chối đề tài - khoa phải nêu rõ lý do. */
public class RejectForm {
    @NotBlank(message = "Vui lòng nhập lý do từ chối")
    @Size(max = 500, message = "Lý do từ chối tối đa 500 ký tự")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}