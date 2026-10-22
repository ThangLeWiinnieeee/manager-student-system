<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="${_csrf.token}">
    <meta name="csrf-header" content="${_csrf.headerName}">
    <title>Quên mật khẩu | Quản lý đề tài</title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
    <script defer src="<c:url value='/js/password-recovery.js'/>"></script>
</head>
<body class="login-page">
<header class="login-header"><a class="brand" href="<c:url value='/login'/>">Quản lý đề tài<small>Khoa Công nghệ thông tin</small></a></header>
<main class="login-main">
<div class="login-card" id="recovery" data-api="<c:url value='/forgot-password'/>">
    <h1>Quên mật khẩu</h1>
    <p>Xác minh email để đặt mật khẩu mới.</p>
    <p id="network-status" class="alert error" role="status" hidden></p>
    <p id="recovery-message" class="alert" role="status" hidden></p>
    <form id="email-form" hidden>
        <label for="recovery-email">Email tài khoản</label>
        <input id="recovery-email" name="email" type="email" maxlength="150" autocomplete="email" required>
        <button class="primary full" type="submit">Gửi mã OTP</button>
    </form>
    <section id="otp-step" hidden>
        <p class="form-intro">Nhập mã 6 số trong email. Mã có hiệu lực 5 phút; tối đa 5 lần nhập sai.</p>
        <form id="otp-form">
            <label for="otp">Mã OTP</label>
            <input id="otp" name="otp" inputmode="numeric" autocomplete="one-time-code" pattern="[0-9]{6}" minlength="6" maxlength="6" required>
            <button class="primary full" type="submit">Xác minh OTP</button>
        </form>
        <button id="resend" type="button" class="full">Gửi lại OTP</button>
        <button id="change-email" type="button" class="link-button full">Nhập email khác</button>
    </section>
    <form id="password-form" hidden>
        <p class="form-intro">Email đã được xác minh. Hoàn tất trong 10 phút.</p>
        <label for="new-password">Mật khẩu mới</label>
        <input id="new-password" name="newPassword" type="password" autocomplete="new-password" minlength="8" maxlength="72" required>
        <label for="confirm-password">Nhập lại mật khẩu mới</label>
        <input id="confirm-password" name="confirmPassword" type="password" autocomplete="new-password" minlength="8" maxlength="72" required>
        <button class="primary full" type="submit">Lưu mật khẩu mới</button>
    </form>
    <div id="done-step" hidden><p class="alert success">Mật khẩu đã được cập nhật. Bạn có thể đăng nhập bằng mật khẩu mới.</p></div>
    <div id="expired-step" hidden><p>Phiên xác minh đã hết hạn, vượt số lần thử hoặc mã đã bị thay thế.</p><button id="restart" class="primary full" type="button">Yêu cầu mã mới</button></div>
    <button id="check-status" class="full" type="button">Kiểm tra trạng thái</button>
    <div class="login-help">Nếu mất mạng, giữ trang này và kiểm tra trạng thái khi có mạng trở lại. Không gửi lại mật khẩu khi chưa biết kết quả lần trước.</div>
    <a class="button full" href="<c:url value='/login'/>">Về đăng nhập</a>
    <noscript><p class="alert error">Bật JavaScript để dùng chức năng OTP và phục hồi khi mất mạng.</p></noscript>
</div>
</main>
</body>
</html>
