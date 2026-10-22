<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="${editing ? 'Sửa đề tài' : 'Đăng ký đề tài'}"/>
<c:choose>
    <c:when test="${editing}"><c:url var="formAction" value="/topics/${topicId}"/></c:when>
    <c:otherwise><c:url var="formAction" value="/topics"/></c:otherwise>
</c:choose>
<%@ include file="../common/header.jsp" %>
<div class="page-heading"><div><h1>${editing ? 'Sửa đề tài' : 'Đăng ký đề tài'}</h1><p>Đề tài cần một hoặc hai giảng viên hướng dẫn và phải gửi duyệt trong thời gian đăng ký của đợt.</p></div><a class="button" href="<c:url value='/topics'/>">Về danh sách</a></div>
<c:if test="${not empty rejectionReason}"><div class="alert error" role="alert"><strong>Lý do từ chối:</strong> <c:out value="${rejectionReason}"/></div></c:if>
<form:form modelAttribute="topicForm" method="post" action="${formAction}" cssClass="form-card">
    <h2>Thông tin đề tài</h2>
    <p class="form-intro">Chọn “Gửi duyệt” để gửi khoa xét duyệt, hoặc “Lưu nháp” để tiếp tục chỉnh sửa sau.</p>
    <form:errors path="*" cssClass="alert error" element="div"/>
    <form:label path="title">Tên đề tài</form:label>
    <form:input path="title" maxlength="200" required="required"/>
    <form:errors path="title" cssClass="field-error"/>
    <form:label path="description">Mô tả nội dung</form:label>
    <form:textarea path="description" rows="4" maxlength="2000"/>
    <form:errors path="description" cssClass="field-error"/>
    <div class="form-grid">
        <div>
            <form:label path="roundId">Đợt đăng ký</form:label>
            <form:select path="roundId" required="required">
                <form:option value="" label="-- Chọn đợt đăng ký --"/>
                <c:forEach items="${periods}" var="period">
                    <form:option value="${period.id}" label="${period.name} - ${period.type.displayName}"/>
                </c:forEach>
            </form:select>
            <form:errors path="roundId" cssClass="field-error"/>
        </div>
        <div>
            <form:label path="majorId">Chuyên ngành phù hợp (tùy chọn)</form:label>
            <form:select path="majorId">
                <form:option value="" label="-- Không giới hạn chuyên ngành --"/>
                <form:options items="${majors}" itemValue="id" itemLabel="name"/>
            </form:select>
            <form:errors path="majorId" cssClass="field-error"/>
        </div>
    </div>
    <form:label path="advisorIds">Giảng viên hướng dẫn (giữ Ctrl để chọn tối đa 2)</form:label>
    <form:select path="advisorIds" multiple="multiple" size="6" required="required">
        <form:options items="${lecturers}" itemValue="id" itemLabel="fullName"/>
    </form:select>
    <form:errors path="advisorIds" cssClass="field-error"/>
    <div class="form-actions">
        <button class="primary" type="submit" name="action" value="submit">Gửi duyệt</button>
        <button type="submit" name="action" value="save">Lưu nháp</button>
        <a class="button" href="<c:url value='/topics'/>">Hủy</a>
    </div>
</form:form>
<%@ include file="../common/footer.jsp" %>
