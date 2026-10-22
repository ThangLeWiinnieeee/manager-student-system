<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Danh sách đề tài"/>
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
    <div><h1>Danh sách đề tài</h1><p>Giảng viên thấy đề tài do mình đăng ký; sinh viên thấy các đề tài đã được công bố.</p></div>
    <c:if test="${pageContext.request.isUserInRole('LECTURER')}">
        <a class="button primary" href="<c:url value='/topics/new'/>">Đăng ký đề tài</a>
    </c:if>
</div>
<form method="get" action="<c:url value='/topics'/>" class="filter-bar">
    <select name="roundId" aria-label="Lọc theo đợt">
        <option value="">-- Tất cả đợt --</option>
        <c:forEach items="${periods}" var="period">
            <option value="${period.id}" ${filterRoundId == period.id ? 'selected' : ''}><c:out value="${period.name}"/></option>
        </c:forEach>
    </select>
    <select name="majorId" aria-label="Lọc theo chuyên ngành">
        <option value="">-- Tất cả chuyên ngành --</option>
        <c:forEach items="${majors}" var="major">
            <option value="${major.id}" ${filterMajorId == major.id ? 'selected' : ''}><c:out value="${major.name}"/></option>
        </c:forEach>
    </select>
    <select name="status" aria-label="Lọc theo trạng thái">
        <option value="">-- Tất cả trạng thái --</option>
        <c:forEach items="${statuses}" var="statusOption">
            <option value="${statusOption}" ${filterStatus == statusOption ? 'selected' : ''}><c:out value="${statusOption.displayName}"/></option>
        </c:forEach>
    </select>
    <button type="submit">Lọc</button>
    <a class="button" href="<c:url value='/topics'/>">Xóa lọc</a>
</form>
<div class="table-wrap" role="region" aria-label="Danh sách đề tài" tabindex="0">
<table aria-label="Danh sách đề tài">
    <thead><tr><th>Tên đề tài</th><th>Đợt đăng ký</th><th>Chuyên ngành</th><th>GVHD</th><th>Người tạo</th><th>Trạng thái</th><th>Cập nhật</th><th>Thao tác</th></tr></thead>
    <tbody>
    <c:forEach items="${topicPage.content}" var="t">
        <tr>
            <td><a href="<c:url value='/topics/${t.id}'/>"><c:out value="${t.title}"/></a></td>
            <td><c:out value="${t.round.name}"/></td>
            <td><c:out value="${t.major.name}" default="Không giới hạn"/></td>
            <td><c:forEach items="${t.advisors}" var="advisor" varStatus="loop"><c:out value="${advisor.fullName}"/><c:if test="${not loop.last}">, </c:if></c:forEach></td>
            <td><c:out value="${t.createdBy.fullName}"/></td>
            <td>
                <c:choose>
                    <c:when test="${t.status == 'DRAFT'}"><span class="badge neutral-badge">Nháp</span></c:when>
                    <c:when test="${t.status == 'PENDING'}"><span class="badge warn">Chờ duyệt</span></c:when>
                    <c:when test="${t.status == 'APPROVED'}"><span class="badge info">Đã duyệt</span></c:when>
                    <c:when test="${t.status == 'PUBLISHED'}"><span class="badge active">Đã công bố</span></c:when>
                    <c:otherwise><span class="badge inactive">Bị từ chối</span></c:otherwise>
                </c:choose>
            </td>
            <td><c:out value="${t.updatedAtText}"/></td>
            <td class="actions">
                <c:if test="${pageContext.request.isUserInRole('LECTURER') and t.createdBy.email == pageContext.request.userPrincipal.name and (t.status == 'DRAFT' or t.status == 'REJECTED')}">
                    <a class="button" href="<c:url value='/topics/${t.id}/edit'/>">Sửa</a>
                    <form action="<c:url value='/topics/${t.id}/submit'/>" method="post" class="inline-form">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <button type="submit" class="button">Gửi duyệt</button>
                    </form>
                </c:if>
                <c:if test="${(pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')) and t.status == 'PENDING'}">
                    <form action="<c:url value='/topics/${t.id}/approve'/>" method="post" class="inline-form">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <button type="submit" class="button">Duyệt</button>
                    </form>
                </c:if>
                <c:if test="${(pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')) and t.status == 'APPROVED'}">
                    <form action="<c:url value='/topics/${t.id}/publish'/>" method="post" class="inline-form">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <button type="submit" class="button">Công bố</button>
                    </form>
                </c:if>
                <a class="button" href="<c:url value='/topics/${t.id}'/>">Chi tiết</a>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty topicPage.content}"><tr><td colspan="8" class="empty">Không có đề tài phù hợp.</td></tr></c:if>
    </tbody>
</table>
</div>
<c:if test="${topicPage.totalPages > 1}">
<div class="pagination">
    <c:if test="${topicPage.hasPrevious()}">
        <c:url var="prevUrl" value="/topics">
            <c:if test="${not empty filterRoundId}"><c:param name="roundId" value="${filterRoundId}"/></c:if>
            <c:if test="${not empty filterMajorId}"><c:param name="majorId" value="${filterMajorId}"/></c:if>
            <c:if test="${not empty filterStatus}"><c:param name="status" value="${filterStatus}"/></c:if>
            <c:param name="page" value="${topicPage.number - 1}"/>
        </c:url>
        <a class="button" href="<c:out value='${prevUrl}'/>">← Trang trước</a>
    </c:if>
    <span class="muted">Trang ${topicPage.number + 1} / ${topicPage.totalPages}</span>
    <c:if test="${topicPage.hasNext()}">
        <c:url var="nextUrl" value="/topics">
            <c:if test="${not empty filterRoundId}"><c:param name="roundId" value="${filterRoundId}"/></c:if>
            <c:if test="${not empty filterMajorId}"><c:param name="majorId" value="${filterMajorId}"/></c:if>
            <c:if test="${not empty filterStatus}"><c:param name="status" value="${filterStatus}"/></c:if>
            <c:param name="page" value="${topicPage.number + 1}"/>
        </c:url>
        <a class="button" href="<c:out value='${nextUrl}'/>">Trang sau →</a>
    </c:if>
</div>
</c:if>
<%@ include file="../common/footer.jsp" %>
