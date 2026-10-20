<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Tổng hợp & Công bố kết quả đề tài" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <h1>Tổng hợp & Công bố kết quả đề tài</h1>
    <p>Trưởng khoa xem kết quả điểm, xếp loại tốt nghiệp và thực hiện công bố hoặc thu hồi kết quả.</p>
  </div>
</div>

<form method="get" action="<c:url value='/dean/results'/>" style="margin-bottom: 20px; display: flex; gap: 12px; align-items: center;">
  <label for="roundId" style="font-weight: 600;">Chọn đợt:</label>
  <select id="roundId" name="roundId" onchange="this.form.submit()" style="padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1;">
    <c:forEach items="${periods}" var="p">
      <option value="${p.id}" ${selectedRoundId == p.id ? 'selected' : ''}>
        <c:out value="${p.name}" /> (<c:out value="${p.type.displayName}" />)
      </option>
    </c:forEach>
  </select>
</form>

<div class="table-wrap" role="region" aria-label="Danh sách kết quả" tabindex="0">
  <table>
    <thead>
      <tr>
        <th style="width: 25%;">Đề tài</th>
        <th style="width: 15%;">Hội đồng</th>
        <th style="width: 10%;">Điểm GVPB</th>
        <th style="width: 10%;">Điểm HĐ</th>
        <th style="width: 10%;">Tổng điểm</th>
        <th style="width: 10%;">Xếp loại</th>
        <th style="width: 10%;">Công bố</th>
        <th style="width: 10%;">Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${results}" var="r">
        <tr>
          <td>
            <a href="<c:url value='/council/results/topic/${r.topic.id}'/>" style="font-weight: 600; color: #1d4ed8; text-decoration: none;">
              <c:out value="${r.topic.title}" />
            </a>
          </td>
          <td><c:out value="${r.council.name}" /></td>
          <td><c:out value="${r.reviewerScore != null ? r.reviewerScore : '-'}" /></td>
          <td><c:out value="${r.councilAvgScore}" /></td>
          <td><strong style="color: #2563eb; font-size: 1.1rem;"><c:out value="${r.finalScore}" /></strong></td>
          <td>
            <span class="badge ${r.grade == 'XUẤT SẮC' || r.grade == 'GIỎI' ? 'active' : (r.grade == 'KHÁ' ? 'info' : 'warn')}">
              <c:out value="${r.grade}" />
            </span>
          </td>
          <td>
            <span class="badge ${r.published ? 'active' : 'inactive'}">
              <c:out value="${r.published ? 'Đã công bố' : 'Chưa công bố'}" />
            </span>
          </td>
          <td class="actions">
            <c:choose>
              <c:when test="${!r.published}">
                <form action="<c:url value='/dean/results/topic/${r.topic.id}/publish'/>" method="post" style="display: inline;">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                  <input type="hidden" name="roundId" value="${selectedRoundId}" />
                  <button type="submit" class="button primary" onclick="return confirm('Công bố kết quả đề tài này cho sinh viên?');">
                    Công bố
                  </button>
                </form>
              </c:when>
              <c:otherwise>
                <form action="<c:url value='/dean/results/topic/${r.topic.id}/revoke'/>" method="post" style="display: inline;">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                  <input type="hidden" name="roundId" value="${selectedRoundId}" />
                  <button type="submit" class="button" style="color: #dc2626;" onclick="return confirm('Thu hồi công bố kết quả đề tài này?');">
                    Thu hồi
                  </button>
                </form>
              </c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty results}">
        <tr>
          <td colspan="8" class="empty">
            Chưa có kết quả đề tài nào được Chủ tịch hội đồng xác nhận trong đợt này.
          </td>
        </tr>
      </c:if>
    </tbody>
  </table>
</div>

<%@ include file="../common/footer.jsp" %>
