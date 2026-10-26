# Khôi phục mật khẩu bằng email

## Sử dụng

Trên Render Free, cấu hình [Gmail API qua HTTPS](GMAIL_API.md) bằng
`GMAIL_CLIENT_ID`, `GMAIL_CLIENT_SECRET`, `GMAIL_REFRESH_TOKEN` và `MAIL_FROM`.
Khi đủ ba biến OAuth, Gmail API được ưu tiên; nếu không có thì dùng Brevo khi có
`BREVO_API_KEY`, cuối cùng mới dùng SMTP theo hướng dẫn dưới đây.

Tại `/login`, chọn **Quên mật khẩu?**: nhập email → nhập OTP → nhập và xác nhận mật khẩu mới → đăng nhập lại.

Điền các biến `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` theo nhà cung cấp SMTP trong môi trường chạy (tham khảo `.env.example`). Khởi động lại ứng dụng. Không commit `.env`, không dùng mật khẩu email cá nhân nếu nhà cung cấp yêu cầu app password. Cấu hình mặc định dùng STARTTLS cổng 587; triển khai qua HTTPS.

Flyway tự chạy `V3__password_recovery.sql`. Không cần tạo bảng thủ công. Thiếu `MAIL_FROM` sẽ báo chưa cấu hình; SMTP bị lỗi sẽ không có OTP sử dụng được. Hệ thống không thể bảo đảm email vào inbox khi nhà cung cấp chặn hoặc chuyển vào spam.

## Quy tắc

- OTP ngẫu nhiên 6 số, có hiệu lực 5 phút; tối đa 5 lần thử sai cho mỗi mã.
- Gửi lại cách nhau ít nhất 60 giây, tối đa 3 lần/giờ/tài khoản, kể cả lần gửi SMTP lỗi. Mã mới thay thế mã cũ.
- Sau xác minh có 10 phút để đặt mật khẩu; mật khẩu tối thiểu 8 ký tự, tối đa 72 byte UTF-8.
- OTP và mật khẩu được hash bằng PasswordEncoder hiện có (BCrypt), không lưu bản rõ. Mật khẩu mới không gửi qua email.
- Quyền đặt lại gắn với phiên trình duyệt đã yêu cầu OTP. CSRF vẫn được bật; xác minh ở client không thay thế kiểm tra server.
- Cập nhật mật khẩu và đánh dấu hoàn thành trong một transaction. Khóa bản ghi ngăn hai yêu cầu đặt lại đồng thời ghi đè nhau. Gửi lại yêu cầu đã hoàn thành không đổi mật khẩu lần nữa.
- Không cho dùng phiên xác minh cũ nếu email, mật khẩu hoặc trạng thái hoạt động của tài khoản đã thay đổi.
- Phản hồi yêu cầu gửi mã không công bố email có tồn tại hay không. Đây không phải bảo đảm chống suy đoán qua thời gian phản hồi; production nên thêm giới hạn theo IP tại reverse proxy.

## Mất mạng và thử lại

| Tình huống | Xử lý |
| --- | --- |
| Đang nhập email, OTP hoặc mật khẩu thì mất mạng | Giữ dữ liệu trong trang đang mở, hiện cảnh báo, khóa nút gửi. Không lưu OTP/mật khẩu vào localStorage, sessionStorage hoặc URL. |
| Bấm gửi OTP nhưng không nhận phản hồi | Không kết luận gửi thất bại. Khi có mạng, kiểm tra trạng thái và hộp thư. Nếu cần gửi lại, server áp dụng thời gian chờ/giới hạn. Yêu cầu gửi bị lặp với cùng mã yêu cầu không gửi thêm email. |
| Gửi OTP để xác minh nhưng mất phản hồi | Tự kiểm tra trạng thái khi mạng trở lại; nếu server đã xác minh thì chuyển đến bước mật khẩu. Có nút kiểm tra thủ công. |
| Bấm đổi mật khẩu rồi mất mạng | Khóa gửi tiếp cho đến khi kiểm tra trạng thái. Nếu server đã commit, hiển thị hoàn tất; nếu chưa, cho gửi lại. Không thông báo thành công chỉ dựa trên việc đã bấm nút. |
| OTP hết hạn trong lúc offline | Thời gian server vẫn chạy; phải yêu cầu mã mới. Không gia hạn bằng tải lại trang hoặc đổi đồng hồ máy. |
| Tải lại trang | Khôi phục bước từ server nếu còn session; dữ liệu OTP/mật khẩu đã nhập không được lưu để khôi phục. |
| Mất session, đổi trình duyệt hoặc server khởi động lại | Với cấu hình session trong bộ nhớ hiện tại, cần yêu cầu OTP mới; không khôi phục quyền reset từ email đơn thuần. |
| SMTP timeout | Vô hiệu mã không xác định đã gửi thành công hay chưa; vẫn tính giới hạn gửi, chờ rồi yêu cầu mã mới. |

## Phạm vi triển khai

Theo ponytail: một controller, một service và một bảng recovery (tối đa một dòng/tài khoản), dùng JDBC sẵn có, không thêm Redis, hàng đợi hoặc lớp interface chỉ có một implementation. Gửi SMTP đồng bộ có timeout kết nối/đọc/ghi 5 giây theo [hướng dẫn Spring Boot](https://docs.spring.io/spring-boot/reference/io/email.html). Với tải lớn nên chuyển sang outbox/worker vì gửi mail hiện giữ transaction; SMTP và database không phải một transaction phân tán.

Các phiên đăng nhập khác đang tồn tại chưa bị thu hồi tự động khi reset. Khi triển khai nhiều instance, cần session dùng chung và giới hạn request tại gateway. Không bật log nội dung request/SMTP chứa OTP hoặc mật khẩu.

## Kiểm thử

Chạy `mvn test` bằng Java 21. `PasswordRecoveryTest` kiểm tra hash, trạng thái, gửi/trả lời lặp, sai OTP, hết hạn, giới hạn gửi, lỗi SMTP, tài khoản không hợp lệ, thay đổi tài khoản và reset đồng thời. Test dùng H2 chế độ PostgreSQL và mail sender giả lập; cần kiểm tra gửi email thực tế với SMTP của môi trường triển khai.
