<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập | Quản lý đề tài sinh viên</title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
</head>
<body class="login-page">
<header class="login-header">
    <div class="brand"><span class="brand-mark" aria-hidden="true"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 7V4h6l3 3h9v13H3z"/><path d="M3 10h18"/></svg></span><span>Quản lý đề tài<small>Khoa Công nghệ thông tin</small></span></div>
</header>
<main class="login-main">
<div class="login-card">
    <div class="login-emblem" aria-hidden="true"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></svg></div>
    <h1>Đăng nhập</h1>
    <p>Truy cập không gian quản lý đề tài của bạn.</p>
    <c:if test="${param.error != null}"><div class="alert error" role="alert">Không thể đăng nhập. Kiểm tra email, mật khẩu hoặc liên hệ khoa nếu tài khoản bị khóa.</div></c:if>
    <c:if test="${param.logout != null}"><div class="alert success" role="status">Bạn đã đăng xuất thành công.</div></c:if>
    <form action="<c:url value='/login'/>" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label for="email">Email</label>
        <input id="email" name="email" type="email" autocomplete="email" placeholder="Nhập địa chỉ email" required autofocus>
        <label for="password">Mật khẩu</label>
        <input id="password" name="password" type="password" autocomplete="current-password" placeholder="Nhập mật khẩu" required>
        <button type="submit" class="primary full">Đăng nhập</button>
    </form>
    <div class="login-help">Cần cấp tài khoản hoặc đặt lại mật khẩu?<br>Liên hệ quản trị viên khoa để được hỗ trợ.</div>
</div>
</main>
<footer class="login-footer">Hệ thống quản lý đề tài sinh viên · Khoa Công nghệ thông tin</footer>
</body>
</html>
