<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" value="Chi tiết đề tài"/>
<%@ include file="../common/header.jsp" %>
<c:set var="manager" value="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}"/>
<c:set var="canEdit" value="${pageContext.request.isUserInRole('LECTURER') and owner and empty topic.proposedGroupId and (topic.status == 'DRAFT' or topic.status == 'REJECTED')}"/>
<c:set var="canReview" value="${manager and empty topic.proposedGroupId and topic.status == 'PENDING'}"/>
<spring:hasBindErrors name="rejectForm"><c:set var="rejectExpanded" value="true"/></spring:hasBindErrors>

<div class="topic-detail">
    <a class="topic-back" href="<c:url value='/topics'/>">← Danh sách đề tài</a>
    <header class="topic-heading">
        <div class="topic-labels">
            <span class="muted">Đề tài #<c:out value="${topic.id}"/></span>
            <span class="badge ${topic.status == 'PUBLISHED' ? 'active' : (topic.status == 'PENDING' ? 'warn' : (topic.status == 'APPROVED' ? 'info' : 'neutral-badge'))}"><c:out value="${topic.status.displayName}"/></span>
            <span class="muted"><c:out value="${topic.round.type.displayName}"/></span>
        </div>
        <h1><c:out value="${topic.title}"/></h1>
        <p class="muted">Cập nhật <c:out value="${topic.updatedAtText}"/></p>
    </header>

    <c:if test="${not empty topic.rejectionReason and topic.status == 'REJECTED'}">
        <div class="alert error" role="alert"><strong>Lý do từ chối:</strong> <c:out value="${topic.rejectionReason}"/></div>
    </c:if>

    <div class="topic-detail-grid">
        <div class="topic-main">
            <section class="panel" aria-labelledby="topic-content-title">
                <h2 id="topic-content-title">Nội dung đề tài</h2>
                <div class="topic-description"><c:out value="${empty topic.description ? 'Chưa có nội dung mô tả.' : topic.description}"/></div>
            </section>

            <c:if test="${canEdit or canReview or (manager and (topic.status == 'APPROVED' or not empty topic.proposedGroupId))}">
                <section class="panel topic-actions" aria-labelledby="topic-actions-title">
                    <h2 id="topic-actions-title">${canReview ? 'Xét duyệt đề tài' : 'Thao tác đề tài'}</h2>
                    <c:if test="${canEdit}">
                        <p>Hoàn thiện nội dung và giảng viên hướng dẫn trước khi gửi Khoa xét duyệt.</p>
                        <div class="topic-action-row">
                            <form action="<c:url value='/topics/${topic.id}/submit'/>" method="post">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button type="submit" class="primary">Gửi duyệt</button>
                            </form>
                            <a class="button" href="<c:url value='/topics/${topic.id}/edit'/>">Sửa đề tài</a>
                            <form action="<c:url value='/topics/${topic.id}/delete'/>" method="post" class="topic-delete"
                                  onsubmit="return confirm('Bạn có chắc muốn xóa đề tài này?');">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button type="submit" class="topic-danger">Xóa đề tài</button>
                            </form>
                        </div>
                    </c:if>
                    <c:if test="${canReview}">
                        <p>Kiểm tra nội dung và GVHD trước khi duyệt. Đề tài được duyệt cần công bố để sinh viên có thể chọn.</p>
                        <form action="<c:url value='/topics/${topic.id}/approve'/>" method="post">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button type="submit" class="primary">Duyệt đề tài</button>
                        </form>
                        <details class="topic-disclosure" ${rejectExpanded ? 'open' : ''}>
                            <summary>Từ chối và ghi lý do</summary>
                            <form:form modelAttribute="rejectForm" method="post" action="${pageContext.request.contextPath}/topics/${topic.id}/reject" cssClass="topic-inline-editor">
                                <form:errors path="*" cssClass="alert error" element="div"/>
                                <form:label path="reason">Lý do từ chối</form:label>
                                <form:textarea path="reason" rows="4" maxlength="500" required="required"/>
                                <button type="submit" class="topic-danger">Xác nhận từ chối</button>
                            </form:form>
                        </details>
                    </c:if>
                    <c:if test="${manager and empty topic.proposedGroupId and topic.status == 'APPROVED'}">
                        <p>Đề tài đã được duyệt. Công bố để các nhóm sinh viên có thể đăng ký trong thời gian quy định.</p>
                        <form action="<c:url value='/topics/${topic.id}/publish'/>" method="post">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button type="submit" class="primary">Công bố đề tài</button>
                        </form>
                    </c:if>
                    <c:if test="${manager and not empty topic.proposedGroupId}">
                        <p>Đây là đề xuất riêng của nhóm sinh viên. Xét duyệt nội dung tại danh sách đăng ký nhóm.</p>
                        <a class="button primary" href="<c:url value='/dean/group-topic-registrations'/>">Xét duyệt đề xuất của nhóm</a>
                    </c:if>
                </section>
            </c:if>
        </div>

        <aside class="topic-sidebar" aria-label="Thông tin liên quan đến đề tài">
            <section class="panel">
                <h2>Thông tin đăng ký</h2>
                <dl class="topic-facts">
                    <dt>Môn/lớp đăng ký</dt><dd><c:out value="${topic.round.name}"/></dd>
                    <dt>Bộ môn</dt><dd><c:out value="${topic.department.name}" default="Chưa thiết lập"/></dd>
                    <dt>Người đề xuất</dt><dd><c:out value="${topic.createdBy.fullName}"/></dd>
                </dl>
            </section>
            <section class="panel">
                <h2>Giảng viên hướng dẫn</h2>
                <ul class="topic-advisors">
                    <c:forEach items="${topic.advisors}" var="advisor"><li><c:out value="${advisor.fullName}"/></li></c:forEach>
                </ul>
                <c:if test="${empty topic.advisors}"><p>Chưa phân công giảng viên hướng dẫn.</p></c:if>
                <c:if test="${manager}">
                    <details class="topic-disclosure">
                        <summary>Phân công giảng viên hướng dẫn</summary>
                        <form action="<c:url value='/topics/${topic.id}/advisors'/>" method="post" class="topic-inline-editor">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <fieldset class="topic-advisor-options">
                                <legend>Chọn 1–2 giảng viên</legend>
                                <c:forEach items="${lecturers}" var="lecturer">
                                    <c:set var="assigned" value="false"/>
                                    <c:forEach items="${topic.advisors}" var="advisor"><c:if test="${advisor.id == lecturer.id}"><c:set var="assigned" value="true"/></c:if></c:forEach>
                                    <label class="checkbox">
                                        <input type="checkbox" name="advisorIds" value="${lecturer.id}" ${assigned ? 'checked' : ''}>
                                        <span><c:out value="${lecturer.fullName}"/></span>
                                    </label>
                                </c:forEach>
                            </fieldset>
                            <button type="submit">Lưu phân công</button>
                        </form>
                    </details>
                </c:if>
            </section>
        </aside>
    </div>
</div>
<%@ include file="../common/footer.jsp" %>
