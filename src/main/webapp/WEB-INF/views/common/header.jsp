<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="currentPath" value="${requestScope['jakarta.servlet.forward.request_uri']}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${empty pageTitle ? 'Quản lý đề tài sinh viên' : pageTitle}"/> | Khoa CNTT</title>
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
</head>
<body>
<a class="skip-link" href="#main-content">Đến nội dung chính</a>
<aside class="sidebar">
    <a class="brand" href="<c:url value='/'/>"><span class="brand-mark" aria-hidden="true"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 7V4h6l3 3h9v13H3z"/><path d="M3 10h18"/></svg></span><span>Quản lý đề tài<small>Khoa Công nghệ thông tin</small></span></a>
    <p class="nav-label">Không gian làm việc</p>
    <nav aria-label="Điều hướng chính">
        <a class="nav-link" href="<c:url value='/'/>" aria-current="${currentPath == pageContext.request.contextPath.concat('/') ? 'page' : 'false'}"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="m3 10 9-7 9 7v10H3zM9 20v-7h6v7"/></svg>Tổng quan</a>
        <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
            <a class="nav-link" href="<c:url value='/admin/users'/>" aria-current="${fn:contains(currentPath, '/admin/users') ? 'page' : 'false'}"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6m2 3a5 5 0 0 1 3 5v2"/></svg>Tài khoản</a>
            <a class="nav-link" href="<c:url value='/admin/departments'/>" aria-current="${fn:contains(currentPath, '/admin/departments') ? 'page' : 'false'}"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 21V5l8-3 8 3v16H4M9 21v-5h6v5M8 8h1m6 0h1M8 12h1m6 0h1"/></svg>Bộ môn</a>
        </c:if>
        <a class="nav-link" href="<c:url value='/profile'/>" aria-current="${fn:contains(currentPath, '/profile') ? 'page' : 'false'}"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></svg>Hồ sơ cá nhân</a>
    </nav>
    <div class="sidebar-bottom">
        <p class="sidebar-note">Cổng quản lý đề tài<br>Dành cho giảng viên và sinh viên</p>
        <form action="<c:url value='/logout'/>" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit" class="link-button">Đăng xuất</button>
        </form>
    </div>
</aside>
<div class="workspace">
<header class="topbar">
    <div class="breadcrumb"><a href="<c:url value='/'/>">Không gian làm việc</a><span>/</span><c:out value="${pageTitle}"/></div>
    <a class="account-link" href="<c:url value='/profile'/>"><span class="avatar" aria-hidden="true"><svg class="nav-icon" viewBox="0 0 24 24"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg></span><c:out value="${pageContext.request.userPrincipal.principal.fullName}"/></a>
</header>
<main class="container" id="main-content">
    <c:if test="${not empty success}"><div class="alert success" role="status"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
