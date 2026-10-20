<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Hội đồng của tôi" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <h1>Hội đồng của tôi</h1>
    <p>Danh sách các hội đồng đánh giá và bảo vệ đề tài mà bạn tham gia làm thành viên.</p>
  </div>
</div>

<div class="table-wrap" role="region" aria-label="Hội đồng của tôi" tabindex="0">
  <table>
    <thead>
      <tr>
        <th>Tên hội đồng</th>
        <th>Đợt đăng ký</th>
        <th>Địa điểm</th>
        <th>Thời gian báo cáo</th>
        <th>Trạng thái</th>
        <th>Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${councils}" var="c">
        <tr>
          <td>
            <strong><c:out value="${c.name}" /></strong>
          </td>
          <td><c:out value="${c.round.name}" /></td>
          <td><c:out value="${empty c.location ? 'Chưa xếp phòng' : c.location}" /></td>
          <td><c:out value="${c.reportDateText}" /></td>
          <td>
            <span class="badge ${c.status == 'ACTIVE' ? 'active' : (c.status == 'COMPLETED' ? 'info' : 'inactive')}">
              <c:out value="${c.status.displayName}" />
            </span>
          </td>
          <td class="actions">
            <a class="button primary" href="<c:url value='/lecturer/councils/${c.id}'/>">Xem chi tiết & Chấm điểm</a>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty councils}">
        <tr>
          <td colspan="6" class="empty">
            Bạn chưa được phân công vào hội đồng nào.
          </td>
        </tr>
      </c:if>
    </tbody>
  </table>
</div>

<%@ include file="../common/footer.jsp" %>
