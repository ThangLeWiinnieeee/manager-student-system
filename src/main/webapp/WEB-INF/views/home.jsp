<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Tổng quan"/>
<%@ include file="common/header.jsp" %>
<div class="page-heading">
    <div><h1>Tổng quan</h1><p>Xin chào <c:out value="${fullName}"/>, đây là không gian làm việc của bạn.</p></div>
    <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
        <a class="button primary" href="<c:url value='/admin/users/new'/>"><span aria-hidden="true">+</span> Thêm tài khoản</a>
    </c:if>
</div>
<c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
<div class="stats" aria-label="Tổng quan hệ thống">
    <section><span>Tài khoản</span><div class="stat-icon"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6m2 3a5 5 0 0 1 3 5v2"/></svg></div><strong><c:out value="${userCount}"/></strong><small>Người dùng trong hệ thống</small></section>
    <section><span>Bộ môn</span><div class="stat-icon"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 21V5l8-3 8 3v16H4M9 21v-5h6v5M8 8h1m6 0h1M8 12h1m6 0h1"/></svg></div><strong><c:out value="${departmentCount}"/></strong><small>Đơn vị chuyên môn của khoa</small></section>
    <section><span>Đề tài</span><div class="stat-icon"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 7V4h6l3 3h9v13H3z"/><path d="M3 10h18"/></svg></div><strong aria-label="Chưa có dữ liệu">—</strong><small>Chưa mở chức năng đăng ký</small></section>
</div>
</c:if>
<div class="dashboard-grid">
    <section class="panel">
        <div class="panel-heading"><h2>Không gian đề tài</h2><span class="neutral-badge">Chưa mở đăng ký</span></div>
        <div class="topic-empty">
            <svg viewBox="0 0 180 128" fill="none" aria-hidden="true">
                <ellipse cx="90" cy="112" rx="68" ry="8" fill="#f0f0f9"/>
                <rect x="47" y="13" width="73" height="92" rx="7" transform="rotate(-9 47 13)" fill="#eeedfc" stroke="#d9d7f2"/>
                <rect x="66" y="8" width="71" height="94" rx="7" transform="rotate(8 66 8)" fill="white" stroke="#d9d7f2"/>
                <path d="m82 30 35 5m-37 9 27 4m-29 10 31 4" stroke="#c9c6eb" stroke-width="4" stroke-linecap="round"/>
                <path d="M31 63a7 7 0 0 1 7-7h36l9 10h60a7 7 0 0 1 7 7l-7 32a8 8 0 0 1-8 6H43a8 8 0 0 1-8-7z" fill="#e8e6ff" stroke="#c6c2f2"/>
                <path d="M37 77h106l-6 27H42z" fill="#f3f2ff"/>
                <circle cx="131" cy="93" r="17" fill="#625bdd"/>
                <path d="M124 93h14m-7-7v14" stroke="white" stroke-width="2" stroke-linecap="round"/>
                <path d="M26 37h8m-4-4v8m122 9h6m-3-3v6" stroke="#b9b5e3" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <h3>Sẵn sàng cho những đề tài mới</h3>
            <p>Chức năng đăng ký đề tài chưa được mở. Trong thời gian này, hãy kiểm tra thông tin hồ sơ để chuẩn bị cho đợt đăng ký.</p>
        </div>
    </section>
    <section class="panel">
        <h2>Truy cập nhanh</h2>
        <p>Các thao tác thường dùng</p>
        <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
            <a class="quick-link" href="<c:url value='/admin/users'/>"><span class="quick-icon"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6m2 3a5 5 0 0 1 3 5v2"/></svg></span><span class="quick-copy"><strong>Quản lý tài khoản</strong><small>Thông tin và phân quyền người dùng</small></span><span aria-hidden="true">›</span></a>
            <a class="quick-link" href="<c:url value='/admin/departments'/>"><span class="quick-icon"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 21V5l8-3 8 3v16H4M9 21v-5h6v5M8 8h1m6 0h1M8 12h1m6 0h1"/></svg></span><span class="quick-copy"><strong>Danh mục bộ môn</strong><small>Các đơn vị chuyên môn trong khoa</small></span><span aria-hidden="true">›</span></a>
        </c:if>
        <a class="quick-link" href="<c:url value='/profile'/>"><span class="quick-icon"><svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></svg></span><span class="quick-copy"><strong>Hồ sơ cá nhân</strong><small>Thông tin tài khoản và bảo mật</small></span><span aria-hidden="true">›</span></a>
    </section>
</div>
<section class="panel process-panel">
    <h2>Hành trình thực hiện đề tài</h2>
    <p>Các giai đoạn theo kế hoạch của khoa</p>
    <ol class="workflow">
        <li><strong>Công bố đề tài</strong><small>Giảng viên đề xuất, khoa xét duyệt và công bố danh sách.</small></li>
        <li><strong>Đăng ký và thực hiện</strong><small>Nhóm sinh viên đăng ký, thực hiện và nộp báo cáo.</small></li>
        <li><strong>Đánh giá kết quả</strong><small>Phản biện, chấm điểm và công bố kết quả đề tài.</small></li>
    </ol>
</section>
<%@ include file="common/footer.jsp" %>
