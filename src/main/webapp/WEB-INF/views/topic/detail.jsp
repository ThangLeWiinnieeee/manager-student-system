<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="Chi tiết đề tài"/>
<%@ include file="../common/header.jsp" %>
<div class="page-heading"><div><h1><c:out value="${topic.title}"/></h1><p>Đăng ký trong đợt <c:out value="${topic.round.name}"/> · Cập nhật <c:out value="${topic.updatedAtText}"/></p></div><a class="button" href="<c:url value='/topics'/>">Về danh sách</a></div>
<c:if test="${not empty topic.rejectionReason and topic.status == 'REJECTED'}">
    <div class="alert error" role="alert"><strong>Lý do từ chối:</strong> <c:out value="${topic.rejectionReason}"/></div>
</c:if>
<div class="profile-layout">
    <section class="panel">
        <h2>Thông tin đề tài</h2>
        <dl class="profile">
            <dt>Trạng thái</dt><dd>
                <c:choose>
                    <c:when test="${topic.status == 'DRAFT'}"><span class="badge neutral-badge">Nháp</span></c:when>
                    <c:when test="${topic.status == 'PENDING'}"><span class="badge warn">Chờ duyệt</span></c:when>
                    <c:when test="${topic.status == 'APPROVED'}"><span class="badge info">Đã duyệt</span></c:when>
                    <c:when test="${topic.status == 'PUBLISHED'}"><span class="badge active">Đã công bố</span></c:when>
                    <c:otherwise><span class="badge inactive">Bị từ chối</span></c:otherwise>
                </c:choose>
            </dd>
            <dt>Đợt đăng ký</dt><dd><c:out value="${topic.round.name}"/> (<c:out value="${topic.round.type.displayName}"/>)</dd>
            <dt>Chuyên ngành</dt><dd><c:out value="${topic.major.name}" default="Không giới hạn"/></dd>
            <dt>Giảng viên tạo</dt><dd><c:out value="${topic.createdBy.fullName}"/></dd>
            <dt>Giảng viên hướng dẫn</dt><dd><c:forEach items="${topic.advisors}" var="advisor" varStatus="loop"><c:out value="${advisor.fullName}"/><c:if test="${not loop.last}">, </c:if></c:forEach></dd>
            <dt>Mô tả</dt><dd><c:out value="${empty topic.description ? '-' : topic.description}"/></dd>
        </dl>
    </section>
    <div>
        <section class="panel">
            <h2>Thao tác</h2>
            <p class="form-intro">Các hành động khả dụng theo vai trò và trạng thái hiện tại.</p>
            <c:if test="${owner and topic.status == 'DRAFT' or owner and topic.status == 'REJECTED'}">
                <a class="button" href="<c:url value='/topics/${topic.id}/edit'/>">Sửa đề tài</a>
                <form action="<c:url value='/topics/${topic.id}/submit'/>" method="post" class="inline-form">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button type="submit" class="primary">Gửi duyệt</button>
                </form>
                <form action="<c:url value='/topics/${topic.id}/delete'/>" method="post" class="inline-form">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button type="submit">Xóa đề tài</button>
                </form>
            </c:if>
            <c:if test="${(pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')) and topic.status == 'PENDING'}">
                <form action="<c:url value='/topics/${topic.id}/approve'/>" method="post" class="inline-form">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button type="submit" class="primary">Duyệt đề tài</button>
                </form>
            </c:if>
            <c:if test="${(pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')) and topic.status == 'APPROVED'}">
                <form action="<c:url value='/topics/${topic.id}/publish'/>" method="post" class="inline-form">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button type="submit" class="primary">Công bố đề tài</button>
                </form>
            </c:if>
            <c:if test="${(pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')) and topic.status == 'PENDING'}">
                <form:form modelAttribute="rejectForm" method="post" action="${pageContext.request.contextPath}/topics/${topic.id}/reject" cssClass="form-card narrow">
                    <h2>Từ chối đề tài</h2>
                    <form:errors path="*" cssClass="alert error" element="div"/>
                    <form:label path="reason">Lý do từ chối</form:label>
                    <form:textarea path="reason" rows="3" maxlength="500" required="required"/>
                    <form:errors path="reason" cssClass="field-error"/>
                    <button type="submit">Từ chối</button>
                </form:form>
            </c:if>
            <c:if test="${owner and topic.status == 'PENDING'}">
                <p class="muted">Đề tài đang chờ khoa xét duyệt.</p>
            </c:if>
            <c:if test="${topic.status == 'PUBLISHED'}">
                <p class="muted">Đề tài đã được công bố - các nhóm sinh viên có thể đăng ký.</p>
            </c:if>
        </section>
    </div>
</div>
<%@ include file="../common/footer.jsp" %>
