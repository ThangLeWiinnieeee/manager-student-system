<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Chi tiết đợt đăng ký" />
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
    <div>
        <h1><c:out value="${period.name}" /></h1>
        <span class="badge ${period.ended ? 'inactive' : 'active'}"><c:out value="${period.stage}" /></span>
    </div>
    <div class="actions">
        <a class="button" href="<c:url value='/admin/registration-periods'/>">Về danh sách</a>
        <c:if test="${manager}"><a class="button primary" href="<c:url value='/admin/registration-periods/${period.id}/edit'/>">Sửa đợt đăng ký</a></c:if>
    </div>
</div>
<div class="profile-layout">
    <section class="panel">
        <h2>Thông tin đợt đăng ký</h2>
        <dl class="profile">
            <dt>Mã đợt</dt><dd><c:out value="${period.id}" /></dd>
            <dt>Loại</dt><dd><c:out value="${period.type.displayName}" /></dd>
        </dl>
    </section>
    <section class="panel">
        <h2>Lịch đăng ký và đánh giá</h2>
        <dl class="profile">
            <dt>GV đăng ký từ</dt><dd><c:out value="${period.gvStartText}" /></dd>
            <dt>GV đăng ký đến</dt><dd><c:out value="${period.gvEndText}" /></dd>
            <dt>SV đăng ký từ</dt><dd><c:out value="${period.svStartText}" /></dd>
            <dt>SV đăng ký đến</dt><dd><c:out value="${period.svEndText}" /></dd>
            <c:if test="${not empty period.gvpbDeadline}">
                <dt>Hạn GVPB nộp điểm</dt><dd><c:out value="${period.gvpbDeadlineText}" /></dd>
            </c:if>
            <c:if test="${not empty period.councilReportDate}">
                <dt>Ngày báo cáo hội đồng</dt><dd><c:out value="${period.councilReportDateText}" /></dd>
            </c:if>
        </dl>
    </section>
</div>
<section class="panel">
    <div class="panel-heading"><h2>Môn/lớp tham gia</h2>
        <c:if test="${pageContext.request.isUserInRole('LECTURER') and period.gvOpen}"><a class="button primary" href="<c:url value='/course-registrations/new?periodId=${period.id}'/>">Đăng ký môn/lớp</a></c:if>
    </div>
    <c:if test="${empty courses}"><p>Chưa có giảng viên đăng ký môn/lớp trong đợt.</p></c:if>
    <ul><c:forEach items="${courses}" var="course"><li><c:out value="${course.courseLabel}"/> — <c:out value="${course.lecturer.fullName}" default="Chưa thiết lập GV"/>
        <c:if test="${manager}"><a href="<c:url value='/course-registrations/${course.id}'/>">Xem chi tiết</a></c:if>
    </li></c:forEach></ul>
</section>
<%@ include file="../common/footer.jsp" %>
