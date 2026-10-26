package vn.edu.fit.topicmanagement.studentgroup;

import java.time.Instant;
import java.util.List;
import java.util.LinkedHashSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;
import vn.edu.fit.topicmanagement.registrationperiod.*;
import vn.edu.fit.topicmanagement.topic.*;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
@Transactional
public class GroupTopicRegistrationService {
    private final GroupTopicRegistrationRepository registrations;
    private final StudentGroupService groups;
    private final TopicRepository topics;
    private final CourseRegistrationService periods;
    private final UserAccountService users;

    public GroupTopicRegistrationService(GroupTopicRegistrationRepository registrations, StudentGroupService groups,
            TopicRepository topics, CourseRegistrationService periods, UserAccountService users) {
        this.registrations = registrations;
        this.groups = groups;
        this.topics = topics;
        this.periods = periods;
        this.users = users;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public Page<Topic> findPublishedTopics(Long groupId, int page) {
        StudentGroup group = groups.getMyGroup(groupId);
        if (periods.get(group.getRoundId()).getRegistrationMode() == RegistrationMode.SELF_PROPOSED) return Page.empty();
        return topics.findAvailable(group.getRoundId(), TopicStatus.PUBLISHED, PageRequest.of(Math.max(0, page), 10));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public GroupTopicRegistration findMyRegistration(Long groupId) {
        groups.getMyGroup(groupId);
        return registrations.findByGroup_Id(groupId).orElse(null);
    }

    @PreAuthorize("hasRole('STUDENT')")
    public GroupTopicRegistration register(Long groupId, Long topicId) {
        StudentGroup group = lockLeaderGroup(groupId);
        CourseRegistration round = periods.get(group.getRoundId());
        if (round.getRegistrationMode() == RegistrationMode.SELF_PROPOSED)
            throw new BusinessRuleException("Đợt này yêu cầu nhóm tự đề xuất đề tài");
        Topic topic = topics.findById(topicId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề tài"));
        if (topic.getStatus() != TopicStatus.PUBLISHED || topic.getProposedGroupId() != null
                || !topic.getRound().getId().equals(group.getRoundId()))
            throw new BusinessRuleException("Chỉ được chọn đề tài đã công bố trong đúng môn/lớp");
        GroupTopicRegistration registration = editableRegistration(group);
        if (round.getRegistrationMode() == RegistrationMode.EXCLUSIVE) {
            if (registrations.existsByTopic_IdAndStatus(topicId, GroupTopicRegistrationStatus.APPROVED))
                throw new BusinessRuleException("Đề tài này vừa được nhóm khác đăng ký. Vui lòng chọn đề tài khác.");
            registration.setExclusiveTopicId(topicId);
        }
        registration.setTopic(topic);
        registration.setStatus(GroupTopicRegistrationStatus.APPROVED);
        registration.setSubmittedAt(Instant.now());
        registration.setReviewedAt(Instant.now());
        return registrations.saveAndFlush(registration);
    }

    @PreAuthorize("hasRole('STUDENT')")
    public GroupTopicRegistration propose(Long groupId, ProposalForm form) {
        StudentGroup group = lockLeaderGroup(groupId);
        CourseRegistration round = periods.get(group.getRoundId());
        if (round.getRegistrationMode() != RegistrationMode.SELF_PROPOSED)
            throw new BusinessRuleException("Đợt này chỉ cho chọn đề tài có sẵn");
        if (round.getLecturer() == null || round.getDepartment() == null)
            throw new BusinessRuleException("Khoa cần cập nhật GV phụ trách và bộ môn cho đợt này");
        if (form.getTitle() == null || form.getTitle().isBlank() || form.getTitle().length() > 200
                || form.getDescription() == null || form.getDescription().isBlank() || form.getDescription().length() > 2000)
            throw new BusinessRuleException("Cần tên (tối đa 200 ký tự) và nội dung (tối đa 2000 ký tự)");
        GroupTopicRegistration registration = editableRegistration(group);
        Topic topic = new Topic();
        topic.setTitle(form.getTitle().trim());
        topic.setDescription(form.getDescription().trim());
        topic.setRound(round);
        topic.setDepartment(round.getDepartment());
        topic.setProposedGroupId(groupId);
        topic.setCreatedBy(users.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName()));
        topic.setAdvisors(new LinkedHashSet<>(List.of(round.getLecturer())));
        topic.setStatus(TopicStatus.PENDING);
        registration.setTopic(topics.save(topic));
        registration.setStatus(GroupTopicRegistrationStatus.PENDING);
        registration.setSubmittedAt(Instant.now());
        return registrations.save(registration);
    }

    private StudentGroup lockLeaderGroup(Long groupId) {
        StudentGroup group = groups.lockMyLeaderGroup(groupId);
        periods.assertSvWindowOpen(group.getRoundId());
        if (!groups.findInvitations(groupId).isEmpty())
            throw new BusinessRuleException("Còn lời mời chưa xử lý. Chờ sinh viên phản hồi hoặc thu hồi lời mời trước khi đăng ký đề tài.");
        if (groups.getMyGroupMembers(groupId).size() > periods.get(group.getRoundId()).getMaxMembers())
            throw new BusinessRuleException("Nhóm vượt giới hạn thành viên của môn/lớp");
        return group;
    }

    private GroupTopicRegistration editableRegistration(StudentGroup group) {
        var registration = registrations.findByGroup_Id(group.getId()).orElseGet(GroupTopicRegistration::new);
        if (registration.getId() != null && registration.getStatus() != GroupTopicRegistrationStatus.REJECTED)
            throw new BusinessRuleException("Nhóm đã đăng ký một đề tài trong đợt này");
        registration.setGroup(group);
        registration.setExclusiveTopicId(null);
        registration.setRejectionReason(null);
        registration.setReviewedBy(null);
        registration.setReviewedAt(null);
        return registration;
    }

    @PreAuthorize("hasAnyRole('DEAN','ADMIN','LECTURER')")
    @Transactional(readOnly = true)
    public List<GroupTopicRegistration> findReviewable() {
        var all = registrations.findAllByOrderBySubmittedAtDesc();
        if (CourseRegistrationService.isManager()) return all;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return all.stream().filter(registration -> auth != null
                && registration.getTopic().getRound().getLecturer() != null
                && registration.getTopic().getRound().getLecturer().getEmail().equalsIgnoreCase(auth.getName()))
                .toList();
    }

    @PreAuthorize("hasAnyRole('DEAN','ADMIN','LECTURER')")
    public void approve(Long id) {
        var registration = pending(id);
        assertCanReview(registration);
        Topic topic = registration.getTopic();
        if (topic.getAdvisors().isEmpty() || topic.getAdvisors().size() > 2 || topic.getDepartment() == null)
            throw new BusinessRuleException("Đề tài cần bộ môn và 1–2 GVHD trước khi duyệt");
        if (topic.getProposedGroupId() != null) topic.setStatus(TopicStatus.APPROVED);
        if (topic.getRound().getRegistrationMode() == RegistrationMode.EXCLUSIVE) {
            if (registrations.existsByTopic_IdAndStatus(topic.getId(), GroupTopicRegistrationStatus.APPROVED))
                throw new BusinessRuleException("Đề tài đã được nhóm khác đăng ký");
            registration.setExclusiveTopicId(topic.getId());
        }
        registration.setStatus(GroupTopicRegistrationStatus.APPROVED);
        stampReview(registration);
    }

    @PreAuthorize("hasAnyRole('DEAN','ADMIN','LECTURER')")
    public void reject(Long id, String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 500)
            throw new BusinessRuleException("Vui lòng nhập lý do từ chối, tối đa 500 ký tự");
        var registration = pending(id);
        assertCanReview(registration);
        registration.setStatus(GroupTopicRegistrationStatus.REJECTED);
        registration.setRejectionReason(reason.trim());
        if (registration.getTopic().getProposedGroupId() != null) {
            registration.getTopic().setStatus(TopicStatus.REJECTED);
            registration.getTopic().setRejectionReason(reason.trim());
        }
        stampReview(registration);
    }

    private GroupTopicRegistration pending(Long id) {
        periods.lock(registrations.findRoundIdById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký")));
        var registration = registrations.findByIdForUpdate(id).orElseThrow();
        if (registration.getStatus() != GroupTopicRegistrationStatus.PENDING)
            throw new BusinessRuleException("Chỉ xử lý đăng ký đang chờ duyệt");
        return registration;
    }

    private void stampReview(GroupTopicRegistration registration) {
        registration.setReviewedBy(users.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName()));
        registration.setReviewedAt(Instant.now());
    }

    private void assertCanReview(GroupTopicRegistration registration) {
        if (CourseRegistrationService.isManager()) return;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var lecturer = registration.getTopic().getRound().getLecturer();
        if (auth == null || lecturer == null || !lecturer.getEmail().equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("Bạn chỉ được xét duyệt đề xuất của lớp mình phụ trách");
        }
    }
}
