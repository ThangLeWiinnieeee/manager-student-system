<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="Hồ sơ"/>
<%@ include file="../common/header.jsp" %>
<div class="page-heading"><div><h1>Hồ sơ cá nhân</h1><p>Thông tin của bạn và cài đặt bảo mật tài khoản.</p></div></div>
<div class="profile-layout">
<section class="panel">
<h2>Thông tin tài khoản</h2>
<p>Liên hệ quản trị viên nếu cần cập nhật thông tin.</p>
<dl class="profile">
    <dt>Tên đăng nhập</dt><dd><c:out value="${account.username}"/></dd>
    <dt>Họ tên</dt><dd><c:out value="${account.fullName}"/></dd>
    <dt>Email</dt><dd><c:out value="${account.email}"/></dd>
    <dt>Vai trò</dt><dd><c:out value="${account.role.displayName}"/></dd>
    <dt>Bộ môn</dt><dd><c:out value="${empty account.department ? '-' : account.department.name}"/></dd>
</dl>
</section>
<form:form modelAttribute="changePasswordForm" method="post" action="${pageContext.request.contextPath}/profile/password" cssClass="form-card narrow">
    <h2>Đổi mật khẩu</h2>
    <p class="form-intro">Sử dụng mật khẩu từ 8 đến 72 ký tự để bảo vệ tài khoản.</p>
    <form:errors path="*" cssClass="alert error" element="div"/>
    <form:label path="currentPassword">Mật khẩu hiện tại</form:label><form:password path="currentPassword" autocomplete="current-password" required="required"/><form:errors path="currentPassword" cssClass="field-error"/>
    <form:label path="newPassword">Mật khẩu mới</form:label><form:password path="newPassword" autocomplete="new-password" minlength="8" maxlength="72" required="required"/><form:errors path="newPassword" cssClass="field-error"/>
    <form:label path="confirmPassword">Xác nhận mật khẩu mới</form:label><form:password path="confirmPassword" autocomplete="new-password" minlength="8" maxlength="72" required="required"/><form:errors path="confirmPassword" cssClass="field-error"/>
    <button class="primary" type="submit">Đổi mật khẩu</button>
</form:form>
</div>
<%@ include file="../common/footer.jsp" %>
