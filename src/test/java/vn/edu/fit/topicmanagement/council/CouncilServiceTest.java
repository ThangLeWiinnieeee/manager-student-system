package vn.edu.fit.topicmanagement.council;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistrationService;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationRepository;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.topic.TopicService;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@ExtendWith(MockitoExtension.class)
class CouncilServiceTest {

    @Mock CouncilRepository councils;
    @Mock vn.edu.fit.topicmanagement.evaluation.EvaluationRepository evaluations;
    @Mock vn.edu.fit.topicmanagement.result.TopicFinalResultRepository results;
    @Mock CouncilMemberRepository members;
    @Mock TopicAssignmentRepository assignments;
    @Mock CourseRegistrationService periods;
    @Mock TopicService topics;
    @Mock UserAccountService users;
    @Mock GroupTopicRegistrationRepository groupRegistrations;

    private CouncilService service;
    private CourseRegistration round;
    private Council council;
    private UserAccount advisorLec;
    private UserAccount otherLec;
    private Topic topic;

    @BeforeEach
    void setUp() {
        service = new CouncilService(councils, members, assignments, periods, topics, users, groupRegistrations, evaluations, results);

        round = new CourseRegistration();
        round.setPeriod(new vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod());
        round.setId(1L);
        round.getPeriod().setType(vn.edu.fit.topicmanagement.registrationperiod.RoundType.MON_HOC);
        round.setCourseName("Đợt KLTN 2026");

        council = new Council();
        council.setName("Hội đồng 01");
        council.setRound(round);
        council.setMembers(new ArrayList<>());

        advisorLec = new UserAccount();
        advisorLec.setId(2L);
        advisorLec.setFullName("Nguyễn Văn Hướng Dẫn");
        advisorLec.setEmail("hd@fit.edu.vn");
        advisorLec.setRole(Role.LECTURER);
        advisorLec.setEnabled(true);

        otherLec = new UserAccount();
        otherLec.setId(3L);
        otherLec.setFullName("Trần Văn Phản Biện");
        otherLec.setEmail("pb@fit.edu.vn");
        otherLec.setRole(Role.LECTURER);
        otherLec.setEnabled(true);

        topic = new Topic();
        topic.setId(10L);
        topic.setTitle("Nghiên cứu AI");
        topic.setRound(round);
        topic.setAdvisors(Set.of(advisorLec));
    }

    @Test
    void createCouncil_duplicateName_throwsException() {
        when(periods.get(1L)).thenReturn(round);
        when(councils.existsByRoundIdAndNameIgnoreCase(round.getId(), "Hội đồng 01")).thenReturn(true);

        CouncilForm form = new CouncilForm();
        form.setName("Hội đồng 01");
        form.setRoundId(1L);

        assertThrows(BusinessRuleException.class, () -> service.create(form));
    }

    @Test
    void addMember_max5Members_throwsException() {
        when(councils.findById(1L)).thenReturn(Optional.of(council));
        when(users.get(2L)).thenReturn(otherLec);
        when(members.existsById(any())).thenReturn(false);
        when(members.countByCouncil_Id(1L)).thenReturn(5L);

        CouncilMemberForm form = new CouncilMemberForm();
        form.setLecturerId(2L);
        form.setRole(CouncilRole.MEMBER);

        assertThrows(BusinessRuleException.class, () -> service.addOrUpdateMember(1L, form));
    }

    @Test
    void assignReviewer_advisorCannotBeReviewer() {

        GroupTopicRegistration reg = new GroupTopicRegistration();
        reg.setStatus(GroupTopicRegistrationStatus.APPROVED);
        reg.setTopic(topic);
        org.springframework.test.util.ReflectionTestUtils.setField(reg, "id", 10L);
        when(groupRegistrations.findRoundIdById(10L)).thenReturn(Optional.of(1L));
        when(groupRegistrations.findById(10L)).thenReturn(Optional.of(reg));
        when(assignments.findByRegistration_Id(10L)).thenReturn(Optional.empty());
        when(users.get(advisorLec.getId())).thenReturn(advisorLec);

        TopicAssignmentForm form = new TopicAssignmentForm();
        form.setRegistrationId(10L);
        form.setReviewerId(advisorLec.getId());
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.assignTopic(form));
        assertTrue(ex.getMessage().contains("Giảng viên hướng dẫn không được làm phản biện"));
    }

    @Test
    void assignCouncil_cannotAssignIfCouncilContainsAdvisor() {
        round.getPeriod().setType(vn.edu.fit.topicmanagement.registrationperiod.RoundType.NCKH);

        GroupTopicRegistration reg = new GroupTopicRegistration();
        reg.setStatus(GroupTopicRegistrationStatus.APPROVED);
        reg.setTopic(topic);
        org.springframework.test.util.ReflectionTestUtils.setField(reg, "id", 10L);
        when(groupRegistrations.findRoundIdById(10L)).thenReturn(Optional.of(1L));
        when(groupRegistrations.findById(10L)).thenReturn(Optional.of(reg));
        when(assignments.findByRegistration_Id(10L)).thenReturn(Optional.empty());

        // Hội đồng có chứa advisorLec
        CouncilMember cm = new CouncilMember(council, advisorLec, CouncilRole.MEMBER);
        var president = new UserAccount(); president.setId(4L); president.setRole(Role.LECTURER); president.setEnabled(true);
        var secretary = new UserAccount(); secretary.setId(5L); secretary.setRole(Role.LECTURER); secretary.setEnabled(true);
        council.setMembers(List.of(cm, new CouncilMember(council, president, CouncilRole.PRESIDENT), new CouncilMember(council, secretary, CouncilRole.SECRETARY)));
        when(members.findByCouncil_IdOrderByRoleAsc(council.getId())).thenReturn(council.getMembers());

        when(councils.findById(1L)).thenReturn(Optional.of(council));

        TopicAssignmentForm form = new TopicAssignmentForm();
        form.setRegistrationId(10L);
        form.setCouncilId(1L);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.assignTopic(form));
        assertTrue(ex.getMessage().contains("Thành viên hội đồng không được là GVHD"));
    }
}
