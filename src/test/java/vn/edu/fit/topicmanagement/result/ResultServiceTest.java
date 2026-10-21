package vn.edu.fit.topicmanagement.result;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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
import vn.edu.fit.topicmanagement.evaluation.Evaluation;
import vn.edu.fit.topicmanagement.evaluation.EvaluationService;
import vn.edu.fit.topicmanagement.evaluation.EvaluationStatus;
import vn.edu.fit.topicmanagement.evaluation.EvaluationType;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationRepository;
import vn.edu.fit.topicmanagement.studentgroup.StudentGroup;
import vn.edu.fit.topicmanagement.studentgroup.StudentGroupMember;
import vn.edu.fit.topicmanagement.studentgroup.StudentGroupMemberRepository;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.topic.TopicService;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock TopicFinalResultRepository results;
    @Mock TopicService topics;
    @Mock CouncilService councils;
    @Mock EvaluationService evaluations;
    @Mock UserAccountService users;
    @Mock GroupTopicRegistrationRepository groupRegistrations;
    @Mock StudentGroupMemberRepository groupMembers;

    private ResultService service;
    private Topic topic;
    private Council council;
    private UserAccount president;
    private UserAccount reviewer;
    private TopicAssignment assignment;

    @BeforeEach
    void setUp() {
        service = new ResultService(results, topics, councils, evaluations, users, groupRegistrations, groupMembers);

        topic = new Topic();
        topic.setId(1L);
        topic.setTitle("Ứng dụng Spring Boot");

        president = new UserAccount();
        president.setId(10L);
        president.setEmail("president@fit.edu.vn");
        president.setRole(Role.LECTURER);

        reviewer = new UserAccount();
        reviewer.setId(20L);
        reviewer.setEmail("reviewer@fit.edu.vn");
        reviewer.setRole(Role.LECTURER);

        council = new Council();
        council.setId(100L);
        council.setName("Hội đồng 01");
        council.setMembers(List.of(new CouncilMember(council, president, CouncilRole.PRESIDENT)));

        assignment = new TopicAssignment();
        assignment.setTopic(topic);
        assignment.setCouncil(council);
        assignment.setReviewer(reviewer);
    }

    @Test
    void confirmByPresident_notAllScoresSubmitted_throwsException() {
        when(topics.get(1L)).thenReturn(topic);
        when(councils.getAssignment(1L)).thenReturn(assignment);
        when(evaluations.findByTopic(1L)).thenReturn(List.of()); // Chưa ai nộp điểm
        when(users.getByEmail("president@fit.edu.vn")).thenReturn(president);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.confirmByPresident(1L, "Tốt", "president@fit.edu.vn"));
        assertTrue(ex.getMessage().contains("Chưa đủ điểm"));
    }

    @Test
    void confirmByPresident_allScoresSubmitted_success() {
        when(topics.get(1L)).thenReturn(topic);
        when(councils.getAssignment(1L)).thenReturn(assignment);

        Evaluation evalReviewer = new Evaluation();
        evalReviewer.setEvaluationType(EvaluationType.REVIEWER);
        evalReviewer.setEvaluator(reviewer);
        evalReviewer.setTotalScore(BigDecimal.valueOf(8.0));
        evalReviewer.setStatus(EvaluationStatus.SUBMITTED);

        Evaluation evalPres = new Evaluation();
        evalPres.setEvaluationType(EvaluationType.COUNCIL);
        evalPres.setEvaluator(president);
        evalPres.setTotalScore(BigDecimal.valueOf(9.0));
        evalPres.setStatus(EvaluationStatus.SUBMITTED);

        when(evaluations.findByTopic(1L)).thenReturn(List.of(evalReviewer, evalPres));
        when(users.getByEmail("president@fit.edu.vn")).thenReturn(president);
        when(results.findByTopic_Id(1L)).thenReturn(Optional.empty());
        when(results.save(any(TopicFinalResult.class))).thenAnswer(i -> i.getArgument(0));

        TopicFinalResult finalRes = service.confirmByPresident(1L, "Bảo vệ xuất sắc", "president@fit.edu.vn");
        assertNotNull(finalRes);
        assertTrue(finalRes.isPresidentConfirmed());
        // Điểm: 8.0 * 0.3 + 9.0 * 0.7 = 2.4 + 6.3 = 8.70 (Xếp loại GIỎI)
        assertEquals(BigDecimal.valueOf(8.70).setScale(2), finalRes.getFinalScore());
        assertEquals("GIỎI", finalRes.getGrade());
        assertEquals("Bảo vệ xuất sắc", finalRes.getConclusion());
    }

    @Test
    void publishAndRevoke_byDean_success() {
        TopicFinalResult r = new TopicFinalResult();
        r.setPresidentConfirmed(true);
        r.setPublished(false);
        when(results.findByTopic_Id(1L)).thenReturn(Optional.of(r));
        when(results.save(any(TopicFinalResult.class))).thenAnswer(i -> i.getArgument(0));

        TopicFinalResult published = service.publish(1L);
        assertTrue(published.isPublished());
        assertNotNull(published.getPublishedAt());

        TopicFinalResult revoked = service.revoke(1L);
        assertFalse(revoked.isPublished());
    }

    @Test
    void getMyStudentResult_onlyReturnsWhenPublished() {
        UserAccount student = new UserAccount();
        student.setEmail("sv@fit.edu.vn");
        when(users.getByEmail("sv@fit.edu.vn")).thenReturn(student);

        StudentGroup group = new StudentGroup();
        group.setName("Nhóm 01");
        StudentGroupMember member = new StudentGroupMember(group, student);
        when(groupMembers.findByStudent_Id(student.getId())).thenReturn(List.of(member));

        GroupTopicRegistration reg = new GroupTopicRegistration();
        reg.setGroup(group);
        reg.setTopic(topic);
        when(groupRegistrations.findByGroup_Id(group.getId())).thenReturn(Optional.of(reg));

        TopicFinalResult r = new TopicFinalResult();
        r.setPublished(false); // Chưa công bố
        when(results.findByTopic_Id(topic.getId())).thenReturn(Optional.of(r));

        ResultService.StudentResultView viewNotPub = service.getMyStudentResult("sv@fit.edu.vn");
        assertNotNull(viewNotPub);
        assertNull(viewNotPub.result()); // Khi chưa công bố, result = null

        r.setPublished(true); // Đã công bố
        ResultService.StudentResultView viewPub = service.getMyStudentResult("sv@fit.edu.vn");
        assertNotNull(viewPub);
        assertNotNull(viewPub.result());
    }
}
