<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Bảng tin thông báo" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <h1>Bảng tin thông báo</h1>
    <p>Thông tin và thông báo chính thức từ Khoa Công nghệ thông tin.</p>
  </div>
  <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
    <a class="button primary" href="<c:url value='/admin/notifications'/>">Quản lý thông báo</a>
  </c:if>
</div>

<style>
  .card-pinned {
    border-left: 4px solid #2563eb !important;
  }
</style>

<div class="card-grid" style="display: grid; gap: 16px;">
  <c:forEach items="${notifications}" var="n">
    <c:set var="roleBadge" value="warn" />
    <c:if test="${n.targetRole == 'ALL'}"><c:set var="roleBadge" value="active" /></c:if>
    <c:if test="${n.targetRole == 'LECTURER'}"><c:set var="roleBadge" value="info" /></c:if>

    <div class="card ${n.pinned ? 'card-pinned' : ''}" style="border: 1px solid var(--border-color, #e2e8f0); border-radius: 8px; padding: 20px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
        <h2 style="font-size: 1.15rem; margin: 0; font-weight: 600;">
          <c:if test="${n.pinned}">
            <span class="badge info" style="margin-right: 6px;">📌 Ghim</span>
          </c:if>
          <a href="<c:url value='/notifications/${n.id}'/>" style="color: inherit; text-decoration: none;">
            <c:out value="${n.title}" />
          </a>
        </h2>
        <span class="badge ${roleBadge}">
          <c:out value="${n.targetLabel}" />
        </span>
      </div>

      <p style="color: #64748b; font-size: 0.875rem; margin: 0 0 12px 0;">
        Đăng bởi: <strong><c:out value="${n.creatorName}" /></strong> · Ngày: <c:out value="${n.createdAtText}" />
      </p>

      <div style="color: #334155; line-height: 1.6; white-space: pre-line; margin-bottom: 16px;">
        <c:choose>
          <c:when test="${fn:length(n.content) > 250}">
            <c:out value="${fn:substring(n.content, 0, 250)}" />...
          </c:when>
          <c:otherwise>
            <c:out value="${n.content}" />
          </c:otherwise>
        </c:choose>
      </div>

      <div>
        <a class="button" href="<c:url value='/notifications/${n.id}'/>">Xem chi tiết →</a>
      </div>
    </div>
  </c:forEach>

  <c:if test="${empty notifications}">
    <div class="empty" style="padding: 40px; text-align: center; color: #64748b; background: #fff; border-radius: 8px; border: 1px dashed #cbd5e1;">
      Hiện tại chưa có thông báo nào dành cho bạn.
    </div>
  </c:if>
</div>

<%@ include file="../common/footer.jsp" %>
