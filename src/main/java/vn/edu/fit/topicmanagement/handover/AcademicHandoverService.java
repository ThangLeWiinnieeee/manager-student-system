package vn.edu.fit.topicmanagement.handover;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.council.*;
import vn.edu.fit.topicmanagement.evaluation.*;
import vn.edu.fit.topicmanagement.registrationperiod.*;
import vn.edu.fit.topicmanagement.result.TopicFinalResultRepository;
import vn.edu.fit.topicmanagement.studentgroup.*;
import vn.edu.fit.topicmanagement.topic.*;
import vn.edu.fit.topicmanagement.user.*;

@Service
@Transactional
@PreAuthorize("hasAnyRole('ADMIN','DEAN')")
public class AcademicHandoverService {
    private final CourseRegistrationService courses;
    private final UserAccountRepository users;
    private final TopicRepository topics;
    private final GroupTopicRegistrationRepository registrations;
    private final StudentGroupRepository groups;
    private final TopicAssignmentRepository assignments;
    private final EvaluationRepository evaluations;
    private final TopicFinalResultRepository results;
    private final CouncilRepository councils;
    private final CouncilMemberRepository members;
    private final HandoverLog history;

    public AcademicHandoverService(CourseRegistrationService courses, UserAccountRepository users,
            TopicRepository topics, GroupTopicRegistrationRepository registrations, StudentGroupRepository groups,
            TopicAssignmentRepository assignments, EvaluationRepository evaluations, TopicFinalResultRepository results,
            CouncilRepository councils, CouncilMemberRepository members, HandoverLog history) {
        this.courses=courses; this.users=users; this.topics=topics; this.registrations=registrations;
        this.groups=groups; this.assignments=assignments; this.evaluations=evaluations; this.results=results;
        this.councils=councils; this.members=members; this.history=history;
    }

    public enum Duty {
        COURSE("GV phụ trách lớp"), ADVISOR("GV hướng dẫn"), REVIEWER("GV chấm / phản biện"),
        SUPPORTER("GV hỗ trợ"), COUNCIL("Thành viên hội đồng");
        private final String label;
        Duty(String label) { this.label = label; }
    }
    public record Task(Duty duty, Long targetId, UserAccount previous, String label) {
        public Duty getDuty() { return duty; }
        public Long getTargetId() { return targetId; }
        public UserAccount getPrevious() { return previous; }
        public String getLabel() { return label; }
    }

    @Transactional(readOnly=true)
    public List<Task> findTasks(Long courseId) {
        var course=courses.get(courseId);
        List<Task> tasks=new ArrayList<>();
        if (course.getLecturer()!=null) tasks.add(new Task(Duty.COURSE, courseId, course.getLecturer(), "GV phụ trách lớp"));
        for (var topic : topics.findAll()) {
            if (!topic.getRound().getId().equals(courseId)) continue;
            for (var advisor : topic.getAdvisors())
                tasks.add(new Task(Duty.ADVISOR, topic.getId(), advisor, "GVHD · " + topic.getTitle()));
        }
        for (var reg : registrations.findAllByOrderBySubmittedAtDesc()) {
            if (!reg.getGroup().getRoundId().equals(courseId) || finalized(reg.getId())) continue;
            var assignment=assignments.findByRegistration_Id(reg.getId()).orElse(null);
            if (assignment==null) continue;
            String label=" · " + reg.getGroup().getName();
            if (assignment.getReviewer()!=null && !submitted(reg.getId(), assignment.getReviewer().getId()))
                tasks.add(new Task(Duty.REVIEWER, reg.getId(), assignment.getReviewer(), "GV chấm / phản biện" + label));
            if (assignment.getSupporter()!=null && !submitted(reg.getId(), assignment.getSupporter().getId()))
                tasks.add(new Task(Duty.SUPPORTER, reg.getId(), assignment.getSupporter(), "GV hỗ trợ" + label));
            if (assignment.getCouncil()!=null)
                for (var member : assignment.getCouncil().getMembers())
                    if (!submitted(reg.getId(), member.getLecturer().getId()))
                        tasks.add(new Task(Duty.COUNCIL, reg.getId(), member.getLecturer(), "Hội đồng · " + member.getRole() + label));
        }
        return tasks;
    }

