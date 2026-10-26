
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle"
       value="Xét duyệt đăng ký đề tài"/>

<%@ include file="../common/header.jsp" %>


<%-- TIEU DE --%>

<div class="page-heading">

    <div>
        <h1>Xét duyệt đăng ký đề tài</h1>

        <p>
            Trưởng khoa xem xét đăng ký đề tài
            của các nhóm sinh viên.
        </p>
    </div>

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


<%-- DANH SACH DANG KY --%>

<section class="panel">

    <div class="panel-heading">

        <h2>Danh sách đăng ký</h2>

        <span class="neutral-badge">
            Đăng ký đề tài
        </span>

    </div>


    <div class="table-wrap"
         role="region"
         aria-label="Danh sách đăng ký đề tài"
         tabindex="0">

        <table>

            <thead>
                <tr>
                    <th>Mã đăng ký</th>
                    <th>Nhóm</th>
                    <th>Đề tài</th>
                    <th>Đợt</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach items="${registrations}"
                           var="registration">

                    <tr>

                        <td>
                            <c:out value="${registration.id}"/>
                        </td>

                        <td>
                            <c:out value="${registration.group.name}"/>
                        </td>

                        <td>
                            <strong>
                                <a href="<c:url value='/topics/${registration.topic.id}'/>"><c:out value="${registration.topic.title}"/></a>
                            </strong>
                        </td>

                        <td>
                            <c:out value="${registration.group.roundId}"/>
                        </td>

                        <td>

                            <c:choose>

                                <c:when test="${registration.status == 'APPROVED'}">

                                    <span class="badge active">
                                        Đã chấp thuận
                                    </span>

                                </c:when>

                                <c:when test="${registration.status == 'REJECTED'}">

                                    <span class="neutral-badge">
                                        Bị từ chối
                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <span class="neutral-badge">
                                        Chờ duyệt
                                    </span>

                                </c:otherwise>

                            </c:choose>

                            <c:if test="${not empty registration.rejectionReason}">

                                <p>
                                    <strong>Lý do:</strong>
                                    <c:out value="${registration.rejectionReason}"/>
                                </p>

                            </c:if>

                        </td>

                        <td class="actions">

                            <%-- CHI XU LY DANG KY PENDING --%>

                            <c:if test="${registration.status == 'PENDING'}">

                                <%-- DUYET --%>

                                <form method="post"
                                      class="inline-form"
                                      action="<c:url value='/dean/group-topic-registrations/${registration.id}/approve'/>"
                                      onsubmit="return confirm('Chấp thuận đăng ký đề tài này?');">

                                    <input type="hidden"
                                           name="${_csrf.parameterName}"
                                           value="${_csrf.token}">

                                    <button type="submit"
                                            class="button">
                                        Chấp thuận
                                    </button>

                                </form>


                                <%-- TU CHOI --%>

                                <form method="post"
                                      action="<c:url value='/dean/group-topic-registrations/${registration.id}/reject'/>">

                                    <label for="reason-${registration.id}">
                                        Lý do từ chối
                                    </label>

                                    <textarea id="reason-${registration.id}"
                                              name="reason"
                                              rows="2"
                                              maxlength="500"
                                              placeholder="Nhập lý do từ chối..."
                                              required></textarea>

                                    <input type="hidden"
                                           name="${_csrf.parameterName}"
                                           value="${_csrf.token}">

                                    <button type="submit"
                                            class="button">
                                        Từ chối
                                    </button>

                                </form>

                            </c:if>

                            <c:if test="${registration.status != 'PENDING'}">

                                <span class="neutral-badge">
                                    Đã xử lý
                                </span>

                            </c:if>

                        </td>

                    </tr>

                </c:forEach>


                <c:if test="${empty registrations}">

                    <tr>
                        <td colspan="6" class="empty">
                            Chưa có nhóm sinh viên nào
                            gửi đăng ký đề tài.
                        </td>
                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

</section>


<%@ include file="../common/footer.jsp" %>
