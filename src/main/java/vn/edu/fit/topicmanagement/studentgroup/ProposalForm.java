package vn.edu.fit.topicmanagement.studentgroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProposalForm {
    @NotBlank(message = "Vui lòng nhập tên đề tài")
    @Size(max = 200)
    private String title;
    @NotBlank(message = "Vui lòng mô tả nội dung đề tài")
    @Size(max = 2000)
    private String description;
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
}
