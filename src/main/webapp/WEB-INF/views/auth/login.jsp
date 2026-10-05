<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập</title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
</head>
<body class="login-page">
<main class="login-card">
    <h1>Đăng nhập</h1>
    <p>Hệ thống quản lý đề tài sinh viên</p>
    <c:if test="${param.error != null}"><div class="alert error">Tên đăng nhập, mật khẩu hoặc trạng thái tài khoản không hợp lệ.</div></c:if>
    <c:if test="${param.logout != null}"><div class="alert success">Bạn đã đăng xuất.</div></c:if>
    <form action="<c:url value='/login'/>" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label for="username">Tên đăng nhập</label>
        <input id="username" name="username" type="text" autocomplete="username" required autofocus>
        <label for="password">Mật khẩu</label>
        <input id="password" name="password" type="password" autocomplete="current-password" required>
        <button type="submit" class="primary full">Đăng nhập</button>
    </form>
</main>
</body>
</html>
