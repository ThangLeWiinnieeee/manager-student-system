package vn.edu.fit.topicmanagement.topic;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;
import vn.edu.fit.topicmanagement.department.Department;
import vn.edu.fit.topicmanagement.department.DepartmentService;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistrationService;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

/**
 * TV2 - Nghiệp vụ đề tài: đăng ký, sửa, gửi duyệt, duyệt/từ chối, công bố, xóa.
 * Mọi ràng buộc "1-2 GVHD", "chỉ chủ sở hữu được sửa", "chặn nhảy trạng thái",
 * "chặn gửi duyệt ngoài thời gian đợt" đều tập trung ở đây để JSP không chứa nghiệp vụ.
 */
@Service
@Transactional
public class TopicService {
    private static final int PAGE_SIZE = 10;

    private final TopicRepository topics;
    private final CourseRegistrationService periods;
    private final DepartmentService departments;
    private final UserAccountService users;
    private final vn.edu.fit.topicmanagement.council.TopicAssignmentRepository assignments;
    private final vn.edu.fit.topicmanagement.studentgroup.StudentGroupMemberRepository members;

    public TopicService(
            TopicRepository topics,
            CourseRegistrationService periods,
            DepartmentService departments,
            UserAccountService users,
            vn.edu.fit.topicmanagement.council.TopicAssignmentRepository assignments,
            vn.edu.fit.topicmanagement.studentgroup.StudentGroupMemberRepository members) {
        this.topics = topics;
        this.periods = periods;
        this.departments = departments;
        this.users = users;
        this.assignments = assignments;
        this.members = members;
    }

    @Transactional(readOnly = true)
    public Topic get(Long id) {
        return topics.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài"));
    }

