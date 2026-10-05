<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="${editing ? 'Sửa tài khoản' : 'Thêm tài khoản'}"/>
<c:choose>
    <c:when test="${editing}"><c:url var="formAction" value="/admin/users/${userId}"/></c:when>
    <c:otherwise><c:url var="formAction" value="/admin/users"/></c:otherwise>
</c:choose>
<%@ include file="../../common/header.jsp" %>
<h1>${editing ? 'Sửa tài khoản' : 'Thêm tài khoản'}</h1>
<form:form modelAttribute="userForm" method="post" action="${formAction}" cssClass="form-card">
    <form:errors path="*" cssClass="alert error" element="div"/>
    <div class="form-grid">
        <div><form:label path="username">Tên đăng nhập</form:label><form:input path="username" maxlength="50" required="required"/><form:errors path="username" cssClass="field-error"/></div>
        <div><form:label path="fullName">Họ và tên</form:label><form:input path="fullName" maxlength="150" required="required"/><form:errors path="fullName" cssClass="field-error"/></div>
        <div><form:label path="email">Email</form:label><form:input path="email" type="email" maxlength="150" required="required"/><form:errors path="email" cssClass="field-error"/></div>
        <div><form:label path="password">${editing ? 'Mật khẩu mới (để trống nếu giữ nguyên)' : 'Mật khẩu'}</form:label><form:password path="password" maxlength="72" required="${not editing}"/><form:errors path="password" cssClass="field-error"/></div>
        <div><form:label path="role">Vai trò</form:label><form:select path="role" required="required"><form:option value="" label="-- Chọn vai trò --"/><form:options items="${roles}" itemLabel="displayName"/></form:select><form:errors path="role" cssClass="field-error"/></div>
        <div><form:label path="departmentId">Bộ môn</form:label><form:select path="departmentId"><form:option value="" label="-- Không chọn --"/><form:options items="${departments}" itemValue="id" itemLabel="name"/></form:select></div>
    </div>
    <label class="checkbox"><form:checkbox path="enabled"/> Tài khoản hoạt động</label>
    <div class="form-actions"><button class="primary" type="submit">Lưu</button><a class="button" href="<c:url value='/admin/users'/>">Hủy</a></div>
</form:form>
<%@ include file="../../common/footer.jsp" %>
