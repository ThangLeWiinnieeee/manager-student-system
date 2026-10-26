<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Hội đồng bảo vệ / nghiệm thu" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <h1>Hội đồng bảo vệ / nghiệm thu</h1>
    <p>Thành lập hội đồng bảo vệ KLTN hoặc nghiệm thu NCKH, bổ nhiệm Chủ tịch, Thư ký và Ủy viên.</p>
  </div>
  <div style="display: flex; gap: 8px;">
    <a class="button" href="<c:url value='/admin/councils/assignments'/>">Phân công đánh giá</a>
    <a class="button primary" href="<c:url value='/admin/councils/new'/>">Tạo hội đồng mới</a>
  </div>
</div>

<form method="get" action="<c:url value='/admin/councils'/>" style="margin-bottom: 20px; display: flex; gap: 12px; align-items: center;">
  <label for="roundId" style="font-weight: 600;">Lọc theo đợt:</label>
  <select id="roundId" name="roundId" onchange="this.form.submit()" style="padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1;">
    <option value="">-- Tất cả các đợt --</option>
    <c:forEach items="${periods}" var="p">
      <option value="${p.id}" ${selectedRoundId == p.id ? 'selected' : ''}>
        <c:out value="${p.selectionLabel}" /> (<c:out value="${p.type.displayName}" />)
      </option>
    </c:forEach>
  </select>
</form>

<div class="table-wrap" role="region" aria-label="Danh sách hội đồng" tabindex="0">
  <table>
    <thead>
      <tr>
        <th>Tên hội đồng</th>
        <th>Môn/lớp đăng ký</th>
        <th>Địa điểm</th>
        <th>Ngày báo cáo</th>
        <th>Số thành viên</th>
        <th>Trạng thái</th>
        <th>Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${councils}" var="c">
        <tr>
          <td>
            <a href="<c:url value='/admin/councils/${c.id}'/>" style="font-weight: 600; color: #1d4ed8; text-decoration: none;">
              <c:out value="${c.name}" />
            </a>
          </td>
          <td><c:out value="${c.round.name}" /></td>
          <td><c:out value="${empty c.location ? '-' : c.location}" /></td>
          <td><c:out value="${c.reportDateText}" /></td>
          <td>
            <span class="badge ${fn:length(c.members) >= 3 && fn:length(c.members) <= 5 ? 'active' : 'warn'}">
              <c:out value="${fn:length(c.members)}" /> / 5 GV
            </span>
          </td>
          <td>
            <span class="badge ${c.status == 'ACTIVE' ? 'active' : (c.status == 'COMPLETED' ? 'info' : 'inactive')}">
              <c:out value="${c.status.displayName}" />
            </span>
          </td>
          <td class="actions" style="display: flex; gap: 6px;">
            <a class="button" href="<c:url value='/admin/councils/${c.id}'/>">Chi tiết</a>
            <a class="button" href="<c:url value='/admin/councils/${c.id}/edit'/>">Sửa</a>
            <form action="<c:url value='/admin/councils/${c.id}/delete'/>" method="post" style="display: inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa hội đồng này?');">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
              <button type="submit" class="button" style="color: #dc2626;">Xóa</button>
            </form>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty councils}">
        <tr>
          <td colspan="7" class="empty">
            Chưa có hội đồng nào được tạo trong đợt này. Nhấn “Tạo hội đồng mới” để bắt đầu.
          </td>
        </tr>
      </c:if>
    </tbody>
  </table>
</div>

<%@ include file="../common/footer.jsp" %>
