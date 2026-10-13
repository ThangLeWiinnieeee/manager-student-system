<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý đợt đăng ký" />
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
  <div>
    <h1>Quản lý đợt đăng ký</h1>
    <p>
      Thiết lập các đợt đăng ký đề tài theo hai giai đoạn: giảng viên đăng ký
      trước, sinh viên đăng ký sau.
    </p>
  </div>
  <a
    class="button primary"
    href="<c:url value='/admin/registration-periods/new'/>"
    >Thêm đợt đăng ký</a
  >
</div>
<div
  class="table-wrap"
  role="region"
  aria-label="Danh sách đợt đăng ký"
  tabindex="0"
>
  <table aria-label="Danh sách đợt đăng ký">
    <thead>
      <tr>
        <th>Tên đợt</th>
        <th>Loại</th>
        <th>Giai đoạn GV</th>
        <th>Giai đoạn SV</th>
        <th>Hạn GVPB nộp điểm</th>
        <th>Ngày báo cáo hội đồng</th>
        <th>Giai đoạn hiện tại</th>
        <th>Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${periods}" var="period">
        <tr>
          <td><c:out value="${period.name}" /></td>
          <td><c:out value="${period.type.displayName}" /></td>
          <td>
            <c:out value="${period.gvStartText}" /> →
            <c:out value="${period.gvEndText}" />
          </td>
          <td>
            <c:out value="${period.svStartText}" /> →
            <c:out value="${period.svEndText}" />
          </td>
          <td><c:out value="${period.gvpbDeadlineText}" /></td>
          <td><c:out value="${period.councilReportDateText}" /></td>
          <td>
            <span
              class="badge ${period.stage == 'Đã kết thúc' ? 'inactive' : 'active'}"
              ><c:out value="${period.stage}"
            /></span>
          </td>
          <td class="actions">
            <a
              class="button"
              href="<c:url value='/admin/registration-periods/${period.id}/edit'/>"
              >Sửa</a
            >
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty periods}"
        ><tr>
          <td colspan="8" class="empty">
            Chưa có đợt đăng ký nào. Chọn “Thêm đợt đăng ký” để tạo đợt đầu
            tiên.
          </td>
        </tr></c:if
      >
    </tbody>
  </table>
</div>
<%@ include file="../common/footer.jsp" %>
