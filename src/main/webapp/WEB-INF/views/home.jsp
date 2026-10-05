<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Trang chủ"/>
<%@ include file="common/header.jsp" %>
<h1>Trang chủ</h1>
<p>Xin chào, <strong><c:out value="${username}"/></strong>.</p>
<div class="stats">
    <section><strong><c:out value="${userCount}"/></strong><span>Tài khoản</span></section>
    <section><strong><c:out value="${departmentCount}"/></strong><span>Bộ môn</span></section>
    <section><strong>0</strong><span>Đề tài</span></section>
</div>
<p class="muted">Các module đợt đăng ký, đề tài, nhóm sinh viên và hội đồng sẽ được bổ sung bởi các thành viên tiếp theo.</p>
<%@ include file="common/footer.jsp" %>
