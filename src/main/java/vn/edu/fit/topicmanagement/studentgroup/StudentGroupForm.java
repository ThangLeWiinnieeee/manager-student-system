
package vn.edu.fit.topicmanagement.studentgroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StudentGroupForm {

    @NotBlank(message = "Tên nhóm không được để trống")
    @Size(max = 150, message = "Tên nhóm tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long roundId;

    public StudentGroupForm() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }
}
