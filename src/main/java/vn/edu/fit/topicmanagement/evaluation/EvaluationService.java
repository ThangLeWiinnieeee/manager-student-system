package vn.edu.fit.topicmanagement.evaluation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;
import vn.edu.fit.topicmanagement.council.CouncilMember;
import vn.edu.fit.topicmanagement.council.CouncilService;
import vn.edu.fit.topicmanagement.council.TopicAssignment;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.topic.TopicService;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
@Transactional
public class EvaluationService {

    private final EvaluationRepository evaluations;
    private final TopicService topics;
    private final CouncilService councils;
    private final UserAccountService users;

    public EvaluationService(
            EvaluationRepository evaluations,
            TopicService topics,
            CouncilService councils,
            UserAccountService users) {
        this.evaluations = evaluations;
        this.topics = topics;
        this.councils = councils;
        this.users = users;
    }

    @PreAuthorize("hasRole('LECTURER')")
    @Transactional(readOnly = true)
    public List<EvaluationItemView> findAssignedTopicsForLecturer(String lecturerEmail) {
        UserAccount lecturer = users.getByEmail(lecturerEmail);
        List<EvaluationItemView> result = new ArrayList<>();

        // 1. Các đề tài làm Giảng viên phản biện (GVPB)
        List<TopicAssignment> reviewerAssignments = councils.getAssignmentsByReviewer(lecturer.getId());
        for (TopicAssignment ta : reviewerAssignments) {
            Evaluation eval = evaluations.findByTopic_IdAndEvaluator_Id(ta.getTopic().getId(), lecturer.getId()).orElse(null);
            result.add(new EvaluationItemView(
                    ta.getTopic(),
                    "Giảng viên phản biện",
                    EvaluationType.REVIEWER,
                    eval != null ? eval.getStatus() : null,
                    eval != null ? eval.getTotalScore() : null,
                    ta.getCouncil() != null ? ta.getCouncil().getName() : "Chưa có"
            ));
        }

        // 2. Các đề tài trong Hội đồng mà giảng viên là thành viên
        List<vn.edu.fit.topicmanagement.council.Council> myCouncils = councils.findMyCouncils(lecturerEmail);
        for (var council : myCouncils) {
            List<TopicAssignment> councilAssignments = councils.getAssignmentsByCouncil(council.getId());
            for (TopicAssignment ta : councilAssignments) {
                // Kiểm tra xem đề tài này có trùng với phần phản biện ở trên không
                boolean isReviewer = ta.getReviewer() != null && ta.getReviewer().getId().equals(lecturer.getId());
                if (!isReviewer) {
                    Evaluation eval = evaluations.findByTopic_IdAndEvaluator_Id(ta.getTopic().getId(), lecturer.getId()).orElse(null);
                    result.add(new EvaluationItemView(
                            ta.getTopic(),
                            "Thành viên hội đồng",
                            EvaluationType.COUNCIL,
                            eval != null ? eval.getStatus() : null,
                            eval != null ? eval.getTotalScore() : null,
                            council.getName()
                    ));
                }
            }
        }

        return result;
    }

    @PreAuthorize("hasRole('LECTURER')")
    @Transactional(readOnly = true)
    public Evaluation getMyEvaluation(Long topicId, String lecturerEmail) {
        UserAccount lecturer = users.getByEmail(lecturerEmail);
        return evaluations.findByTopic_IdAndEvaluator_Id(topicId, lecturer.getId()).orElse(null);
    }

    @PreAuthorize("hasRole('LECTURER')")
    public Evaluation evaluate(Long topicId, EvaluationForm form, String lecturerEmail) {
        UserAccount lecturer = users.getByEmail(lecturerEmail);
        Topic topic = topics.get(topicId);

        // 1. Chặn xung đột: GVHD không được phép chấm
        boolean isAdvisor = topic.getAdvisors().stream().anyMatch(adv -> adv.getId().equals(lecturer.getId()));
        if (isAdvisor) {
            throw new BusinessRuleException("Giảng viên hướng dẫn không được phép chấm đề tài này!");
        }

        // 2. Xác định quyền và loại chấm điểm
        TopicAssignment assignment = councils.getAssignment(topicId);
        if (assignment == null) {
            throw new BusinessRuleException("Đề tài này chưa được phân công hội đồng hoặc phản biện");
        }

        EvaluationType type = null;
        if (assignment.getReviewer() != null && assignment.getReviewer().getId().equals(lecturer.getId())) {
            type = EvaluationType.REVIEWER;
        } else if (assignment.getCouncil() != null) {
            boolean isMember = assignment.getCouncil().getMembers().stream()
                    .anyMatch(m -> m.getLecturer().getId().equals(lecturer.getId()));
            if (isMember) {
                type = EvaluationType.COUNCIL;
            }
        }

        if (type == null) {
            throw new AccessDeniedException("Bạn không được phân công chấm đề tài này");
        }

        // 3. Kiểm tra xem điểm đã bị khóa chưa
        Evaluation eval = evaluations.findByTopic_IdAndEvaluator_Id(topicId, lecturer.getId())
                .orElseGet(() -> {
                    Evaluation newEval = new Evaluation();
                    newEval.setTopic(topic);
                    newEval.setEvaluator(lecturer);
                    return newEval;
                });

        if (eval.getStatus() == EvaluationStatus.SUBMITTED) {
            throw new BusinessRuleException("Điểm đã được xác nhận nộp, đã khóa chỉnh sửa!");
        }

        eval.setEvaluationType(type);
        eval.setScoreContent(form.getScoreContent() == null ? BigDecimal.ZERO : form.getScoreContent());
        eval.setScorePresentation(form.getScorePresentation() == null ? BigDecimal.ZERO : form.getScorePresentation());
        eval.setScoreQa(form.getScoreQa() == null ? BigDecimal.ZERO : form.getScoreQa());
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

    @Transactional(readOnly = true)
    public List<Evaluation> findByTopic(Long topicId) {
        return evaluations.findByTopic_Id(topicId);
    }

    public record EvaluationItemView(
            Topic topic,
            String roleName,
            EvaluationType evaluationType,
            EvaluationStatus status,
            BigDecimal totalScore,
            String councilName
    ) {
        public Topic getTopic() { return topic; }
        public String getRoleName() { return roleName; }
        public EvaluationType getEvaluationType() { return evaluationType; }
        public EvaluationStatus getStatus() { return status; }
        public BigDecimal getTotalScore() { return totalScore; }
        public String getCouncilName() { return councilName; }
    }
}
