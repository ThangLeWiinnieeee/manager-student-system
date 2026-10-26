package vn.edu.fit.topicmanagement.evaluation;

import java.math.BigDecimal;
import vn.edu.fit.topicmanagement.registrationperiod.RoundType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.council.CouncilService;
import vn.edu.fit.topicmanagement.council.TopicAssignment;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
@Transactional
public class EvaluationService {

    private final EvaluationRepository evaluations;
    private final vn.edu.fit.topicmanagement.submission.ReportSubmissionRepository reports;
    private final CouncilService councils;
    private final UserAccountService users;

    public EvaluationService(
            EvaluationRepository evaluations,
            vn.edu.fit.topicmanagement.submission.ReportSubmissionRepository reports,
            CouncilService councils,
            UserAccountService users) {
        this.evaluations = evaluations;
        this.reports = reports;
        this.councils = councils;
        this.users = users;
    }

    @PreAuthorize("hasRole('LECTURER')")
    @Transactional(readOnly = true)
    public List<EvaluationItemView> findAssignedTopicsForLecturer(String lecturerEmail) {
        UserAccount lecturer = users.getByEmail(lecturerEmail);
        List<EvaluationItemView> result = new ArrayList<>();

        var assigned = new java.util.LinkedHashMap<Long, TopicAssignment>();
        councils.getAssignmentsByReviewer(lecturer.getId()).forEach(a -> assigned.put(a.getRegistration().getId(), a));
        councils.getAssignmentsBySupporter(lecturer.getId()).forEach(a -> assigned.put(a.getRegistration().getId(), a));
        for (var council : councils.findMyCouncils(lecturerEmail))
            councils.getAssignmentsByCouncil(council.getId()).forEach(a -> assigned.put(a.getRegistration().getId(), a));
        for (var assignment : assigned.values()) {
            EvaluationType type = evaluationType(assignment, lecturer.getId());
            var eval = evaluations.findByRegistration_IdAndEvaluator_Id(assignment.getRegistration().getId(), lecturer.getId()).orElse(null);
            String roleName = !assignment.isLegacyAssessment() && assignment.getTopic().getRound().getType() == RoundType.MON_HOC
                    ? "Giảng viên chấm môn học" : type.getDisplayName();
            result.add(new EvaluationItemView(assignment.getRegistration(), assignment.getTopic(), roleName, type,
                    eval == null ? null : eval.getStatus(), eval == null ? null : eval.getTotalScore(),
                    assignment.getCouncil() == null ? "Không áp dụng / chưa phân công" : assignment.getCouncil().getName()));
        }

        return result;
    }

    @PreAuthorize("hasRole('LECTURER')")
    @Transactional(readOnly = true)
    public Evaluation getMyEvaluation(Long registrationId, String lecturerEmail) {
        UserAccount lecturer = users.getByEmail(lecturerEmail);
        return evaluations.findByRegistration_IdAndEvaluator_Id(registrationId, lecturer.getId()).orElse(null);
    }

