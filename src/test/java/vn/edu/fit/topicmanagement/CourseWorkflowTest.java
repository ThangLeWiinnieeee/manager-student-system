package vn.edu.fit.topicmanagement;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.council.*;
import vn.edu.fit.topicmanagement.department.*;
import vn.edu.fit.topicmanagement.evaluation.*;
import vn.edu.fit.topicmanagement.registrationperiod.*;
import vn.edu.fit.topicmanagement.result.*;
import vn.edu.fit.topicmanagement.studentgroup.*;
import vn.edu.fit.topicmanagement.submission.*;
import vn.edu.fit.topicmanagement.topic.*;
import vn.edu.fit.topicmanagement.user.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.datasource.url=jdbc:h2:mem:course_workflows;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;LOCK_TIMEOUT=10000;INIT=CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE")
class CourseWorkflowTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired UserAccountRepository users;
    @Autowired UserAccountService accounts;
    @Autowired DepartmentRepository departments;
    @Autowired CourseRegistrationService periods;
    @Autowired CourseRegistrationRepository periodRepository;
    @Autowired RegistrationPeriodRepository windows;
    @Autowired RegistrationPeriodService windowService;
    @Autowired StudentGroupService groups;
    @Autowired GroupTopicRegistrationService registrations;
    @Autowired GroupTopicRegistrationRepository registrationRepository;
    @Autowired TopicRepository topics;
    @Autowired TopicService topicService;
    @Autowired CouncilService councils;
    @Autowired TopicAssignmentRepository assignmentRepository;
    @Autowired EvaluationService evaluations;
    @Autowired EvaluationRepository evaluationRepository;
    @Autowired ResultService results;
    @Autowired ReportSubmissionService submissions;
    @Autowired ReportReviewService reports;
    @Autowired vn.edu.fit.topicmanagement.handover.AcademicHandoverService handovers;
    @Autowired vn.edu.fit.topicmanagement.handover.HandoverLog handoverHistory;
    @Autowired GroupInvitationRepository invitations;

    UserAccount teacher, dean, first, second;
    Department department;

    @BeforeEach
    void setup() {
        teacher = user(Role.LECTURER);
        dean = user(Role.DEAN);
        first = user(Role.STUDENT);
        second = user(Role.STUDENT);
        department = departments.findAll().getFirst();
    }

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test
    void deanCanReadGroupsForAcademicWorkflowsButCannotModifyThem() {
        var course = round(RegistrationMode.SELF_PROPOSED, 3);
        var group = group(first, course);
        login(dean);
        var form = new StudentGroupForm();
        form.setName("Changed by dean"); form.setRoundId(course.getId());
        assertThrows(AccessDeniedException.class, () -> groups.create(form));
        assertThrows(AccessDeniedException.class, () -> groups.update(group.getId(), form));
        assertThrows(AccessDeniedException.class, () -> groups.delete(group.getId()));
        assertThrows(AccessDeniedException.class, () -> groups.addMember(group.getId(), second.getEmail()));
        assertThrows(AccessDeniedException.class, () -> groups.removeMember(group.getId(), first.getId()));
        assertThrows(AccessDeniedException.class, () -> groups.assignLeader(group.getId(), second.getId()));
        assertEquals(group.getName(), groups.get(group.getId()).getName());
        assertEquals(first.getId(), groups.get(group.getId()).getLeader().getId());
        assertEquals(1, groups.getMembers(group.getId()).size());
        login(teacher);
        form.setName("Changed by lecturer");
        groups.update(group.getId(), form);
        join(second, course);
        login(teacher);
        groups.addMember(group.getId(), second.getEmail());
        login(second); groups.respondToInvitation(group.getId(), true); login(teacher);
        groups.assignLeader(group.getId(), second.getId());
        assertEquals(second.getId(), groups.get(group.getId()).getLeader().getId());
        groups.removeMember(group.getId(), first.getId());
        assertEquals(1, groups.getMembers(group.getId()).size());
    }

    @Test
    void sharedWindowSupportsIndependentClassesAndStudentsJoinOneGroupPerClass() {
        login(dean);
        var schedule = openWindowForm();
        var window = windowService.create(schedule);
        var form = courseForm(window.getId(), "WEB-A", 3);
        login(teacher);
        assertThrows(AccessDeniedException.class, () -> windowService.create(schedule));
        assertThrows(AccessDeniedException.class, () -> windowService.update(window.getId(), schedule));
        assertThrows(AccessDeniedException.class, () -> windowService.delete(window.getId()));
        form.setLecturerId(dean.getId()); // The client cannot choose a different owner.
        var web = periods.create(form);
        assertEquals(teacher.getId(), web.getLecturer().getId());
        assertThrows(BusinessRuleException.class, () -> periods.create(form));
        var java = periods.create(courseForm(window.getId(), "JAVA-B", 5));
        assertEquals(web.getPeriod().getId(), java.getPeriod().getId());
        assertThrows(BusinessRuleException.class, () -> group(first, web));
        login(dean);
        assertThrows(BusinessRuleException.class, () -> windowService.delete(window.getId()));
        schedule.setType(RoundType.NCKH);
        assertThrows(BusinessRuleException.class, () -> windowService.update(window.getId(), schedule));
        schedule.setType(RoundType.MON_HOC);
        schedule.setGvStart(LocalDateTime.now().minusDays(4)); schedule.setGvEnd(LocalDateTime.now().minusDays(2));
        schedule.setSvStart(LocalDateTime.now().minusDays(1)); schedule.setSvEnd(LocalDateTime.now().plusDays(1));
        windowService.update(window.getId(), schedule);
        assertEquals(schedule.getSvStart().withNano(0), periods.get(web.getId()).getSvStart().withNano(0));
        assertEquals(schedule.getSvStart().withNano(0), periods.get(java.getId()).getSvStart().withNano(0));
        assertNotNull(group(first, web));
        assertNotNull(group(first, java));
        assertThrows(BusinessRuleException.class, () -> group(first, web));
        login(first);
        assertEquals(2, groups.findMyGroups().size());
        login(teacher);
        assertThrows(BusinessRuleException.class, () -> periods.create(courseForm(window.getId(), "LATE", 5)));
    }

    @Test
    void futureWindowsRejectCourseRegistrationAndOnlyEmptyUnfinishedWindowsCanBeDeleted() {
        login(dean);
        var schedule = openWindowForm();
        schedule.setGvStart(LocalDateTime.now().plusDays(1)); schedule.setGvEnd(LocalDateTime.now().plusDays(2));
        schedule.setSvStart(LocalDateTime.now().plusDays(3)); schedule.setSvEnd(LocalDateTime.now().plusDays(4));
        var future = windowService.create(schedule);
        login(teacher);
        assertThrows(BusinessRuleException.class, () -> periods.create(courseForm(future.getId(), "EARLY", 3)));
        login(dean);
        windowService.delete(future.getId());
        assertFalse(windows.existsById(future.getId()));
        schedule.setGvStart(LocalDateTime.now().minusDays(4)); schedule.setGvEnd(LocalDateTime.now().minusDays(3));
        schedule.setSvStart(LocalDateTime.now().minusDays(2)); schedule.setSvEnd(LocalDateTime.now().minusDays(1));
        var ended = windowService.create(schedule);
        assertThrows(BusinessRuleException.class, () -> windowService.delete(ended.getId()));
        assertTrue(windows.existsById(ended.getId()));
    }

    @Test
    void concurrentRegistrationOfTheSameClassHasOnlyOneWinner() throws Exception {
        login(dean);
        var window = windowService.create(openWindowForm());
        var outcomes = race(
                () -> { login(teacher); periods.create(courseForm(window.getId(), "SAME", 3)); return true; },
                () -> { login(teacher); periods.create(courseForm(window.getId(), "SAME", 3)); return true; });
        assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
        assertEquals(1, periodRepository.findByPeriod_IdOrderByCourseNameAsc(window.getId()).size());
    }

    @Test
    void academicActivitiesAutomaticallyNameCoursesWhileSubjectsRequireNames() {
        for (var type : RoundType.values()) {
            login(dean);
            var schedule = openWindowForm();
            schedule.setType(type);
            if (type.requiresGvpbDeadline()) schedule.setGvpbDeadline(schedule.getSvEnd().plusDays(1));
            if (type.requiresCouncilReportDate()) schedule.setCouncilReportDate(schedule.getSvEnd().plusDays(2));
            var window = windowService.create(schedule);
            login(teacher);
            var form = courseForm(window.getId(), "AUTO", 3);
            form.setCourseName(null);
            if (type == RoundType.MON_HOC) {
                assertThrows(BusinessRuleException.class, () -> periods.create(form));
                form.setCourseName("Web programming");
                assertEquals("Web programming", periods.create(form).getCourseName());
            } else {
                var course = periods.create(form);
                assertEquals(type.getDisplayName(), course.getCourseName());
                form.setCourseName("Client supplied name");
                assertEquals(type.getDisplayName(), periods.update(course.getId(), form).getCourseName());
                form.setCourseName(null);
                assertThrows(BusinessRuleException.class, () -> periods.create(form));
            }
        }
    }

    RegistrationPeriodForm openWindowForm() {
        var form = new RegistrationPeriodForm();
        form.setType(RoundType.MON_HOC);
        form.setGvStart(LocalDateTime.now().minusDays(1)); form.setGvEnd(LocalDateTime.now().plusDays(1));
        form.setSvStart(LocalDateTime.now().plusDays(2)); form.setSvEnd(LocalDateTime.now().plusDays(3));
        return form;
    }

    CourseRegistrationForm courseForm(Long periodId, String classCode, int limit) {
        var form = new CourseRegistrationForm();
        form.setPeriodId(periodId); form.setCourseName("Course " + classCode); form.setClassCode(classCode);
        form.setDepartmentId(department.getId()); form.setMaxMembers(limit);
        return form;
    }

    @Test
    void membershipIsUniquePerRoundAndLecturerLimitIsEnforced() {
        var round = round(RegistrationMode.SHARED, 3);
        var group = group(first, round);
        join(second, round);
        var third = user(Role.STUDENT); join(third, round);
        login(first);
        groups.addMemberByLeader(group.getId(), second.getEmail());
        groups.addMemberByLeader(group.getId(), third.getEmail());
        login(second); groups.respondToInvitation(group.getId(), true);
        login(third); groups.respondToInvitation(group.getId(), true); login(first);
        assertThrows(BusinessRuleException.class,
                () -> groups.addMemberByLeader(group.getId(), user(Role.STUDENT).getEmail()));
        assertThrows(BusinessRuleException.class, () -> group(first, round));
        assertThrows(BusinessRuleException.class, () -> group(second, round));
        var otherRound = round(RegistrationMode.EXCLUSIVE, 5);
        assertNotNull(group(first, otherRound).getId());
        login(first);
        assertEquals(2, groups.findMyGroups().size());
        assertPageAs(first, "/student/my-groups", group.getName());
        assertPageAs(first, "/student/my-groups/" + group.getId(), group.getName());
    }

    @Test
    void concurrentExclusiveClaimsHaveOneWinnerAndHideTheTakenTopic() throws Exception {
        var round = round(RegistrationMode.EXCLUSIVE, 3);
        var topic = publishedTopic(round);
        var a = group(first, round);
        var b = group(second, round);
        var outcomes = race(
                () -> { login(first); registrations.register(a.getId(), topic.getId()); return true; },
                () -> { login(second); registrations.register(b.getId(), topic.getId()); return true; });
        assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
        assertEquals(1, registrationRepository.findByTopic_Id(topic.getId()).size());
        login(first);
        assertTrue(registrations.findPublishedTopics(a.getId(), 0).isEmpty());
        assertPageAs(first, "/student/my-groups/" + a.getId() + "/topics", "Đăng ký đề tài");
        login(second);
        assertTrue(registrations.findPublishedTopics(b.getId(), 0).isEmpty());
    }

    @Test
    void concurrentMembersCannotExceedTheLecturerLimit() throws Exception {
        var course = round(RegistrationMode.SHARED, 2);
        var group = group(first, course);
        var third = user(Role.STUDENT);
        join(second, course); join(third, course);
        login(first);
        groups.addMemberByLeader(group.getId(), second.getEmail());
        groups.addMemberByLeader(group.getId(), third.getEmail());
        var outcomes = race(
                () -> { login(second); groups.respondToInvitation(group.getId(), true); return true; },
                () -> { login(third); groups.respondToInvitation(group.getId(), true); return true; });
        assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
        login(first);
        assertEquals(2, groups.getMyGroupMembers(group.getId()).size());
    }

    @Test
    void lecturerOnlyManagesOwnRoundsAndCannotChangeRulesAfterGroupsExist() {
        var round = round(RegistrationMode.SHARED, 3);
        var group = group(first, round);
        login(user(Role.LECTURER));
        assertThrows(AccessDeniedException.class, () -> periods.update(round.getId(), periods.toForm(round)));
        assertThrows(AccessDeniedException.class, () -> groups.get(group.getId()));
        assertTrue(groups.findAll().isEmpty());
        login(teacher);
        assertPageAs(teacher, "/course-registrations", round.getName());
        assertPageAs(teacher, "/course-registrations/new", "maxMembers");
        assertPageAs(teacher, "/course-registrations/" + round.getId() + "/edit", "Web programming");
        var form = periods.toForm(round);
        form.setMaxMembers(5);
        assertThrows(BusinessRuleException.class, () -> periods.update(round.getId(), form));
        form.setMaxMembers(6);
        assertThrows(BusinessRuleException.class, () -> periods.create(form));
    }

    @Test
    void duplicateStudentProposalTitlesRemainIndependentAndPrivate() {
        var round = round(RegistrationMode.SELF_PROPOSED, 5);
        var a = group(first, round);
        var b = group(second, round);
        assertPageAs(first, "/student/my-groups/" + a.getId() + "/topics", "proposalForm");
        ProposalForm form = new ProposalForm();
        form.setTitle("Same project title");
        form.setDescription("Independent student project description");
        login(first);
        var ra = registrations.propose(a.getId(), form);
        assertThrows(AccessDeniedException.class, () -> registrations.propose(b.getId(), form));
        login(second);
        var rb = registrations.propose(b.getId(), form);
        assertPageAs(dean, "/dean/group-topic-registrations", "Same project title");
        assertPageAs(dean, "/topics/" + ra.getTopic().getId(), "Phân công giảng viên hướng dẫn");
        assertNotEquals(ra.getTopic().getId(), rb.getTopic().getId());
        assertThrows(AccessDeniedException.class, () -> topicService.getVisible(ra.getTopic().getId(), second.getEmail()));
        assertTrue(registrations.findPublishedTopics(b.getId(), 0).isEmpty());
        login(dean);
        registrations.approve(ra.getId());
        registrations.reject(rb.getId(), "Please clarify the scope");
        assertEquals(GroupTopicRegistrationStatus.APPROVED, registrationRepository.findById(ra.getId()).orElseThrow().getStatus());
        assertEquals(TopicStatus.APPROVED, topics.findById(ra.getTopic().getId()).orElseThrow().getStatus());
        assertEquals(TopicStatus.REJECTED, topics.findById(rb.getTopic().getId()).orElseThrow().getStatus());
        assertThrows(BusinessRuleException.class, () -> topicService.publish(ra.getTopic().getId()));
    }

    @Test
    void lecturerReviewsOnlyStudentProposalsFromTheirOwnClasses() {
        var ownCourse = round(RegistrationMode.SELF_PROPOSED, 3);
        var ownGroup = group(first, ownCourse);
        var proposal = new ProposalForm();
        proposal.setTitle("Lecturer review proposal");
        proposal.setDescription("Proposal assigned to the course lecturer");
        login(first);
        var ownRegistration = registrations.propose(ownGroup.getId(), proposal);

        var otherLecturer = user(Role.LECTURER);
        var originalTeacher = teacher;
        teacher = otherLecturer;
        var otherCourse = round(RegistrationMode.SELF_PROPOSED, 3);
        var otherGroup = group(second, otherCourse);
        proposal.setTitle("Other lecturer proposal");
        login(second);
        var otherRegistration = registrations.propose(otherGroup.getId(), proposal);
        teacher = originalTeacher;

        var ownPage = requestAs(originalTeacher, "/dean/group-topic-registrations", null);
        assertEquals(200, ownPage.statusCode());
        assertTrue(ownPage.body().contains("Lecturer review proposal"));
        assertFalse(ownPage.body().contains("Other lecturer proposal"));
        var otherPage = requestAs(otherLecturer, "/dean/group-topic-registrations", null);
        assertEquals(200, otherPage.statusCode());
        assertFalse(otherPage.body().contains("Lecturer review proposal"));
        assertTrue(otherPage.body().contains("Other lecturer proposal"));

        login(otherLecturer);
        assertThrows(AccessDeniedException.class, () -> registrations.approve(ownRegistration.getId()));
        login(originalTeacher);
        registrations.approve(ownRegistration.getId());
        assertEquals(GroupTopicRegistrationStatus.APPROVED,
                registrationRepository.findById(ownRegistration.getId()).orElseThrow().getStatus());
        assertEquals(originalTeacher.getId(), registrationRepository.findById(ownRegistration.getId())
                .orElseThrow().getReviewedBy().getId());
        login(otherLecturer);
        registrations.reject(otherRegistration.getId(), "Please revise the proposed scope");
        assertEquals(GroupTopicRegistrationStatus.REJECTED,
                registrationRepository.findById(otherRegistration.getId()).orElseThrow().getStatus());
    }

    @Test
    void proposalPagesFollowReviewStatusAndStudentWindow() {
        var course = round(RegistrationMode.SELF_PROPOSED, 3);
        var group = group(first, course);
        String path = "/student/my-groups/" + group.getId() + "/topics";
        var createPage = requestAs(teacher, "/topics/new", null);
        assertEquals(200, createPage.statusCode());
        var courseOptions = java.util.regex.Pattern.compile("(?s)<select[^>]*id=\"roundId\"[^>]*>(.*?)</select>")
                .matcher(createPage.body());
        assertTrue(courseOptions.find());
        assertFalse(courseOptions.group(1).contains("value=\"" + course.getId() + "\""));
        var proposal = new ProposalForm();
        proposal.setTitle("Proposal lifecycle check");
        proposal.setDescription("Original proposal content");
        login(first);
        var registration = registrations.propose(group.getId(), proposal);
        var pending = requestAs(first, path, null);
        assertEquals(200, pending.statusCode());
        assertFalse(pending.body().contains("id=\"proposalForm\""));
        assertTrue(pending.body().contains("Đề xuất đang chờ khoa xét duyệt"));
        var list = requestAs(dean, "/topics/" + registration.getTopic().getId(), null);
        assertEquals(200, list.statusCode());
        assertFalse(list.body().contains("/topics/" + registration.getTopic().getId() + "/approve"));
        assertTrue(list.body().contains("Xét duyệt đề xuất của nhóm"));
        login(dean);
        registrations.reject(registration.getId(), "Clarify scope");
        var rejected = requestAs(first, path, null);
        assertEquals(200, rejected.statusCode());
        assertTrue(rejected.body().contains("value=\"Proposal lifecycle check\""));
        assertTrue(rejected.body().contains("Original proposal content</textarea>"));
        var invalid = requestAs(first, path + "/propose", java.util.Map.of("title", "Revised title", "description", ""));
        assertEquals(200, invalid.statusCode());
        assertTrue(invalid.body().contains("value=\"Revised title\""));
        course.getPeriod().setSvEnd(LocalDateTime.now().minusMinutes(1));
        windows.save(course.getPeriod());
        var closed = requestAs(first, path, null);
        assertEquals(200, closed.statusCode());
        assertFalse(closed.body().contains("id=\"proposalForm\""));
        assertTrue(closed.body().contains("Hiện không trong thời gian sinh viên đăng ký"));
        login(first);
        assertThrows(BusinessRuleException.class, () -> registrations.propose(group.getId(), proposal));
    }

    @Test
    void myGroupsSeparatesClassesAndPreselectsTheRequestedClass() {
        var firstCourse = round(RegistrationMode.SHARED, 3);
        var secondCourse = round(RegistrationMode.SHARED, 3);
        var firstGroup = group(first, firstCourse);
        join(first, secondCourse);
        var page = requestAs(first, "/student/my-groups", null);
        assertEquals(200, page.statusCode());
        var firstCard = java.util.regex.Pattern.compile("(?s)id=\"course-" + firstCourse.getId() + "\".*?</section>").matcher(page.body());
        var secondCard = java.util.regex.Pattern.compile("(?s)id=\"course-" + secondCourse.getId() + "\".*?</section>").matcher(page.body());
        assertTrue(firstCard.find()); assertTrue(secondCard.find());
        assertTrue(firstCard.group().contains("/student/my-groups/" + firstGroup.getId() + "\""));
        assertFalse(secondCard.group().contains("/student/my-groups/" + firstGroup.getId() + "\""));
        assertTrue(secondCard.group().contains("new?roundId=" + secondCourse.getId()));
        var create = requestAs(first, "/student/my-groups/new?roundId=" + secondCourse.getId(), null);
        assertEquals(200, create.statusCode());
        assertTrue(java.util.regex.Pattern.compile("value=\"" + secondCourse.getId() + "\"\\s+selected=\"selected\"").matcher(create.body()).find());
        var secondGroup = group(first, secondCourse);
        var updated = requestAs(first, "/student/my-groups", null);
        assertEquals(200, updated.statusCode());
        assertTrue(updated.body().contains("/student/my-groups/" + firstGroup.getId() + "\""));
        assertTrue(updated.body().contains("/student/my-groups/" + secondGroup.getId() + "\""));
        assertFalse(updated.body().contains("Tạo nhóm trong lớp này"));
    }

    @Test
    void closedWindowAndWrongRoundCannotBeBypassed() {
        var round = round(RegistrationMode.EXCLUSIVE, 3);
        var group = group(first, round);
        var elsewhere = publishedTopic(round(RegistrationMode.SHARED, 3));
        login(first);
        assertThrows(BusinessRuleException.class, () -> registrations.register(group.getId(), elsewhere.getId()));
        var topic = publishedTopic(round);
        round.getPeriod().setSvEnd(LocalDateTime.now().minusMinutes(1));
        windows.save(round.getPeriod());
        assertThrows(BusinessRuleException.class, () -> registrations.register(group.getId(), topic.getId()));
    }

    @Test
    void sharedTopicHasIndependentScoresReportsAndPublicationForEachGroup() {
        var round = round(RegistrationMode.SHARED, 3);
        round.getPeriod().setType(RoundType.NCKH); windows.save(round.getPeriod());
        var topic = publishedTopic(round);
        var a = group(first, round);
        var b = group(second, round);
        login(first);
        var ra = registrations.register(a.getId(), topic.getId());
        assertEquals(GroupTopicRegistrationStatus.APPROVED, ra.getStatus());
        login(second);
        var rb = registrations.register(b.getId(), topic.getId());
        var president = user(Role.LECTURER);
        var secretary = user(Role.LECTURER);
        var member = user(Role.LECTURER);
        var council = council(round, president, secretary, member);
        assign(ra, council, null);
        assign(rb, council, null);
        assertPageAs(dean, "/admin/councils/assignments?roundId=" + round.getId(), a.getName());
        assertPageAs(president, "/lecturer/councils/" + council.getId(), b.getName());
        assertPageAs(president, "/lecturer/evaluations", a.getName());
        assertPageAs(president, "/lecturer/evaluations/registration/" + ra.getId(), a.getName());
        assertPageAs(president, "/council/results/registration/" + ra.getId(), a.getName());
        login(president);
        assertThrows(BusinessRuleException.class, () -> evaluations.evaluate(ra.getId(), score(4), president.getEmail()));
        login(first);
        var report = submissions.upload(a.getId(), zipReport());
        login(second);
        submissions.upload(b.getId(), zipReport());
        assertThrows(AccessDeniedException.class, () -> submissions.download(a.getId(), report.getId()));
        login(president);
        assertNotNull(reports.download(a.getId(), report.getId()));
        evaluate(ra, president, 4); // 10
        evaluate(ra, secretary, 2); // 8
        evaluate(ra, member, 0); // 6 -> mean 8
        evaluate(rb, president, 1); // 7
        evaluate(rb, secretary, 1);
        evaluate(rb, member, 1);
        assertEquals(3, evaluationRepository.findByRegistration_Id(ra.getId()).size());
        login(first);
        assertThrows(BusinessRuleException.class, () -> submissions.upload(a.getId(), zipReport()));
        login(president);
        assertThrows(BusinessRuleException.class, () -> evaluations.evaluate(ra.getId(), score(3), president.getEmail()));
        assertEquals(new BigDecimal("8.00"), results.confirmByPresident(ra.getId(), "Passed", president.getEmail()).getFinalScore());
        assertEquals(new BigDecimal("7.00"), results.confirmByPresident(rb.getId(), "Passed", president.getEmail()).getFinalScore());
        login(dean);
        assertThrows(BusinessRuleException.class, () -> topicService.assignAdvisors(topic.getId(), List.of(member.getId())));
        assertThrows(BusinessRuleException.class, () -> councils.removeMember(council.getId(), member.getId()));
        results.publish(ra.getId());
        login(first);
        assertEquals(new BigDecimal("8.00"), results.getMyStudentResults(first.getEmail()).getFirst().result().getFinalScore());
        assertPageAs(first, "/student/my-results", "8.00");
        assertPageAs(dean, "/dean/results?roundId=" + round.getId(), a.getName());
        login(second);
        assertNull(results.getMyStudentResults(second.getEmail()).getFirst().result());
        login(dean);
        results.publish(rb.getId());
        login(second);
        assertEquals(new BigDecimal("7.00"), results.getMyStudentResults(second.getEmail()).getFirst().result().getFinalScore());
        var outsider = user(Role.LECTURER);
        login(outsider);
        assertThrows(AccessDeniedException.class, () -> results.assertCanView(ra.getId(), outsider.getEmail()));
        assertThrows(AccessDeniedException.class, () -> reports.download(a.getId(), report.getId()));
        login(teacher);
        assertThrows(AccessDeniedException.class, () -> evaluations.evaluate(ra.getId(), score(4), teacher.getEmail()));
    }

    @Test
    void incompleteCouncilAndAdvisorConflictAreRejected() {
        var round = round(RegistrationMode.SHARED, 3);
        round.getPeriod().setType(RoundType.NCKH); windows.save(round.getPeriod());
        var topic = publishedTopic(round);
        var group = group(first, round);
        login(first);
        var registration = registrations.register(group.getId(), topic.getId());
        submissions.upload(group.getId(), zipReport());
        var president = user(Role.LECTURER);
        var secretary = user(Role.LECTURER);
        var member = user(Role.LECTURER);
        var form = new CouncilForm();
        form.setName("Incomplete"); form.setRoundId(round.getId());
        login(dean);
        var incomplete = councils.create(form);
        assertThrows(BusinessRuleException.class, () -> assign(registration, incomplete, null));
        var council = council(round, president, secretary, member);
        assertThrows(BusinessRuleException.class, () -> assign(registration, council, teacher));
        assign(registration, council, null);
        login(president);
        assertThrows(BusinessRuleException.class, () -> results.confirmByPresident(registration.getId(), "", president.getEmail()));
    }

    @Test
    void studentResultsIncludeEveryRegisteredRound() {
        for (int i = 0; i < 2; i++) {
            var round = round(RegistrationMode.SHARED, 3);
            var topic = publishedTopic(round);
            var group = group(first, round);
            login(first);
            registrations.register(group.getId(), topic.getId());
        }
        assertEquals(2, results.getMyStudentResults(first.getEmail()).size());
    }

    @Test
    void emptyRoundCanBeDeletedInEveryUnfinishedStage() {
        for (int phase = 0; phase < 4; phase++) {
            var round = round(RegistrationMode.SHARED, 3);
            var start = LocalDateTime.now().plusHours(1 - phase * 2);
            round.getPeriod().setGvStart(start); round.getPeriod().setGvEnd(start.plusHours(2));
            round.getPeriod().setSvStart(start.plusHours(4)); round.getPeriod().setSvEnd(start.plusHours(6));
            windows.save(round.getPeriod());
            periods.delete(round.getId());
            assertFalse(periodRepository.existsById(round.getId()));
        }
    }

    @Test
    void deletingRoundNeverDeletesRelatedTopicsGroupsOrCouncils() {
        var withTopic = round(RegistrationMode.SHARED, 3);
        var topic = publishedTopic(withTopic);
        assertThrows(BusinessRuleException.class, () -> periods.delete(withTopic.getId()));
        assertTrue(topics.existsById(topic.getId()));
        var withGroup = round(RegistrationMode.SHARED, 3);
        var group = group(first, withGroup);
        login(teacher);
        assertThrows(BusinessRuleException.class, () -> periods.delete(withGroup.getId()));
        assertNotNull(groups.get(group.getId()));
        var withCouncil = round(RegistrationMode.SHARED, 3);
        var form = new CouncilForm(); form.setName("Keep council"); form.setRoundId(withCouncil.getId());
        login(dean);
        var council = councils.create(form);
        assertThrows(BusinessRuleException.class, () -> periods.delete(withCouncil.getId()));
        assertNotNull(councils.get(council.getId()));
    }

    @Test
    void joinCodeControlsMembershipAndCannotBypassTheClassOrWindow() throws Exception {
        var course = round(RegistrationMode.SHARED, 3);
        String original = course.getJoinCode();
        login(first);
        var form = new StudentGroupForm(); form.setName("No membership"); form.setRoundId(course.getId());
        assertThrows(BusinessRuleException.class, () -> groups.createMyGroup(form));
        assertTrue(groups.findOpenStudentPeriods().isEmpty());
        assertThrows(BusinessRuleException.class, () -> periods.join("not-a-code"));
        periods.join(" " + original.toLowerCase() + " ");
        periods.join(original);
        assertEquals(1, periods.findJoinedCourses().size());
        var group = groups.createMyGroup(form);
        assertThrows(BusinessRuleException.class, () -> groups.addMemberByLeader(group.getId(), second.getEmail()));
        login(teacher);
        assertThrows(BusinessRuleException.class, () -> groups.addMember(group.getId(), second.getEmail()));
        login(user(Role.LECTURER));
        assertThrows(AccessDeniedException.class, () -> periods.updateJoinCode(course.getId(), true, true));
        login(teacher);
        periods.updateJoinCode(course.getId(), true, true);
        String replacement = periods.get(course.getId()).getJoinCode();
        assertNotEquals(original, replacement);
        login(second);
        assertThrows(BusinessRuleException.class, () -> periods.join(original));
        periods.join(replacement);
        login(teacher); periods.updateJoinCode(course.getId(), false, false);
        login(user(Role.STUDENT));
        assertThrows(BusinessRuleException.class, () -> periods.join(replacement));
        login(first);
        groups.addMemberByLeader(group.getId(), second.getEmail()); // Closing the code does not remove participants.
        login(second); groups.respondToInvitation(group.getId(), true); login(first);
        assertEquals(2, groups.getMyGroupMembers(group.getId()).size());
        assertPageAs(first, "/student/my-groups", "Tham gia lớp đề tài");
        assertPageAs(teacher, "/course-registrations/" + course.getId(), replacement);
        login(teacher); periods.updateJoinCode(course.getId(), false, true);
        course.getPeriod().setSvEnd(LocalDateTime.now().minusMinutes(1)); windows.save(course.getPeriod());
        login(user(Role.STUDENT));
        assertThrows(BusinessRuleException.class, () -> periods.join(replacement));
    }

    @Test
    void concurrentJoinIsIdempotent() throws Exception {
        var course = round(RegistrationMode.SHARED, 3);
        var outcomes = race(() -> { join(first, course); return true; }, () -> { join(first, course); return true; });
        assertTrue(outcomes.stream().allMatch(Boolean::booleanValue));
        login(first);
        assertEquals(1, periods.findJoinedCourses().size());
    }

    @Test
    void courseAssessmentUsesOneIndependentLecturerAndDeanPublishesWithoutCouncil() {
        var course = round(RegistrationMode.SHARED, 3);
        var reg = registeredReport(course, first);
        var grader = user(Role.LECTURER);
        assertThrows(BusinessRuleException.class, () -> assign(reg, null, teacher));
        assign(reg, null, grader);
        assertPageAs(dean, "/admin/councils/assignments?roundId=" + course.getId(), "Phân công đánh giá");
        assertPageAs(grader, "/lecturer/evaluations", "Giảng viên chấm môn học");
        login(dean);
        assertThrows(BusinessRuleException.class, () -> results.publish(reg.getId()));
        evaluate(reg, grader, 2);
        login(grader);
        assertThrows(AccessDeniedException.class, () -> results.publish(reg.getId()));
        assertThrows(AccessDeniedException.class, () -> results.confirmByPresident(reg.getId(), "", grader.getEmail()));
        assertPageAs(dean, "/dean/results?roundId=" + course.getId(), "Xác nhận và công bố");
        assertPageAs(dean, "/council/results/registration/" + reg.getId(), "8.00");
        login(first); assertNull(results.getMyStudentResults(first.getEmail()).getFirst().result());
        login(dean);
        var result = results.publish(reg.getId());
        assertNull(result.getCouncil()); assertEquals(new BigDecimal("8.00"), result.getFinalScore());
        results.revoke(reg.getId());
        login(first); assertNull(results.getMyStudentResults(first.getEmail()).getFirst().result());
        login(dean); results.publish(reg.getId());
        assertPageAs(first, "/student/my-results", "8.00");
    }

    @Test
    void tlcnRequiresSupporterThenReviewerAndAveragesBothScores() {
        var course = round(RegistrationMode.SHARED, 3);
        course.getPeriod().setType(RoundType.TLCN);
        course.getPeriod().setGvpbDeadline(LocalDateTime.now().plusDays(5)); windows.save(course.getPeriod());
        var reg = registeredReport(course, first);
        var reviewer = user(Role.LECTURER);
        var form = new TopicAssignmentForm(); form.setRegistrationId(reg.getId()); form.setReviewerId(reviewer.getId());
        login(dean);
        assertThrows(BusinessRuleException.class, () -> councils.assignTopic(form));
        form.setSupporterId(reviewer.getId());
        assertThrows(BusinessRuleException.class, () -> councils.assignTopic(form));
        form.setSupporterId(teacher.getId()); councils.assignTopic(form);
        assertPageAs(teacher, "/lecturer/evaluations", reg.getGroup().getName());
        assertPageAs(dean, "/admin/councils/assignments?roundId=" + course.getId(), "supporterId");
        login(reviewer);
        assertThrows(BusinessRuleException.class, () -> evaluations.evaluate(reg.getId(), score(4), reviewer.getEmail()));
        login(teacher);
        var draft = score(2); draft.setAction("draft");
        evaluations.evaluate(reg.getId(), draft, teacher.getEmail());
        login(reviewer);
        assertThrows(BusinessRuleException.class, () -> evaluations.evaluate(reg.getId(), score(4), reviewer.getEmail()));
        evaluate(reg, teacher, 2);
        login(dean);
        assertThrows(BusinessRuleException.class, () -> results.publish(reg.getId()));
        evaluate(reg, reviewer, 4);
        login(dean);
        var result = results.publish(reg.getId());
        assertEquals(new BigDecimal("8.00"), result.getSupporterScore());
        assertEquals(new BigDecimal("10.00"), result.getReviewerScore());
        assertEquals(new BigDecimal("9.00"), result.getFinalScore());
        assertNull(result.getCouncil());
        assertPageAs(dean, "/council/results/registration/" + reg.getId(), "GV hỗ trợ");
        assertPageAs(first, "/student/my-results", "9.00");
    }

    @Test
    void tlcnReviewerDeadlineStillAppliesAfterSupporterSubmits() {
        var course = round(RegistrationMode.SHARED, 3);
        course.getPeriod().setType(RoundType.TLCN);
        course.getPeriod().setGvpbDeadline(LocalDateTime.now().minusMinutes(1)); windows.save(course.getPeriod());
        var reg = registeredReport(course, first);
        var reviewer = user(Role.LECTURER);
        login(dean); var form = new TopicAssignmentForm(); form.setRegistrationId(reg.getId());
        form.setReviewerId(reviewer.getId()); form.setSupporterId(teacher.getId()); councils.assignTopic(form);
        evaluate(reg, teacher, 3);
        login(reviewer);
        assertThrows(BusinessRuleException.class, () -> evaluations.evaluate(reg.getId(), score(4), reviewer.getEmail()));
    }

    @Test
    void kltnReviewerMustExplicitlyApproveBeforeCouncilAndUsesTwoComponentAverage() {
        var course = round(RegistrationMode.SHARED, 3);
        course.getPeriod().setType(RoundType.KLTN);
        course.getPeriod().setGvpbDeadline(LocalDateTime.now().plusDays(5)); windows.save(course.getPeriod());
        var reg = registeredReport(course, first);
        var reviewer = user(Role.LECTURER);
        var president = user(Role.LECTURER); var secretary = user(Role.LECTURER); var member = user(Role.LECTURER);
        var council = council(course, president, secretary, member);
        assertThrows(BusinessRuleException.class, () -> assign(reg, council, reviewer));
        assign(reg, null, reviewer);
        login(reviewer);
        assertThrows(BusinessRuleException.class, () -> evaluations.evaluate(reg.getId(), score(4), reviewer.getEmail()));
        var review = score(4); review.setDefenseAllowed(true); review.setAction("draft");
        evaluations.evaluate(reg.getId(), review, reviewer.getEmail());
        assertThrows(BusinessRuleException.class, () -> assign(reg, council, reviewer));
        login(reviewer); review.setAction("submit"); evaluations.evaluate(reg.getId(), review, reviewer.getEmail());
        login(dean); assertThrows(BusinessRuleException.class, () -> results.publish(reg.getId()));
        var conflicting = council(course, reviewer, secretary, member);
        assertThrows(BusinessRuleException.class, () -> assign(reg, conflicting, reviewer));
        assign(reg, council, reviewer); // Adding the council must work after the reviewer score is locked.
        assertThrows(BusinessRuleException.class, () -> assign(reg, council, president));
        evaluate(reg, president, 0); evaluate(reg, secretary, 0);
        login(president);
        assertThrows(BusinessRuleException.class, () -> results.confirmByPresident(reg.getId(), "Not yet", president.getEmail()));
        evaluate(reg, member, 0);
        login(dean); assertThrows(BusinessRuleException.class, () -> results.publish(reg.getId()));
        login(president);
        var result = results.confirmByPresident(reg.getId(), "Defense complete", president.getEmail());
        assertEquals(new BigDecimal("8.00"), result.getFinalScore()); // (10 + mean(6,6,6)) / 2; not mean(10,6,6,6).
        login(dean); results.publish(reg.getId());
        assertPageAs(first, "/student/my-results", "8.00");
        assertPageAs(reviewer, "/lecturer/evaluations/registration/" + reg.getId(), "Được bảo vệ trước hội đồng");
    }

    @Test
    void kltnRefusalKeepsReviewerScoreButPublishesZeroOnlyAfterDeanConfirmation() {
        var course = round(RegistrationMode.SHARED, 3);
        course.getPeriod().setType(RoundType.KLTN);
        course.getPeriod().setGvpbDeadline(LocalDateTime.now().plusDays(5)); windows.save(course.getPeriod());
        var reg = registeredReport(course, first);
        var reviewer = user(Role.LECTURER);
        assign(reg, null, reviewer);
        var form = score(1); form.setDefenseAllowed(false);
        login(reviewer);
        assertThrows(BusinessRuleException.class, () -> evaluations.evaluate(reg.getId(), form, reviewer.getEmail()));
        form.setComments("Missing required research results");
        evaluations.evaluate(reg.getId(), form, reviewer.getEmail());
        login(first); assertNull(results.getMyStudentResults(first.getEmail()).getFirst().result());
        var council = council(course, user(Role.LECTURER), user(Role.LECTURER), user(Role.LECTURER));
        assertThrows(BusinessRuleException.class, () -> assign(reg, council, reviewer));
        login(dean);
        var result = results.publish(reg.getId());
        assertEquals(new BigDecimal("0.00"), result.getFinalScore());
        assertEquals(new BigDecimal("7.00"), result.getReviewerScore());
        assertFalse(result.getDefenseAllowed());
        assertNull(result.getCouncil());
        assertPageAs(first, "/student/my-results", "Missing required research results");
        assertPageAs(dean, "/dean/results?roundId=" + course.getId(), "0.00");
    }

    @Test
    void existingAssessmentKeepsItsOriginalAverageAndCanStillFinish() {
        var course = round(RegistrationMode.SHARED, 3);
        course.getPeriod().setType(RoundType.TLCN);
        course.getPeriod().setGvpbDeadline(LocalDateTime.now().plusDays(5)); windows.save(course.getPeriod());
        var reg = registeredReport(course, first);
        var president = user(Role.LECTURER); var secretary = user(Role.LECTURER); var member = user(Role.LECTURER);
        var council = council(course, president, secretary, member);
        var old = new TopicAssignment(); old.setRegistration(reg); old.setCouncil(council);
        old.setReviewer(president); old.setLegacyAssessment(true); assignmentRepository.save(old);
        evaluate(reg, president, 4); evaluate(reg, secretary, 2); evaluate(reg, member, 0);
        login(president);
        assertEquals(new BigDecimal("8.00"), results.confirmByPresident(reg.getId(), "Legacy average", president.getEmail()).getFinalScore());
        login(dean);
        results.publish(reg.getId());
        results.revoke(reg.getId()); results.publish(reg.getId());
        assertEquals(new BigDecimal("8.00"), results.getSummaryView(reg.getId()).finalScore());
        assertPageAs(dean, "/council/results/registration/" + reg.getId(), "Hồ sơ phân công trước nâng cấp");
    }

    @Test
    void joinAndCodeControlsWorkThroughHttpAndEnforceRoles() {
        var course = round(RegistrationMode.SHARED, 3);
        var joinPath = "/student/my-groups/join";
        var response = requestAs(first, joinPath, java.util.Map.of("code", course.getJoinCode()));
        assertEquals(200, response.statusCode()); assertTrue(response.body().contains("Đã tham gia lớp"));
        login(first); assertEquals(1, periods.findJoinedCourses().size());
        assertEquals(403, requestAs(teacher, joinPath, java.util.Map.of("code", course.getJoinCode())).statusCode());
        String codePath = "/course-registrations/" + course.getId() + "/join-code";
        assertEquals(403, requestAs(first, codePath, java.util.Map.of("renew", "true")).statusCode());
        assertEquals(200, requestAs(teacher, codePath, java.util.Map.of("enabled", "false")).statusCode());
        assertTrue(requestAs(second, joinPath, java.util.Map.of("code", course.getJoinCode())).body().contains("Mã tham gia đã đóng"));
    }

    @Test
    void kltnFormsBindDecisionAndDeanPublishesRejectedDefenseThroughHttp() {
        var course = round(RegistrationMode.SHARED, 3);
        course.getPeriod().setType(RoundType.KLTN);
        course.getPeriod().setGvpbDeadline(LocalDateTime.now().plusDays(5)); windows.save(course.getPeriod());
        var reg = registeredReport(course, first); var reviewer = user(Role.LECTURER);
        var assignment = java.util.Map.of("registrationId", reg.getId().toString(), "reviewerId", reviewer.getId().toString(),
                "roundId", course.getId().toString());
        var assigned = requestAs(dean, "/admin/councils/assignments", assignment);
        assertEquals(200, assigned.statusCode()); assertTrue(assigned.body().contains("Đã lưu phân công"));
        String path = "/lecturer/evaluations/registration/" + reg.getId();
        var form = new java.util.HashMap<String,String>(java.util.Map.of("scoreContent", "2", "scorePresentation", "2",
                "scoreQa", "2", "action", "submit", "defenseAllowed", "", "comments", "Research incomplete"));
        var missing = requestAs(reviewer, path, form);
        assertEquals(200, missing.statusCode()); assertTrue(missing.body().contains("Vui lòng quyết định"));
        form.put("defenseAllowed", "false");
        var submitted = requestAs(reviewer, path, form);
        assertEquals(200, submitted.statusCode()); assertTrue(submitted.body().contains("Đã xác nhận nộp"));
        String publish = "/dean/results/registration/" + reg.getId() + "/publish";
        assertEquals(403, requestAs(reviewer, publish, java.util.Map.of()).statusCode());
        var published = requestAs(dean, publish, java.util.Map.of("roundId", course.getId().toString()));
        assertEquals(200, published.statusCode()); assertTrue(published.body().contains("0.00"));
        assertPageAs(first, "/student/my-results", "Research incomplete");
    }

    @Test
    void courseIdentityCannotChangeOnceStudentsJoinedOrTopicsExist() {
        var joined = round(RegistrationMode.SHARED, 3);
        join(first, joined);
        login(dean);
        var form = periods.toForm(joined); form.setClassCode("Another class");
        assertThrows(BusinessRuleException.class, () -> periods.update(joined.getId(), form));
        form.setClassCode(joined.getClassCode()); form.setMaxMembers(5);
        assertThrows(BusinessRuleException.class, () -> periods.update(joined.getId(), form));
        assertEquals(joined.getClassCode(), periods.get(joined.getId()).getClassCode());
        var withTopic = round(RegistrationMode.SHARED, 3);
        publishedTopic(withTopic);
        login(dean);
        var changed = periods.toForm(withTopic); changed.setLecturerId(user(Role.LECTURER).getId());
        assertThrows(BusinessRuleException.class, () -> periods.update(withTopic.getId(), changed));
        assertEquals(teacher.getId(), periods.get(withTopic.getId()).getLecturer().getId());
        assertNotNull(periods.update(withTopic.getId(), periods.toForm(withTopic)));
    }

    @Test
    void activeCourseAndGroupResponsibilitiesPreventDisablingOrChangingRole() {
        var course = round(RegistrationMode.SHARED, 3);
        login(dean);
        assertThrows(BusinessRuleException.class, () -> accounts.toggle(teacher.getId(), dean.getEmail()));
        var form = accounts.toForm(teacher); form.setRole(Role.STUDENT);
        assertThrows(BusinessRuleException.class, () -> accounts.update(teacher.getId(), form, dean.getEmail()));
        var group = group(first, course);
        var reg = registrations.register(group.getId(), publishedTopic(course).getId());
        var reviewer = user(Role.LECTURER);
        assign(reg, null, reviewer);
        login(dean);
        assertThrows(BusinessRuleException.class, () -> accounts.toggle(first.getId(), dean.getEmail()));
        assertThrows(BusinessRuleException.class, () -> accounts.toggle(reviewer.getId(), dean.getEmail()));
        var disabled = accounts.toForm(reviewer); disabled.setEnabled(false);
        assertThrows(BusinessRuleException.class, () -> accounts.update(reviewer.getId(), disabled, dean.getEmail()));
        assertTrue(users.findById(reviewer.getId()).orElseThrow().isEnabled());
        assertEquals(0, users.findById(reviewer.getId()).orElseThrow().getAuthVersion());
        // Editing ordinary profile fields remains allowed.
        disabled.setEnabled(true); disabled.setFullName("Updated reviewer");
        accounts.update(reviewer.getId(), disabled, dean.getEmail());
        assertEquals("Updated reviewer", accounts.get(reviewer.getId()).getFullName());
    }

    @Test
    void councilMembersAreProtectedUntilResultsAreFinalAndHistoricalResultsRemainReadable() {
        var course = round(RegistrationMode.SHARED, 3);
        course.getPeriod().setType(RoundType.NCKH); windows.save(course.getPeriod());
        var reg = registeredReport(course, first);
        var president = user(Role.LECTURER); var secretary = user(Role.LECTURER); var member = user(Role.LECTURER);
        var council = council(course, president, secretary, member);
        assign(reg, council, null);
        evaluate(reg, president, 4);
        login(dean);
        assertThrows(BusinessRuleException.class, () -> accounts.toggle(member.getId(), dean.getEmail()));
        var changed = accounts.toForm(secretary); changed.setRole(Role.STUDENT);
        assertThrows(BusinessRuleException.class, () -> accounts.update(secretary.getId(), changed, dean.getEmail()));
        evaluate(reg, secretary, 2); evaluate(reg, member, 0);
        login(president); results.confirmByPresident(reg.getId(), "Finished", president.getEmail());
        login(dean);
        accounts.toggle(member.getId(), dean.getEmail());
        accounts.update(secretary.getId(), changed, dean.getEmail());
        assertEquals(new BigDecimal("8.00"), results.getSummaryView(reg.getId()).finalScore());
        results.publish(reg.getId());
        assertPageAs(dean, "/dean/results?roundId=" + course.getId(), "8.00");
        assertPageAs(first, "/student/my-results", "8.00");
    }

    GroupTopicRegistration registeredReport(CourseRegistration course, UserAccount student) {
        var topic = publishedTopic(course);
        var group = group(student, course);
        var reg = registrations.register(group.getId(), topic.getId());
        submissions.upload(group.getId(), zipReport());
        return reg;
    }

    @Test
    void invitationsRequireConsentAndMustBeResolvedBeforeClaimingTopic() {
        var course=round(RegistrationMode.EXCLUSIVE, 3);
        var topic=publishedTopic(course);
        var group=group(first, course);
        join(second, course); login(first);
        groups.addMemberByLeader(group.getId(), second.getEmail());
        groups.addMemberByLeader(group.getId(), second.getEmail());
        assertEquals(1, groups.getMyGroupMembers(group.getId()).size());
        assertEquals(1, groups.findInvitations(group.getId()).size());
        assertThrows(BusinessRuleException.class, () -> registrations.register(group.getId(), topic.getId()));
        login(user(Role.STUDENT));
        assertThrows(vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException.class,
                () -> groups.respondToInvitation(group.getId(), true));
        assertThrows(AccessDeniedException.class, () -> groups.cancelInvitation(group.getId(), second.getId()));
        login(second);
        assertTrue(groups.findMyGroups().isEmpty());
        assertEquals(1, groups.findMyInvitations().size());
        groups.respondToInvitation(group.getId(), false);
        assertTrue(groups.findMyInvitations().isEmpty());
        login(first); groups.addMemberByLeader(group.getId(), second.getEmail());
        groups.cancelInvitation(group.getId(), second.getId());
        assertNotNull(registrations.register(group.getId(), topic.getId()));
        login(second);
        assertThrows(vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException.class,
                () -> groups.respondToInvitation(group.getId(), true));
    }

    @Test
    void concurrentInvitationAcceptanceCannotJoinTwoGroupsInTheSameClass() throws Exception {
        var course=round(RegistrationMode.SHARED, 3);
        var a=group(first, course); var b=group(second, course);
        var invitee=user(Role.STUDENT); join(invitee, course);
        login(first); groups.addMemberByLeader(a.getId(), invitee.getEmail());
        login(second); groups.addMemberByLeader(b.getId(), invitee.getEmail());
        var outcomes=race(() -> { login(invitee); groups.respondToInvitation(a.getId(), true); return true; },
                () -> { login(invitee); groups.respondToInvitation(b.getId(), true); return true; });
        assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
        login(invitee); assertEquals(1, groups.findMyGroups().size());
    }

    @Test
    void expiredInvitationCanBeDeclinedButCannotAddMember() {
        var course=round(RegistrationMode.SHARED, 3); var group=group(first, course);
        join(second, course); login(first); groups.addMemberByLeader(group.getId(), second.getEmail());
        course.getPeriod().setSvEnd(LocalDateTime.now().minusMinutes(1)); windows.save(course.getPeriod());
        login(second);
        assertThrows(BusinessRuleException.class, () -> groups.respondToInvitation(group.getId(), true));
        groups.respondToInvitation(group.getId(), false);
        assertTrue(groups.findMyInvitations().isEmpty());
    }

    @Test
    void lecturerCanTransferLeaderAfterTopicClaimWithoutChangingRosterOrReports() {
        var course=round(RegistrationMode.SHARED, 3); var topic=publishedTopic(course);
        var group=group(first, course); join(second, course);
        login(first); groups.addMemberByLeader(group.getId(), second.getEmail());
        login(second); groups.respondToInvitation(group.getId(), true);
        login(first); var reg=registrations.register(group.getId(), topic.getId());
        var original=submissions.upload(group.getId(), zipReport());
        login(user(Role.LECTURER));
        assertThrows(AccessDeniedException.class, () -> groups.assignLeader(group.getId(), second.getId()));
        login(teacher); groups.assignLeader(group.getId(), second.getId());
        assertEquals(2, groups.getMembers(group.getId()).size());
        assertThrows(BusinessRuleException.class, () -> groups.removeMember(group.getId(), first.getId()));
        assertEquals(1, handoverHistory.findByCourse(course.getId()).size());
        login(first); assertFalse(groups.isMyGroupLeader(group.getId()));
        assertThrows(AccessDeniedException.class, () -> submissions.upload(group.getId(), zipReport()));
        login(second); assertTrue(groups.isMyGroupLeader(group.getId()));
        assertNotNull(submissions.download(group.getId(), original.getId()));
        submissions.upload(group.getId(), zipReport());
        var reviewer=user(Role.LECTURER); assign(reg, null, reviewer); evaluate(reg, reviewer, 4);
        login(dean); results.publish(reg.getId());
        login(teacher); assertThrows(BusinessRuleException.class, () -> groups.assignLeader(group.getId(), first.getId()));
    }

    @Test
    void courseHandoverRequiresDeanAndReasonAndKeepsAdvisorsSeparate() {
        var course=round(RegistrationMode.SHARED, 3); var topic=publishedTopic(course);
        var replacement=user(Role.LECTURER);
        login(teacher);
        assertThrows(AccessDeniedException.class, () -> transfer(course, "COURSE", course.getId(), teacher, replacement));
        login(dean);
        assertThrows(BusinessRuleException.class, () -> handovers.transfer(course.getId(),
                vn.edu.fit.topicmanagement.handover.AcademicHandoverService.Duty.COURSE,
                course.getId(), teacher.getId(), replacement.getId(), " "));
        assertEquals(teacher.getId(), periods.get(course.getId()).getLecturer().getId());
        assertTrue(handoverHistory.findByCourse(course.getId()).isEmpty());
        transfer(course, "COURSE", course.getId(), teacher, replacement);
        assertEquals(replacement.getId(), periods.get(course.getId()).getLecturer().getId());
        assertTrue(topics.findById(topic.getId()).orElseThrow().getAdvisors().stream().anyMatch(u -> u.getId().equals(teacher.getId())));
        topic.setStatus(TopicStatus.DRAFT); topics.save(topic);
        login(teacher); assertThrows(BusinessRuleException.class, () -> topicService.getEditable(topic.getId(),teacher.getEmail()));
        assertThrows(AccessDeniedException.class, () -> periods.updateJoinCode(course.getId(), true, true));
        login(replacement); assertEquals(topic.getId(),topicService.getEditable(topic.getId(),replacement.getEmail()).getId());
        assertTrue(topicService.search(course.getId(),null,null,replacement.getEmail(),0).getContent().stream().anyMatch(t -> t.getId().equals(topic.getId())));
        assertPageAs(replacement,"/topics/"+topic.getId()+"/edit",topic.getTitle());
        login(replacement); periods.updateJoinCode(course.getId(), true, true);
        assertPageAs(dean, "/admin/handovers?courseId="+course.getId(), "Test handover");
        assertEquals(403, requestAs(teacher, "/admin/handovers?courseId="+course.getId(), null).statusCode());
    }

    @Test
    void reviewerHandoverKeepsOldDraftAndNewLecturerCanFinishAndPublish() {
        var course=round(RegistrationMode.SHARED, 3); var topic=publishedTopic(course);
        var group=group(first, course); var reg=registrations.register(group.getId(), topic.getId());
        submissions.upload(group.getId(), zipReport());
        var old=user(Role.LECTURER); var replacement=user(Role.LECTURER); assign(reg,null,old);
        var draft=score(0); draft.setAction("draft"); login(old);
        evaluations.evaluate(reg.getId(),draft,old.getEmail());
        login(dean);
        assertThrows(BusinessRuleException.class, () -> transfer(course,"REVIEWER",reg.getId(),old,teacher));
        var another=round(RegistrationMode.SHARED,3); login(dean);
        assertThrows(BusinessRuleException.class, () -> transfer(another,"REVIEWER",reg.getId(),old,replacement));
        transfer(course,"REVIEWER",reg.getId(),old,replacement);
        assertEquals(EvaluationStatus.DRAFT, evaluationRepository.findByRegistration_IdAndEvaluator_Id(reg.getId(),old.getId()).orElseThrow().getStatus());
        login(old); assertThrows(AccessDeniedException.class, () -> evaluations.evaluate(reg.getId(),score(4),old.getEmail()));
        evaluate(reg,replacement,4); login(dean);
        assertEquals(new BigDecimal("10.00"),results.getSummaryView(reg.getId()).finalScore());
        assertThrows(BusinessRuleException.class, () -> transfer(course,"REVIEWER",reg.getId(),replacement,user(Role.LECTURER)));
        results.publish(reg.getId()); login(first);
        assertEquals(new BigDecimal("10.00"),results.getMyStudentResults(first.getEmail()).getFirst().result().getFinalScore());
    }

    @Test
    void advisorHandoverAlsoTransfersUnsubmittedTlcnSupporterAndPreservesOrder() {
        var course=round(RegistrationMode.SHARED,3);
        course.getPeriod().setType(RoundType.TLCN); course.getPeriod().setGvpbDeadline(LocalDateTime.now().plusDays(3)); windows.save(course.getPeriod());
        var topic=publishedTopic(course); var group=group(first,course);
        var reg=registrations.register(group.getId(),topic.getId()); submissions.upload(group.getId(),zipReport());
        var reviewer=user(Role.LECTURER); var replacement=user(Role.LECTURER);
        login(dean); var form=new TopicAssignmentForm(); form.setRegistrationId(reg.getId());
        form.setReviewerId(reviewer.getId()); form.setSupporterId(teacher.getId()); councils.assignTopic(form);
        login(teacher); var draft=score(0); draft.setAction("draft"); evaluations.evaluate(reg.getId(),draft,teacher.getEmail());
        login(dean); transfer(course,"ADVISOR",topic.getId(),teacher,replacement);
        assertEquals(replacement.getId(),councils.getAssignment(reg.getId()).getSupporter().getId());
        login(reviewer); assertThrows(BusinessRuleException.class, () -> evaluations.evaluate(reg.getId(),score(4),reviewer.getEmail()));
        evaluate(reg,replacement,2); evaluate(reg,reviewer,4); login(dean);
        assertEquals(new BigDecimal("9.00"),results.publish(reg.getId()).getFinalScore());
        assertThrows(BusinessRuleException.class, () -> transfer(course,"ADVISOR",topic.getId(),replacement,teacher));
    }

    @Test
    void councilHandoverIsPerGroupAndPreservesOtherGroupsAndSubmittedScores() {
        var course=round(RegistrationMode.SHARED,3); course.getPeriod().setType(RoundType.NCKH); windows.save(course.getPeriod());
        var topic=publishedTopic(course); var a=group(first,course);
        var ra=registrations.register(a.getId(),topic.getId()); submissions.upload(a.getId(),zipReport());
        var b=group(second,course); var rb=registrations.register(b.getId(),topic.getId()); submissions.upload(b.getId(),zipReport());
        var president=user(Role.LECTURER); var secretary=user(Role.LECTURER); var old=user(Role.LECTURER);
        var replacement=user(Role.LECTURER); var council=council(course,president,secretary,old);
        assign(ra,council,null); assign(rb,council,null);
        evaluate(ra,president,4); evaluate(ra,secretary,4); evaluate(rb,old,1);
        login(old); var draft=score(0); draft.setAction("draft"); evaluations.evaluate(ra.getId(),draft,old.getEmail());
        login(dean); transfer(course,"COUNCIL",ra.getId(),old,replacement);
        assertNotEquals(council.getId(),councils.getAssignment(ra.getId()).getCouncil().getId());
        assertEquals(council.getId(),councils.getAssignment(rb.getId()).getCouncil().getId());
        assertEquals(new BigDecimal("7.00"),evaluationRepository.findByRegistration_IdAndEvaluator_Id(rb.getId(),old.getId()).orElseThrow().getTotalScore());
        assertThrows(BusinessRuleException.class, () -> transfer(course,"COUNCIL",rb.getId(),old,replacement));
        evaluate(ra,replacement,4); login(president); results.confirmByPresident(ra.getId(),"Complete",president.getEmail());
        login(dean); assertEquals(new BigDecimal("10.00"),results.publish(ra.getId()).getFinalScore());
        assertPageAs(dean,"/admin/handovers?courseId="+course.getId(),"Test handover");
    }

    @Test
    void invitationsAndHandoversWorkOverHttpAndEnforceRoles() {
        var course=round(RegistrationMode.SHARED,3); var group=group(first,course); join(second,course);
        var invite=requestAs(first,"/student/my-groups/"+group.getId()+"/members",java.util.Map.of("email",second.getEmail()));
        assertEquals(200,invite.statusCode()); assertTrue(invite.body().contains("Lời mời đang chờ"));
        assertPageAs(second,"/student/my-groups","Đồng ý tham gia");
        var accept=requestAs(second,"/student/my-groups/"+group.getId()+"/invitations/respond",java.util.Map.of("accept","true"));
        assertEquals(200,accept.statusCode()); assertTrue(accept.body().contains("Đã tham gia nhóm"));
        var replacement=user(Role.LECTURER);
        var fields=java.util.Map.of("courseId",course.getId().toString(),"duty","COURSE","targetId",course.getId().toString(),
                "previousId",teacher.getId().toString(),"replacementId",replacement.getId().toString(),"reason","HTTP handover");
        assertEquals(403,requestAs(teacher,"/admin/handovers",fields).statusCode());
        var response=requestAs(dean,"/admin/handovers",fields);
        assertEquals(200,response.statusCode()); assertTrue(response.body().contains("HTTP handover"));
        assertEquals(replacement.getId(),periods.get(course.getId()).getLecturer().getId());
    }

    void transfer(CourseRegistration course,String duty,Long targetId,UserAccount previous,UserAccount replacement) {
        handovers.transfer(course.getId(),vn.edu.fit.topicmanagement.handover.AcademicHandoverService.Duty.valueOf(duty),
                targetId,previous.getId(),replacement.getId(),"Test handover");
    }

    UserAccount user(Role role) {
        var user = new UserAccount();
        user.setEmail(UUID.randomUUID() + "@example.test");
        user.setFullName(role.name());
        user.setPasswordHash("{noop}TestPassword123!");
        user.setRole(role); user.setEnabled(true);
        return users.save(user);
    }

    void login(UserAccount user) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), "unused",
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
        SecurityContextHolder.setContext(context);
    }

    CourseRegistration round(RegistrationMode mode, int limit) {
        login(teacher);
        var window = new RegistrationPeriod();
        window.setName("Round " + UUID.randomUUID()); window.setType(RoundType.MON_HOC);
        window.setGvStart(LocalDateTime.now().minusDays(1)); window.setGvEnd(LocalDateTime.now().plusDays(1));
        window.setSvStart(LocalDateTime.now().plusDays(2)); window.setSvEnd(LocalDateTime.now().plusDays(3));
        windows.save(window);
        var form = new CourseRegistrationForm();
        form.setPeriodId(window.getId());
        form.setCourseName("Web programming"); form.setClassCode(UUID.randomUUID().toString());
        form.setDepartmentId(department.getId()); form.setMaxMembers(limit); form.setRegistrationMode(mode);
        var course = periods.create(form);
        window.setGvStart(LocalDateTime.now().minusDays(4)); window.setGvEnd(LocalDateTime.now().minusDays(2));
        window.setSvStart(LocalDateTime.now().minusDays(1)); window.setSvEnd(LocalDateTime.now().plusDays(1));
        course.setPeriod(windows.save(window));
        return course;
    }

    void join(UserAccount student, CourseRegistration course) {
        login(student);
        periods.join(course.getJoinCode());
    }

    StudentGroup group(UserAccount student, CourseRegistration round) {
        login(student);
        periods.join(round.getJoinCode());
        var form = new StudentGroupForm();
        form.setName("Group " + UUID.randomUUID()); form.setRoundId(round.getId());
        return groups.createMyGroup(form);
    }

    Topic publishedTopic(CourseRegistration round) {
        var topic = new Topic();
        topic.setTitle("Project " + UUID.randomUUID()); topic.setDescription("Test project scope");
        topic.setCreatedBy(teacher); topic.setAdvisors(Set.of(teacher));
        topic.setRound(round); topic.setDepartment(department); topic.setStatus(TopicStatus.PUBLISHED);
        return topics.save(topic);
    }

    Council council(CourseRegistration round, UserAccount president, UserAccount secretary, UserAccount member) {
        login(dean);
        var form = new CouncilForm(); form.setName("Council " + UUID.randomUUID()); form.setRoundId(round.getId());
        var council = councils.create(form);
        var lecturers = List.of(president, secretary, member);
        var roles = List.of(CouncilRole.PRESIDENT, CouncilRole.SECRETARY, CouncilRole.MEMBER);
        for (int i = 0; i < 3; i++) {
            var membership = new CouncilMemberForm();
            membership.setLecturerId(lecturers.get(i).getId()); membership.setRole(roles.get(i));
            councils.addOrUpdateMember(council.getId(), membership);
        }
        return councils.get(council.getId());
    }

    void assign(GroupTopicRegistration registration, Council council, UserAccount reviewer) {
        login(dean);
        var form = new TopicAssignmentForm(); form.setRegistrationId(registration.getId());
        form.setCouncilId(council == null ? null : council.getId()); form.setReviewerId(reviewer == null ? null : reviewer.getId());
        councils.assignTopic(form);
    }

    EvaluationForm score(int content) {
        var form = new EvaluationForm(); form.setScoreContent(BigDecimal.valueOf(content));
        form.setScorePresentation(BigDecimal.valueOf(3)); form.setScoreQa(BigDecimal.valueOf(3));
        form.setAction("submit"); return form;
    }

    void evaluate(GroupTopicRegistration registration, UserAccount lecturer, int content) {
        login(lecturer);
        evaluations.evaluate(registration.getId(), score(content), lecturer.getEmail());
    }

    @Test
    void courseLecturerDownloadsZipWithoutBeingAdvisorOrGrader() throws Exception {
        var course = round(RegistrationMode.SHARED, 3);
        var topic = publishedTopic(course);
        topic.setAdvisors(Set.of(user(Role.LECTURER)));
        topics.save(topic);
        var group = group(first, course);
        login(first);
        registrations.register(group.getId(), topic.getId());
        var zip = zipReport();
        assertThrows(BusinessRuleException.class, () -> submissions.upload(group.getId(),
                new MockMultipartFile("file", "fake.zip", "application/zip", "not a zip".getBytes())));
        assertThrows(BusinessRuleException.class, () -> submissions.upload(group.getId(),
                new MockMultipartFile("file", "old.pdf", "application/pdf", "%PDF-test".getBytes())));
        var report = submissions.upload(group.getId(), zip);
        assertEquals("application/zip", report.getContentType());
        login(teacher);
        assertArrayEquals(zip.getBytes(), reports.download(group.getId(), report.getId()).resource().getContentAsByteArray());
        String path = "/review/submissions/groups/" + group.getId() + "/files/" + report.getId() + "/download";
        var response = requestAs(teacher, path, null);
        assertEquals(200, response.statusCode());
        assertEquals("application/zip", response.headers().firstValue("Content-Type").orElseThrow());
        assertTrue(response.headers().firstValue("Content-Disposition").orElseThrow().contains("attachment"));
        assertEquals(403, requestAs(user(Role.LECTURER), path, null).statusCode());
    }

    MockMultipartFile zipReport() {
        try {
            var bytes = new java.io.ByteArrayOutputStream();
            try (var zip = new java.util.zip.ZipOutputStream(bytes)) {
                zip.putNextEntry(new java.util.zip.ZipEntry("report.txt"));
                zip.write("Test report".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            return new MockMultipartFile("file", "report.zip", "application/zip", bytes.toByteArray());
        } catch (java.io.IOException ex) { throw new AssertionError(ex); }
    }

    List<Boolean> race(Callable<Boolean> first, Callable<Boolean> second) throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var futures = List.of(first, second).stream().map(task -> executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(5, TimeUnit.SECONDS));
                try { return task.call(); }
                catch (BusinessRuleException expected) { return false; }
                finally { SecurityContextHolder.clearContext(); }
            })).toList();
            assertTrue(ready.await(5, TimeUnit.SECONDS)); start.countDown();
            return List.of(futures.get(0).get(20, TimeUnit.SECONDS), futures.get(1).get(20, TimeUnit.SECONDS));
        }
    }

    void assertPageAs(UserAccount user, String path, String expected) {
        var response = requestAs(user, path, null);
        assertEquals(200, response.statusCode(), path);
        assertTrue(response.body().contains(expected), path + " must show " + expected);
    }

    java.net.http.HttpResponse<String> requestAs(UserAccount user, String path, java.util.Map<String, String> fields) {
        try {
            var client = java.net.http.HttpClient.newBuilder().cookieHandler(new java.net.CookieManager())
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL).build();
            String base = "http://localhost:" + port;
            var loginPage = client.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create(base + "/login")).GET().build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            var csrf = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(loginPage.body());
            assertTrue(csrf.find());
            String body = "email=" + java.net.URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                    + "&password=TestPassword123!&_csrf=" + csrf.group(1);
            var loginResult = client.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create(base + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body)).build(), java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals("/", loginResult.uri().getPath());
            var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(base + path));
            if (fields == null) {
                request.GET();
            } else {
                var profile = client.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create(base + "/profile")).GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                csrf = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(profile.body());
                assertTrue(csrf.find());
                String encoded = fields.entrySet().stream().map(e -> java.net.URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + java.net.URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
                request.header("Content-Type", "application/x-www-form-urlencoded").POST(
                        java.net.http.HttpRequest.BodyPublishers.ofString(encoded + "&_csrf=" + csrf.group(1)));
            }
            return client.send(request.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) { throw new AssertionError(path, ex); }
    }
}
