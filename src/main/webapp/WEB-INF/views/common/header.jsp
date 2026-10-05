<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${empty pageTitle ? 'Quản lý đề tài sinh viên' : pageTitle}"/></title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
</head>
<body>
<header class="topbar">
    <a class="brand" href="<c:url value='/'/>">Quản lý đề tài</a>
    <nav aria-label="Điều hướng chính">
        <a href="<c:url value='/'/>">Trang chủ</a>
        <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
            <a href="<c:url value='/admin/users'/>">Tài khoản</a>
            <a href="<c:url value='/admin/departments'/>">Bộ môn</a>
        </c:if>
        <a href="<c:url value='/profile'/>">Hồ sơ</a>
        <form action="<c:url value='/logout'/>" method="post" class="inline-form">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit" class="link-button">Đăng xuất</button>
        </form>
    </nav>
</header>
<main class="container">
    <c:if test="${not empty success}"><div class="alert success"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
