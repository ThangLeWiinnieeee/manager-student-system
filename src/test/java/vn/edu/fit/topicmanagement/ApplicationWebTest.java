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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import vn.edu.fit.topicmanagement.major.Major;
import vn.edu.fit.topicmanagement.major.MajorRepository;
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
    @Autowired MajorRepository majors;
    @Autowired vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodRepository periods;
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
        for (String code : new String[]{"CNPM", "HTTT", "MANM", "TTNT"}) {
            assertTrue(majors.existsByCodeIgnoreCase(code));
        }
        assertPage(submit("/admin/majors/new", "/admin/majors",
                Map.of("code", "WEB", "name", "Web test", "enabled", "true")), "Web test");
        Major major = majors.findAll().stream()
                .filter(item -> item.getCode().equals("WEB")).findFirst().orElseThrow();
        assertPage(submit("/admin/users/new", "/admin/users", Map.of(
                "fullName", "Web Student", "email", "web@example.test", "role", "STUDENT",
                "password", "StudentPassword123!", "enabled", "true", "majorId", major.getId().toString())),
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
        String majorPath = "/admin/majors/" + major.getId();
        assertPage(submit(majorPath + "/edit", majorPath,
                Map.of("code", "WEB", "name", "Web test", "enabled", "false")), "Không thể khóa chuyên ngành đang có người dùng");
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
    void facultyLecturerCanCreateAndEditTopicWithOptionalMajor() throws Exception {
        Major major = majors.findAll().getFirst();
        UserCreateForm form = new UserCreateForm();
        form.setEmail("advisor@example.test");
        form.setFullName("Faculty Advisor");
        form.setRole(Role.LECTURER);
        form.setPassword("AdvisorPassword123!");
        form.setMajorId(major.getId());
        UserAccount lecturer = accounts.create(form);
        assertNull(users.findById(lecturer.getId()).orElseThrow().getMajor());

        var round = new vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod();
        round.setName("Advisor web test");
        round.setType(vn.edu.fit.topicmanagement.registrationperiod.RoundType.MON_HOC);
        var now = java.time.LocalDateTime.now();
        round.setGvStart(now.minusDays(1));
        round.setGvEnd(now.plusDays(1));
        round.setSvStart(now.plusDays(2));
        round.setSvEnd(now.plusDays(3));
        periods.save(round);
        login(form.getEmail(), form.getPassword());
        assertPage(get("/profile"), "Công nghệ thông tin");
        Map<String, String> fields = new java.util.HashMap<>(Map.of("title", "Optional major topic",
                "roundId", round.getId().toString(), "advisorIds", lecturer.getId().toString(), "majorId", ""));
        HttpResponse<String> created = submit("/topics/new", "/topics", fields);
        assertPage(created, "Không giới hạn");
        String path = created.uri().getPath();
        assertTrue(path.matches("/topics/\\d+"));
        assertPage(get("/topics"), "Optional major topic");
        assertTrue(!get("/topics?majorId=" + major.getId()).body().contains("Optional major topic"));

        fields.put("majorId", major.getId().toString());
        assertPage(submit(path + "/edit", path, fields), major.getName());
        assertPage(get("/topics?majorId=" + major.getId()), "Optional major topic");
        fields.put("majorId", "");
        assertPage(submit(path + "/edit", path, fields), "Không giới hạn");
    }

    private void login(String email, String password) throws Exception {
        HttpResponse<String> response = submit("/login", "/login", Map.of("email", email, "password", password));
        assertEquals("/", response.uri().getPath(), response.body());
        assertPage(response, "Tổng quan");
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
