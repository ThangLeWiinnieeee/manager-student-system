<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Tổng hợp đánh giá đề tài" />
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
<div>
<h1>Tổng hợp đánh giá đề tài</h1>
<p>
<strong>
<c:out value="${summary.registration.group.name}"/>
</strong> — <c:out value="${summary.topic.title}"/>
</p>
<p>
<c:out value="${summary.topic.round.selectionLabel}"/> · <c:out value="${summary.topic.round.type.displayName}"/>
</p>
</div>
</div>
<section class="panel">
<p>
<c:out value="${summary.formula}"/>
</p>
<p>Trạng thái: <strong>${summary.isFullyEvaluated ? 'Đã đủ điểm' : 'Chờ hoàn tất đánh giá'}</strong>
</p>
<c:if test="${!summary.legacyAssessment && summary.topic.round.type == 'KLTN'}">
<p>Quyết định GVPB:
<strong>
<c:choose>
<c:when test="${summary.defenseAllowed == null}">Chưa quyết định</c:when>
<c:when test="${summary.defenseAllowed}">Được bảo vệ</c:when>
<c:otherwise>Không được bảo vệ</c:otherwise>
</c:choose>
</strong>
</p>
</c:if>
<div class="table-wrap">
<table>
<thead>
<tr>
<th>Giảng viên</th>
<th>Vai trò</th>
<th>Trạng thái</th>
<th>Điểm / 10</th>
<th>Nhận xét</th>
</tr>
</thead>
<tbody>
<c:if test="${summary.supporter != null}">
<tr>
<td>
<c:out value="${summary.supporter.fullName}"/>
</td>
<td>GV hỗ trợ</td>
<td>
<c:out value="${summary.supporterEvaluation.status.displayName}" default="Chưa chấm"/>
</td>
<td>
<c:out value="${summary.supporterScore}" default="—"/>
</td>
<td>
<c:out value="${summary.supporterEvaluation.comments}"/>
</td>
</tr>
</c:if>
<c:if test="${summary.reviewer != null}">
<tr>
<td>
<c:out value="${summary.reviewer.fullName}"/>
</td>
<td>${summary.topic.round.type == 'MON_HOC' ? 'GV chấm môn học' : 'GVPB'}</td>
<td>
<c:out value="${summary.reviewerEvaluation.status.displayName}" default="Chưa chấm"/>
</td>
<td>
<c:out value="${summary.reviewerScore}" default="—"/>
</td>
<td>
<c:out value="${summary.reviewerEvaluation.comments}"/>
</td>
</tr>
</c:if>
<c:forEach items="${summary.memberScores}" var="ms">
<tr>
<td>
<c:out value="${ms.lecturer.fullName}"/>
</td>
<td>
<c:out value="${ms.role.displayName}"/>
</td>
<td>
<c:out value="${ms.evaluation.status.displayName}" default="Chưa chấm"/>
</td>
<td>
<c:out value="${ms.evaluation.status == 'SUBMITTED' ? ms.evaluation.totalScore : '—'}"/>
</td>
<td>
<c:out value="${ms.evaluation.comments}"/>
</td>
</tr>
</c:forEach>
</tbody>
</table>
</div>
<c:if test="${summary.needsCouncil}">
<p>Trung bình hội đồng: <c:out value="${summary.councilAvgScore}" default="Chưa đủ điểm"/>
</p>
</c:if>
<p>Điểm tổng kết: <strong>
<c:out value="${summary.finalScore}" default="Chưa đủ điểm"/>
</strong> · <c:out value="${summary.grade}"/>
</p>
<c:if test="${summary.finalResult != null}">
<p>Kết luận: <c:out value="${summary.finalResult.conclusion}"/>
</p>
<p>${summary.finalResult.published ? 'Khoa đã xác nhận và công bố' : 'Chưa công bố cho sinh viên'}</p>
</c:if>
</section>
<c:if test="${summary.needsCouncil}">
<section class="panel">
<h2>Xác nhận của Chủ tịch hội đồng</h2>
<c:choose>
<c:when test="${summary.finalResult.presidentConfirmed}">
<p>Đã xác nhận lúc <c:out value="${summary.finalResult.presidentConfirmedAtText}"/>
</p>
</c:when>
<c:when test="${isPresident && summary.isFullyEvaluated}">
<form action="<c:url value='/council/results/registration/${summary.registration.id}/confirm'/>" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<label for="conclusion">Kết luận hội đồng</label>
<textarea id="conclusion" name="conclusion" rows="3" maxlength="1000" required>
</textarea>
<button class="button primary" type="submit">Xác nhận kết quả</button>
</form>
</c:when>
<c:otherwise>
<p>Chờ đủ điểm và Chủ tịch hội đồng xác nhận.</p>
</c:otherwise>
</c:choose>
</section>
</c:if>
<c:if test="${pageContext.request.isUserInRole('ADMIN') || pageContext.request.isUserInRole('DEAN')}">
<c:if test="${summary.isFullyEvaluated && (!summary.needsCouncil || summary.finalResult.presidentConfirmed) && !summary.finalResult.published}">
<form action="<c:url value='/dean/results/registration/${summary.registration.id}/publish'/>" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<input type="hidden" name="roundId" value="${summary.topic.round.id}"/>
<button class="button primary" type="submit" onclick="return confirm('Xác nhận và công bố kết quả này cho sinh viên?')">Khoa xác nhận và công bố</button>
</form>
</c:if>
</c:if>
<%@ include file="../common/footer.jsp" %>
