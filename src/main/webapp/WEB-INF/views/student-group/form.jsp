
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="pageTitle"
       value="${editing ? 'Sửa nhóm sinh viên' : 'Thêm nhóm sinh viên'}"/>

<c:choose>
    <c:when test="${editing}">
        <c:url var="formAction"
               value="/admin/student-groups/${groupId}"/>
    </c:when>

    <c:otherwise>
        <c:url var="formAction"
               value="/admin/student-groups"/>
    </c:otherwise>
</c:choose>

<%@ include file="../common/header.jsp" %>

<div class="page-heading">
    <div>
        <h1><c:out value="${pageTitle}"/></h1>

        <p>
            Thiết lập thông tin nhóm sinh viên
            trong môn/lớp đăng ký đề tài.
        </p>
    </div>

    <a class="button"
       href="<c:url value='/admin/student-groups'/>">
        Về danh sách
    </a>
</div>

<form:form
    modelAttribute="groupForm"
    method="post"
    action="${formAction}"
    cssClass="form-card narrow">

    <h2>Thông tin nhóm sinh viên</h2>

    <p class="form-intro">
        Vui lòng điền các thông tin bắt buộc.
    </p>

    <form:errors
        path="*"
        cssClass="alert error"
        element="div"/>

    <!-- TEN NHOM -->
    <form:label path="name">
        Tên nhóm sinh viên
    </form:label>

    <form:input
        path="name"
        maxlength="150"
        required="required"
        placeholder="Ví dụ: Nhóm CNTT 01"/>

    <form:errors
        path="name"
        cssClass="field-error"/>

    <!-- DOT DANG KY -->
    <form:label path="roundId">
        Mã môn/lớp đăng ký
    </form:label>

    <form:input
        path="roundId"
        type="number"
        min="1"
        required="required"
        placeholder="Nhập mã môn/lớp đăng ký"/>

    <form:errors
        path="roundId"
        cssClass="field-error"/>

    <p class="form-intro">
        Xem mã đăng ký môn/lớp tại trang
        <a href="<c:url value='/course-registrations'/>">
            Quản lý môn/lớp đăng ký
        </a>.
    </p>

    <!-- NUT LUU VA HUY -->
    <div class="form-actions">

        <button class="primary" type="submit">
            <c:choose>
                <c:when test="${editing}">
                    Cập nhật nhóm
                </c:when>

                <c:otherwise>
                    Tạo nhóm
                </c:otherwise>
            </c:choose>
        </button>

        <a class="button"
           href="<c:url value='/admin/student-groups'/>">
            Hủy
        </a>

    </div>

</form:form>

<%@ include file="../common/footer.jsp" %>
