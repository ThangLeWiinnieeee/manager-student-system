# Cấu trúc dự án

Đây là một Maven module Spring MVC, đóng gói thành một executable WAR. Frontend JSP và Backend Java được tách theo vị trí chuẩn, không phải hai ứng dụng độc lập.

```text
src/main/java/vn/edu/fit/topicmanagement/
├── config/                  Cấu hình Spring và bảo mật
├── common/exception/        Xử lý lỗi dùng chung
├── auth/                    Đăng nhập
├── dashboard/               Trang tổng quan
├── user/                    Tài khoản, vai trò, hồ sơ
├── department/              Bộ môn
├── registrationperiod/      Đợt đăng ký
├── topic/                   Đề tài và GVHD
├── studentgroup/            Nhóm sinh viên và đăng ký đề tài
├── submission/              Báo cáo
├── council/                 Hội đồng và phân công phản biện
├── evaluation/              Chấm điểm và nhận xét
├── result/                  Kết quả cuối cùng
└── notification/            Thông báo

src/main/resources/
├── application.properties
├── db/migration/            Flyway SQL
└── static/                  CSS, JavaScript, hình ảnh

src/main/webapp/WEB-INF/views/
├── common/                  Header, footer, thông báo
├── auth/                    Đăng nhập
├── profile/                 Hồ sơ cá nhân
├── admin/                   Tài khoản và bộ môn
├── registration-period/     Đợt đăng ký
├── topic/                   Đề tài
├── student-group/           Nhóm sinh viên
├── submission/              Báo cáo
├── council/                 Hội đồng
├── evaluation/              Chấm điểm
├── result/                  Kết quả
├── notification/            Thông báo
└── error/                   Trang lỗi
```

## Cấu trúc bên trong một module

Chỉ tạo class khi chức năng cần đến. Ví dụ module `topic`:

```text
topic/
├── Topic.java
├── TopicStatus.java
├── TopicForm.java
├── TopicRepository.java
├── TopicService.java
└── TopicController.java
```

Luồng phụ thuộc một chiều:

```text
JSP → Controller → Service → Repository → PostgreSQL
```

- Controller nhận request, validation và chọn view.
- Service chứa nghiệp vụ và transaction.
- Repository chỉ truy cập dữ liệu.
- Entity biểu diễn dữ liệu; Form nhận dữ liệu từ JSP.
- JSP chỉ hiển thị và gửi form, không chứa nghiệp vụ hoặc SQL.
