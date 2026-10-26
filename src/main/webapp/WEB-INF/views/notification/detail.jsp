<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${notification.title}" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <a href="<c:url value='/notifications'/>" style="text-decoration: none; color: #64748b; font-size: 0.9rem;">← Quay lại danh sách thông báo</a>
    <h1 style="margin-top: 8px;">
      <c:if test="${notification.pinned}">
        <span class="badge info" style="margin-right: 6px;">📌 Ghim</span>
      </c:if>
      <c:out value="${notification.title}" />
    </h1>
    <p>
      Đối tượng: <span class="badge ${notification.targetRole == 'ALL' ? 'active' : 'info'}"><c:out value="${notification.targetLabel}" /></span>
      · Người đăng: <strong><c:out value="${notification.creatorName}" /></strong>
      · Thời gian: <c:out value="${notification.createdAtText}" />
    </p>
  </div>
  <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
    <a class="button" href="<c:url value='/admin/notifications/${notification.id}/edit'/>">Chỉnh sửa thông báo</a>
  </c:if>
</div>

<div class="card" style="background: #fff; padding: 24px; border-radius: 8px; border: 1px solid var(--border-color, #e2e8f0); line-height: 1.8; font-size: 1.05rem; white-space: pre-wrap; color: #1e293b;">
<c:out value="${notification.content}" />
</div>

<%@ include file="../common/footer.jsp" %>
