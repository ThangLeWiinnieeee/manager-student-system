<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Phân công đánh giá đề tài" />
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
<div>
<h1>Phân công đánh giá đề tài</h1>
<a class="button" href="<c:url value='/admin/councils'/>">Quản lý hội đồng bảo vệ / nghiệm thu</a>
<p>Môn học: một GV chấm · TLCN: GV hỗ trợ rồi GVPB · KLTN: GVPB duyệt bảo vệ trước hội đồng · NCKH: hội đồng nghiệm thu.</p>
</div>
</div>
<form method="get" action="<c:url value='/admin/councils/assignments'/>">
<label for="roundId">Môn/lớp đề tài</label>
<select id="roundId" name="roundId" onchange="this.form.submit()">
<c:forEach items="${periods}" var="p">
<option value="${p.id}" ${selectedRoundId == p.id ? 'selected' : ''}>
<c:out value="${p.selectionLabel}"/> — <c:out value="${p.type.displayName}"/>
</option>
</c:forEach>
</select>
<button class="button" type="submit">Xem</button>
</form>
<div class="table-wrap" role="region" aria-label="Phân công đánh giá" tabindex="0">
<table>
<thead>
<tr>
<th>Nhóm / Đề tài</th>
<th>GV hỗ trợ TLCN</th>
<th>GV chấm / GVPB</th>
<th>Hội đồng</th>
<th>Thao tác</th>
</tr>
</thead>
<tbody>
<c:forEach items="${registrations}" var="reg">
<c:set var="topic" value="${reg.topic}"/>
<c:set var="asgn" value="${assignmentsMap[reg.id]}"/>
<c:set var="type" value="${topic.round.type}"/>
<c:set var="decision" value="${type == 'KLTN' ? councilService.getDefenseDecision(reg.id) : null}"/>
<tr>
<td>
<c:if test="${asgn.legacyAssessment}">
<p class="badge">Hồ sơ theo quy trình cũ</p>
</c:if>
<strong>
<c:out value="${reg.group.name}"/>
</strong>
<br>
<a href="<c:url value='/topics/${topic.id}'/>">
<c:out value="${topic.title}"/>
</a>
</td>
<td>
<c:choose>
<c:when test="${type == 'TLCN'}">
<label class="sr-only" for="supporter-${reg.id}">GV hỗ trợ</label>
<select id="supporter-${reg.id}" name="supporterId" form="assign-${reg.id}" required>
<option value="">Chọn GV hướng dẫn chấm trước</option>
<c:forEach items="${topic.advisors}" var="adv">
<option value="${adv.id}" ${asgn.supporter.id == adv.id ? 'selected' : ''}>
<c:out value="${adv.fullName}"/>
</option>
</c:forEach>
</select>
</c:when>
<c:otherwise>Không áp dụng</c:otherwise>
</c:choose>
</td>
<td>
<c:choose>
<c:when test="${type != 'NCKH'}">
<label class="sr-only" for="reviewer-${reg.id}">GV chấm / GVPB</label>
<select id="reviewer-${reg.id}" name="reviewerId" form="assign-${reg.id}" required>
<option value="">Chọn giảng viên</option>
<c:forEach items="${lecturers}" var="lec">
<c:set var="advisor" value="false"/>
<c:forEach items="${topic.advisors}" var="adv">
<c:if test="${adv.id == lec.id}">
<c:set var="advisor" value="true"/>
</c:if>
</c:forEach>
<c:if test="${!advisor}">
<option value="${lec.id}" ${asgn.reviewer.id == lec.id ? 'selected' : ''}>
<c:out value="${lec.fullName}"/>
</option>
</c:if>
</c:forEach>
</select>
<c:if test="${type == 'KLTN'}">
<p>
<c:choose>
<c:when test="${decision == null}">Chưa có quyết định bảo vệ</c:when>
<c:when test="${decision}">Được bảo vệ — có thể xếp hội đồng</c:when>
<c:otherwise>Không được bảo vệ — chuyển Khoa xác nhận điểm 0</c:otherwise>
</c:choose>
</p>
</c:if>
</c:when>
<c:otherwise>Không áp dụng</c:otherwise>
</c:choose>
</td>
<td>
<c:choose>
<c:when test="${type == 'NCKH' || (type == 'KLTN' && decision == true)}">
<label class="sr-only" for="council-${reg.id}">Hội đồng</label>
<select id="council-${reg.id}" name="councilId" form="assign-${reg.id}" required>
<option value="">Chọn hội đồng</option>
<c:forEach items="${councils}" var="c">
<option value="${c.id}" ${asgn.council.id == c.id ? 'selected' : ''}>
<c:out value="${c.name}"/>
</option>
</c:forEach>
</select>
</c:when>
<c:when test="${type == 'KLTN'}">Chờ GVPB cho phép bảo vệ</c:when>
<c:otherwise>Không áp dụng</c:otherwise>
</c:choose>
</td>
<td>
<form id="assign-${reg.id}" action="<c:url value='/admin/councils/assignments'/>" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<input type="hidden" name="registrationId" value="${reg.id}"/>
<input type="hidden" name="roundId" value="${selectedRoundId}"/>
<button class="button primary" type="submit">Lưu phân công</button>
</form>
<c:if test="${asgn != null}">
<a class="button" href="<c:url value='/council/results/registration/${reg.id}'/>">Xem đánh giá</a>
</c:if>
</td>
</tr>
</c:forEach>
<c:if test="${empty registrations}">
<tr>
<td colspan="5" class="empty">Chưa có nhóm được chấp thuận đề tài.</td>
</tr>
</c:if>
</tbody>
</table>
</div>
<%@ include file="../common/footer.jsp" %>
