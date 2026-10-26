<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Xác nhận và công bố kết quả" />
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
<div>
<h1>Xác nhận và công bố kết quả</h1>
<p>Khoa kiểm tra điểm từng nhóm trước khi công bố cho sinh viên.</p>
</div>
</div>
<form method="get" action="<c:url value='/dean/results'/>">
<label for="roundId">Môn/lớp đề tài</label>
<select id="roundId" name="roundId" onchange="this.form.submit()">
<c:forEach items="${periods}" var="p">
<option value="${p.id}" ${selectedRoundId == p.id ? 'selected' : ''}>
<c:out value="${p.selectionLabel}"/>
</option>
</c:forEach>
</select>
<button class="button" type="submit">Xem</button>
</form>
<div class="table-wrap">
<table>
<thead>
<tr>
<th>Nhóm / Đề tài</th>
<th>Loại</th>
<th>Điểm</th>
<th>Trạng thái</th>
<th>Thao tác</th>
</tr>
</thead>
<tbody>
<c:forEach items="${results}" var="s">
<tr>
<td>
<a href="<c:url value='/council/results/registration/${s.registration.id}'/>">
<strong>
<c:out value="${s.registration.group.name}"/>
</strong> — <c:out value="${s.topic.title}"/>
</a>
</td>
<td>
<c:out value="${s.topic.round.type.displayName}"/>
</td>
<td>
<c:out value="${s.finalScore}" default="Chưa đủ điểm"/>
</td>
<td>
<c:choose>
<c:when test="${s.finalResult.published}">Đã công bố</c:when>
<c:when test="${!s.isFullyEvaluated}">Đang chấm</c:when>
<c:when test="${s.needsCouncil && !s.finalResult.presidentConfirmed}">Chờ Chủ tịch xác nhận</c:when>
<c:otherwise>Chờ Khoa xác nhận</c:otherwise>
</c:choose>
</td>
<td>
<c:choose>
<c:when test="${s.finalResult.published}">
<form method="post" action="<c:url value='/dean/results/registration/${s.registration.id}/revoke'/>">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<input type="hidden" name="roundId" value="${selectedRoundId}"/>
<button class="button" type="submit">Thu hồi công bố</button>
</form>
</c:when>
<c:when test="${s.isFullyEvaluated && (!s.needsCouncil || s.finalResult.presidentConfirmed)}">
<form method="post" action="<c:url value='/dean/results/registration/${s.registration.id}/publish'/>">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<input type="hidden" name="roundId" value="${selectedRoundId}"/>
<button class="button primary" type="submit" onclick="return confirm('Khoa xác nhận và công bố kết quả này?')">Xác nhận và công bố</button>
</form>
</c:when>
<c:otherwise>Chưa đủ điều kiện</c:otherwise>
</c:choose>
</td>
</tr>
</c:forEach>
<c:if test="${empty results}">
<tr>
<td colspan="5" class="empty">Chưa có nhóm được phân công đánh giá trong lớp này.</td>
</tr>
</c:if>
</tbody>
</table>
</div>
<%@ include file="../common/footer.jsp" %>
