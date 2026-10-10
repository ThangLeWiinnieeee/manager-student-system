<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="${editing ? 'Sửa đợt đăng ký' : 'Thêm đợt đăng ký'}"/>
<c:choose>
    <c:when test="${editing}"><c:url var="formAction" value="/admin/registration-periods/${periodId}"/></c:when>
    <c:otherwise><c:url var="formAction" value="/admin/registration-periods"/></c:otherwise>
</c:choose>
<%@ include file="../common/header.jsp" %>
<div class="page-heading"><div><h1>${editing ? 'Sửa đợt đăng ký' : 'Thêm đợt đăng ký'}</h1><p>Đợt đăng ký gồm hai giai đoạn riêng biệt: giảng viên đăng ký đề tài, sau đó sinh viên đăng ký đề tài.</p></div><a class="button" href="<c:url value='/admin/registration-periods'/>">Về danh sách</a></div>
<form:form modelAttribute="periodForm" method="post" action="${formAction}" cssClass="form-card">
    <h2>Thông tin đợt đăng ký</h2>
    <p class="form-intro">Hạn GVPB nộp điểm chỉ bắt buộc với đợt TLCN/KLTN; ngày báo cáo hội đồng chỉ bắt buộc với đợt KLTN.</p>
    <form:errors path="*" cssClass="alert error" element="div"/>
    <div class="form-grid">
        <div><form:label path="name">Tên đợt đăng ký</form:label><form:input path="name" maxlength="200" required="required" placeholder="VD: Đợt đăng ký KLTN HK1 2026-2027"/><form:errors path="name" cssClass="field-error"/></div>
        <div>
            <form:label path="type">Loại đợt</form:label>
            <form:select path="type" required="required">
                <form:option value="" label="-- Chọn loại đợt --"/>
                <form:options items="${roundTypes}" itemValue="name" itemLabel="displayName"/>
            </form:select>
            <form:errors path="type" cssClass="field-error"/>
        </div>
        <div><form:label path="gvStart">GV bắt đầu đăng ký</form:label><form:input path="gvStart" type="datetime-local" required="required"/><form:errors path="gvStart" cssClass="field-error"/></div>
        <div><form:label path="gvEnd">GV kết thúc đăng ký</form:label><form:input path="gvEnd" type="datetime-local" required="required"/><form:errors path="gvEnd" cssClass="field-error"/></div>
        <div><form:label path="svStart">SV bắt đầu đăng ký</form:label><form:input path="svStart" type="datetime-local" required="required"/><form:errors path="svStart" cssClass="field-error"/></div>
        <div><form:label path="svEnd">SV kết thúc đăng ký</form:label><form:input path="svEnd" type="datetime-local" required="required"/><form:errors path="svEnd" cssClass="field-error"/></div>
        <div><form:label path="gvpbDeadline">Hạn GVPB nộp điểm (TLCN/KLTN)</form:label><form:input path="gvpbDeadline" type="datetime-local"/><form:errors path="gvpbDeadline" cssClass="field-error"/></div>
        <div><form:label path="councilReportDate">Ngày báo cáo hội đồng (KLTN)</form:label><form:input path="councilReportDate" type="datetime-local"/><form:errors path="councilReportDate" cssClass="field-error"/></div>
    </div>
    <div class="form-actions"><button class="primary" type="submit">Lưu đợt đăng ký</button><a class="button" href="<c:url value='/admin/registration-periods'/>">Hủy</a></div>
</form:form>
<%@ include file="../common/footer.jsp" %>