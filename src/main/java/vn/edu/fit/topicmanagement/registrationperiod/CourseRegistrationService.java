package vn.edu.fit.topicmanagement.registrationperiod;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

@Service
@Transactional
public class CourseRegistrationService {
    private final CourseRegistrationRepository periods;
    private final RegistrationPeriodService windows;
    private final CourseParticipantRepository participants;
    private final vn.edu.fit.topicmanagement.user.UserAccountRepository users;
    private final vn.edu.fit.topicmanagement.department.DepartmentRepository departments;

    public CourseRegistrationService(CourseRegistrationRepository periods, RegistrationPeriodService windows,
            vn.edu.fit.topicmanagement.user.UserAccountRepository users,
            vn.edu.fit.topicmanagement.department.DepartmentRepository departments, CourseParticipantRepository participants) {
        this.periods = periods;
        this.windows = windows;
        this.users = users;
        this.departments = departments;
        this.participants = participants;
    }

    @Transactional(readOnly = true)
    public List<CourseRegistration> findAll() {
        return periods.findAllByOrderByPeriod_GvStartDesc();
    }

    @Transactional(readOnly = true)
    public CourseRegistration get(Long id) {
        return periods.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn/lớp đăng ký"));
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('LECTURER')")
    public CourseRegistration create(CourseRegistrationForm form) {
        if (form.getPeriodId() == null) throw new BusinessRuleException("Vui lòng chọn đợt đăng ký");
        var window = windows.lock(form.getPeriodId());
        fillCourseName(form, window);
        validate(form);
        if (!window.isGvOpen()) throw new BusinessRuleException("Ngoài thời gian giảng viên đăng ký môn/lớp");
        CourseRegistration period = new CourseRegistration();
        period.setPeriod(window);
        requireUniqueClass(period, form);
        apply(period, form);
        return periods.save(period);
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','DEAN','LECTURER')")
    public CourseRegistration update(Long id, CourseRegistrationForm form) {
        CourseRegistration period = lock(id);
        assertCanManage(period);
        fillCourseName(form, period.getPeriod());
        validate(form);
        if (!isManager() && !period.isGvWindowOpen(LocalDateTime.now()))
            throw new BusinessRuleException("Ngoài thời gian giảng viên cập nhật đăng ký môn/lớp");
        if (!period.getPeriod().getId().equals(form.getPeriodId()))
            throw new BusinessRuleException("Không thể chuyển môn/lớp sang đợt khác");
        requireUniqueClass(period, form);
        if ((periods.hasRelatedData(id) || participants.existsByCourse_Id(id))
                && (period.getRegistrationMode() != form.getRegistrationMode()
                || period.getMaxMembers() != form.getMaxMembers()
                || (!"LEGACY".equals(period.getClassCode()) && ((period.getType() == RoundType.MON_HOC && !java.util.Objects.equals(period.getCourseName(), form.getCourseName().trim()))
                    || !java.util.Objects.equals(period.getClassCode(), form.getClassCode().trim())))
                || (period.getDepartment() != null && !java.util.Objects.equals(period.getDepartment().getId(), form.getDepartmentId()))
                || (isManager() && period.getLecturer() != null && !java.util.Objects.equals(period.getLecturer().getId(), form.getLecturerId())))) {
            throw new BusinessRuleException("Lớp đã có sinh viên, đề tài, nhóm hoặc hội đồng: không thể đổi môn/lớp, GV, bộ môn, hình thức hoặc giới hạn nhóm");
        }
        apply(period, form);
        return period;
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    public void delete(Long id) {
        CourseRegistration period = lock(id);
        assertCanManage(period);
        if (period.isEnded()) {
            throw new BusinessRuleException("Không thể xóa môn/lớp thuộc đợt đăng ký đã kết thúc");
        }
        if (periods.hasRelatedData(id) || participants.existsByCourse_Id(id)) {
            throw new BusinessRuleException("Không thể xóa môn/lớp đã có đề tài, nhóm sinh viên hoặc hội đồng");
        }
        periods.delete(period);
        periods.flush();
    }

    @Transactional(readOnly = true)
    public CourseRegistrationForm toForm(CourseRegistration period) {
        CourseRegistrationForm form = new CourseRegistrationForm();
        form.setPeriodId(period.getPeriod().getId());
        form.setCourseName(period.getCourseName());
        form.setClassCode(period.getClassCode());
        form.setLecturerId(period.getLecturer() == null ? null : period.getLecturer().getId());
        form.setDepartmentId(period.getDepartment() == null ? null : period.getDepartment().getId());
        form.setRegistrationMode(period.getRegistrationMode());
        form.setMaxMembers(period.getMaxMembers());
        return form;
    }

    /**
     * Chặn đăng ký đề tài của giảng viên ngoài thời gian quy định.
     * TopicService gọi hàm này khi GV gửi đề tài duyệt; TV3 sẽ dùng biến thể SV tương tự.
     */
    public void assertGvWindowOpen(Long id) {
        CourseRegistration period = get(id);
        if (!period.isGvWindowOpen(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Ngoài thời gian đăng ký đề tài của giảng viên (" + period.getGvStartText()
                            + " - " + period.getGvEndText() + ")");
        }
    }

    /** Chặn đăng ký đề tài của nhóm sinh viên ngoài thời gian quy định (TV3 dùng). */
    public void assertSvWindowOpen(Long id) {
        CourseRegistration period = get(id);
        if (!period.isSvWindowOpen(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Ngoài thời gian đăng ký đề tài của sinh viên (" + period.getSvStartText()
                            + " - " + period.getSvEndText() + ")");
        }
    }

    private void fillCourseName(CourseRegistrationForm form, RegistrationPeriod window) {
        if (window.getType() != RoundType.MON_HOC) {
            form.setCourseName(window.getType().getDisplayName());
        }
    }

    private void validate(CourseRegistrationForm form) {
        if (form.getCourseName() == null || form.getCourseName().isBlank()
                || form.getClassCode() == null || form.getClassCode().isBlank()
                || form.getMaxMembers() < 1 || form.getMaxMembers() > 5
                || form.getRegistrationMode() == null || form.getDepartmentId() == null) {
            throw new BusinessRuleException("Cần môn/lớp, bộ môn, hình thức đăng ký và giới hạn 1–5 thành viên");
        }
        if (form.getPeriodId() == null) throw new BusinessRuleException("Vui lòng chọn đợt đăng ký");
    }

    private void requireUniqueClass(CourseRegistration course, CourseRegistrationForm form) {
        if (periods.findByPeriod_IdAndCourseNameIgnoreCaseAndClassCodeIgnoreCase(
                course.getPeriod().getId(), form.getCourseName().trim(), form.getClassCode().trim()).stream()
                .anyMatch(other -> !java.util.Objects.equals(other.getId(), course.getId())))
            throw new BusinessRuleException("Môn/lớp này đã đăng ký trong đợt");
    }

    private void apply(CourseRegistration period, CourseRegistrationForm form) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new org.springframework.security.access.AccessDeniedException("Bạn chưa đăng nhập");
        var lecturer = isManager()
                ? users.findById(form.getLecturerId() == null ? -1L : form.getLecturerId()).orElseThrow(() -> new BusinessRuleException("Vui lòng chọn GV phụ trách"))
                : users.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> new BusinessRuleException("Không tìm thấy giảng viên"));
        if (lecturer.getRole() != vn.edu.fit.topicmanagement.user.Role.LECTURER || !lecturer.isEnabled())
            throw new BusinessRuleException("GV phụ trách phải là giảng viên đang hoạt động");
        var department = departments.findById(form.getDepartmentId()).orElseThrow(() -> new BusinessRuleException("Không tìm thấy bộ môn"));
        if (!department.isEnabled()) throw new BusinessRuleException("Bộ môn đã bị khóa");
        period.setLecturer(lecturer);
        period.setDepartment(department);
        period.setCourseName(form.getCourseName().trim());
        period.setClassCode(form.getClassCode().trim());
        period.setRegistrationMode(form.getRegistrationMode());
        period.setMaxMembers(form.getMaxMembers());
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','DEAN','LECTURER')")
    public void updateJoinCode(Long id, boolean renew, boolean enabled) {
        var course = lock(id);
        assertCanManage(course);
        if (renew || course.getJoinCode() == null) course.renewJoinCode();
        course.setJoiningEnabled(enabled);
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STUDENT')")
    public CourseRegistration join(String code) {
        String normalized = code == null ? "" : code.trim().toUpperCase(java.util.Locale.ROOT);
        if (!normalized.matches("[A-F0-9]{16}")) throw new BusinessRuleException("Mã tham gia không hợp lệ");
        var courseId = periods.findIdByJoinCode(normalized)
                .orElseThrow(() -> new BusinessRuleException("Mã tham gia không hợp lệ"));
        var course = lock(courseId);
        // Recheck under the course lock in case the lecturer just replaced the code.
        if (!normalized.equals(course.getJoinCode()) || !course.isJoiningEnabled())
            throw new BusinessRuleException("Mã tham gia đã đóng hoặc đã thay đổi");
        assertSvWindowOpen(course.getId());
        var student = currentStudent();
        if (!participants.existsByCourse_IdAndStudent_Id(course.getId(), student.getId()))
            participants.save(new CourseParticipant(course, student));
        return course;
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<CourseRegistration> findJoinedCourses() {
        return participants.findByStudent_Id(currentStudent().getId()).stream().map(CourseParticipant::getCourse).toList();
    }

    public void assertParticipant(Long courseId, Long studentId) {
        if (!participants.existsByCourse_IdAndStudent_Id(courseId, studentId))
            throw new BusinessRuleException("Sinh viên cần nhập mã tham gia lớp đề tài trước khi vào nhóm");
    }

    private vn.edu.fit.topicmanagement.user.UserAccount currentStudent() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        var student = users.findByEmailIgnoreCase(auth == null ? "" : auth.getName())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Bạn chưa đăng nhập"));
        if (!student.isEnabled() || student.getRole() != vn.edu.fit.topicmanagement.user.Role.STUDENT)
            throw new org.springframework.security.access.AccessDeniedException("Tài khoản không có quyền sinh viên");
        return student;
    }

    public CourseRegistration lock(Long id) {
        windows.lock(periods.findPeriodIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn/lớp đăng ký")));
        return periods.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn/lớp đăng ký"));
    }

    public static boolean isManager() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DEAN"));
    }

    public void assertCanManage(CourseRegistration period) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (!isManager() && (auth == null || period.getLecturer() == null
                || !period.getLecturer().getEmail().equalsIgnoreCase(auth.getName()))) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không phụ trách môn/lớp của đợt này");
        }
    }

    @Transactional(readOnly = true)
    public List<CourseRegistration> findManageable() {
        if (isManager()) return findAll();
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return findAll().stream().filter(p -> auth != null && p.getLecturer() != null
                && p.getLecturer().getEmail().equalsIgnoreCase(auth.getName())).toList();
    }
}
