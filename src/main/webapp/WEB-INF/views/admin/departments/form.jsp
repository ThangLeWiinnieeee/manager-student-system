<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="${editing ? 'Sửa bộ môn' : 'Thêm bộ môn'}"/>
<c:choose>
    <c:when test="${editing}"><c:url var="formAction" value="/admin/departments/${departmentId}"/></c:when>
    <c:otherwise><c:url var="formAction" value="/admin/departments"/></c:otherwise>
</c:choose>
<%@ include file="../../common/header.jsp" %>
<h1>${editing ? 'Sửa bộ môn' : 'Thêm bộ môn'}</h1>
<form:form modelAttribute="departmentForm" method="post" action="${formAction}" cssClass="form-card narrow">
    <form:errors path="*" cssClass="alert error" element="div"/>
    <form:label path="code">Mã bộ môn</form:label>
    <form:input path="code" maxlength="20" required="required"/>
    <form:errors path="code" cssClass="field-error"/>
    <form:label path="name">Tên bộ môn</form:label>
    <form:input path="name" maxlength="150" required="required"/>
    <form:errors path="name" cssClass="field-error"/>
    <label class="checkbox"><form:checkbox path="enabled"/> Bộ môn hoạt động</label>
    <div class="form-actions"><button class="primary" type="submit">Lưu</button><a class="button" href="<c:url value='/admin/departments'/>">Hủy</a></div>
</form:form>
<%@ include file="../../common/footer.jsp" %>
