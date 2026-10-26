<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Xét duyệt đăng ký đề tài"/>
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
    <div><h1>Xét duyệt đăng ký đề tài</h1><p>Đọc nội dung đề xuất, kiểm tra lớp và nhóm trước khi chấp thuận.</p></div>
</div>
<c:if test="${not empty success}"><p class="alert alert-success" role="status"><c:out value="${success}"/></p></c:if>
<c:if test="${not empty error}"><p class="alert alert-danger" role="alert"><c:out value="${error}"/></p></c:if>
<c:forEach items="${['PENDING','APPROVED','REJECTED']}" var="reviewStatus">
    <c:set var="sectionHasItems" value="false"/>
    <c:forEach items="${registrations}" var="registration">
        <c:if test="${registration.status == reviewStatus}">
            <c:if test="${not sectionHasItems}">
                <h2>${reviewStatus == 'PENDING' ? 'Chờ xét duyệt' : (reviewStatus == 'APPROVED' ? 'Đã chấp thuận' : 'Đã từ chối')}</h2>
                <c:set var="sectionHasItems" value="true"/>
            </c:if>
            <article class="panel">
                <div class="panel-heading">
                    <h3><a href="<c:url value='/topics/${registration.topic.id}'/>"><c:out value="${registration.topic.title}"/></a></h3>
                    <span class="badge ${reviewStatus == 'PENDING' ? 'warn' : (reviewStatus == 'APPROVED' ? 'active' : 'inactive')}"><c:out value="${registration.status.displayName}"/></span>
                </div>
                <p><strong>Nhóm:</strong> <c:out value="${registration.group.name}"/> · <strong>Môn/lớp:</strong> <c:out value="${registration.topic.round.courseLabel}"/></p>
                <p><strong>Đợt:</strong> <c:out value="${registration.topic.round.period.name}"/></p>
                <details ${reviewStatus == 'PENDING' ? 'open' : ''}>
                    <summary>Nội dung đề xuất</summary>
                    <p style="white-space: pre-wrap; overflow-wrap: anywhere"><c:out value="${registration.topic.description}"/></p>
                </details>
                <c:if test="${not empty registration.rejectionReason}"><p class="alert error"><strong>Lý do từ chối:</strong> <c:out value="${registration.rejectionReason}"/></p></c:if>
                <c:choose>
                    <c:when test="${reviewStatus == 'PENDING'}">
                        <div class="form-actions">
                            <form method="post" action="<c:url value='/dean/group-topic-registrations/${registration.id}/approve'/>" onsubmit="return confirm('Chấp thuận đề xuất của nhóm này?');">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button class="primary" type="submit">Chấp thuận đề tài</button>
                            </form>
                        </div>
                        <details>
                            <summary>Từ chối và yêu cầu chỉnh sửa</summary>
                            <form method="post" action="<c:url value='/dean/group-topic-registrations/${registration.id}/reject'/>" class="form-card">
                                <label for="reason-${registration.id}">Lý do từ chối</label>
                                <textarea id="reason-${registration.id}" name="reason" rows="3" maxlength="500" required placeholder="Nêu rõ nội dung nhóm cần điều chỉnh"></textarea>
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button type="submit">Xác nhận từ chối</button>
                            </form>
                        </details>
                    </c:when>
                    <c:when test="${reviewStatus == 'APPROVED'}">
                        <a class="button" href="<c:url value='/review/submissions/groups/${registration.group.id}'/>">Xem báo cáo của nhóm</a>
                    </c:when>
                </c:choose>
            </article>
        </c:if>
    </c:forEach>
</c:forEach>
<c:if test="${empty registrations}"><section class="panel"><p>Chưa có nhóm sinh viên nào gửi đăng ký đề tài.</p></section></c:if>
<%@ include file="../common/footer.jsp" %>
