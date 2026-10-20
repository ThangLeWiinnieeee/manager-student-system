<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý thông báo" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <h1>Quản lý thông báo</h1>
    <p>Đăng, chỉnh sửa, ghim hoặc ẩn thông báo tới giảng viên và sinh viên.</p>
  </div>
  <a class="button primary" href="<c:url value='/admin/notifications/new'/>">Tạo thông báo mới</a>
</div>

<div class="table-wrap" role="region" aria-label="Danh sách quản lý thông báo" tabindex="0">
  <table>
    <thead>
      <tr>
        <th>Tiêu đề</th>
        <th>Đối tượng</th>
        <th>Người tạo</th>
        <th>Ngày đăng</th>
        <th>Trạng thái</th>
        <th>Ghim</th>
        <th>Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${notifications}" var="n">
        <tr>
          <td>
            <strong><c:out value="${n.title}" /></strong>
          </td>
          <td>
            <span class="badge ${n.targetRole == 'ALL' ? 'active' : (n.targetRole == 'LECTURER' ? 'info' : 'warn')}">
              <c:out value="${n.targetRole.displayName}" />
            </span>
          </td>
          <td><c:out value="${n.createdBy.fullName}" /></td>
          <td><c:out value="${n.createdAtText}" /></td>
          <td>
            <span class="badge ${n.published ? 'active' : 'inactive'}">
              <c:out value="${n.published ? 'Hiển thị' : 'Đã ẩn'}" />
            </span>
          </td>
          <td>
            <c:choose>
              <c:when test="${n.pinned}">
                <span class="badge info">Đã ghim</span>
              </c:when>
              <c:otherwise>
                <span style="color: #94a3b8;">-</span>
              </c:otherwise>
            </c:choose>
          </td>
          <td class="actions" style="display: flex; gap: 6px;">
            <a class="button" href="<c:url value='/admin/notifications/${n.id}/edit'/>">Sửa</a>

            <form action="<c:url value='/admin/notifications/${n.id}/toggle-publish'/>" method="post" style="display: inline;">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
              <button type="submit" class="button">
                <c:out value="${n.published ? 'Ẩn' : 'Hiện'}" />
              </button>
            </form>

            <form action="<c:url value='/admin/notifications/${n.id}/toggle-pin'/>" method="post" style="display: inline;">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
              <button type="submit" class="button">
                <c:out value="${n.pinned ? 'Bỏ ghim' : 'Ghim'}" />
              </button>
            </form>

            <form action="<c:url value='/admin/notifications/${n.id}/delete'/>" method="post" style="display: inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa thông báo này?');">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
              <button type="submit" class="button" style="color: #dc2626;">Xóa</button>
            </form>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty notifications}">
        <tr>
          <td colspan="7" class="empty">
            Chưa có thông báo nào. Nhấn “Tạo thông báo mới” để đăng thông báo đầu tiên.
          </td>
        </tr>
      </c:if>
    </tbody>
  </table>
</div>

<%@ include file="../common/footer.jsp" %>
