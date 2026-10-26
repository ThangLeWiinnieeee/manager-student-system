package vn.edu.fit.topicmanagement.council;

import java.util.List;
import vn.edu.fit.topicmanagement.registrationperiod.RoundType;
import vn.edu.fit.topicmanagement.evaluation.EvaluationStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistrationService;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationRepository;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.topic.TopicService;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
@Transactional
public class CouncilService {

    private final CouncilRepository councils;
    private final CouncilMemberRepository members;
    private final TopicAssignmentRepository assignments;
    private final CourseRegistrationService periods;
    private final TopicService topics;
    private final UserAccountService users;
    private final GroupTopicRegistrationRepository groupRegistrations;
    private final vn.edu.fit.topicmanagement.evaluation.EvaluationRepository evaluations;
    private final vn.edu.fit.topicmanagement.result.TopicFinalResultRepository results;

    public CouncilService(
            CouncilRepository councils,
            CouncilMemberRepository members,
            TopicAssignmentRepository assignments,
            CourseRegistrationService periods,
            TopicService topics,
            UserAccountService users,
            GroupTopicRegistrationRepository groupRegistrations,
            vn.edu.fit.topicmanagement.evaluation.EvaluationRepository evaluations,
            vn.edu.fit.topicmanagement.result.TopicFinalResultRepository results) {
        this.councils = councils;
        this.members = members;
        this.assignments = assignments;
        this.periods = periods;
        this.topics = topics;
        this.users = users;
        this.groupRegistrations = groupRegistrations;
        this.evaluations = evaluations;
        this.results = results;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    @Transactional(readOnly = true)
    public List<Council> findAll() {
        return councils.findAllByOrderByCreatedAtDesc();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    @Transactional(readOnly = true)
    public List<Council> findByRound(Long roundId) {
        return councils.findByRoundIdOrderByNameAsc(roundId);
    }

    @Transactional(readOnly = true)
    public Council get(Long id) {
        return councils.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội đồng"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public Council create(CouncilForm form) {
        String name = form.getName().trim();
        CourseRegistration round = periods.get(form.getRoundId());
        if (councils.existsByRoundIdAndNameIgnoreCase(round.getId(), name)) {
            throw new BusinessRuleException("Tên hội đồng đã tồn tại trong đợt đăng ký này");
        }
        Council council = new Council();
        council.setName(name);
        council.setRound(round);
        council.setLocation(form.getLocation() == null ? "" : form.getLocation().trim());
        council.setReportDate(form.getReportDate());
        council.setStatus(CouncilStatus.DRAFT);
        return councils.save(council);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public Council update(Long id, CouncilForm form) {
        Council council = get(id);
        periods.lock(council.getRound().getId());
        assertCouncilEditable(id);
        String name = form.getName().trim();
        CourseRegistration round = periods.get(form.getRoundId());
        if (councils.existsByRoundIdAndNameIgnoreCaseAndIdNot(round.getId(), name, id)) {
            throw new BusinessRuleException("Tên hội đồng đã tồn tại trong đợt đăng ký này");
        }
        if (!council.getRound().getId().equals(round.getId()) && members.countByCouncil_Id(id) > 0) {
            throw new BusinessRuleException("Không thể đổi đợt của hội đồng đã có thành viên");
        }
        council.setName(name);
        council.setRound(round);
        council.setLocation(form.getLocation() == null ? "" : form.getLocation().trim());
        council.setReportDate(form.getReportDate());
        return council;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void delete(Long id) {
        Council council = get(id);
        if (!assignments.findByCouncil_Id(id).isEmpty()) {
            throw new BusinessRuleException("Không thể xóa hội đồng đang được phân công đề tài");
        }
        councils.delete(council);
    }

    // ==========================================
    // Quản lý thành viên hội đồng
    // ==========================================

    @Transactional(readOnly = true)
    public List<CouncilMember> getMembers(Long councilId) {
        get(councilId);
        return members.findByCouncil_IdOrderByRoleAsc(councilId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void addOrUpdateMember(Long councilId, CouncilMemberForm form) {
        Council council = get(councilId);
        periods.lock(council.getRound().getId());
        assertCouncilEditable(councilId);
        UserAccount lecturer = users.get(form.getLecturerId());
        if (lecturer.getRole() != Role.LECTURER || !lecturer.isEnabled()) {
            throw new BusinessRuleException("Thành viên hội đồng phải là giảng viên đang hoạt động");
        }

        CouncilMemberId memberId = new CouncilMemberId(councilId, lecturer.getId());
        boolean isExisting = members.existsById(memberId);

        if (!isExisting) {
            long currentCount = members.countByCouncil_Id(councilId);
            if (currentCount >= 5) {
                throw new BusinessRuleException("Hội đồng chỉ được phép có tối đa 5 thành viên");
            }
        }

        if (form.getRole() == CouncilRole.PRESIDENT) {
            boolean hasPresident = members.findByCouncil_IdOrderByRoleAsc(councilId).stream()
                    .anyMatch(m -> m.getRole() == CouncilRole.PRESIDENT && !m.getLecturer().getId().equals(lecturer.getId()));
            if (hasPresident) {
                throw new BusinessRuleException("Hội đồng chỉ được có đúng 1 Chủ tịch");
            }
        }

        if (form.getRole() == CouncilRole.SECRETARY) {
            boolean hasSecretary = members.findByCouncil_IdOrderByRoleAsc(councilId).stream()
                    .anyMatch(m -> m.getRole() == CouncilRole.SECRETARY && !m.getLecturer().getId().equals(lecturer.getId()));
            if (hasSecretary) {
                throw new BusinessRuleException("Hội đồng chỉ được có đúng 1 Thư ký");
            }
        }

        // Chặn nếu hội đồng đã được phân công đề tài mà giảng viên này đang hướng dẫn đề tài đó
        List<TopicAssignment> assignedTopics = assignments.findByCouncil_Id(councilId);
        for (TopicAssignment ta : assignedTopics) {
            if (ta.getTopic().getAdvisors().stream().anyMatch(adv -> adv.getId().equals(lecturer.getId()))) {
                throw new BusinessRuleException("Giảng viên " + lecturer.getFullName()
                        + " đang hướng dẫn đề tài '" + ta.getTopic().getTitle() + "' đã phân công vào hội đồng này");
            }
        }

        CouncilMember member = new CouncilMember(council, lecturer, form.getRole());
        members.save(member);
        updateCouncilStatus(council);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void removeMember(Long councilId, Long lecturerId) {
        Council council = get(councilId);
        periods.lock(council.getRound().getId());
        assertCouncilEditable(councilId);
        CouncilMemberId memberId = new CouncilMemberId(councilId, lecturerId);
        if (!members.existsById(memberId)) {
            throw new ResourceNotFoundException("Thành viên không thuộc hội đồng này");
        }
        members.deleteById(memberId);
        council.getMembers().removeIf(m -> m.getLecturer().getId().equals(lecturerId));
        updateCouncilStatus(council);
    }

    private void updateCouncilStatus(Council council) {
        long count = members.countByCouncil_Id(council.getId());
        boolean hasPres = members.existsByCouncil_IdAndRole(council.getId(), CouncilRole.PRESIDENT);
        boolean hasSec = members.existsByCouncil_IdAndRole(council.getId(), CouncilRole.SECRETARY);
        if (count >= 3 && count <= 5 && hasPres && hasSec) {
            council.setStatus(CouncilStatus.ACTIVE);
        } else {
            council.setStatus(CouncilStatus.DRAFT);
        }
        councils.save(council);
    }

    // ==========================================
    // Phân công đề tài & GVPB
    // ==========================================

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public TopicAssignment assignTopic(TopicAssignmentForm form) {
        var registration = lockAssessment(form.getRegistrationId());
        Topic topic = registration.getTopic();
        if (registration.getStatus() != GroupTopicRegistrationStatus.APPROVED)
            throw new BusinessRuleException("Chỉ được phân công nhóm đã được chấp thuận đề tài");

        RoundType type = topic.getRound().getType();
        TopicAssignment assignment = assignments.findByRegistration_Id(registration.getId()).orElse(null);
        boolean addingDefenseCouncil = type == RoundType.KLTN && assignment != null
                && assignment.getCouncil() == null && form.getCouncilId() != null
                && assignment.getReviewer() != null
                && assignment.getReviewer().getId().equals(form.getReviewerId())
                && form.getSupporterId() == null && Boolean.TRUE.equals(getDefenseDecision(registration.getId()))
                && results.findByRegistration_Id(registration.getId()).isEmpty();
        if (!addingDefenseCouncil) assertAssessmentEditable(registration.getId());

        if (type != RoundType.NCKH && form.getReviewerId() == null)
            throw new BusinessRuleException("Vui lòng chọn giảng viên chấm / phản biện");
        if (type == RoundType.NCKH && (form.getCouncilId() == null || form.getReviewerId() != null))
            throw new BusinessRuleException("NCKH cần hội đồng nghiệm thu, không phân công GVPB riêng");
        if ((type == RoundType.MON_HOC || type == RoundType.TLCN) && form.getCouncilId() != null)
            throw new BusinessRuleException("Môn học và TLCN không sử dụng hội đồng");
        if (type == RoundType.TLCN && form.getSupporterId() == null)
            throw new BusinessRuleException("TLCN cần chọn một GV hướng dẫn làm GV hỗ trợ chấm trước");
        if (type != RoundType.TLCN && form.getSupporterId() != null)
            throw new BusinessRuleException("Chỉ TLCN có lượt chấm của GV hỗ trợ");
        if (type == RoundType.KLTN && form.getCouncilId() != null
                && !Boolean.TRUE.equals(getDefenseDecision(registration.getId())))
            throw new BusinessRuleException("Chỉ xếp hội đồng sau khi GVPB đã cho phép bảo vệ");

        if (assignment == null) {
            assignment = new TopicAssignment();
            assignment.setRegistration(registration);
        }
        UserAccount reviewer = form.getReviewerId() == null ? null : activeLecturer(form.getReviewerId());
        if (reviewer != null && topic.getAdvisors().stream().anyMatch(a -> a.getId().equals(reviewer.getId())))
            throw new BusinessRuleException("Giảng viên hướng dẫn không được làm phản biện cho chính đề tài của mình");
        assignment.setReviewer(reviewer);

        UserAccount supporter = form.getSupporterId() == null ? null : activeLecturer(form.getSupporterId());
        if (supporter != null && (topic.getAdvisors().stream().noneMatch(a -> a.getId().equals(supporter.getId()))
                || supporter.getId().equals(reviewer.getId())))
            throw new BusinessRuleException("GV hỗ trợ phải là GV hướng dẫn và khác GVPB");
        assignment.setSupporter(supporter);

        Council council = form.getCouncilId() == null ? null : get(form.getCouncilId());
        if (council != null) {
            assertReady(council);
            if (!council.getRound().getId().equals(topic.getRound().getId()))
                throw new BusinessRuleException("Hội đồng phải thuộc cùng môn/lớp với đề tài");
            for (CouncilMember cm : council.getMembers()) {
                Long lecturerId = cm.getLecturer().getId();
                if (topic.getAdvisors().stream().anyMatch(a -> a.getId().equals(lecturerId)))
                    throw new BusinessRuleException("Thành viên hội đồng không được là GVHD của đề tài");
                if (reviewer != null && reviewer.getId().equals(lecturerId))
                    throw new BusinessRuleException("GVPB KLTN không kiêm thành viên hội đồng để tránh tính điểm hai lần");
            }
        }
        assignment.setCouncil(council);
        assignment.setLegacyAssessment(false);

        return assignments.save(assignment);
    }

    private UserAccount activeLecturer(Long id) {
        var lecturer = users.get(id);
        if (!lecturer.isEnabled() || lecturer.getRole() != Role.LECTURER)
            throw new BusinessRuleException("Người chấm phải là giảng viên đang hoạt động");
        return lecturer;
    }

    @Transactional(readOnly = true)
    public Boolean getDefenseDecision(Long registrationId) {
        var assignment = getAssignment(registrationId);
        if (assignment == null || assignment.getReviewer() == null) return null;
        return evaluations.findByRegistration_IdAndEvaluator_Id(registrationId, assignment.getReviewer().getId())
                .filter(e -> e.getStatus() == EvaluationStatus.SUBMITTED)
                .map(vn.edu.fit.topicmanagement.evaluation.Evaluation::getDefenseAllowed).orElse(null);
    }

    public GroupTopicRegistration getRegistration(Long id) {
        return groupRegistrations.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký của nhóm"));
    }

    public GroupTopicRegistration lockAssessment(Long id) {
        periods.lock(groupRegistrations.findRoundIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký của nhóm")));
        return getRegistration(id);
    }

    public void assertReady(Council council) {
        if (council == null) throw new BusinessRuleException("Nhóm chưa được phân công hội đồng");
        var list = members.findByCouncil_IdOrderByRoleAsc(council.getId());
        if (list.size() < 3 || list.size() > 5
                || list.stream().filter(m -> m.getRole() == CouncilRole.PRESIDENT).count() != 1
                || list.stream().filter(m -> m.getRole() == CouncilRole.SECRETARY).count() != 1
                || list.stream().anyMatch(m -> !m.getLecturer().isEnabled() || m.getLecturer().getRole() != Role.LECTURER))
            throw new BusinessRuleException("Hội đồng cần 3–5 GV đang hoạt động, đúng một chủ tịch và một thư ký");
    }

    private void assertCouncilEditable(Long councilId) {
        if (!assignments.findByCouncil_Id(councilId).isEmpty())
            throw new BusinessRuleException("Hội đồng đã nhận nhóm: không thể đổi thành viên hoặc thông tin");
    }

    private void assertAssessmentEditable(Long registrationId) {
        if (!evaluations.findByRegistration_Id(registrationId).isEmpty() || results.findByRegistration_Id(registrationId).isPresent())
            throw new BusinessRuleException("Nhóm đã có phiếu chấm/kết quả: không thể thay đổi phân công");
    }

    public void assertCanViewAssessment(Long registrationId, String email) {
        if (CourseRegistrationService.isManager()) return;
        var assignment = getAssignment(registrationId);
        if (assignment != null && ((assignment.getSupporter() != null && assignment.getSupporter().getEmail().equalsIgnoreCase(email))
                || (assignment.getReviewer() != null && assignment.getReviewer().getEmail().equalsIgnoreCase(email))
                || (assignment.getCouncil() != null && assignment.getCouncil().getMembers().stream()
                .anyMatch(m -> m.getLecturer().getEmail().equalsIgnoreCase(email))))) return;
        throw new org.springframework.security.access.AccessDeniedException("Bạn không được phân công chấm nhóm này");
    }

    @Transactional(readOnly = true)
    public TopicAssignment getAssignment(Long registrationId) {
        return assignments.findByRegistration_Id(registrationId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TopicAssignment> getAssignmentsByCouncil(Long councilId) {
        return assignments.findByCouncil_Id(councilId);
    }

    @Transactional(readOnly = true)
    public List<TopicAssignment> getAssignmentsByReviewer(Long reviewerId) {
        return assignments.findByReviewer_Id(reviewerId);
    }

    @Transactional(readOnly = true)
    public List<TopicAssignment> getAssignmentsBySupporter(Long lecturerId) {
        return assignments.findBySupporter_Id(lecturerId);
    }

    @Transactional(readOnly = true)
    public List<Council> findMyCouncils(String lecturerEmail) {
        UserAccount lecturer = users.getByEmail(lecturerEmail);
        return councils.findByLecturerId(lecturer.getId());
    }

    @Transactional(readOnly = true)
    public CouncilForm toForm(Council council) {
        CouncilForm form = new CouncilForm();
        form.setName(council.getName());
        form.setRoundId(council.getRound().getId());
        form.setLocation(council.getLocation());
        form.setReportDate(council.getReportDate());
        return form;
    }
}
