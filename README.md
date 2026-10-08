# Hệ thống quản lý đề tài sinh viên

Phần Thành viên 1: nền tảng Spring Boot/JSP/PostgreSQL, xác thực, phân quyền, quản lý tài khoản, quản lý bộ môn và hồ sơ cá nhân.

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

## Phân quyền hiện có

| Vai trò | Quyền hiện tại |
|---|---|
| `ADMIN` | Quản lý tài khoản, vai trò, bộ môn và hồ sơ |
| `DEAN` | Quản lý tài khoản, vai trò, bộ môn và hồ sơ |
| `LECTURER` | Đăng nhập, xem trang chủ và đổi mật khẩu |
| `STUDENT` | Đăng nhập, xem trang chủ và đổi mật khẩu |

Các module đợt đăng ký, đề tài, nhóm sinh viên và hội đồng sẽ dùng lại `UserAccount`, `Department`, `Role` và cấu hình bảo mật hiện có.
