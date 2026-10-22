<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Chấm điểm & Đánh giá đề tài" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <h1>Chấm điểm & Đánh giá đề tài</h1>
    <p>Danh sách các đề tài bạn được phân công làm Giảng viên phản biện hoặc Thành viên hội đồng chấm.</p>
  </div>
</div>

<div class="table-wrap" role="region" aria-label="Danh sách đề tài cần chấm" tabindex="0">
  <table>
    <thead>
      <tr>
        <th style="width: 35%;">Tên đề tài</th>
        <th style="width: 15%;">Vai trò chấm</th>
        <th style="width: 20%;">Hội đồng</th>
        <th style="width: 15%;">Trạng thái chấm</th>
        <th style="width: 15%;">Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${items}" var="it">
        <tr>
          <td>
            <a href="<c:url value='/topics/${it.topic.id}'/>" style="font-weight: 600; color: #1d4ed8; text-decoration: none;">
              <c:out value="${it.topic.title}" />
            </a>
            <div style="font-size: 0.85rem; color: #64748b; margin-top: 4px;">
              Chuyên ngành: <c:out value="${it.topic.major.name}" default="Không giới hạn" />
            </div>
          </td>
          <td>
            <span class="badge ${it.evaluationType == 'REVIEWER' ? 'info' : 'active'}">
              <c:out value="${it.roleName}" />
            </span>
          </td>
          <td><c:out value="${it.councilName}" /></td>
          <td>
            <c:choose>
              <c:when test="${it.status == 'SUBMITTED'}">
                <span class="badge active">Đã nộp (<c:out value="${it.totalScore}" /> đ)</span>
              </c:when>
              <c:when test="${it.status == 'DRAFT'}">
                <span class="badge warn">Lưu nháp (<c:out value="${it.totalScore}" /> đ)</span>
              </c:when>
              <c:otherwise>
                <span class="badge inactive">Chưa chấm</span>
              </c:otherwise>
            </c:choose>
          </td>
          <td class="actions">
            <a class="button primary" href="<c:url value='/lecturer/evaluations/topic/${it.topic.id}'/>">
              <c:out value="${it.status == 'SUBMITTED' ? 'Xem phiếu chấm' : 'Vào chấm điểm'}" />
            </a>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty items}">
        <tr>
          <td colspan="5" class="empty">
            Hiện tại bạn chưa được phân công phản biện hoặc chấm hội đồng cho đề tài nào.
          </td>
        </tr>
      </c:if>
    </tbody>
  </table>
</div>

<%@ include file="../common/footer.jsp" %>
