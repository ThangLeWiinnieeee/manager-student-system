# Hệ thống quản lý đề tài sinh viên

Phần Thành viên 1: nền tảng Spring Boot/JSP/PostgreSQL, xác thực, phân quyền, quản lý tài khoản, quản lý chuyên ngành và hồ sơ cá nhân.

## Phạm vi quản lý

Hệ thống phục vụ một Khoa Công nghệ thông tin. Chuyên ngành là hướng đào tạo của sinh viên; giảng viên thuộc khoa và không bị ràng buộc vào chuyên ngành của sinh viên. Mỗi đề tài chọn trực tiếp 1–2 giảng viên hướng dẫn đang hoạt động, có thể phân loại theo chuyên ngành hoặc để trống.

Quản lý chuyên ngành tại `/admin/majors`. Migration V12 đổi bảng `departments` thành `majors`, giữ nguyên ID và dữ liệu, chuyển liên kết sinh viên sang `users.major_id` và giữ `users.department_id` làm dữ liệu lịch sử. Liên kết đề tài và giảng viên hướng dẫn được bảo toàn. Flyway tự áp dụng V12 khi khởi động; không chạy đồng thời phiên bản ứng dụng cũ trên cùng database sau migration.

## Cấu trúc dự án

Đây là một Maven module Spring MVC duy nhất:

- Backend Java: [`src/main/java`](src/main/java).
- Frontend JSP: [`src/main/webapp/WEB-INF/views`](src/main/webapp/WEB-INF/views).
- CSS/JavaScript/hình ảnh: [`src/main/resources/static`](src/main/resources/static).
- PostgreSQL migration: [`src/main/resources/db/migration`](src/main/resources/db/migration).
- Kiểm thử: [`src/test`](src/test).

Xem [cấu trúc chi tiết](docs/PROJECT_STRUCTURE.md) và [phân chia module cho nhóm](docs/TEAM_MODULES.md).

Luồng MVC:

```text
JSP/CSS/JavaScript → Controller → Service → Repository → PostgreSQL
```

## Công nghệ

- Java 21, Spring Boot 3.5, Spring MVC, Spring Security
- JSP/JSTL, CSS thuần
- Spring Data JPA, PostgreSQL, Flyway
- Maven, executable WAR, Tomcat

## Chạy ứng dụng

Yêu cầu: Java 21, Maven 3.6.3+ và PostgreSQL.

1. Khởi động PostgreSQL:

   ```bash
   docker compose up -d postgres
   ```

2. Cấu hình biến môi trường. `ADMIN_PASSWORD` bắt buộc ở lần chạy đầu để tạo tài khoản quản trị:

   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/student_topic_management"
   $env:DB_USERNAME="postgres"
   $env:DB_PASSWORD="postgres"
   $env:ADMIN_EMAIL="admin@fit.edu.vn"
   $env:ADMIN_PASSWORD="ChangeThisPassword123!"
   ```

3. Chạy ứng dụng:

   ```bash
   mvn spring-boot:run
   ```

4. Mở `http://localhost:8080` và đăng nhập bằng email quản trị vừa cấu hình.

Có thể build hoàn toàn bằng Docker nếu máy chưa có Java 21/Maven:

```bash
docker build -t student-topic-management .
```

## Kiểm thử và đóng gói

```bash
mvn test
mvn clean package
java -jar target/student-topic-management.war
```

Test dùng H2 trong bộ nhớ và SMTP giả lập, không cần kết nối database/email thật. Flyway chạy migration trong `db/migration/common` và thư mục tương ứng loại database (`postgresql` hoặc `h2`). Bản H2 của V4 chỉ có trong `src/test/resources`; khi đổi dữ liệu seed cần giữ hai bản đồng bộ. Sau khi chuyển vị trí migration, chạy `mvn clean test` để xóa resource cũ trong `target`.

`ApplicationWebTest` khởi động Tomcat trên cổng ngẫu nhiên và gửi HTTP thật để kiểm tra JSP, đăng nhập, phân quyền, quản lý tài khoản/chuyên ngành và đổi mật khẩu. `PasswordRecoveryTest` kiểm tra luồng OTP với SMTP giả lập. Cách chọn migration theo database dùng cấu hình `{vendor}` của [Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway).

Trên Windows, nếu đã có Java/Maven trong `.build-tools`, có thể chạy:

```powershell
$env:JAVA_HOME = Join-Path $PWD '.build-tools\jdk\jdk-21.0.12.1+1'
& .\.build-tools\maven\apache-maven-3.9.11\bin\mvn.cmd clean verify
```

## Phân quyền hiện có

| Vai trò | Quyền hiện tại |
|---|---|
| `ADMIN` | Quản lý tài khoản, vai trò, chuyên ngành và hồ sơ |
| `DEAN` | Quản lý tài khoản, vai trò, chuyên ngành và hồ sơ |
| `LECTURER` | Đăng nhập, xem trang chủ và đổi mật khẩu |
| `STUDENT` | Đăng nhập, xem trang chủ và đổi mật khẩu |

Các module đợt đăng ký, đề tài, nhóm sinh viên và hội đồng sẽ dùng lại `UserAccount`, `Major`, `Role` và cấu hình bảo mật hiện có.
