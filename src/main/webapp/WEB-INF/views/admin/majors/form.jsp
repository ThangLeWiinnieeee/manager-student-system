<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="${editing ? 'Sửa chuyên ngành' : 'Thêm chuyên ngành'}"/>
<c:choose>
    <c:when test="${editing}"><c:url var="formAction" value="/admin/majors/${majorId}"/></c:when>
    <c:otherwise><c:url var="formAction" value="/admin/majors"/></c:otherwise>
</c:choose>
<%@ include file="../../common/header.jsp" %>
<div class="page-heading"><div><h1>${editing ? 'Sửa chuyên ngành' : 'Thêm chuyên ngành'}</h1><p>Thiết lập thông tin chuyên ngành đào tạo trong khoa.</p></div><a class="button" href="<c:url value='/admin/majors'/>">Về danh sách</a></div>
<form:form modelAttribute="majorForm" method="post" action="${formAction}" cssClass="form-card narrow">
    <h2>Thông tin chuyên ngành</h2>
    <p class="form-intro">Mã và tên chuyên ngành là thông tin bắt buộc.</p>
    <form:errors path="*" cssClass="alert error" element="div"/>
    <form:label path="code">Mã chuyên ngành</form:label>
    <form:input path="code" maxlength="20" required="required"/>
    <form:errors path="code" cssClass="field-error"/>
    <form:label path="name">Tên chuyên ngành</form:label>
    <form:input path="name" maxlength="150" required="required"/>
    <form:errors path="name" cssClass="field-error"/>
    <label class="checkbox"><form:checkbox path="enabled"/> Chuyên ngành hoạt động</label>
    <div class="form-actions"><button class="primary" type="submit">Lưu chuyên ngành</button><a class="button" href="<c:url value='/admin/majors'/>">Hủy</a></div>
</form:form>
<%@ include file="../../common/footer.jsp" %>
