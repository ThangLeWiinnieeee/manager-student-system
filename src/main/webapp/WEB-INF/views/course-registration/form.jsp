<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="${editing ? 'Sửa môn/lớp' : 'Đăng ký môn/lớp'}"/>
<c:choose>
    <c:when test="${editing}"><c:url var="formAction" value="/course-registrations/${periodId}"/></c:when>
    <c:otherwise><c:url var="formAction" value="/course-registrations"/></c:otherwise>
</c:choose>
<%@ include file="../common/header.jsp" %>
<div class="page-heading"><div><h1>${editing ? 'Sửa môn/lớp' : 'Đăng ký môn/lớp'}</h1><p>Môn/lớp dùng lịch chung của Khoa. Bạn là giảng viên phụ trách môn/lớp đã đăng ký.</p></div><a class="button" href="<c:url value='/course-registrations'/>">Về danh sách</a></div>
<form:form modelAttribute="periodForm" method="post" action="${formAction}" cssClass="form-card">
    <h2>Thông tin môn/lớp</h2>
    <p class="form-intro">Chọn hình thức đề tài và giới hạn từ 1 đến 5 sinh viên mỗi nhóm.</p>
    <form:errors path="*" cssClass="alert error" element="div"/>
    <c:choose>
        <c:when test="${editing}"><p>Đợt chung: <c:out value="${window.name}"/> (#<c:out value="${window.id}"/>)</p><form:hidden path="periodId"/></c:when>
        <c:otherwise>
            <form:label path="periodId">Đợt đang mở cho giảng viên</form:label>
            <form:select path="periodId" required="required"><form:option value="" label="-- Chọn đợt --"/>
                <c:forEach items="${windows}" var="w"><form:option value="${w.id}"><c:out value="${w.name}"/> (#<c:out value="${w.id}"/>)</form:option></c:forEach>
            </form:select>
            <c:if test="${empty windows}"><p>Hiện chưa có đợt nào mở cho giảng viên đăng ký.</p></c:if>
        </c:otherwise>
    </c:choose>
    <div class="form-grid">
        <div><form:label path="courseName">Môn học / hoạt động</form:label><form:input path="courseName" maxlength="150" required="required"/></div>
        <div><form:label path="classCode">Mã lớp</form:label><form:input path="classCode" maxlength="100" required="required"/></div>
        <div><form:label path="departmentId">Bộ môn</form:label><form:select path="departmentId" required="required"><form:option value="" label="-- Chọn bộ môn --"/><form:options items="${departments}" itemValue="id" itemLabel="name"/></form:select></div>
        <c:if test="${manager}"><div><form:label path="lecturerId">GV phụ trách môn/lớp</form:label><form:select path="lecturerId" required="required"><form:option value="" label="-- Chọn giảng viên --"/><form:options items="${lecturers}" itemValue="id" itemLabel="fullName"/></form:select></div></c:if>
        <div><form:label path="registrationMode">Hình thức đăng ký</form:label><form:select path="registrationMode"><form:options items="${registrationModes}" itemValue="name" itemLabel="displayName"/></form:select></div>
        <div><form:label path="maxMembers">Số thành viên tối đa mỗi nhóm</form:label><form:input path="maxMembers" type="number" min="1" max="5" required="required"/></div>

    </div>
    <div class="form-actions"><button class="primary" type="submit">Lưu môn/lớp</button><a class="button" href="<c:url value='/course-registrations'/>">Hủy</a></div>
</form:form>
<%@ include file="../common/footer.jsp" %>