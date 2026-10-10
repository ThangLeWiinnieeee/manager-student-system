package vn.edu.fit.topicmanagement.registrationperiod;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

@Service
@Transactional
public class RegistrationPeriodService {
    private final RegistrationPeriodRepository periods;

    public RegistrationPeriodService(RegistrationPeriodRepository periods) {
        this.periods = periods;
    }

    @Transactional(readOnly = true)
    public List<RegistrationPeriod> findAll() {
        return periods.findAllByOrderByGvStartDesc();
    }

    @Transactional(readOnly = true)
    public RegistrationPeriod get(Long id) {
        return periods.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký"));
    }

    public RegistrationPeriod create(RegistrationPeriodForm form) {
        validate(form);
        RegistrationPeriod period = new RegistrationPeriod();
        apply(period, form);
        return periods.save(period);
    }

    public RegistrationPeriod update(Long id, RegistrationPeriodForm form) {
        RegistrationPeriod period = get(id);
        validate(form);
        apply(period, form);
        return period;
    }

    @Transactional(readOnly = true)
    public RegistrationPeriodForm toForm(RegistrationPeriod period) {
        RegistrationPeriodForm form = new RegistrationPeriodForm();
        form.setName(period.getName());
        form.setType(period.getType());
        form.setGvStart(period.getGvStart());
        form.setGvEnd(period.getGvEnd());
        form.setSvStart(period.getSvStart());
        form.setSvEnd(period.getSvEnd());
        form.setGvpbDeadline(period.getGvpbDeadline());
        form.setCouncilReportDate(period.getCouncilReportDate());
        return form;
    }

    /**
     * Chặn đăng ký đề tài của giảng viên ngoài thời gian quy định.
     * TopicService gọi hàm này khi GV gửi đề tài duyệt; TV3 sẽ dùng biến thể SV tương tự.
     */
    public void assertGvWindowOpen(Long id) {
        RegistrationPeriod period = get(id);
        if (!period.isGvWindowOpen(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Ngoài thời gian đăng ký đề tài của giảng viên (" + period.getGvStartText()
                            + " - " + period.getGvEndText() + ")");
        }
    }

    /** Chặn đăng ký đề tài của nhóm sinh viên ngoài thời gian quy định (TV3 dùng). */
    public void assertSvWindowOpen(Long id) {
        RegistrationPeriod period = get(id);
        if (!period.isSvWindowOpen(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Ngoài thời gian đăng ký đề tài của sinh viên (" + period.getSvStartText()
                            + " - " + period.getSvEndText() + ")");
        }
    }

    /**
     * Kiểm tra tính hợp lệ của các mốc thời gian theo loại đợt:
     * - Hai giai đoạn GV/SV phải theo đúng thứ tự.
     * - Hạn GVPB nộp điểm: bắt buộc và chỉ cho phép với TLCN/KLTN.
     * - Ngày báo cáo hội đồng: bắt buộc và chỉ cho phép với KLTN.
     */
    private void validate(RegistrationPeriodForm form) {
        String error = null;
        if (!form.getGvStart().isBefore(form.getGvEnd())) {
            error = "Thời gian bắt đầu đăng ký của giảng viên phải trước thời gian kết thúc";
        } else if (!form.getSvStart().isBefore(form.getSvEnd())) {
            error = "Thời gian bắt đầu đăng ký của sinh viên phải trước thời gian kết thúc";
        } else if (form.getSvStart().isBefore(form.getGvEnd())) {
            error = "Giai đoạn sinh viên phải bắt đầu sau khi giai đoạn giảng viên kết thúc (hai giai đoạn riêng biệt)";
        } else if (form.getType().requiresGvpbDeadline() && form.getGvpbDeadline() == null) {
            error = "Hạn GVPB nộp điểm là bắt buộc với đợt " + form.getType().getDisplayName();
        } else if (!form.getType().requiresGvpbDeadline() && form.getGvpbDeadline() != null) {
            error = "Chỉ đợt Tiểu luận chuyên ngành hoặc Khóa luận tốt nghiệp mới thiết lập hạn GVPB nộp điểm";
        } else if (form.getType().requiresGvpbDeadline() && !form.getGvpbDeadline().isAfter(form.getSvEnd())) {
            error = "Hạn GVPB nộp điểm phải sau khi đợt đăng ký của sinh viên kết thúc";
        } else if (form.getType().requiresCouncilReportDate() && form.getCouncilReportDate() == null) {
            error = "Ngày báo cáo hội đồng là bắt buộc với đợt Khóa luận tốt nghiệp";
        } else if (!form.getType().requiresCouncilReportDate() && form.getCouncilReportDate() != null) {
            error = "Chỉ đợt Khóa luận tốt nghiệp mới thiết lập ngày báo cáo hội đồng";
        } else if (form.getType().requiresCouncilReportDate()
                && !form.getCouncilReportDate().isAfter(form.getGvpbDeadline())) {
            error = "Ngày báo cáo hội đồng phải sau hạn GVPB nộp điểm";
        }
        if (error != null) {
            throw new BusinessRuleException(error);
        }
    }

    private static void apply(RegistrationPeriod period, RegistrationPeriodForm form) {
        period.setName(form.getName().trim());
        period.setType(form.getType());
        period.setGvStart(form.getGvStart());
        period.setGvEnd(form.getGvEnd());
        period.setSvStart(form.getSvStart());
        period.setSvEnd(form.getSvEnd());
        period.setGvpbDeadline(form.getGvpbDeadline());
        period.setCouncilReportDate(form.getCouncilReportDate());
    }
}