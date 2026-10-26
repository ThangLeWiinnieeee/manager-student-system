package vn.edu.fit.topicmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import vn.edu.fit.topicmanagement.department.Department;
import vn.edu.fit.topicmanagement.department.DepartmentRepository;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;
import vn.edu.fit.topicmanagement.user.UserAccountService;
import vn.edu.fit.topicmanagement.user.UserCreateForm;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:web_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE",
        "app.bootstrap.admin-email=admin@example.test",
        "app.bootstrap.admin-password=AdminPassword123!"
})
class ApplicationWebTest {
    @LocalServerPort int port;
    @Autowired UserAccountService accounts;
    @Autowired UserAccountRepository users;
    @Autowired DepartmentRepository departments;
    @Autowired vn.edu.fit.topicmanagement.registrationperiod.CourseRegistrationRepository periods;
    @Autowired vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodRepository windows;
    private HttpClient client;

    @BeforeEach
    void setup() {
        client = HttpClient.newBuilder().cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(5)).build();
    }

    @Test
    void publicPagesRenderAndLoginRequiresCsrf() throws Exception {
        assertPage(get("/login"), "name=\"email\"");
        assertPage(get("/forgot-password"), "id=\"otp-form\"");
        assertPage(get("/css/app.css"), ".login-card");
        assertPage(get("/js/password-recovery.js"), "uncertainReset");
        assertEquals("/login", get("/admin/users").uri().getPath());
        assertEquals(403, post("/login", Map.of("email", "admin@example.test", "password", "AdminPassword123!")).statusCode());
    }

    @Test
    void adminCrudRendersFormsAndPreservesBusinessRules() throws Exception {
        login("admin@example.test", "AdminPassword123!");
        assertPage(get("/"), "Tổng quan");
        assertPage(get("/"), "href=\"/admin/users\"");
        for (String code : new String[]{"CNPM", "HTTT", "MANM", "TTNT"}) {
            assertTrue(departments.existsByCodeIgnoreCase(code));
        }
        assertPage(submit("/admin/departments/new", "/admin/departments",
                Map.of("code", "WEB", "name", "Web test", "enabled", "true")), "Web test");
        Department department = departments.findAll().stream()
                .filter(item -> item.getCode().equals("WEB")).findFirst().orElseThrow();
        assertPage(submit("/admin/users/new", "/admin/users", Map.of(
                "fullName", "Web Student", "email", "web@example.test", "role", "STUDENT",
                "password", "StudentPassword123!", "enabled", "true", "departmentId", department.getId().toString())),
                "Web Student");
        UserAccount student = users.findByEmailIgnoreCase("web@example.test").orElseThrow();
        String userPath = "/admin/users/" + student.getId();
        String edit = userPath + "/edit";
        assertPage(get(edit), "Đặt lại mật khẩu");
        assertPage(submit(edit, userPath, Map.of("fullName", "", "email", "web@example.test", "role", "STUDENT")),
                "Họ tên không được để trống");
        assertPage(submit(edit, userPath + "/password", Map.of(
                "newPassword", "NewPassword123!", "confirmPassword", "Mismatch123!")), "Xác nhận mật khẩu mới không khớp");
        assertPage(submit(edit, userPath + "/password", Map.of(
                "newPassword", "NewPassword123!", "confirmPassword", "NewPassword123!")), "Đã đặt lại mật khẩu");
        String departmentPath = "/admin/departments/" + department.getId();
        assertPage(submit(departmentPath + "/edit", departmentPath,
                Map.of("code", "WEB", "name", "Web test", "enabled", "false")), "Không thể khóa bộ môn đang có người dùng");
        UserAccount admin = users.findByEmailIgnoreCase("admin@example.test").orElseThrow();
        String adminPath = "/admin/users/" + admin.getId();
        assertPage(submit(adminPath + "/edit", adminPath, Map.of(
                "fullName", "Admin", "email", "admin@example.test", "role", "ADMIN", "enabled", "false")),
                "Không thể khóa tài khoản đang đăng nhập");
        assertEquals(404, get("/admin/users/999999/edit").statusCode());
        submit("/", "/logout", Map.of());
        login("web@example.test", "NewPassword123!");
        assertEquals(403, get("/admin/users").statusCode());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEAN", "LECTURER", "STUDENT"})
    void onlyAdminCanAccessAccountManagement(Role role) throws Exception {
        UserCreateForm form = new UserCreateForm();
        form.setFullName("Restricted " + role);
        form.setEmail("restricted-" + role.name().toLowerCase(java.util.Locale.ROOT) + "@example.test");
        form.setRole(role);
        form.setPassword("RestrictedPassword123!");
        UserAccount account = accounts.create(form);
        login(form.getEmail(), form.getPassword());
        assertTrue(!get("/").body().contains("href=\"/admin/users\""));
        assertPage(get("/profile"), form.getFullName());
        if (role == Role.DEAN) {
            assertPage(get("/admin/departments"), "Bộ môn");
            assertEquals(200, get("/admin/registration-periods").statusCode());
        }
        String ownPath = "/admin/users/" + account.getId();
        String adminPath = "/admin/users/" + users.findByEmailIgnoreCase("admin@example.test").orElseThrow().getId();
        for (String path : new String[]{"/admin/users", "/admin/users/new", ownPath + "/edit", adminPath + "/edit"}) {
            assertEquals(403, get(path).statusCode(), role + " GET " + path);
        }
        for (String path : new String[]{"/admin/users", ownPath, ownPath + "/password", ownPath + "/toggle",
                adminPath, adminPath + "/password", adminPath + "/toggle"}) {
            assertEquals(403, submit("/profile", path, Map.of()).statusCode(), role + " POST " + path);
        }
        assertEquals(role, users.findById(account.getId()).orElseThrow().getRole());
        assertTrue(users.findById(account.getId()).orElseThrow().isEnabled());
    }

    @Test
    void profileValidatesInheritedFieldsAndChangesLoginPassword() throws Exception {
        UserCreateForm form = new UserCreateForm();
        form.setFullName("Profile Student");
        form.setEmail("profile@example.test");
        form.setRole(Role.STUDENT);
        form.setPassword("OldPassword123!");
        accounts.create(form);
        login("profile@example.test", "OldPassword123!");
        assertPage(get("/profile"), "Profile Student");
        assertPage(submit("/profile", "/profile/password", Map.of("currentPassword", "OldPassword123!",
                "newPassword", "short", "confirmPassword", "short")), "Mật khẩu mới phải từ 8 đến 72 ký tự");
        String tooLong = "ấ".repeat(25);
        assertPage(submit("/profile", "/profile/password", Map.of("currentPassword", "OldPassword123!",
                "newPassword", tooLong, "confirmPassword", tooLong)), "tối đa 72 byte UTF-8");
        assertPage(submit("/profile", "/profile/password", Map.of("currentPassword", "OldPassword123!",
                "newPassword", "NewPassword123!", "confirmPassword", "NewPassword123!")), "Đã đổi mật khẩu");
        submit("/profile", "/logout", Map.of());
        login("profile@example.test", "NewPassword123!");
    }

    @Test
    void facultyLecturerCanCreateAndEditTopicWithOptionalDepartment() throws Exception {
        Department department = departments.findAll().getFirst();
        UserCreateForm form = new UserCreateForm();
        form.setEmail("advisor@example.test");
        form.setFullName("Faculty Advisor");
        form.setRole(Role.LECTURER);
        form.setPassword("AdvisorPassword123!");
        form.setDepartmentId(department.getId());
        UserAccount lecturer = accounts.create(form);
        assertEquals(department.getId(), users.findById(lecturer.getId()).orElseThrow().getDepartment().getId());

        var round = new vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration();
        round.setPeriod(new vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod());
        round.getPeriod().setName("Advisor web test"); round.setCourseName("Advisor web test");
        round.setLecturer(lecturer);
        round.setDepartment(department);
        round.getPeriod().setType(vn.edu.fit.topicmanagement.registrationperiod.RoundType.MON_HOC);
        var now = java.time.LocalDateTime.now();
        round.getPeriod().setGvStart(now.minusDays(1));
        round.getPeriod().setGvEnd(now.plusDays(1));
        round.getPeriod().setSvStart(now.plusDays(2));
        round.getPeriod().setSvEnd(now.plusDays(3));
        round.setPeriod(windows.save(round.getPeriod()));
        periods.save(round);
        login(form.getEmail(), form.getPassword());
        assertPage(get("/profile"), "Công nghệ thông tin");
        Map<String, String> fields = new java.util.HashMap<>(Map.of("title", "Optional department topic",
                "roundId", round.getId().toString(), "advisorIds", lecturer.getId().toString(), "departmentId", ""));
        fields.put("description", "First paragraph\nSecond paragraph <script>alert(1)</script>");
        HttpResponse<String> created = submit("/topics/new", "/topics", fields);
        assertPage(created, department.getName());
        String path = created.uri().getPath();
        assertTrue(path.matches("/topics/\\d+"));
        assertPage(get("/topics"), "Optional department topic");
        assertPage(get("/topics?departmentId=" + department.getId()), "Optional department topic");

        fields.put("departmentId", department.getId().toString());
        assertPage(submit(path + "/edit", path, fields), department.getName());
        assertPage(get("/topics?departmentId=" + department.getId()), "Optional department topic");
        fields.put("departmentId", "");
        assertPage(submit(path + "/edit", path, fields), department.getName());
        assertPage(get(path), "Second paragraph &lt;script&gt;");
        assertTrue(!get(path).body().contains("Phân công giảng viên hướng dẫn"));
        assertPage(submit(path, path + "/submit", Map.of()), "Chờ duyệt");
        assertTrue(!get(path).body().contains("Sửa đề tài"));

        submit("/", "/logout", Map.of());
        login("admin@example.test", "AdminPassword123!");
        assertPage(get(path), "Duyệt đề tài");
        var invalidRejection = submit(path, path + "/reject", Map.of("reason", ""));
        assertPage(invalidRejection, "Vui lòng nhập lý do từ chối");
        assertTrue(invalidRejection.body().contains("class=\"topic-disclosure\" open"));
        var emptyAdvisors = submit(path, path + "/advisors", Map.of());
        assertPage(emptyAdvisors, "Đề tài phải có ít nhất một giảng viên hướng dẫn");
        assertTrue(Pattern.compile("name=\"advisorIds\" value=\"" + lecturer.getId() + "\" checked")
                .matcher(emptyAdvisors.body()).find());
        assertPage(submit(path, path + "/advisors", Map.of("advisorIds", lecturer.getId().toString())), "Faculty Advisor");
        assertPage(submit(path, path + "/approve", Map.of()), "Công bố đề tài");
        var published = submit(path, path + "/publish", Map.of());
        assertPage(published, "Đã công bố");
        assertTrue(!published.body().contains("id=\"topic-actions-title\""));
    }

    @Test
    void deletingRoundRequiresOwnershipCsrfAndUnfinishedStage() throws Exception {
        UserCreateForm form = new UserCreateForm();
        form.setFullName("Round owner"); form.setEmail("round-owner@example.test");
        form.setPassword("OwnerPassword123!"); form.setRole(Role.LECTURER);
        var owner = accounts.create(form);
        var active = deletionRound("Delete active round", owner, false);
        var ended = deletionRound("Keep ended round", owner, true);
        var foreign = deletionRound("Other owner's round", users.findByEmailIgnoreCase("admin@example.test").orElseThrow(), false);
        String list = "/course-registrations";
        String activeDelete = list + "/" + active.getId() + "/delete";
        String endedDelete = list + "/" + ended.getId() + "/delete";
        String foreignDelete = list + "/" + foreign.getId() + "/delete";
        login(form.getEmail(), form.getPassword());
        String page = get(list).body();
        assertTrue(page.contains(activeDelete));
        assertTrue(!page.contains(endedDelete));
        assertTrue(!page.contains(foreignDelete));
        assertPage(get(list + "/" + active.getId()), "Chưa thiết lập");
        assertEquals(403, get(list + "/" + foreign.getId()).statusCode());
        assertEquals(404, get(list + "/999999").statusCode());
        assertEquals(403, post(activeDelete, Map.of()).statusCode());
        assertTrue(periods.existsById(active.getId()));
        assertEquals(403, submit(list, foreignDelete, Map.of()).statusCode());
        assertTrue(periods.existsById(foreign.getId()));
        assertPage(submit(list, endedDelete, Map.of()), "Không thể xóa môn/lớp thuộc đợt đăng ký đã kết thúc");
        assertTrue(periods.existsById(ended.getId()));
        assertPage(submit(list, activeDelete, Map.of()), "Đã xóa môn/lớp đăng ký");
        assertTrue(!periods.existsById(active.getId()));
        submit("/", "/logout", Map.of());
        login("admin@example.test", "AdminPassword123!");
        assertPage(submit(list, foreignDelete, Map.of()), "Đã xóa môn/lớp đăng ký");
        assertTrue(!periods.existsById(foreign.getId()));
    }

    @Test
    void roundDetailsKeepScheduleAndSettingsOutOfTheSummaryList() throws Exception {
        var admin = users.findByEmailIgnoreCase("admin@example.test").orElseThrow();
        var round = deletionRound("Detailed graduation round", admin, false);
        round.getPeriod().setType(vn.edu.fit.topicmanagement.registrationperiod.RoundType.KLTN);
        round.setCourseName("Graduation project course"); round.setClassCode("DETAIL-01");
        round.setDepartment(departments.findAll().getFirst()); round.setMaxMembers(3);
        round.getPeriod().setGvpbDeadline(round.getSvEnd().plusDays(7));
        round.getPeriod().setCouncilReportDate(round.getSvEnd().plusDays(14));
        round.setPeriod(windows.save(round.getPeriod()));
        periods.save(round);
        login("admin@example.test", "AdminPassword123!");
        String list = get("/course-registrations").body();
        assertTrue(list.contains("Graduation project course"));
        assertTrue(list.contains("Xem chi tiết"));
        assertTrue(!list.contains(round.getGvpbDeadlineText()));
        assertTrue(!list.contains("Hạn GVPB nộp điểm"));
        var detail = get("/course-registrations/" + round.getId());
        for (String text : new String[]{round.getCourseName(), round.getClassCode(), round.getDepartment().getName(),
                round.getRegistrationMode().getDisplayName(), round.getGvStartText(), round.getGvEndText(),
                round.getSvStartText(), round.getSvEndText(), round.getGvpbDeadlineText(), round.getCouncilReportDateText()}) {
            assertPage(detail, text);
        }
    }

    private vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration deletionRound(String name, UserAccount owner, boolean ended) {
        var round = new vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration();
        round.setPeriod(new vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod());
        var start = java.time.LocalDateTime.now().minusDays(ended ? 5 : 1);
        round.getPeriod().setName(name); round.setCourseName(name); round.setLecturer(owner);
        round.getPeriod().setType(vn.edu.fit.topicmanagement.registrationperiod.RoundType.MON_HOC);
        round.getPeriod().setGvStart(start); round.getPeriod().setGvEnd(start.plusDays(1));
        round.getPeriod().setSvStart(start.plusDays(2)); round.getPeriod().setSvEnd(start.plusDays(3));
        round.setPeriod(windows.save(round.getPeriod()));
        return periods.save(round);
    }

    @Test
    void deanOpensSharedWindowAndLecturerRegistersCourseWithoutChoosingScheduleOrOwner() throws Exception {
        UserCreateForm teacherForm = new UserCreateForm();
        teacherForm.setFullName("Window lecturer"); teacherForm.setEmail("window-lecturer@example.test");
        teacherForm.setPassword("WindowPassword123!"); teacherForm.setRole(Role.LECTURER);
        var teacher = accounts.create(teacherForm);
        UserCreateForm deanForm = new UserCreateForm();
        deanForm.setFullName("Window dean"); deanForm.setEmail("window-dean@example.test");
        deanForm.setPassword("WindowPassword123!"); deanForm.setRole(Role.DEAN);
        var dean = accounts.create(deanForm);
        login(deanForm.getEmail(), deanForm.getPassword());
        String base = "/admin/registration-periods";
        var form = get(base + "/new");
        assertPage(form, "name=\"gvStart\"");
        for (String removed : new String[]{"name", "courseName", "classCode", "lecturerId", "departmentId", "registrationMode", "maxMembers"})
            assertTrue(!form.body().contains("name=\"" + removed + "\""), removed);
        assertPage(get("/js/registration-period.js"), "input.required = enabled");
        var now = java.time.LocalDateTime.now().withNano(0);
        Map<String, String> schedule = new java.util.HashMap<>(Map.of("type", "MON_HOC",
                "gvStart", now.minusDays(1).toString(), "gvEnd", now.plusDays(1).toString(),
                "svStart", now.plusDays(2).toString(), "svEnd", now.plusDays(3).toString()));
        var created = submit(base + "/new", base, schedule);
        assertPage(created, "Môn/lớp tham gia");
        String windowPath = created.uri().getPath();
        Long windowId = Long.valueOf(windowPath.substring(windowPath.lastIndexOf('/') + 1));
        assertEquals(403, get("/course-registrations/new").statusCode());
        submit("/", "/logout", Map.of());
        login(teacherForm.getEmail(), teacherForm.getPassword());
        assertPage(get(windowPath), "Đăng ký môn/lớp");
        assertEquals(403, get(base + "/new").statusCode());
        assertEquals(403, get(windowPath + "/edit").statusCode());
        for (String path : new String[]{base, windowPath, windowPath + "/delete"})
            assertEquals(403, submit("/profile", path, schedule).statusCode());
        String courseForm = "/course-registrations/new?periodId=" + windowId;
        var fields = new java.util.HashMap<>(Map.of("periodId", windowId.toString(), "courseName", "Web design",
                "classCode", "WEB-01", "departmentId", departments.findAll().getFirst().getId().toString(),
                "maxMembers", "3", "registrationMode", "SELF_PROPOSED", "lecturerId", dean.getId().toString(),
                "gvEnd", now.plusYears(1).toString()));
        assertPage(submit(courseForm, "/course-registrations", fields), "Web design");
        var course = periods.findByPeriod_IdOrderByCourseNameAsc(windowId).getFirst();
        assertEquals(teacher.getId(), course.getLecturer().getId());
        assertEquals(3, course.getMaxMembers());
        assertEquals(now.plusDays(1), windows.findById(windowId).orElseThrow().getGvEnd());
        assertPage(get("/course-registrations/" + course.getId() + "/edit"), "Đợt chung");
        assertPage(submit(courseForm, "/course-registrations", fields), "Môn/lớp này đã đăng ký trong đợt");
        submit("/", "/logout", Map.of());
        login(deanForm.getEmail(), deanForm.getPassword());
        assertPage(submit(windowPath, windowPath + "/delete", Map.of()), "Đợt đã có môn/lớp đăng ký, không thể xóa");
        schedule.put("type", "KLTN");
        assertPage(submit(windowPath + "/edit", windowPath, schedule), "Hạn GVPB nộp điểm là bắt buộc");
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void registrationNoticesAreVisibleOnlyToTheCurrentPhaseAudience(Role role) throws Exception {
        var account = new UserCreateForm();
        account.setFullName("Notice user");
        account.setEmail("notice-" + java.util.UUID.randomUUID() + "@example.test");
        account.setPassword("NoticePassword123!");
        account.setRole(role);
        accounts.create(account);
        var now = java.time.LocalDateTime.now();
        var lecturer = new vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod();
        lecturer.setName("NOTICE-GV-" + role);
        lecturer.setType(vn.edu.fit.topicmanagement.registrationperiod.RoundType.MON_HOC);
        lecturer.setGvStart(now.minusDays(1)); lecturer.setGvEnd(now.plusDays(1));
        lecturer.setSvStart(now.plusDays(1)); lecturer.setSvEnd(now.plusDays(2));
        var student = new vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod();
        student.setName("NOTICE-SV-" + role);
        student.setType(vn.edu.fit.topicmanagement.registrationperiod.RoundType.MON_HOC);
        student.setGvStart(now.minusDays(2)); student.setGvEnd(now.minusDays(1));
        student.setSvStart(now.minusDays(1)); student.setSvEnd(now.plusDays(1));
        windows.saveAll(java.util.List.of(lecturer, student));
        try {
            login(account.getEmail(), account.getPassword());
            for (String path : new String[]{"/", "/notifications"}) {
                var response = get(path);
                assertEquals(200, response.statusCode(), response.body());
                assertEquals(role == Role.LECTURER || role == Role.DEAN, response.body().contains(lecturer.getName()));
                assertEquals(role == Role.STUDENT, response.body().contains(student.getName()));
                if (role != Role.ADMIN) assertPage(response, path.equals("/") ? "Thông báo mở đăng ký" : "Hệ thống");
            }
        } finally {
            windows.deleteAll(java.util.List.of(lecturer, student));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"STUDENT", "LECTURER"})
    void accountDepartmentChangesPersistThroughEditForm(Role role) throws Exception {
        login("admin@example.test", "AdminPassword123!");
        var available = departments.findAll().stream().filter(Department::isEnabled).toList();
        var initial = available.get(0);
        var replacement = available.get(1);
        var email = "department-" + java.util.UUID.randomUUID() + "@example.test";
        var fields = new java.util.HashMap<>(Map.of("fullName", "Department change test", "email", email,
                "role", role.name(), "enabled", "true", "departmentId", initial.getId().toString(),
                "password", "DepartmentPassword123!"));
        assertPage(submit("/admin/users/new", "/admin/users", fields), "Đã tạo tài khoản");
        var account = users.findByEmailIgnoreCase(email).orElseThrow();
        assertEquals(initial.getId(), account.getDepartment().getId());
        String path = "/admin/users/" + account.getId();
        fields.remove("password");
        fields.put("departmentId", replacement.getId().toString());
        assertPage(submit(path + "/edit", path, fields), "Đã cập nhật tài khoản");
        assertEquals(replacement.getId(), users.findById(account.getId()).orElseThrow().getDepartment().getId());
        var edit = get(path + "/edit");
        assertEquals(200, edit.statusCode());
        var selected = Pattern.compile("<option value=\"" + replacement.getId() + "\"[^>]*selected=\"selected\"");
        assertTrue(selected.matcher(edit.body()).find());
        fields.put("departmentId", "");
        assertPage(submit(path + "/edit", path, fields), "Đã cập nhật tài khoản");
        assertNull(users.findById(account.getId()).orElseThrow().getDepartment());
    }

    private void login(String email, String password) throws Exception {
        HttpResponse<String> response = submit("/login", "/login", Map.of("email", email, "password", password));
        assertEquals("/", response.uri().getPath(), response.body());
        assertPage(response, "Tổng quan");
    }

    @Test
    void deanCannotUseGroupManagementButAdminAndLecturerCan() throws Exception {
        UserCreateForm form = new UserCreateForm();
        form.setFullName("Group lecturer"); form.setEmail("group-staff@example.test");
        form.setPassword("GroupPassword123!"); form.setRole(Role.LECTURER);
        var teacher = accounts.create(form);
        var course = deletionRound("Group access course", teacher, false);
        String base = "/admin/student-groups";
        login(form.getEmail(), form.getPassword());
        assertPage(get("/"), "href=\"" + base + "\"");
        assertPage(submit(base + "/new", base, Map.of("name", "Staff-managed group", "roundId", course.getId().toString())),
                "Staff-managed group");
        form.setEmail("group-dean@example.test"); form.setFullName("Group dean"); form.setRole(Role.DEAN);
        accounts.create(form);
        submit("/", "/logout", Map.of());
        login(form.getEmail(), form.getPassword());
        assertTrue(!get("/").body().contains("href=\"" + base + "\""));
        for (String path : new String[]{base, base + "/new", base + "/1", base + "/1/edit"})
            assertEquals(403, get(path).statusCode(), path);
        for (String path : new String[]{base, base + "/1", base + "/1/delete", base + "/1/members",
                base + "/1/members/1/remove", base + "/1/leader/1"})
            assertEquals(403, submit("/profile", path, Map.of()).statusCode(), path);
        assertEquals(200, get("/dean/group-topic-registrations").statusCode());
        assertEquals(200, get("/admin/councils/assignments").statusCode());
        assertEquals(200, get("/dean/results").statusCode());
        submit("/", "/logout", Map.of());
        login("admin@example.test", "AdminPassword123!");
        assertPage(get("/"), "href=\"" + base + "\"");
        assertPage(submit(base + "/new", base, Map.of("name", "Admin-managed group", "roundId", course.getId().toString())),
                "Admin-managed group");
    }

    @Test
    void roleChangeInvalidatesEveryExistingSessionAndNewLoginUsesNewRole() throws Exception {
        UserCreateForm create = new UserCreateForm();
        create.setFullName("Session dean"); create.setEmail("session-dean@example.test");
        create.setPassword("SessionPassword123!"); create.setRole(Role.DEAN);
        var account = accounts.create(create);
        login(create.getEmail(), create.getPassword());
        var firstClient = client;
        setup();
        login(create.getEmail(), create.getPassword());
        var secondClient = client;
        var form = accounts.toForm(account); form.setRole(Role.STUDENT);
        accounts.update(account.getId(), form, "admin@example.test");
        for (var browser : java.util.List.of(firstClient, secondClient)) {
            client = browser;
            var expired = get("/admin/registration-periods");
            assertEquals("/login", expired.uri().getPath());
            assertPage(expired, "Vui lòng đăng nhập lại");
        }
        login(create.getEmail(), create.getPassword());
        assertEquals(403, get("/admin/registration-periods").statusCode());
        assertEquals(200, get("/student/my-groups").statusCode());
    }

    @Test
    void disabledThenReenabledAccountCannotReuseItsOldSession() throws Exception {
        UserCreateForm create = new UserCreateForm();
        create.setFullName("Session student"); create.setEmail("session-student@example.test");
        create.setPassword("SessionPassword123!"); create.setRole(Role.STUDENT);
        var account = accounts.create(create);
        login(create.getEmail(), create.getPassword());
        accounts.toggle(account.getId(), "admin@example.test");
        accounts.toggle(account.getId(), "admin@example.test");
        assertEquals("/login", get("/student/my-groups").uri().getPath());
        login(create.getEmail(), create.getPassword());
        var form = accounts.toForm(accounts.get(account.getId())); form.setEnabled(false);
        accounts.update(account.getId(), form, "admin@example.test");
        assertEquals("/login", get("/profile").uri().getPath());
        assertTrue(submit("/login", "/login", Map.of("email", create.getEmail(), "password", create.getPassword()))
                .uri().toString().contains("error"));
    }

    @Test
    void editingDisplayNameKeepsSessionButChangingEmailRequiresNewLogin() throws Exception {
        UserCreateForm create = new UserCreateForm();
        create.setFullName("Original"); create.setEmail("session-email@example.test");
        create.setPassword("SessionPassword123!"); create.setRole(Role.STUDENT);
        var account = accounts.create(create);
        login(create.getEmail(), create.getPassword());
        var form = accounts.toForm(account); form.setFullName("Updated name");
        accounts.update(account.getId(), form, "admin@example.test");
        assertEquals("/profile", get("/profile").uri().getPath());
        form.setEmail("session-email-updated@example.test");
        accounts.update(account.getId(), form, "admin@example.test");
        assertEquals("/login", get("/profile").uri().getPath());
        login(form.getEmail(), create.getPassword());
    }

    private HttpResponse<String> submit(String formPath, String action, Map<String, String> fields) throws Exception {
        HttpResponse<String> page = get(formPath);
        assertEquals(200, page.statusCode(), page.body());
        var matcher = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(page.body());
        assertTrue(matcher.find(), "CSRF token missing on " + formPath);
        var data = new java.util.HashMap<>(fields);
        data.put("_csrf", matcher.group(1));
        return post(action, data);
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(request(path).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, Map<String, String> fields) throws Exception {
        String body = fields.entrySet().stream().map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        return client.send(request(path).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(30));
    }

    private static void assertPage(HttpResponse<String> response, String text) {
        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains(text), "Expected page text: " + text);
    }
}
