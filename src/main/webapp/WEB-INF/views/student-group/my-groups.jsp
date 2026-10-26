<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Nhóm của tôi"/>
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
    <div><h1>Nhóm của tôi</h1><p>Mỗi lớp có nhóm riêng. Tham gia lớp bằng mã, sau đó tạo nhóm hoặc chấp nhận lời mời của lớp đó.</p></div>
</div>
<c:if test="${not empty success}"><p class="alert alert-success" role="status"><c:out value="${success}"/></p></c:if>
<c:if test="${not empty error}"><p class="alert alert-danger" role="alert"><c:out value="${error}"/></p></c:if>

<section class="panel">
    <h2>Tham gia lớp đề tài</h2>
    <p>Nhập mã giảng viên cung cấp để tham gia lớp. Thao tác này chưa tạo nhóm và không thay đổi nhóm ở lớp khác.</p>
    <form method="post" action="<c:url value='/student/my-groups/join'/>">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label for="joinCode">Mã tham gia lớp</label>
        <input id="joinCode" name="code" maxlength="16" minlength="16" required autocomplete="off"/>
        <button class="button primary" type="submit">Tham gia lớp</button>
    </form>
</section>
<c:if test="${empty joinedCourses}"><section class="panel"><p>Bạn chưa tham gia lớp đề tài nào. Hãy nhập mã lớp giảng viên cung cấp.</p></section></c:if>
<c:forEach items="${joinedCourses}" var="course">
    <section class="panel" id="course-${course.id}">
        <h2><c:out value="${course.courseName}"/> — <c:out value="${course.classCode}"/></h2>
        <p>Giảng viên: <strong><c:out value="${course.lecturer.fullName}" default="Chưa phân công"/></strong></p>
        <p>Đợt: <c:out value="${course.name}"/> · <c:out value="${course.stage}"/></p>
        <c:set var="hasGroup" value="false"/>
        <c:forEach items="${myGroups}" var="group">
            <c:if test="${group.roundId == course.id}">
                <c:set var="hasGroup" value="true"/>
                <h3>Nhóm: <c:out value="${group.name}"/> (#<c:out value="${group.id}"/>)</h3>
                <p>Vai trò của tôi: <strong>${group.leader ? 'Nhóm trưởng' : 'Thành viên'}</strong></p>
                <a class="button" href="<c:url value='/student/my-groups/${group.id}'/>">Chi tiết nhóm</a>
            </c:if>
        </c:forEach>
        <c:if test="${not hasGroup}">
            <p>Bạn chưa có nhóm trong lớp này.</p>
            <c:choose>
                <c:when test="${openCourseIds.contains(course.id)}">
                    <c:url var="createGroupUrl" value="/student/my-groups/new"><c:param name="roundId" value="${course.id}"/></c:url>
                    <a class="button primary" href="${createGroupUrl}">Tạo nhóm trong lớp này</a>
                    <p>Bạn cũng có thể chấp nhận lời mời vào nhóm của lớp này.</p>
                </c:when>
                <c:otherwise><p>Hiện không trong thời gian đăng ký nhóm: <c:out value="${course.svStartText}"/> – <c:out value="${course.svEndText}"/>.</p></c:otherwise>
            </c:choose>
        </c:if>
        <c:forEach items="${invitations}" var="invitation">
            <c:if test="${invitation.group.roundId == course.id}">
                <form method="post" action="<c:url value='/student/my-groups/${invitation.group.id}/invitations/respond'/>">
                    <p>Lời mời vào nhóm: <strong><c:out value="${invitation.group.name}"/></strong></p>
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <button class="button primary" type="submit" name="accept" value="true">Đồng ý tham gia</button>
                    <button class="button" type="submit" name="accept" value="false">Từ chối</button>
                </form>
            </c:if>
        </c:forEach>
    </section>
</c:forEach>
<%@ include file="../common/footer.jsp" %>
