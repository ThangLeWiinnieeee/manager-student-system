# Gửi OTP bằng Gmail API trên Render

Ứng dụng gửi qua HTTPS, không dùng cổng SMTP. Thứ tự chọn: Gmail API khi đủ
ba biến OAuth; nếu không có thì Brevo khi có API key; còn lại dùng SMTP local.
Thiếu một phần bộ OAuth sẽ báo lỗi cấu hình khi khởi động. Lỗi gửi không tự
chuyển nhà cung cấp; OTP không xác nhận được gửi sẽ bị vô hiệu hóa.

## Chuẩn bị Google Cloud

1. Tạo/chọn project trong Google Cloud Console, bật **Gmail API**.
2. Trong Google Auth Platform, cấu hình Branding và Audience (External cho
   Gmail cá nhân). Khi Testing, thêm Gmail người gửi vào Test users.
3. Trong Data Access, thêm `https://www.googleapis.com/auth/gmail.send`.
4. Tạo OAuth Client loại Web application, thêm Authorized redirect URI:
   `https://developers.google.com/oauthplayground`.
5. Mở OAuth Playground, vào bánh răng, bật **Use your own OAuth credentials**,
   nhập Client ID/Client secret của project, chọn Access type Offline.
6. Authorize scope `https://www.googleapis.com/auth/gmail.send` bằng Gmail người
   gửi. Exchange authorization code for tokens, giữ **Refresh token**.

## Cấu hình

Điền vào `.env` khi chạy local và Render → Environment khi deploy:

```dotenv
GMAIL_CLIENT_ID=
GMAIL_CLIENT_SECRET=
GMAIL_REFRESH_TOKEN=
MAIL_FROM=
```

`MAIL_FROM` là Gmail đã cấp quyền. Không thay các biến này bằng Gmail App Password
hoặc API key. Không commit `.env`, refresh token hoặc client secret. Blueprint
`render.yaml` yêu cầu các biến Gmail cho service mới; service đã có cần thêm chúng
trong Environment. Render không đọc `.env` trên máy.

Khởi động lại local hoặc Save and deploy trên Render. Thử quên mật khẩu bằng
tài khoản có sẵn, kiểm tra Inbox/Spam và thư Sent của Gmail người gửi.

OAuth External ở trạng thái Testing thường cấp refresh token hết hạn sau 7 ngày
với scope Gmail. Trước khi dùng lâu dài, chuyển sang Production, thực hiện yêu cầu
xác minh mà Google áp dụng và cấp quyền lại để lấy token mới. Production không
đồng nghĩa đã được xác minh, và token vẫn có thể bị thu hồi.

Khi Google trả `invalid_grant`, kiểm tra token hết hạn/bị thu hồi và cấp quyền lại.
Khi lỗi quyền, kiểm tra Gmail API đã bật và scope `gmail.send`. Không bật log HTTP
DEBUG/TRACE khi dùng khóa thật vì request có thể chứa token hoặc nội dung OTP.

Tham khảo:
- https://developers.google.com/workspace/gmail/api/guides/sending
- https://developers.google.com/identity/protocols/oauth2/web-server
- https://developers.google.com/identity/protocols/oauth2
