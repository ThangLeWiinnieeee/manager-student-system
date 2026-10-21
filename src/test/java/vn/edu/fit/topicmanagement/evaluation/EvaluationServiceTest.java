package vn.edu.fit.topicmanagement.evaluation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.council.Council;
import vn.edu.fit.topicmanagement.council.CouncilMember;
import vn.edu.fit.topicmanagement.council.CouncilRole;
import vn.edu.fit.topicmanagement.council.CouncilService;
import vn.edu.fit.topicmanagement.council.TopicAssignment;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.topic.TopicService;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock EvaluationRepository evaluations;
    @Mock TopicService topics;
    @Mock CouncilService councils;
    @Mock UserAccountService users;

    private EvaluationService service;
    private UserAccount advisorLec;
    private UserAccount reviewerLec;
    private Topic topic;
    private Council council;
    private TopicAssignment assignment;

    @BeforeEach
    void setUp() {
        service = new EvaluationService(evaluations, topics, councils, users);

        advisorLec = new UserAccount();
        advisorLec.setId(10L);
        advisorLec.setEmail("advisor@fit.edu.vn");
        advisorLec.setRole(Role.LECTURER);

        reviewerLec = new UserAccount();
        reviewerLec.setId(20L);
        reviewerLec.setEmail("reviewer@fit.edu.vn");
        reviewerLec.setRole(Role.LECTURER);

        topic = new Topic();
        topic.setId(1L);
        topic.setTitle("Đề tài KLTN");
        topic.setAdvisors(Set.of(advisorLec));

        council = new Council();
        council.setId(100L);
        council.setName("Hội đồng 01");
        council.setMembers(List.of(new CouncilMember(council, reviewerLec, CouncilRole.MEMBER)));

        assignment = new TopicAssignment();
        assignment.setTopic(topic);
        assignment.setReviewer(reviewerLec);
        assignment.setCouncil(council);
    }

    @Test
    void evaluate_advisorCannotEvaluate_throwsException() {
        when(users.getByEmail("advisor@fit.edu.vn")).thenReturn(advisorLec);
        when(topics.get(1L)).thenReturn(topic);

        EvaluationForm form = new EvaluationForm();
        assertThrows(BusinessRuleException.class, () -> service.evaluate(1L, form, "advisor@fit.edu.vn"));
    }

    @Test
    void evaluate_saveDraft_success() {
        when(users.getByEmail("reviewer@fit.edu.vn")).thenReturn(reviewerLec);
        when(topics.get(1L)).thenReturn(topic);
        when(councils.getAssignment(1L)).thenReturn(assignment);
        when(evaluations.findByTopic_IdAndEvaluator_Id(1L, reviewerLec.getId())).thenReturn(Optional.empty());
        when(evaluations.save(any(Evaluation.class))).thenAnswer(i -> i.getArgument(0));

        EvaluationForm form = new EvaluationForm();
        form.setScoreContent(BigDecimal.valueOf(3.5));
        form.setScorePresentation(BigDecimal.valueOf(2.5));
        form.setScoreQa(BigDecimal.valueOf(2.5));
        form.setComments("Làm tốt");
        form.setAction("draft");

        Evaluation result = service.evaluate(1L, form, "reviewer@fit.edu.vn");
        assertNotNull(result);
        assertEquals(EvaluationStatus.DRAFT, result.getStatus());
        assertEquals(BigDecimal.valueOf(8.5).setScale(2), result.getTotalScore());
    }

    @Test
    void evaluate_alreadySubmitted_lockedFromEditing() {
        when(users.getByEmail("reviewer@fit.edu.vn")).thenReturn(reviewerLec);
        when(topics.get(1L)).thenReturn(topic);
        when(councils.getAssignment(1L)).thenReturn(assignment);

        Evaluation existing = new Evaluation();
        existing.setStatus(EvaluationStatus.SUBMITTED);
        when(evaluations.findByTopic_IdAndEvaluator_Id(1L, reviewerLec.getId())).thenReturn(Optional.of(existing));

        EvaluationForm form = new EvaluationForm();
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.evaluate(1L, form, "reviewer@fit.edu.vn"));
        assertTrue(ex.getMessage().contains("đã khóa chỉnh sửa"));
    }
}
