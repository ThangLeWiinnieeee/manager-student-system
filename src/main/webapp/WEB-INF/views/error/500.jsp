<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Lỗi hệ thống"/>
<%@ include file="../common/header.jsp" %>
<div class="error-page"><h1>500</h1><p>Hệ thống không thể xử lý yêu cầu. Vui lòng thử lại.</p><a class="button primary" href="<c:url value='/'/>">Về trang chủ</a></div>
<%@ include file="../common/footer.jsp" %>