    public void transfer(Long courseId, Duty duty, Long targetId, Long previousId, Long replacementId, String reason) {
        var course=courses.lock(courseId);
        if (previousId==null || replacementId==null || previousId.equals(replacementId))
            throw new BusinessRuleException("Cần chọn một giảng viên thay thế khác người hiện tại");
        var replacement=users.findLockedById(replacementId)
                .orElseThrow(() -> new BusinessRuleException("Không tìm thấy giảng viên thay thế"));
        if (!replacement.isEnabled() || replacement.getRole()!=Role.LECTURER)
            throw new BusinessRuleException("Người nhận bàn giao phải là giảng viên đang hoạt động");
        switch (duty) {
            case COURSE -> {
                require(courseId.equals(targetId) && course.getLecturer()!=null
                        && course.getLecturer().getId().equals(previousId), "GV phụ trách đã thay đổi");
                boolean unfinished=groups.findByRoundIdOrderByNameAsc(courseId).stream().anyMatch(g ->
                        registrations.findByGroup_Id(g.getId()).map(r -> !finalized(r.getId())).orElse(true));
                require(!course.isEnded() || unfinished, "Lớp đã kết thúc và không còn nhóm đang thực hiện");
                course.setLecturer(replacement);
            }
            case ADVISOR -> transferAdvisor(courseId, targetId, previousId, replacement);
            case REVIEWER, SUPPORTER, COUNCIL -> transferAssessment(courseId, duty, targetId, previousId, replacement);
        }
        history.record(courseId, duty.label, targetId, previousId, replacementId, reason);
    }

    private void transferAdvisor(Long courseId, Long topicId, Long previousId, UserAccount replacement) {
        var topic=topics.findById(topicId).orElseThrow(() -> new BusinessRuleException("Không tìm thấy đề tài"));
        require(topic.getRound().getId().equals(courseId), "Đề tài không thuộc lớp");
        require(topic.getAdvisors().stream().anyMatch(u -> u.getId().equals(previousId)), "GVHD đã thay đổi");
        require(topic.getAdvisors().stream().noneMatch(u -> u.getId().equals(replacement.getId())), "GV này đã hướng dẫn đề tài");
        for (var reg : registrations.findAllByOrderBySubmittedAtDesc()) {
            if (!reg.getTopic().getId().equals(topicId)) continue;
            require(!finalized(reg.getId()), "Đề tài có nhóm đã chốt kết quả; không đổi GVHD của hồ sơ lịch sử");
            require(!submitted(reg.getId(), previousId), "GVHD đã nộp điểm hỗ trợ; không thay người đã chấm");
            var assignment=assignments.findByRegistration_Id(reg.getId()).orElse(null);
            if (assignment==null) continue;
            require(assignment.getReviewer()==null || !assignment.getReviewer().getId().equals(replacement.getId()), "GVHD không được là GVPB");
            require(assignment.getCouncil()==null || assignment.getCouncil().getMembers().stream()
                    .noneMatch(m -> m.getLecturer().getId().equals(replacement.getId())), "GVHD không được thuộc hội đồng chấm");
            if (assignment.getSupporter()!=null && assignment.getSupporter().getId().equals(previousId)) {
                requireNoPreviousEvaluation(reg.getId(), replacement.getId());
                assignment.setSupporter(replacement);
            }
        }
        topic.getAdvisors().removeIf(u -> u.getId().equals(previousId));
        topic.getAdvisors().add(replacement);
    }

