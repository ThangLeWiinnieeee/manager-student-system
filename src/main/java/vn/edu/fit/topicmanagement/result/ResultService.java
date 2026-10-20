package vn.edu.fit.topicmanagement.result;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;
import vn.edu.fit.topicmanagement.council.Council;
import vn.edu.fit.topicmanagement.council.CouncilMember;
import vn.edu.fit.topicmanagement.council.CouncilRole;
import vn.edu.fit.topicmanagement.council.CouncilService;
import vn.edu.fit.topicmanagement.council.TopicAssignment;
import vn.edu.fit.topicmanagement.evaluation.Evaluation;
import vn.edu.fit.topicmanagement.evaluation.EvaluationService;
import vn.edu.fit.topicmanagement.evaluation.EvaluationStatus;
import vn.edu.fit.topicmanagement.evaluation.EvaluationType;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationRepository;
import vn.edu.fit.topicmanagement.studentgroup.StudentGroupMemberRepository;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.topic.TopicService;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
@Transactional
public class ResultService {

    private final TopicFinalResultRepository results;
    private final TopicService topics;
    private final CouncilService councils;
    private final EvaluationService evaluations;
    private final UserAccountService users;
    private final GroupTopicRegistrationRepository groupRegistrations;
    private final StudentGroupMemberRepository groupMembers;

    public ResultService(
            TopicFinalResultRepository results,
            TopicService topics,
            CouncilService councils,
            EvaluationService evaluations,
            UserAccountService users,
            GroupTopicRegistrationRepository groupRegistrations,
            StudentGroupMemberRepository groupMembers) {
        this.results = results;
        this.topics = topics;
        this.councils = councils;
        this.evaluations = evaluations;
        this.users = users;
        this.groupRegistrations = groupRegistrations;
        this.groupMembers = groupMembers;
    }

