package vn.edu.fit.topicmanagement.evaluation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class EvaluationForm {

    @NotNull(message = "Vui lòng nhập điểm nội dung / chuyên môn")
    @DecimalMin(value = "0.0", message = "Điểm tối thiểu là 0.0")
    @DecimalMax(value = "4.0", message = "Điểm nội dung tối đa là 4.0")
    private BigDecimal scoreContent = BigDecimal.ZERO;

    @NotNull(message = "Vui lòng nhập điểm báo cáo / trình bày")
    @DecimalMin(value = "0.0", message = "Điểm tối thiểu là 0.0")
    @DecimalMax(value = "3.0", message = "Điểm trình bày tối đa là 3.0")
    private BigDecimal scorePresentation = BigDecimal.ZERO;

    @NotNull(message = "Vui lòng nhập điểm trả lời câu hỏi / phản biện")
    @DecimalMin(value = "0.0", message = "Điểm tối thiểu là 0.0")
    @DecimalMax(value = "3.0", message = "Điểm câu hỏi tối đa là 3.0")
    private BigDecimal scoreQa = BigDecimal.ZERO;

    @Size(max = 2000, message = "Nhận xét tối đa 2000 ký tự")
    private String comments;

    private String action = "draft"; // "draft" hoặc "submit"

    public BigDecimal getScoreContent() { return scoreContent; }
    public void setScoreContent(BigDecimal scoreContent) { this.scoreContent = scoreContent; }
    public BigDecimal getScorePresentation() { return scorePresentation; }
    public void setScorePresentation(BigDecimal scorePresentation) { this.scorePresentation = scorePresentation; }
    public BigDecimal getScoreQa() { return scoreQa; }
    public void setScoreQa(BigDecimal scoreQa) { this.scoreQa = scoreQa; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
