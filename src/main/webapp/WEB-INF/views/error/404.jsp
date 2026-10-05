<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Không tìm thấy"/>
<%@ include file="../common/header.jsp" %>
<div class="error-page"><h1>404</h1><p><c:out value="${empty message ? 'Không tìm thấy dữ liệu yêu cầu.' : message}"/></p><a class="button primary" href="<c:url value='/'/>">Về trang chủ</a></div>
<%@ include file="../common/footer.jsp" %>
