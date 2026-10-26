package vn.edu.fit.topicmanagement.registrationperiod;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

@Service
@Transactional
public class RegistrationPeriodService {
    private final RegistrationPeriodRepository periods;
    private final CourseRegistrationRepository courses;
    public RegistrationPeriodService(RegistrationPeriodRepository periods, CourseRegistrationRepository courses) {
        this.periods = periods;
        this.courses = courses;
    }
    @Transactional(readOnly = true)
    public List<RegistrationPeriod> findAll() { return periods.findAllByOrderByGvStartDesc(); }
    @Transactional(readOnly = true)
    public RegistrationPeriod get(Long id) {
        return periods.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký"));
    }
    public RegistrationPeriod lock(Long id) {
        return periods.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đăng ký"));
    }
    @PreAuthorize("hasAnyRole('ADMIN','DEAN')")
    public RegistrationPeriod create(RegistrationPeriodForm form) {
        validate(form);
        RegistrationPeriod period = new RegistrationPeriod();
        apply(period, form);
        return periods.save(period);
    }
    @PreAuthorize("hasAnyRole('ADMIN','DEAN')")
    public RegistrationPeriod update(Long id, RegistrationPeriodForm form) {
        RegistrationPeriod period = lock(id);
        validate(form);
        if (period.getType() != form.getType() && courses.existsByPeriod_Id(id))
            throw new BusinessRuleException("Đợt đã có môn/lớp: không thể đổi loại đợt");
        apply(period, form);
        return period;
    }
    @PreAuthorize("hasAnyRole('ADMIN','DEAN')")
    public void delete(Long id) {
        RegistrationPeriod period = lock(id);
        if (period.isEnded()) throw new BusinessRuleException("Không thể xóa đợt đăng ký đã kết thúc");
        if (courses.existsByPeriod_Id(id)) throw new BusinessRuleException("Đợt đã có môn/lớp đăng ký, không thể xóa");
        periods.delete(period);
        periods.flush();
    }
    public RegistrationPeriodForm toForm(RegistrationPeriod period) {
        RegistrationPeriodForm form = new RegistrationPeriodForm();
        form.setType(period.getType());
        form.setGvStart(period.getGvStart()); form.setGvEnd(period.getGvEnd());
        form.setSvStart(period.getSvStart()); form.setSvEnd(period.getSvEnd());
        form.setGvpbDeadline(period.getGvpbDeadline()); form.setCouncilReportDate(period.getCouncilReportDate());
        return form;
    }
    private void apply(RegistrationPeriod period, RegistrationPeriodForm form) {
        period.setName(form.getType().getDisplayName() + " — " + form.getGvStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        period.setType(form.getType());
        period.setGvStart(form.getGvStart()); period.setGvEnd(form.getGvEnd());
        period.setSvStart(form.getSvStart()); period.setSvEnd(form.getSvEnd());
        period.setGvpbDeadline(form.getGvpbDeadline()); period.setCouncilReportDate(form.getCouncilReportDate());
    }
    private void validate(RegistrationPeriodForm form) {
        if (form.getType() == null || form.getGvStart() == null || form.getGvEnd() == null
                || form.getSvStart() == null || form.getSvEnd() == null)
            throw new BusinessRuleException("Cần loại đợt và đầy đủ thời gian đăng ký GV/SV");
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

}