    @PreAuthorize("hasRole('LECTURER')")
    public Evaluation evaluate(Long registrationId, EvaluationForm form, String lecturerEmail) {
        UserAccount lecturer = users.getByEmail(lecturerEmail);
        var registration = councils.lockAssessment(registrationId);
        councils.assertCanViewAssessment(registrationId, lecturerEmail);
        Topic topic = registration.getTopic();
        if (registration.getStatus() != vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus.APPROVED)
            throw new BusinessRuleException("Nhóm chưa được chấp thuận đề tài");
        if (reports.countByGroup_Id(registration.getGroup().getId()) == 0)
            throw new BusinessRuleException("Nhóm chưa nộp báo cáo");


        if (!lecturer.isEnabled() || lecturer.getRole() != Role.LECTURER)
            throw new AccessDeniedException("Tài khoản không có quyền chấm");
        TopicAssignment assignment = councils.getAssignment(registrationId);
        if (assignment == null) throw new BusinessRuleException("Nhóm chưa được phân công chấm");
        RoundType roundType = topic.getRound().getType();
        boolean legacy = assignment.isLegacyAssessment();
        if (legacy) councils.assertReady(assignment.getCouncil());
        EvaluationType type = evaluationType(assignment, lecturer.getId());
        boolean isAdvisor = topic.getAdvisors().stream().anyMatch(a -> a.getId().equals(lecturer.getId()));
        if (isAdvisor && !(roundType == RoundType.TLCN && type == EvaluationType.SUPPORTER))
            throw new BusinessRuleException("Giảng viên hướng dẫn chỉ được chấm ở lượt GV hỗ trợ TLCN");
        if (type == EvaluationType.SUPPORTER && (roundType != RoundType.TLCN || !isAdvisor))
            throw new BusinessRuleException("GV hỗ trợ TLCN phải là GV hướng dẫn");
        if (type == EvaluationType.REVIEWER && roundType.requiresGvpbDeadline()
                && topic.getRound().getGvpbDeadline() != null
                && java.time.LocalDateTime.now().isAfter(topic.getRound().getGvpbDeadline()))
            throw new BusinessRuleException("Đã quá hạn GVPB nộp điểm");
        if (!legacy && roundType == RoundType.TLCN && type == EvaluationType.REVIEWER) {
            if (assignment.getSupporter() == null || evaluations.findByRegistration_IdAndEvaluator_Id(
                    registrationId, assignment.getSupporter().getId())
                    .filter(e -> e.getStatus() == EvaluationStatus.SUBMITTED).isEmpty())
                throw new BusinessRuleException("GV hỗ trợ phải nộp điểm trước GVPB");
        }
        if (type == EvaluationType.COUNCIL) {
            if (!legacy && roundType != RoundType.KLTN && roundType != RoundType.NCKH)
                throw new BusinessRuleException("Loại đề tài này không chấm qua hội đồng");
            councils.assertReady(assignment.getCouncil());
            if (!legacy && roundType == RoundType.KLTN && !Boolean.TRUE.equals(councils.getDefenseDecision(registrationId)))
                throw new BusinessRuleException("GVPB chưa cho phép nhóm bảo vệ");
        }
        boolean defenseReview = !legacy && roundType == RoundType.KLTN && type == EvaluationType.REVIEWER;
        if (defenseReview && "submit".equalsIgnoreCase(form.getAction())) {
            if (form.getDefenseAllowed() == null)
                throw new BusinessRuleException("Vui lòng quyết định được bảo vệ hoặc không được bảo vệ");
            if (!form.getDefenseAllowed() && (form.getComments() == null || form.getComments().isBlank()))
                throw new BusinessRuleException("Cần ghi lý do không cho phép bảo vệ");
        }
        if (form.getComments() != null && form.getComments().length() > 2000)
            throw new BusinessRuleException("Nhận xét tối đa 2000 ký tự");

        // 3. Kiểm tra xem điểm đã bị khóa chưa
        Evaluation eval = evaluations.findByRegistration_IdAndEvaluator_Id(registrationId, lecturer.getId())
                .orElseGet(() -> {
                    Evaluation newEval = new Evaluation();
                    newEval.setRegistration(registration);
                    newEval.setEvaluator(lecturer);
                    return newEval;
                });

        if (eval.getStatus() == EvaluationStatus.SUBMITTED) {
            throw new BusinessRuleException("Điểm đã được xác nhận nộp, đã khóa chỉnh sửa!");
        }

        if (form.getScoreContent() == null || form.getScorePresentation() == null || form.getScoreQa() == null
                || form.getScoreContent().signum() < 0 || form.getScoreContent().compareTo(BigDecimal.valueOf(4)) > 0
                || form.getScorePresentation().signum() < 0 || form.getScorePresentation().compareTo(BigDecimal.valueOf(3)) > 0
                || form.getScoreQa().signum() < 0 || form.getScoreQa().compareTo(BigDecimal.valueOf(3)) > 0)
            throw new BusinessRuleException("Điểm thành phần phải nằm trong thang 4–3–3");
        eval.setEvaluationType(type);
        eval.setDefenseAllowed(defenseReview ? form.getDefenseAllowed() : null);
        eval.setScoreContent(form.getScoreContent());
        eval.setScorePresentation(form.getScorePresentation());
        eval.setScoreQa(form.getScoreQa());
        eval.calculateTotal();
        eval.setComments(form.getComments() == null ? "" : form.getComments().trim());

        if ("submit".equalsIgnoreCase(form.getAction())) {
            eval.setStatus(EvaluationStatus.SUBMITTED);
            eval.setSubmittedAt(Instant.now());
        } else {
            eval.setStatus(EvaluationStatus.DRAFT);
        }

        return evaluations.save(eval);
    }

    private EvaluationType evaluationType(TopicAssignment assignment, Long lecturerId) {
        if (assignment.getSupporter() != null && assignment.getSupporter().getId().equals(lecturerId))
            return EvaluationType.SUPPORTER;
        if (assignment.getReviewer() != null && assignment.getReviewer().getId().equals(lecturerId))
            return EvaluationType.REVIEWER;
        if (assignment.getCouncil() != null && assignment.getCouncil().getMembers().stream()
                .anyMatch(m -> m.getLecturer().getId().equals(lecturerId)))
            return EvaluationType.COUNCIL;
        throw new AccessDeniedException("Bạn không được phân công chấm nhóm này");
    }

    @Transactional(readOnly = true)
    public List<Evaluation> findByTopic(Long registrationId) {
        return evaluations.findByRegistration_Id(registrationId);
    }

    public record EvaluationItemView(
            vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration registration,
            Topic topic,
            String roleName,
            EvaluationType evaluationType,
            EvaluationStatus status,
            BigDecimal totalScore,
            String councilName
    ) {
        public vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration getRegistration() { return registration; }
        public Topic getTopic() { return topic; }
        public String getRoleName() { return roleName; }
        public EvaluationType getEvaluationType() { return evaluationType; }
        public EvaluationStatus getStatus() { return status; }
        public BigDecimal getTotalScore() { return totalScore; }
        public String getCouncilName() { return councilName; }
    }
}
