package vn.edu.fit.topicmanagement.council;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodService;
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
    private final RegistrationPeriodService periods;
    private final TopicService topics;
    private final UserAccountService users;
    private final GroupTopicRegistrationRepository groupRegistrations;

    public CouncilService(
            CouncilRepository councils,
            CouncilMemberRepository members,
            TopicAssignmentRepository assignments,
            RegistrationPeriodService periods,
            TopicService topics,
            UserAccountService users,
            GroupTopicRegistrationRepository groupRegistrations) {
        this.councils = councils;
        this.members = members;
        this.assignments = assignments;
        this.periods = periods;
        this.topics = topics;
        this.users = users;
        this.groupRegistrations = groupRegistrations;
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
        RegistrationPeriod round = periods.get(form.getRoundId());
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
        String name = form.getName().trim();
        RegistrationPeriod round = periods.get(form.getRoundId());
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
            boolean hasPresident = council.getMembers().stream()
                    .anyMatch(m -> m.getRole() == CouncilRole.PRESIDENT && !m.getLecturer().getId().equals(lecturer.getId()));
            if (hasPresident) {
                throw new BusinessRuleException("Hội đồng chỉ được có đúng 1 Chủ tịch");
            }
        }

        if (form.getRole() == CouncilRole.SECRETARY) {
            boolean hasSecretary = council.getMembers().stream()
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
        Topic topic = topics.get(form.getTopicId());

        // Kiểm tra đề tài phải có nhóm sinh viên được duyệt
        List<GroupTopicRegistration> topicRegs = groupRegistrations.findByTopic_Id(form.getTopicId());
        boolean isApproved = topicRegs.stream().anyMatch(r -> r.getStatus() == GroupTopicRegistrationStatus.APPROVED);
        if (!isApproved) {
            throw new BusinessRuleException("Chỉ được phân công đề tài đã có nhóm sinh viên được duyệt chính thức");
        }

        TopicAssignment assignment = assignments.findByTopic_Id(form.getTopicId())
                .orElseGet(() -> {
                    TopicAssignment ta = new TopicAssignment();
                    ta.setTopic(topic);
                    return ta;
                });

        // 1. Kiểm tra GVPB (reviewer)
        if (form.getReviewerId() != null) {
            UserAccount reviewer = users.get(form.getReviewerId());
            if (reviewer.getRole() != Role.LECTURER || !reviewer.isEnabled()) {
                throw new BusinessRuleException("Giảng viên phản biện phải là giảng viên đang hoạt động");
            }
            // Chặn xung đột GVHD không được làm GVPB
            boolean isAdvisor = topic.getAdvisors().stream().anyMatch(adv -> adv.getId().equals(reviewer.getId()));
            if (isAdvisor) {
                throw new BusinessRuleException("Giảng viên hướng dẫn không được làm phản biện cho chính đề tài của mình");
            }
            assignment.setReviewer(reviewer);
        } else {
            assignment.setReviewer(null);
        }

        // 2. Kiểm tra Hội đồng
        if (form.getCouncilId() != null) {
            Council council = get(form.getCouncilId());
            if (!council.getRound().getId().equals(topic.getRound().getId())) {
                throw new BusinessRuleException("Hội đồng phải thuộc cùng đợt đăng ký với đề tài");
            }
            // Chặn xung đột: Thành viên trong hội đồng không được là GVHD của đề tài này
            for (CouncilMember cm : council.getMembers()) {
                boolean isAdvisor = topic.getAdvisors().stream().anyMatch(adv -> adv.getId().equals(cm.getLecturer().getId()));
                if (isAdvisor) {
                    throw new BusinessRuleException("Giảng viên " + cm.getLecturer().getFullName()
                            + " trong hội đồng là GVHD của đề tài này. Không thể phân công vào hội đồng này!");
                }
            }
            assignment.setCouncil(council);
        } else {
            assignment.setCouncil(null);
        }

        return assignments.save(assignment);
    }

    @Transactional(readOnly = true)
    public TopicAssignment getAssignment(Long topicId) {
        return assignments.findByTopic_Id(topicId).orElse(null);
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
