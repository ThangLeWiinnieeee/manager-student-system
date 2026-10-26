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
    private GroupTopicRegistration registration;

    @BeforeEach
    void setUp() {
        service = new ResultService(results, topics, councils, evaluations, users, groupRegistrations, groupMembers);

        topic = new Topic();
        topic.setId(1L);
        var course = new vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration();
        var window = new vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod();
        window.setType(vn.edu.fit.topicmanagement.registrationperiod.RoundType.KLTN);
        course.setPeriod(window); topic.setRound(course);
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
        var secretary = new UserAccount(); secretary.setId(30L);
        var member = new UserAccount(); member.setId(40L);
        council.setMembers(List.of(new CouncilMember(council, president, CouncilRole.PRESIDENT),
                new CouncilMember(council, secretary, CouncilRole.SECRETARY), new CouncilMember(council, member, CouncilRole.MEMBER)));

        registration = new GroupTopicRegistration();
        registration.setTopic(topic);
        registration.setStatus(vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus.APPROVED);
        org.springframework.test.util.ReflectionTestUtils.setField(registration, "id", 1L);
        assignment = new TopicAssignment();
        assignment.setTopic(topic);
        assignment.setCouncil(council);
        assignment.setReviewer(reviewer);
    }

    @Test
    void confirmByPresident_notAllScoresSubmitted_throwsException() {
        when(councils.getRegistration(1L)).thenReturn(registration);
        when(councils.getAssignment(1L)).thenReturn(assignment);
        when(evaluations.findByTopic(1L)).thenReturn(List.of()); // Chưa ai nộp điểm
        when(users.getByEmail("president@fit.edu.vn")).thenReturn(president);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.confirmByPresident(1L, "Tốt", "president@fit.edu.vn"));
        assertTrue(ex.getMessage().contains("Chưa đủ điểm"));
    }

    @Test
    void confirmByPresident_allScoresSubmitted_success() {
        when(councils.getRegistration(1L)).thenReturn(registration);
        when(councils.getAssignment(1L)).thenReturn(assignment);

        Evaluation evalReviewer = new Evaluation();
        evalReviewer.setEvaluationType(EvaluationType.REVIEWER);
        evalReviewer.setEvaluator(reviewer);
        evalReviewer.setTotalScore(BigDecimal.valueOf(8.0));
        evalReviewer.setStatus(EvaluationStatus.SUBMITTED);
        evalReviewer.setDefenseAllowed(true);

        Evaluation evalPres = new Evaluation();
        evalPres.setEvaluationType(EvaluationType.COUNCIL);
        evalPres.setEvaluator(president);
        evalPres.setTotalScore(BigDecimal.valueOf(9.0));
        evalPres.setStatus(EvaluationStatus.SUBMITTED);

        var scores = new java.util.ArrayList<Evaluation>(List.of(evalReviewer, evalPres));
        for (var member : council.getMembers().subList(1, 3)) {
            var evaluation = new Evaluation(); evaluation.setEvaluator(member.getLecturer());
            evaluation.setStatus(EvaluationStatus.SUBMITTED); evaluation.setTotalScore(new BigDecimal("9.00"));
            scores.add(evaluation);
        }
        when(evaluations.findByTopic(1L)).thenReturn(scores);
        when(users.getByEmail("president@fit.edu.vn")).thenReturn(president);
        when(results.findByRegistration_Id(1L)).thenReturn(Optional.empty());
        when(results.save(any(TopicFinalResult.class))).thenAnswer(i -> i.getArgument(0));

        TopicFinalResult finalRes = service.confirmByPresident(1L, "Bảo vệ xuất sắc", "president@fit.edu.vn");
        assertNotNull(finalRes);
        assertTrue(finalRes.isPresidentConfirmed());
        // Trung bình cộng (8 + 9) / 2 = 8.50
        assertEquals(BigDecimal.valueOf(8.50).setScale(2), finalRes.getFinalScore());
        assertEquals("GIỎI", finalRes.getGrade());
        assertEquals("Bảo vệ xuất sắc", finalRes.getConclusion());
    }

    @Test
    void publishRefusesIncompleteAssessment() {
        when(councils.getRegistration(1L)).thenReturn(registration);
        when(councils.getAssignment(1L)).thenReturn(assignment);
        when(evaluations.findByTopic(1L)).thenReturn(List.of());
        assertThrows(BusinessRuleException.class, () -> service.publish(1L));
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
        reg.setStatus(vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus.APPROVED);
        org.springframework.test.util.ReflectionTestUtils.setField(reg, "id", 1L);
        reg.setTopic(topic);
        when(groupRegistrations.findByGroup_Id(group.getId())).thenReturn(Optional.of(reg));

        TopicFinalResult r = new TopicFinalResult();
        r.setPublished(false); // Chưa công bố
        when(results.findByRegistration_Id(topic.getId())).thenReturn(Optional.of(r));

        ResultService.StudentResultView viewNotPub = service.getMyStudentResults("sv@fit.edu.vn").getFirst();
        assertNotNull(viewNotPub);
        assertNull(viewNotPub.result()); // Khi chưa công bố, result = null

        r.setPublished(true); // Đã công bố
        ResultService.StudentResultView viewPub = service.getMyStudentResults("sv@fit.edu.vn").getFirst();
        assertNotNull(viewPub);
        assertNotNull(viewPub.result());
    }
}
