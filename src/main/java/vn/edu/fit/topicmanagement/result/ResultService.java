package vn.edu.fit.topicmanagement.result;

import java.math.BigDecimal;
import vn.edu.fit.topicmanagement.registrationperiod.RoundType;
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

    public void assertCanView(Long registrationId, String email) { councils.assertCanViewAssessment(registrationId, email); }

    @Transactional(readOnly = true)
    public TopicSummaryView getSummaryView(Long registrationId) {
        var registration = councils.getRegistration(registrationId);
        Topic topic = registration.getTopic();
        TopicAssignment assignment = councils.getAssignment(registrationId);
        if (assignment == null) throw new BusinessRuleException("Nhóm chưa được phân công chấm");
        TopicFinalResult existing = results.findByRegistration_Id(registrationId).orElse(null);
        boolean finalized = existing != null && (existing.isPresidentConfirmed() || existing.getPublishedAt() != null);
        RoundType type = topic.getRound().getType();
        boolean legacy = assignment.isLegacyAssessment();
        Council council = assignment.getCouncil();
        List<Evaluation> allEvals = evaluations.findByTopic(registrationId);
        Evaluation reviewerEval = evaluationOf(allEvals, assignment.getReviewer());
        Evaluation supporterEval = evaluationOf(allEvals, assignment.getSupporter());
        BigDecimal reviewerScore = submittedScore(reviewerEval);
        BigDecimal supporterScore = submittedScore(supporterEval);
        Boolean defenseAllowed = reviewerScore != null && reviewerEval != null ? reviewerEval.getDefenseAllowed() : null;
        boolean defenseRejected = type == RoundType.KLTN && Boolean.FALSE.equals(defenseAllowed);
        boolean needsCouncil = legacy || type == RoundType.NCKH || (type == RoundType.KLTN && !defenseRejected);
        List<CouncilMemberScoreView> memberScores = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        if (needsCouncil && council != null) {
            if (!finalized) councils.assertReady(council);
            for (CouncilMember member : council.getMembers()) {
                var evaluation = evaluationOf(allEvals, member.getLecturer());
                memberScores.add(new CouncilMemberScoreView(member.getLecturer(), member.getRole(), evaluation));
                var score = submittedScore(evaluation);
                if (score != null) { sum = sum.add(score); count++; }
            }
        }
        boolean councilComplete = council != null && count >= 3 && count == council.getMembers().size();
        BigDecimal councilAverage = councilComplete
                ? sum.divide(BigDecimal.valueOf(count), 8, RoundingMode.HALF_UP) : null;
        boolean complete = switch (type) {
            case MON_HOC -> reviewerScore != null;
            case TLCN -> supporterScore != null && reviewerScore != null;
            case KLTN -> reviewerScore != null && (defenseRejected || (Boolean.TRUE.equals(defenseAllowed) && councilComplete));
            case NCKH -> councilComplete;
        };
        if (legacy) complete = councilComplete && (assignment.getReviewer() == null || reviewerScore != null);
        BigDecimal finalScore = null;
        if (complete) {
            if (legacy) {
                var ids = new java.util.HashSet<Long>();
                council.getMembers().forEach(m -> ids.add(m.getLecturer().getId()));
                if (assignment.getReviewer() != null) ids.add(assignment.getReviewer().getId());
                finalScore = allEvals.stream().filter(e -> ids.contains(e.getEvaluator().getId())
                                && e.getStatus() == EvaluationStatus.SUBMITTED)
                        .map(Evaluation::getTotalScore).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(ids.size()), 2, RoundingMode.HALF_UP);
            } else finalScore = switch (type) {
                case MON_HOC -> reviewerScore;
                case TLCN -> supporterScore.add(reviewerScore).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                case KLTN -> defenseRejected ? BigDecimal.ZERO
                        : reviewerScore.add(councilAverage).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                case NCKH -> councilAverage;
            };
            finalScore = finalScore.setScale(2, RoundingMode.HALF_UP);
        }
        // A finalized historical result keeps its recorded score and formula.
        if (finalized) {
            finalScore = existing.getFinalScore();
        }
        String grade = !legacy && type == RoundType.NCKH ? "CHƯA XẾP LOẠI" : determineGrade(finalScore);
        if (finalized) grade = existing.getGrade();
        return new TopicSummaryView(registration, topic, council, assignment.getReviewer(), reviewerEval,
                memberScores, finalized || complete, councilAverage == null ? null : councilAverage.setScale(2, RoundingMode.HALF_UP),
                reviewerScore, finalScore, grade, existing, assignment.getSupporter(), supporterEval,
                supporterScore, defenseAllowed, needsCouncil, legacy);
    }

    private Evaluation evaluationOf(List<Evaluation> list, UserAccount lecturer) {
        return lecturer == null ? null : list.stream().filter(e -> e.getEvaluator().getId().equals(lecturer.getId()))
                .findFirst().orElse(null);
    }

    private BigDecimal submittedScore(Evaluation evaluation) {
        return evaluation != null && evaluation.getStatus() == EvaluationStatus.SUBMITTED ? evaluation.getTotalScore() : null;
    }

    @PreAuthorize("hasRole('LECTURER')")
    public TopicFinalResult confirmByPresident(Long registrationId, String conclusion, String presidentEmail) {
        councils.lockAssessment(registrationId);
        if (conclusion != null && conclusion.length() > 1000) throw new BusinessRuleException("Kết luận tối đa 1000 ký tự");
        TopicSummaryView summary = getSummaryView(registrationId);
        UserAccount currentLecturer = users.getByEmail(presidentEmail);

        // Kiểm tra giảng viên có phải là Chủ tịch của hội đồng này
        boolean isPresident = summary.needsCouncil() && summary.council() != null && summary.council().getMembers().stream()
                .anyMatch(m -> m.getRole() == CouncilRole.PRESIDENT && m.getLecturer().getId().equals(currentLecturer.getId()));
        if (!isPresident) {
            throw new AccessDeniedException("Chỉ Chủ tịch hội đồng mới có quyền xác nhận kết quả cuối cùng");
        }

        if (!summary.isFullyEvaluated()) {
            throw new BusinessRuleException("Chưa đủ điểm: Tất cả thành viên hội đồng và giảng viên phản biện phải xác nhận nộp điểm trước!");
        }

        TopicFinalResult result = results.findByRegistration_Id(registrationId)
                .orElseGet(() -> {
                    TopicFinalResult r = new TopicFinalResult();
                    r.setRegistration(summary.registration());
                    r.setCouncil(summary.council());
                    return r;
                });

        if (result.isPresidentConfirmed() || result.isPublished()) throw new BusinessRuleException("Kết quả đã chốt, không thể ghi đè");
        applyScores(result, summary);
        result.setConclusion(conclusion == null ? "" : conclusion.trim());
        result.setPresidentConfirmed(true);
        result.setPresidentConfirmedAt(Instant.now());

        return results.save(result);
    }

    @PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
    public TopicFinalResult publish(Long registrationId) {
        councils.lockAssessment(registrationId);
        TopicSummaryView summary = getSummaryView(registrationId);
        TopicFinalResult result = results.findByRegistration_Id(registrationId).orElse(null);
        boolean finalized = result != null && result.isPresidentConfirmed();
        if (!finalized && !summary.isFullyEvaluated()) throw new BusinessRuleException("Chưa đủ điểm hợp lệ");
        if (summary.needsCouncil() && !finalized)
            throw new BusinessRuleException("Cần Chủ tịch hội đồng xác nhận trước khi Khoa công bố");
        if (result == null) {
            result = new TopicFinalResult();
            result.setRegistration(summary.registration());
            result.setCouncil(summary.council());
        }
        if (!finalized && !result.isPublished()) {
            applyScores(result, summary);
            if (Boolean.FALSE.equals(summary.defenseAllowed())) {
                String reason = summary.reviewerEvaluation().getComments();
                result.setConclusion("Không được bảo vệ: " + (reason.length() <= 970 ? reason : reason.substring(0, 970) + "…"));
            } else {
                result.setConclusion("Khoa đã xác nhận kết quả đánh giá");
            }
        }
        result.setPublished(true);
        result.setPublishedAt(Instant.now());
        return results.save(result);
    }

    @PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
    public TopicFinalResult revoke(Long registrationId) {
        councils.lockAssessment(registrationId);
        TopicFinalResult result = results.findByRegistration_Id(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả đề tài"));
        result.setPublished(false);
        return results.save(result);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<StudentResultView> getMyStudentResults(String studentEmail) {
        UserAccount student = users.getByEmail(studentEmail);
        List<StudentResultView> views = new ArrayList<>();
        for (var member : groupMembers.findByStudent_Id(student.getId())) {
            var reg = groupRegistrations.findByGroup_Id(member.getGroup().getId()).orElse(null);
            if (reg != null && reg.getStatus() == vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus.APPROVED) {
                var result = results.findByRegistration_Id(reg.getId()).filter(TopicFinalResult::isPublished).orElse(null);
                views.add(new StudentResultView(member.getGroup(), reg.getTopic(), result));
            }
        }
        return views;
    }

    @PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<TopicFinalResult> findByRound(Long roundId) {
        return results.findByRoundId(roundId);
    }

    private void applyScores(TopicFinalResult result, TopicSummaryView summary) {
        result.setReviewerScore(summary.reviewerScore());
        result.setSupporterScore(summary.supporterScore());
        result.setDefenseAllowed(summary.defenseAllowed());
        result.setCouncilAvgScore(summary.councilAvgScore());
        result.setFinalScore(summary.finalScore());
        result.setGrade(summary.grade());
    }

    @PreAuthorize("hasAnyRole('DEAN','ADMIN')")
    @Transactional(readOnly = true)
    public List<TopicSummaryView> findSummariesByRound(Long roundId) {
        return groupRegistrations.findAllByOrderBySubmittedAtDesc().stream()
                .filter(r -> r.getStatus() == vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus.APPROVED
                        && r.getTopic().getRound().getId().equals(roundId) && councils.getAssignment(r.getId()) != null)
                .map(r -> getSummaryView(r.getId())).toList();
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
            GroupTopicRegistration registration,
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
            TopicFinalResult finalResult,
            UserAccount supporter,
            Evaluation supporterEvaluation,
            BigDecimal supporterScore,
            Boolean defenseAllowed,
            boolean needsCouncil,
            boolean legacyAssessment
    ) {
        public UserAccount getSupporter() { return supporter; }
        public Evaluation getSupporterEvaluation() { return supporterEvaluation; }
        public BigDecimal getSupporterScore() { return supporterScore; }
        public Boolean getDefenseAllowed() { return defenseAllowed; }
        public boolean isNeedsCouncil() { return needsCouncil; }
        public boolean isLegacyAssessment() { return legacyAssessment; }
        public String getFormula() {
            if (legacyAssessment) return "Hồ sơ phân công trước nâng cấp: trung bình các phiếu đã nộp, mỗi người chấm tính một lần";
            return switch (topic.getRound().getType()) {
                case MON_HOC -> "Điểm GV được phân công chấm";
                case TLCN -> "(Điểm GV hỗ trợ + Điểm GVPB) / 2";
                case KLTN -> Boolean.FALSE.equals(defenseAllowed) ? "Không được bảo vệ: điểm cuối cùng = 0"
                        : "(Điểm GVPB + Điểm trung bình hội đồng) / 2";
                case NCKH -> "Trung bình điểm các thành viên hội đồng nghiệm thu; chưa thiết lập ngưỡng xếp loại";
            };
        }
        public GroupTopicRegistration getRegistration() { return registration; }
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
