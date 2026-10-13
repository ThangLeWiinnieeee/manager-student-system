package vn.edu.fit.topicmanagement.topic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.department.Department;
import vn.edu.fit.topicmanagement.department.DepartmentService;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodService;
import vn.edu.fit.topicmanagement.registrationperiod.RoundType;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {
    @Mock TopicRepository topics;
    @Mock RegistrationPeriodService periods;
    @Mock DepartmentService departments;
    @Mock UserAccountService users;

    private UserAccount owner;
    private UserAccount lecturerA;
    private UserAccount lecturerB;
    private Department department;
    private RegistrationPeriod round;
    private TopicForm validForm;

    @BeforeEach
    void setUp() {
        owner = lecturer("gv001@fit.edu.vn");
        lecturerA = lecturer("gv002@fit.edu.vn");
        lecturerB = lecturer("gv003@fit.edu.vn");
        department = new Department();
        department.setCode("SE");
        department.setName("Kỹ thuật phần mềm");
        department.setEnabled(true);
        round = new RegistrationPeriod();
        round.setName("Đợt KLTN HK1");
        round.setType(RoundType.KLTN);

        validForm = new TopicForm();
        validForm.setTitle("Xây dựng hệ thống quản lý đề tài");
        validForm.setDescription("Đồ án cuối kỳ");
        validForm.setRoundId(1L);
        validForm.setDepartmentId(1L);
        validForm.setAdvisorIds(List.of(2L));
    }

    private static UserAccount lecturer(String email) {
        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setFullName(email);
        account.setRole(Role.LECTURER);
        account.setEnabled(true);
        return account;
    }

    private void stubCreateHappyPath() {
        when(users.getByEmail("gv001@fit.edu.vn")).thenReturn(owner);
        when(departments.get(1L)).thenReturn(department);
        when(periods.get(1L)).thenReturn(round);
    }

    private Topic persistedDraftTopic() {
        Topic topic = new Topic();
        topic.setTitle(validForm.getTitle());
        topic.setCreatedBy(owner);
        topic.setDepartment(department);
        topic.setRound(round);
        topic.getAdvisors().add(lecturerA);
        topic.setStatus(TopicStatus.DRAFT);
        return topic;
    }

    @Test
    void createDraftThenSubmitSetsPending() {
        stubCreateHappyPath();
        when(users.get(2L)).thenReturn(lecturerA);
        when(topics.save(any(Topic.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(topics.findById(any())).thenAnswer(invocation -> Optional.of(persistedDraftTopic()));
        TopicService service = new TopicService(topics, periods, departments, users);

        Topic submitted = service.create("gv001@fit.edu.vn", validForm, true);

        assertEquals(TopicStatus.PENDING, submitted.getStatus());
        verify(periods).assertGvWindowOpen(round.getId());
    }

    @Test
    void createWithoutSubmitKeepsDraft() {
        stubCreateHappyPath();
        when(users.get(2L)).thenReturn(lecturerA);
        when(topics.save(any(Topic.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TopicService service = new TopicService(topics, periods, departments, users);

        Topic draft = service.create("gv001@fit.edu.vn", validForm, false);

        assertEquals(TopicStatus.DRAFT, draft.getStatus());
        verify(periods, never()).assertGvWindowOpen(anyLong());
    }

    @Test
    void twoAdvisorsAccepted() {
        stubCreateHappyPath();
        when(users.get(2L)).thenReturn(lecturerA);
        when(users.get(3L)).thenReturn(lecturerB);
        when(topics.save(any(Topic.class))).thenAnswer(invocation -> invocation.getArgument(0));
        validForm.setAdvisorIds(List.of(2L, 3L));
        TopicService service = new TopicService(topics, periods, departments, users);

        Topic draft = service.create("gv001@fit.edu.vn", validForm, false);

        assertEquals(TopicStatus.DRAFT, draft.getStatus());
        assertEquals(2, draft.getAdvisors().size());
    }

    @Test
    void rejectsThreeAdvisors() {
        stubCreateHappyPath();
        validForm.setAdvisorIds(List.of(2L, 3L, 4L));
        TopicService service = new TopicService(topics, periods, departments, users);

        assertThrows(BusinessRuleException.class,
                () -> service.create("gv001@fit.edu.vn", validForm, false));
        verify(topics, never()).save(any(Topic.class));
    }

    @Test
    void rejectsZeroAdvisors() {
        stubCreateHappyPath();
        validForm.setAdvisorIds(List.of());
        TopicService service = new TopicService(topics, periods, departments, users);

        assertThrows(BusinessRuleException.class,
                () -> service.create("gv001@fit.edu.vn", validForm, false));
    }

    @Test
    void advisorMustBeActiveLecturer() {
        stubCreateHappyPath();
        when(users.get(2L)).thenReturn(lecturerA);
        lecturerA.setRole(Role.STUDENT);
        TopicService service = new TopicService(topics, periods, departments, users);

        assertThrows(BusinessRuleException.class,
                () -> service.create("gv001@fit.edu.vn", validForm, false));
    }

    @Test
    void submitBlockedOutsideGvWindow() {
        Topic draft = persistedDraftTopic();
        when(topics.findById(10L)).thenReturn(Optional.of(draft));
        doThrow(new BusinessRuleException("Ngoài thời gian đăng ký đề tài của giảng viên"))
                .when(periods).assertGvWindowOpen(round.getId());
        TopicService service = new TopicService(topics, periods, departments, users);

        assertThrows(BusinessRuleException.class, () -> service.submit(10L, "gv001@fit.edu.vn"));
        verify(topics, never()).save(any(Topic.class));
    }

    @Test
    void illegalTransitionDraftToApprovedBlocked() {
        when(topics.findById(10L)).thenReturn(Optional.of(persistedDraftTopic()));
        TopicService service = new TopicService(topics, periods, departments, users);

        assertThrows(BusinessRuleException.class, () -> service.approve(10L));
    }

    @Test
    void illegalTransitionPendingToPublishedBlocked() {
        Topic pending = persistedDraftTopic();
        pending.setStatus(TopicStatus.PENDING);
        when(topics.findById(10L)).thenReturn(Optional.of(pending));
        TopicService service = new TopicService(topics, periods, departments, users);

        assertThrows(BusinessRuleException.class, () -> service.publish(10L));
    }

    @Test
    void approvePublishRejectSetTargetStatus() {
        Topic pending = persistedDraftTopic();
        pending.setStatus(TopicStatus.PENDING);
        when(topics.findById(10L)).thenReturn(Optional.of(pending));
        when(topics.save(any(Topic.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TopicService service = new TopicService(topics, periods, departments, users);

        assertEquals(TopicStatus.APPROVED, service.approve(10L).getStatus());

        assertEquals(TopicStatus.PUBLISHED, service.publish(10L).getStatus());
    }

    @Test
    void rejectSetsRejectedStatusAndReason() {
        Topic pending = persistedDraftTopic();
        pending.setStatus(TopicStatus.PENDING);
        when(topics.findById(10L)).thenReturn(Optional.of(pending));
        when(topics.save(any(Topic.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TopicService service = new TopicService(topics, periods, departments, users);

        Topic rejected = service.reject(10L, "Thiếu cơ sở khoa học");

        assertEquals(TopicStatus.REJECTED, rejected.getStatus());
        assertEquals("Thiếu cơ sở khoa học", rejected.getRejectionReason());
    }

    @Test
    void onlyOwnerCanEdit() {
        when(topics.findById(10L)).thenReturn(Optional.of(persistedDraftTopic()));
        TopicService service = new TopicService(topics, periods, departments, users);

        assertThrows(BusinessRuleException.class,
                () -> service.update(10L, "other@fit.edu.vn", validForm, false));
    }

    @Test
    void onlyEditableStatusCanBeUpdated() {
        Topic approved = persistedDraftTopic();
        approved.setStatus(TopicStatus.APPROVED);
        when(topics.findById(10L)).thenReturn(Optional.of(approved));
        TopicService service = new TopicService(topics, periods, departments, users);

        assertThrows(BusinessRuleException.class,
                () -> service.update(10L, "gv001@fit.edu.vn", validForm, false));
    }
}