    private void transferAssessment(Long courseId, Duty duty, Long registrationId, Long previousId, UserAccount replacement) {
        var reg=registrations.findById(registrationId).orElseThrow(() -> new BusinessRuleException("Không tìm thấy nhóm"));
        require(reg.getGroup().getRoundId().equals(courseId), "Nhóm không thuộc lớp");
        require(!finalized(registrationId), "Kết quả đã chốt; không bàn giao người chấm");
        require(!submitted(registrationId, previousId), "Người cũ đã nộp điểm; không thay đổi phiếu đã nộp");
        requireNoPreviousEvaluation(registrationId, replacement.getId());
        var assignment=assignments.findByRegistration_Id(registrationId)
                .orElseThrow(() -> new BusinessRuleException("Nhóm chưa được phân công"));
        boolean advisor=reg.getTopic().getAdvisors().stream().anyMatch(u -> u.getId().equals(replacement.getId()));
        if (duty==Duty.SUPPORTER) {
            require(assignment.getSupporter()!=null && assignment.getSupporter().getId().equals(previousId), "GV hỗ trợ đã thay đổi");
            require(advisor && (assignment.getReviewer()==null || !assignment.getReviewer().getId().equals(replacement.getId())),
                    "GV hỗ trợ phải là GVHD và khác GVPB");
            assignment.setSupporter(replacement);
        } else {
            require(!advisor, "GVHD không được làm phản biện hoặc thành viên hội đồng");
            if (duty==Duty.REVIEWER) {
                require(assignment.getReviewer()!=null && assignment.getReviewer().getId().equals(previousId), "GVPB đã thay đổi");
                require(assignment.getCouncil()==null || assignment.getCouncil().getMembers().stream()
                        .noneMatch(m -> m.getLecturer().getId().equals(replacement.getId())), "GVPB không kiêm thành viên hội đồng");
                assignment.setReviewer(replacement);
            } else {
                var original=assignment.getCouncil();
                require(original!=null, "Nhóm chưa có hội đồng");
                require(original.getMembers().stream().anyMatch(m -> m.getLecturer().getId().equals(previousId)), "Thành viên đã thay đổi");
                require(original.getMembers().stream().noneMatch(m -> m.getLecturer().getId().equals(replacement.getId())), "GV đã thuộc hội đồng");
                require(assignment.getReviewer()==null || !assignment.getReviewer().getId().equals(replacement.getId()), "GVPB không kiêm thành viên hội đồng");
                // Copy this group's council so other groups and their submitted scores stay unchanged.
                var council=new Council();
                String name=original.getName();
                council.setName(name.substring(0, Math.min(95, name.length())) + " · Nhóm " + reg.getGroup().getId() + " · " + UUID.randomUUID().toString().substring(0,8));
                council.setRound(original.getRound()); council.setLocation(original.getLocation());
                council.setReportDate(original.getReportDate()); council.setStatus(original.getStatus());
                councils.saveAndFlush(council);
                for (var member : original.getMembers()) {
                    var lecturer=member.getLecturer().getId().equals(previousId) ? replacement : member.getLecturer();
                    var copy=new CouncilMember(council, lecturer, member.getRole());
                    council.getMembers().add(members.save(copy));
                }
                assignment.setCouncil(council);
            }
        }
        // Old drafts remain stored under the former evaluator; only the current assignment is scored.
    }

    private boolean submitted(Long registrationId, Long lecturerId) {
        return evaluations.findByRegistration_IdAndEvaluator_Id(registrationId, lecturerId)
                .filter(e -> e.getStatus()==EvaluationStatus.SUBMITTED).isPresent();
    }
    private boolean finalized(Long registrationId) {
        return results.findByRegistration_Id(registrationId)
                .filter(r -> r.isPresidentConfirmed() || r.getPublishedAt()!=null).isPresent();
    }
    private void requireNoPreviousEvaluation(Long registrationId, Long lecturerId) {
        require(evaluations.findByRegistration_IdAndEvaluator_Id(registrationId, lecturerId).isEmpty(),
                "GV thay thế đã có phiếu chấm của nhóm; hãy chọn GV khác để giữ nguyên lịch sử");
    }
    private void require(boolean valid, String message) {
        if (!valid) throw new BusinessRuleException(message);
    }
}
