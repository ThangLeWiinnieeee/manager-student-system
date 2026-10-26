<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty registrationNotices}">
    <section class="panel" aria-label="Thông báo mở đăng ký">
        <h2>Thông báo mở đăng ký</h2>
        <c:forEach items="${registrationNotices}" var="noticePeriod">
            <article class="card">
                <h3><c:out value="${noticePeriod.name}"/></h3>
                <c:choose>
                    <c:when test="${pageContext.request.isUserInRole('STUDENT')}">
                        <p>Đã mở đăng ký dành cho sinh viên. Bạn có thể tham gia lớp, tạo hoặc tham gia nhóm và đăng ký đề tài.</p>
                        <p>Thời gian: <c:out value="${noticePeriod.svStartText}"/> – <c:out value="${noticePeriod.svEndText}"/>.</p>
                        <a class="button primary" href="<c:url value='/student/my-groups'/>">Đến nhóm của tôi</a>
                    </c:when>
                    <c:otherwise>
                        <p>Đã mở đăng ký môn/lớp và đề tài dành cho giảng viên.</p>
                        <p>Thời gian: từ <c:out value="${noticePeriod.gvStartText}"/> đến trước <c:out value="${noticePeriod.gvEndText}"/>.</p>
                        <a class="button primary" href="<c:url value='/admin/registration-periods/${noticePeriod.id}'/>">Xem đợt đăng ký</a>
                    </c:otherwise>
                </c:choose>
            </article>
        </c:forEach>
    </section>
</c:if>
