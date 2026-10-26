
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="pageTitle" value="Đăng ký đề tài"/>

<%@ include file="../common/header.jsp" %>

<div class="page-heading">

    <div>
        <h1>Đăng ký đề tài</h1>

        <p>
            Nhóm:
            <c:out value="${group.name}"/>

            | Môn/lớp đăng ký:
            <c:out value="${group.roundId}"/>
        </p>
    </div>

    <a class="button"
       href="<c:url value='/student/my-groups/${group.id}'/>">
        Quay lại nhóm
    </a>

</div>

<%-- THONG BAO --%>

<c:if test="${not empty success}">
    <p class="alert alert-success" role="status">
        <c:out value="${success}"/>
    </p>
</c:if>

<c:if test="${not empty error}">
    <p class="alert alert-danger" role="alert">
        <c:out value="${error}"/>
    </p>
</c:if>

<%-- TRANG THAI DANG KY HIEN TAI --%>

<c:if test="${not empty registration}">

    <section class="panel">

        <div class="panel-heading">
            <h2>Đăng ký hiện tại của nhóm</h2>
        </div>

        <p>
            <strong>Đề tài:</strong>
            <c:out value="${registration.topic.title}"/>
        </p>

        <p>
            <strong>Trạng thái:</strong>
            <c:out value="${registration.status.displayName}"/>
        </p>
        <p><strong>Nội dung:</strong> <c:out value="${registration.topic.description}"/></p>
        <c:if test="${registration.status == 'PENDING'}"><p>Đề xuất đang chờ khoa xét duyệt.</p></c:if>
        <c:if test="${registration.status == 'APPROVED'}"><p>Nhóm đã nhận đề tài. Hãy thực hiện đề tài và nộp báo cáo theo hướng dẫn.</p></c:if>

        <c:if test="${not empty registration.rejectionReason}">

            <p class="alert alert-danger">
                <strong>Lý do từ chối:</strong>
                <c:out value="${registration.rejectionReason}"/>
            </p>

        </c:if>

    </section>

</c:if>


<p>Môn/lớp: <strong><c:out value="${round.courseLabel}"/></strong> · Tối đa ${round.maxMembers} thành viên/nhóm · <c:out value="${round.registrationMode.displayName}"/></p>
<c:if test="${not registrationOpen}">
    <p class="alert">Hiện không trong thời gian sinh viên đăng ký. Thời gian đăng ký: <c:out value="${round.svStartText}"/> – <c:out value="${round.svEndText}"/>.</p>
</c:if>
<c:if test="${round.registrationMode == 'SELF_PROPOSED' and not isLeader and empty registration}"><p>Chỉ nhóm trưởng có thể gửi đề xuất đề tài cho nhóm.</p></c:if>
<c:if test="${round.registrationMode == 'SELF_PROPOSED' and registrationOpen and isLeader and (empty registration or registration.status == 'REJECTED')}">
    <form:form modelAttribute="proposalForm" method="post" action="${pageContext.request.contextPath}/student/my-groups/${group.id}/topics/propose" cssClass="form-card">
        <h2>Đề xuất đề tài của nhóm</h2>
        <p>Các nhóm được phép trùng tên đề tài; báo cáo và điểm được quản lý riêng.</p>
        <form:errors path="*" cssClass="alert error" element="div"/>
        <form:label path="title">Tên đề tài</form:label><form:input path="title" maxlength="200" required="required"/>
        <form:label path="description">Nội dung đề tài</form:label><form:textarea path="description" maxlength="2000" required="required"/>
        <button class="primary" type="submit">Gửi đề xuất</button>
    </form:form>
</c:if>
<c:if test="${round.registrationMode != 'SELF_PROPOSED'}">
<%-- DANH SACH DE TAI --%>

<section class="panel">

    <div class="panel-heading">
        <h2>Đề tài đã công bố</h2>
    </div>

    <div class="table-wrap">

        <table>

            <thead>
                <tr>
                    <th>Mã đề tài</th>
                    <th>Tên đề tài</th>
                    <th>Bộ môn</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach items="${publishedTopics.content}"
                           var="topic">

                    <tr>

                        <td>
                            <c:out value="${topic.id}"/>
                        </td>

                        <td>
                            <c:out value="${topic.title}"/>
                        </td>

                        <td>
                            <c:out value="${topic.department.name}" default="Không giới hạn"/>
                        </td>

                        <td>
                            <span class="badge active">
                                Đã công bố
                            </span>
                        </td>

                        <td class="actions">

                            <c:if test="${registrationOpen and isLeader and
                                (empty registration or
                                 registration.status == 'REJECTED')}">

                                <form method="post"
                                      class="inline-form"
                                      action="<c:url value='/student/my-groups/${group.id}/topics/register'/>"
                                      onsubmit="return confirm('Bạn muốn đăng ký đề tài này?');">

                                    <input type="hidden"
                                           name="topicId"
                                           value="${topic.id}">

                                    <input type="hidden"
                                           name="${_csrf.parameterName}"
                                           value="${_csrf.token}">

                                    <button type="submit"
                                            class="button">
                                        Đăng ký
                                    </button>

                                </form>

                            </c:if>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty publishedTopics.content}">

                    <tr>
                        <td colspan="5" class="empty">
                            Chưa có đề tài nào được công bố
                            trong môn/lớp đăng ký này.
                        </td>
                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

    <%-- PHAN TRANG --%>

    <c:if test="${publishedTopics.totalPages > 1}">

        <div class="form-actions">

            <c:if test="${publishedTopics.number > 0}">
                <a class="button"
                   href="<c:url value='/student/my-groups/${group.id}/topics?page=${publishedTopics.number - 1}'/>">
                    Trang trước
                </a>
            </c:if>

            <span>
                Trang ${publishedTopics.number + 1}
                / ${publishedTopics.totalPages}
            </span>

            <c:if test="${publishedTopics.number + 1 < publishedTopics.totalPages}">
                <a class="button"
                   href="<c:url value='/student/my-groups/${group.id}/topics?page=${publishedTopics.number + 1}'/>">
                    Trang sau
                </a>
            </c:if>

        </div>

    </c:if>

</section>

</c:if>
<%@ include file="../common/footer.jsp" %>
