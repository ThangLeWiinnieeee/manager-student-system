# Phân chia module cho nhóm

| Thành viên | Java package | JSP | Migration |
|---|---|---|---|
| TV1 | `config`, `common`, `auth`, `dashboard`, `user`, `major` | `common`, `auth`, `profile`, `admin`, `error` | `V1__create_identity_schema.sql` |
| TV2 | `registrationperiod`, `topic` | `registration-period`, `topic` | Bắt đầu từ `V5__...sql` (V1–V4 đã dùng) |
| TV3 | `studentgroup`, `submission` | `student-group`, `submission` | Phiên bản tiếp theo sau TV2 |
| TV4 | `council`, `evaluation`, `result`, `notification` | Các thư mục cùng tên | Phiên bản tiếp theo sau TV3 |

## Quy tắc làm việc

1. Mỗi chức năng hoàn thiện theo chiều dọc: migration → entity → repository → service → controller → JSP → test.
2. Không truy cập repository của module khác từ Controller; gọi Service của module sở hữu dữ liệu.
3. Không tạo `Service` interface nếu chỉ có một implementation.
4. Không bind trực tiếp Entity vào form; dùng class `*Form` có Jakarta Validation.
5. Mọi POST đều giữ CSRF; quyền phải kiểm tra ở Backend.
6. Không sửa migration đã được merge; tạo migration phiên bản mới.
   SQL dùng chung đặt trong `db/migration/common`; SQL riêng PostgreSQL đặt trong `db/migration/postgresql`. Khi có cú pháp riêng, thêm bản tương đương H2 trong `src/test/resources/db/migration/h2` để chạy test.
7. Không đưa mật khẩu, token hoặc cấu hình cá nhân vào Git.
8. Pull Request chỉ nên chứa một chức năng hoàn chỉnh hoặc một lỗi cụ thể.
