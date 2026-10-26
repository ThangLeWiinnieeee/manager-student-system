<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Chi tiết môn/lớp" />
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
    <div>
        <h1><c:out value="${period.name}" /></h1>
        <span class="badge ${period.ended and not period.reviewerGradingOpen ? 'inactive' : 'active'}"><c:out value="${period.stage}" /></span>
    </div>
    <div class="actions">
        <a class="button" href="<c:url value='/course-registrations'/>">Về danh sách</a>
        <a class="button primary" href="<c:url value='/course-registrations/${period.id}/edit'/>">Sửa môn/lớp</a>
    </div>
</div>
<section class="panel">
<h2>Mã tham gia lớp đề tài</h2>
<p>Chỉ chia sẻ mã này cho sinh viên của lớp. SV nhập tại trang “Nhóm của tôi” trong thời gian đăng ký SV.</p>
<p><strong><c:out value="${period.joinCode}" default="Chưa tạo mã"/></strong> · ${period.joiningEnabled ? 'Đang cho phép tham gia' : 'Đã đóng mã'}</p>
<div class="actions">
<form method="post" action="<c:url value='/course-registrations/${period.id}/join-code'/>">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<input type="hidden" name="renew" value="true"/><input type="hidden" name="enabled" value="${period.joiningEnabled}"/>
<button class="button" type="submit" onclick="return confirm('Tạo mã mới? Mã cũ sẽ hết hiệu lực; sinh viên đã tham gia vẫn ở trong lớp.')">Tạo / đổi mã</button></form>
<form method="post" action="<c:url value='/course-registrations/${period.id}/join-code'/>">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/><input type="hidden" name="enabled" value="${!period.joiningEnabled}"/>
<button class="button" type="submit">${period.joiningEnabled ? 'Đóng mã tham gia' : 'Mở mã tham gia'}</button></form>
</div></section>
<div class="profile-layout">
    <section class="panel">
        <h2>Thông tin môn/lớp</h2>
        <dl class="profile">
            <dt>Mã đăng ký</dt><dd><c:out value="${period.id}" /></dd>
            <dt>Đợt chung</dt><dd><a href="<c:url value='/admin/registration-periods/${period.period.id}'/>"><c:out value="${period.period.name}"/></a></dd>
            <dt>Loại</dt><dd><c:out value="${period.type.displayName}" /></dd>
            <dt>Môn học / hoạt động</dt><dd><c:out value="${empty period.courseName ? 'Chưa thiết lập' : period.courseName}" /></dd>
            <dt>Mã lớp / mã đợt</dt><dd><c:out value="${empty period.classCode or period.classCode == 'LEGACY' ? 'Chưa thiết lập' : period.classCode}" /></dd>
            <dt>Bộ môn</dt><dd><c:out value="${period.department.name}" default="Chưa thiết lập" /></dd>
            <dt>GV phụ trách</dt><dd><c:out value="${period.lecturer.fullName}" default="Chưa thiết lập" /></dd>
            <dt>Hình thức đăng ký</dt><dd><c:out value="${period.registrationMode.displayName}" /></dd>
            <dt>Giới hạn nhóm</dt><dd>Tối đa <c:out value="${period.maxMembers}" /> sinh viên/nhóm</dd>
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
<c:if test="${manager}"><section class="panel"><h2>Điều chỉnh người phụ trách</h2>
<p>Bàn giao nhiệm vụ chưa hoàn tất và xem lịch sử thay đổi.</p>
<a class="button" href="<c:url value='/admin/handovers?courseId=${period.id}'/>">Bàn giao giảng viên</a></section></c:if>
<%@ include file="../common/footer.jsp" %>