    @Transactional(readOnly = true)
    public TopicSummaryView getSummaryView(Long topicId) {
        Topic topic = topics.get(topicId);
        TopicAssignment assignment = councils.getAssignment(topicId);
        if (assignment == null || assignment.getCouncil() == null) {
            throw new BusinessRuleException("Đề tài chưa được phân công hội đồng bảo vệ");
        }

        Council council = assignment.getCouncil();
        List<Evaluation> allEvals = evaluations.findByTopic(topicId);

        Evaluation reviewerEval = null;
        if (assignment.getReviewer() != null) {
            reviewerEval = allEvals.stream()
                    .filter(e -> e.getEvaluationType() == EvaluationType.REVIEWER)
                    .findFirst().orElse(null);
        }

        List<CouncilMemberScoreView> memberScores = new ArrayList<>();
        BigDecimal sumCouncilScores = BigDecimal.ZERO;
        int councilSubmittedCount = 0;

        for (CouncilMember cm : council.getMembers()) {
            Evaluation eval = allEvals.stream()
                    .filter(e -> e.getEvaluationType() == EvaluationType.COUNCIL
                            && e.getEvaluator().getId().equals(cm.getLecturer().getId()))
                    .findFirst().orElse(null);

            boolean isSub = eval != null && eval.getStatus() == EvaluationStatus.SUBMITTED;
            if (isSub) {
                sumCouncilScores = sumCouncilScores.add(eval.getTotalScore());
                councilSubmittedCount++;
            }
            memberScores.add(new CouncilMemberScoreView(cm.getLecturer(), cm.getRole(), eval));
        }

        boolean allCouncilSubmitted = councilSubmittedCount == council.getMembers().size() && !council.getMembers().isEmpty();
        boolean reviewerSubmitted = assignment.getReviewer() == null
                || (reviewerEval != null && reviewerEval.getStatus() == EvaluationStatus.SUBMITTED);
        boolean isFullyEvaluated = allCouncilSubmitted && reviewerSubmitted;

        BigDecimal councilAvg = councilSubmittedCount > 0
                ? sumCouncilScores.divide(BigDecimal.valueOf(councilSubmittedCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal reviewerScore = reviewerEval != null && reviewerEval.getStatus() == EvaluationStatus.SUBMITTED
                ? reviewerEval.getTotalScore()
                : null;

        BigDecimal finalScore = calculateFinalScore(reviewerScore, councilAvg);
        String grade = determineGrade(finalScore);

        TopicFinalResult existingResult = results.findByTopic_Id(topicId).orElse(null);

        return new TopicSummaryView(
                topic,
                council,
                assignment.getReviewer(),
                reviewerEval,
                memberScores,
                isFullyEvaluated,
                councilAvg,
                reviewerScore,
                finalScore,
                grade,
                existingResult
        );
    }

    @PreAuthorize("hasRole('LECTURER')")
    public TopicFinalResult confirmByPresident(Long topicId, String conclusion, String presidentEmail) {
        TopicSummaryView summary = getSummaryView(topicId);
        UserAccount currentLecturer = users.getByEmail(presidentEmail);

        // Kiểm tra giảng viên có phải là Chủ tịch của hội đồng này
        boolean isPresident = summary.council().getMembers().stream()
                .anyMatch(m -> m.getRole() == CouncilRole.PRESIDENT && m.getLecturer().getId().equals(currentLecturer.getId()));
        if (!isPresident) {
            throw new AccessDeniedException("Chỉ Chủ tịch hội đồng mới có quyền xác nhận kết quả cuối cùng");
        }

        if (!summary.isFullyEvaluated()) {
            throw new BusinessRuleException("Chưa đủ điểm: Tất cả thành viên hội đồng và giảng viên phản biện phải xác nhận nộp điểm trước!");
        }

        TopicFinalResult result = results.findByTopic_Id(topicId)
                .orElseGet(() -> {
                    TopicFinalResult r = new TopicFinalResult();
                    r.setTopic(summary.topic());
                    r.setCouncil(summary.council());
                    return r;
                });

        result.setReviewerScore(summary.reviewerScore());
        result.setCouncilAvgScore(summary.councilAvgScore());
        result.setFinalScore(summary.finalScore());
        result.setGrade(summary.grade());
        result.setConclusion(conclusion == null ? "" : conclusion.trim());
        result.setPresidentConfirmed(true);
        result.setPresidentConfirmedAt(Instant.now());

        return results.save(result);
    }

    @PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
    public TopicFinalResult publish(Long topicId) {
        TopicFinalResult result = results.findByTopic_Id(topicId)
                .orElseThrow(() -> new BusinessRuleException("Đề tài chưa được Chủ tịch hội đồng xác nhận kết quả"));

        if (!result.isPresidentConfirmed()) {
            throw new BusinessRuleException("Đề tài chưa được Chủ tịch hội đồng xác nhận kết quả");
        }

        result.setPublished(true);
        result.setPublishedAt(Instant.now());
        return results.save(result);
    }

    @PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
    public TopicFinalResult revoke(Long topicId) {
        TopicFinalResult result = results.findByTopic_Id(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả đề tài"));
        result.setPublished(false);
        return results.save(result);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public StudentResultView getMyStudentResult(String studentEmail) {
        UserAccount student = users.getByEmail(studentEmail);
        var memberships = groupMembers.findByStudent_Id(student.getId());
        if (memberships.isEmpty()) {
            return null;
        }

        for (var member : memberships) {
            Long groupId = member.getGroup().getId();
            GroupTopicRegistration reg = groupRegistrations.findByGroup_Id(groupId).orElse(null);
            if (reg != null && reg.getTopic() != null) {
                TopicFinalResult res = results.findByTopic_Id(reg.getTopic().getId()).orElse(null);
                if (res != null && res.isPublished()) {
                    return new StudentResultView(member.getGroup(), reg.getTopic(), res);
                } else if (res != null) {
                    return new StudentResultView(member.getGroup(), reg.getTopic(), null); // Chưa công bố
                }
            }
        }
        return null;
    }

    @PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<TopicFinalResult> findByRound(Long roundId) {
        return results.findByRoundId(roundId);
    }

    private BigDecimal calculateFinalScore(BigDecimal reviewerScore, BigDecimal councilAvg) {
        if (reviewerScore != null) {
            BigDecimal partRev = reviewerScore.multiply(BigDecimal.valueOf(0.3));
            BigDecimal partCou = councilAvg.multiply(BigDecimal.valueOf(0.7));
            return partRev.add(partCou).setScale(2, RoundingMode.HALF_UP);
        }
        return councilAvg.setScale(2, RoundingMode.HALF_UP);
    }

    private String determineGrade(BigDecimal score) {
        if (score == null) return "CHƯA XẾP LOẠI";
        double s = score.doubleValue();
        if (s >= 9.0) return "XUẤT SẮC";
        if (s >= 8.0) return "GIỎI";
        if (s >= 6.5) return "KHÁ";
        if (s >= 5.0) return "TRUNG BÌNH";
        return "KHÔNG ĐẠT";
    }

    public record CouncilMemberScoreView(
            UserAccount lecturer,
            CouncilRole role,
            Evaluation evaluation
    ) {
        public UserAccount getLecturer() { return lecturer; }
        public CouncilRole getRole() { return role; }
        public Evaluation getEvaluation() { return evaluation; }
    }

    public record TopicSummaryView(
            Topic topic,
            Council council,
            UserAccount reviewer,
            Evaluation reviewerEvaluation,
            List<CouncilMemberScoreView> memberScores,
            boolean isFullyEvaluated,
            BigDecimal councilAvgScore,
            BigDecimal reviewerScore,
            BigDecimal finalScore,
            String grade,
            TopicFinalResult finalResult
    ) {
        public Topic getTopic() { return topic; }
        public Council getCouncil() { return council; }
        public UserAccount getReviewer() { return reviewer; }
        public Evaluation getReviewerEvaluation() { return reviewerEvaluation; }
        public List<CouncilMemberScoreView> getMemberScores() { return memberScores; }
        public boolean isFullyEvaluated() { return isFullyEvaluated; }
        public boolean getIsFullyEvaluated() { return isFullyEvaluated; }
        public BigDecimal getCouncilAvgScore() { return councilAvgScore; }
        public BigDecimal getReviewerScore() { return reviewerScore; }
        public BigDecimal getFinalScore() { return finalScore; }
        public String getGrade() { return grade; }
        public TopicFinalResult getFinalResult() { return finalResult; }
    }

    public record StudentResultView(
            vn.edu.fit.topicmanagement.studentgroup.StudentGroup group,
            Topic topic,
            TopicFinalResult result
    ) {
        public vn.edu.fit.topicmanagement.studentgroup.StudentGroup getGroup() { return group; }
        public Topic getTopic() { return topic; }
        public TopicFinalResult getResult() { return result; }
    }
}