    @Transactional(readOnly = true)
    public Page<Topic> search(Long roundId, Long departmentId, TopicStatus status,
                              String viewerEmail, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("updatedAt").descending());
        return topics.search(roundId, departmentId, status, viewerEmail, TopicStatus.PUBLISHED, pageable);
    }

    /** GV tạo đề tài (mặc định là bản nháp); submitNow = true sẽ gửi duyệt ngay. */
    public Topic create(String ownerEmail, TopicForm form, boolean submitNow) {
        UserAccount owner = users.getByEmail(ownerEmail.trim());
        Department department = resolveDepartment(form.getDepartmentId());
        CourseRegistration round = periods.get(form.getRoundId());
        periods.assertCanManage(round);
        if (round.getRegistrationMode() == vn.edu.fit.topicmanagement.registrationperiod.RegistrationMode.SELF_PROPOSED)
            throw new BusinessRuleException("Đợt này dành cho sinh viên tự đề xuất; không cần tạo đề tài trống");
        if (department == null && round.getDepartment() == null) throw new BusinessRuleException("Vui lòng chọn bộ môn");
        if (department == null) department = round.getDepartment();
        Set<UserAccount> advisors = resolveAdvisors(form.getAdvisorIds());

        Topic topic = new Topic();
        topic.setCreatedBy(owner);
        topic.setDepartment(department);
        topic.setRound(round);
        topic.setStatus(TopicStatus.DRAFT);
        apply(topic, form, advisors);
        Topic saved = topics.save(topic);
        return submitNow ? submit(saved.getId(), ownerEmail) : saved;
    }

    public Topic update(Long id, String ownerEmail, TopicForm form, boolean submitNow) {
        Topic topic = get(id);
        requireOwner(topic, ownerEmail);
        requireEditable(topic);
        topic.setDepartment(resolveDepartment(form.getDepartmentId()));
        topic.setRound(periods.get(form.getRoundId()));
        periods.assertCanManage(topic.getRound());
        if (topic.getRound().getRegistrationMode() == vn.edu.fit.topicmanagement.registrationperiod.RegistrationMode.SELF_PROPOSED)
            throw new BusinessRuleException("Đợt này dành cho sinh viên tự đề xuất đề tài");
        if (topic.getDepartment() == null) topic.setDepartment(topic.getRound().getDepartment());
        if (topic.getDepartment() == null) throw new BusinessRuleException("Vui lòng chọn bộ môn");
        Set<UserAccount> advisors = resolveAdvisors(form.getAdvisorIds());
        apply(topic, form, advisors);
        Topic saved = topics.save(topic);
        return submitNow ? submit(id, ownerEmail) : saved;
    }

    /** GV gửi đề tài chờ duyệt - phải trong thời gian đăng ký của đợt. */
    public Topic submit(Long id, String ownerEmail) {
        Topic topic = get(id);
        requireOwner(topic, ownerEmail);
        requireStatusTransition(topic, TopicStatus.PENDING);
        periods.assertGvWindowOpen(topic.getRound().getId());
        requireContent(topic);
        topic.setStatus(TopicStatus.PENDING);
        topic.setRejectionReason(null);
        return topics.save(topic);
    }

    /** Khoa duyệt đề tài. */
    public Topic approve(Long id) {
        Topic topic = get(id);
        if (topic.getProposedGroupId() != null) throw new BusinessRuleException("Duyệt nội dung đề xuất tại danh sách đăng ký nhóm");
        requireContent(topic);
        requireStatusTransition(topic, TopicStatus.APPROVED);
        topic.setStatus(TopicStatus.APPROVED);
        topic.setRejectionReason(null);
        return topics.save(topic);
    }

    /** Khoa từ chối kèm lý do - lý do hiển thị cho GV để sửa và gửi lại. */
    public Topic reject(Long id, String reason) {
        Topic topic = get(id);
        if (topic.getProposedGroupId() != null) throw new BusinessRuleException("Từ chối đề xuất tại danh sách đăng ký nhóm");
        requireStatusTransition(topic, TopicStatus.REJECTED);
        topic.setStatus(TopicStatus.REJECTED);
        topic.setRejectionReason(reason == null ? "" : reason.trim());
        return topics.save(topic);
    }

    /** Khoa công bố đề tài - chỉ đề tài đã duyệt mới được công bố. */
    public Topic publish(Long id) {
        Topic topic = get(id);
        if (topic.getProposedGroupId() != null) throw new BusinessRuleException("Đề xuất riêng thuộc nhóm, không đưa vào danh sách chọn đề tài");
        requireStatusTransition(topic, TopicStatus.PUBLISHED);
        topic.setStatus(TopicStatus.PUBLISHED);
        return topics.save(topic);
    }

    /** GV xóa đề tài nháp/bị từ chối của chính mình. */
    public void delete(Long id, String ownerEmail) {
        Topic topic = get(id);
        requireOwner(topic, ownerEmail);
        requireEditable(topic);
        topics.delete(topic);
    }

    @Transactional(readOnly = true)
    public TopicForm toForm(Topic topic) {
        TopicForm form = new TopicForm();
        form.setTitle(topic.getTitle());
        form.setDescription(topic.getDescription());
        form.setRoundId(topic.getRound().getId());
        form.setDepartmentId(topic.getDepartment() == null ? null : topic.getDepartment().getId());
        form.setAdvisorIds(topic.getAdvisors().stream().map(UserAccount::getId).toList());
        return form;
    }

    private void apply(Topic topic, TopicForm form, Set<UserAccount> advisors) {
        topic.setTitle(form.getTitle().trim());
        topic.setDescription(form.getDescription() == null ? "" : form.getDescription().trim());
        topic.setAdvisors(advisors);
    }

    private void requireOwner(Topic topic, String email) {
        if (!topic.isManagedBy(email)) {
            throw new BusinessRuleException("Bạn không phụ trách đề tài của môn/lớp này");
        }
    }

    private void requireEditable(Topic topic) {
        if (!topic.isEditableStatus()) {
            throw new BusinessRuleException(
                    "Chỉ đề tài ở trạng thái " + TopicStatus.DRAFT.getDisplayName() + " hoặc "
                            + TopicStatus.REJECTED.getDisplayName() + " mới được chỉnh sửa");
        }
    }

    private void requireStatusTransition(Topic topic, TopicStatus target) {
        if (!topic.getStatus().canTransitionTo(target)) {
            throw new BusinessRuleException("Không thể chuyển đề tài từ trạng thái "
                    + topic.getStatus().getDisplayName() + " sang " + target.getDisplayName());
        }
    }

    private Department resolveDepartment(Long id) {
        if (id == null) return null;
        Department department = departments.get(id);
        if (!department.isEnabled()) {
            throw new BusinessRuleException("Bộ môn đã bị khóa");
        }
        return department;
    }

    private void requireContent(Topic topic) {
        if (topic.getDepartment() == null || topic.getDescription() == null || topic.getDescription().isBlank())
            throw new BusinessRuleException("Đề tài cần bộ môn và mô tả nội dung trước khi gửi duyệt");
    }

    @Transactional(readOnly = true)
    public Topic getVisible(Long id, String email) {
        Topic topic = get(id);
        if (topic.isManagedBy(email) || CourseRegistrationService.isManager()
                || (topic.getProposedGroupId() == null && topic.getStatus() == TopicStatus.PUBLISHED)
                || topic.getCreatedBy().getEmail().equalsIgnoreCase(email)
                || topic.getAdvisors().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email))
                || (topic.getProposedGroupId() != null && members.existsByGroup_IdAndStudent_EmailIgnoreCase(topic.getProposedGroupId(), email))
                || assignments.findByTopic_Id(id).stream().anyMatch(a ->
                    (a.getReviewer() != null && a.getReviewer().getEmail().equalsIgnoreCase(email))
                    || (a.getCouncil() != null && a.getCouncil().getMembers().stream().anyMatch(m -> m.getLecturer().getEmail().equalsIgnoreCase(email))))) return topic;
        throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền xem đề tài này");
    }

    @Transactional(readOnly = true)
    public Topic getEditable(Long id, String email) {
        Topic topic = get(id);
        requireOwner(topic, email);
        requireEditable(topic);
        return topic;
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','DEAN')")
    public void assignAdvisors(Long id, List<Long> advisorIds) {
        Topic topic = get(id);
        periods.lock(topic.getRound().getId());
        if (!assignments.findByTopic_Id(id).isEmpty()) throw new BusinessRuleException("Đã phân công chấm: không thể thay đổi GVHD");
        topic.setAdvisors(resolveAdvisors(advisorIds));
    }

    /** Ràng buộc "ít nhất một, tối đa hai GVHD" + GVHD phải là giảng viên đang hoạt động. */
    private Set<UserAccount> resolveAdvisors(List<Long> advisorIds) {
        Set<Long> distinctIds = new LinkedHashSet<>(advisorIds == null ? List.of() : advisorIds);
        if (distinctIds.isEmpty()) {
            throw new BusinessRuleException("Đề tài phải có ít nhất một giảng viên hướng dẫn");
        }
        if (distinctIds.size() > 2) {
            throw new BusinessRuleException("Đề tài chỉ được hướng dẫn bởi tối đa hai giảng viên");
        }
        Set<UserAccount> advisors = new LinkedHashSet<>();
        for (Long advisorId : distinctIds) {
            UserAccount lecturer = users.get(advisorId);
            if (lecturer.getRole() != Role.LECTURER || !lecturer.isEnabled()) {
                throw new BusinessRuleException("Người được chọn hướng dẫn phải là giảng viên đang hoạt động");
            }
            advisors.add(lecturer);
        }
        return advisors;
    }
}
