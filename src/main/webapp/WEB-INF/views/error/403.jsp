<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Không có quyền truy cập"/>
<%@ include file="../common/header.jsp" %>
<div class="error-page"><h1>403</h1><p>Bạn không có quyền truy cập chức năng này.</p><a class="button primary" href="<c:url value='/'/>">Về trang chủ</a></div>
<%@ include file="../common/footer.jsp" %>
