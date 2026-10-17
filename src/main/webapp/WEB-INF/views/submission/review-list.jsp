
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle"
       value="Báo cáo của nhóm sinh viên"/>

<%@ include file="../common/header.jsp" %>

<%-- TIEU DE --%>

<div class="page-heading">

    <div>
        <h1>Báo cáo của nhóm sinh viên</h1>

        <p>
            Xem báo cáo của các nhóm có đề tài
            đã được chấp thuận và thuộc phạm vi quản lý.
        </p>
    </div>

</div>

<%-- DANH SACH --%>

<section class="panel">

    <div class="panel-heading">
        <h2>Danh sách nhóm</h2>

        <span class="neutral-badge">
            Báo cáo đề tài
        </span>
    </div>

    <div class="table-wrap"
         role="region"
         aria-label="Danh sách nhóm"
         tabindex="0">

        <table>

            <thead>
                <tr>
                    <th>Mã nhóm</th>
                    <th>Tên nhóm</th>
                    <th>Đề tài</th>
                    <th>Đợt</th>
                    <th>Số báo cáo</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach items="${groupReports}"
                           var="group">

                    <tr>

                        <td>
                            <c:out value="${group.groupId}"/>
                        </td>

                        <td>
                            <strong>
                                <c:out value="${group.groupName}"/>
                            </strong>
                        </td>

                        <td>
                            <c:out value="${group.topicTitle}"/>
                        </td>

                        <td>
                            <c:out value="${group.roundId}"/>
                        </td>

                        <td>
                            <c:out value="${group.reportCount}"/>
                        </td>

                        <td class="actions">

                            <a class="button"
                               href="<c:url value='/review/submissions/groups/${group.groupId}'/>">
                                Xem báo cáo
                            </a>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty groupReports}">

                    <tr>
                        <td colspan="6" class="empty">
                            Không có nhóm nào có đăng ký
                            đề tài đã được chấp thuận
                            trong phạm vi bạn được phép xem.
                        </td>
                    </tr>

                </c:if>

            </tbody>
        </table>

    </div>

</section>

<%@ include file="../common/footer.jsp" %>
