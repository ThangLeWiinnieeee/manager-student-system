<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="${editing ? 'Sửa tài khoản' : 'Thêm tài khoản'}"/>
<c:choose>
    <c:when test="${editing}"><c:url var="formAction" value="/admin/users/${userId}"/></c:when>
    <c:otherwise><c:url var="formAction" value="/admin/users"/></c:otherwise>
</c:choose>
<%@ include file="../../common/header.jsp" %>
<div class="page-heading"><div><h1>${editing ? 'Sửa tài khoản' : 'Thêm tài khoản'}</h1><p>Cập nhật thông tin, vai trò và quyền truy cập hệ thống.</p></div><a class="button" href="<c:url value='/admin/users'/>">Về danh sách</a></div>
<form:form modelAttribute="userForm" method="post" action="${formAction}" cssClass="form-card">
    <h2>Thông tin tài khoản</h2>
    <p class="form-intro">Điền thông tin người dùng và chọn vai trò phù hợp. Bộ môn là tùy chọn.</p>
    <form:errors path="*" cssClass="alert error" element="div"/>
    <div class="form-grid">
        <div><form:label path="fullName">Họ và tên</form:label><form:input path="fullName" maxlength="150" required="required"/><form:errors path="fullName" cssClass="field-error"/></div>
        <div><form:label path="email">Email</form:label><form:input path="email" type="email" maxlength="150" required="required"/><form:errors path="email" cssClass="field-error"/></div>
        <c:if test="${not editing}"><div><form:label path="password">Mật khẩu</form:label><form:password path="password" minlength="8" maxlength="72" autocomplete="new-password" required="required"/><form:errors path="password" cssClass="field-error"/></div></c:if>
        <div>
            <form:label path="role">Vai trò</form:label>
            <c:choose>
                <c:when test="${editingSelfAdmin}">
                    <form:select path="role" disabled="true"><form:options items="${roles}" itemLabel="displayName"/></form:select>
                    <form:hidden path="role" id="lockedRole"/>
                    <small class="muted">Quản trị viên không thể tự thay đổi vai trò của mình.</small>
                </c:when>
                <c:otherwise><form:select path="role" required="required"><form:option value="" label="-- Chọn vai trò --"/><form:options items="${roles}" itemLabel="displayName"/></form:select></c:otherwise>
            </c:choose>
            <form:errors path="role" cssClass="field-error"/>
        </div>
        <div><form:label path="departmentId">Bộ môn</form:label><form:select path="departmentId"><form:option value="" label="-- Không chọn --"/><form:options items="${departments}" itemValue="id" itemLabel="name"/></form:select><small class="muted">Tài khoản quản trị viên không thuộc bộ môn.</small></div>
    </div>
    <label class="checkbox"><form:checkbox path="enabled"/> Tài khoản hoạt động</label>
    <div class="form-actions"><button class="primary" type="submit">Lưu tài khoản</button><a class="button" href="<c:url value='/admin/users'/>">Hủy</a></div>
</form:form>
<c:if test="${editing}">
<form:form modelAttribute="passwordResetForm" method="post" action="${pageContext.request.contextPath}/admin/users/${userId}/password" cssClass="form-card narrow">
    <h2>Đặt lại mật khẩu</h2>
    <p class="form-intro">Chỉ quản trị viên và trưởng khoa được phép đặt lại mật khẩu tài khoản.</p>
    <form:errors path="*" cssClass="alert error" element="div"/>
    <form:label path="newPassword">Mật khẩu mới</form:label><form:password path="newPassword" minlength="8" maxlength="72" autocomplete="new-password" required="required"/><form:errors path="newPassword" cssClass="field-error"/>
    <form:label path="confirmPassword">Xác nhận mật khẩu mới</form:label><form:password path="confirmPassword" minlength="8" maxlength="72" autocomplete="new-password" required="required"/><form:errors path="confirmPassword" cssClass="field-error"/>
    <button class="primary" type="submit">Đặt lại mật khẩu</button>
</form:form>
</c:if>
<%@ include file="../../common/footer.jsp" %>
