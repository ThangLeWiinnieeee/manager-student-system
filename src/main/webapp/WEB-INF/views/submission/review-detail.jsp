
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle"
       value="Chi tiết báo cáo nhóm"/>

<%@ include file="../common/header.jsp" %>

<%-- TIEU DE --%>

<div class="page-heading">

    <div>

        <h1>Chi tiết báo cáo nhóm</h1>

        <p>
            Nhóm:
            <c:out value="${groupInfo.groupName}"/>
        </p>

    </div>

    <a class="button"
       href="<c:url value='/review/submissions'/>">
        Quay lại
    </a>

</div>

<%-- THONG TIN NHOM --%>

<section class="panel">

    <div class="panel-heading">
        <h2>Thông tin đề tài</h2>
        <span class="badge active">
            Đã chấp thuận
        </span>
    </div>

    <p>
        <strong>Mã nhóm:</strong>
        <c:out value="${groupInfo.groupId}"/>
    </p>

    <p>
        <strong>Tên nhóm:</strong>
        <c:out value="${groupInfo.groupName}"/>
    </p>

    <p>
        <strong>Đợt đăng ký:</strong>
        <c:out value="${groupInfo.roundId}"/>
    </p>

    <p>
        <strong>Đề tài:</strong>
        <c:out value="${groupInfo.topicTitle}"/>
    </p>

</section>

<%-- LICH SU BAO CAO --%>

<section class="panel">

    <div class="panel-heading">

        <h2>Lịch sử nộp báo cáo</h2>

        <span class="neutral-badge">
            Tệp báo cáo
        </span>

    </div>

    <div class="table-wrap"
         role="region"
         aria-label="Lịch sử báo cáo"
         tabindex="0">

        <table>

            <thead>
                <tr>
                    <th>Mã báo cáo</th>
                    <th>Tên tệp</th>
                    <th>Người nộp</th>
                    <th>Dung lượng</th>
                    <th>Thời gian nộp</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach items="${reports}"
                           var="report">

                    <tr>

                        <td>
                            <c:out value="${report.reportId}"/>
                        </td>

                        <td>
                            <strong>
                                <c:out value="${report.originalFilename}"/>
                            </strong>
                        </td>

                        <td>
                            <c:out value="${report.uploadedByEmail}"/>
                        </td>

                        <td>
                            <c:out value="${report.sizeBytes}"/>
                            bytes
                        </td>

                        <td>
                            <c:out value="${report.uploadedAtText}"/>
                        </td>

                        <td class="actions">

                            <a class="button"
                               href="<c:url value='/review/submissions/groups/${groupInfo.groupId}/files/${report.reportId}/download'/>">
                                Tải báo cáo
                            </a>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty reports}">

                    <tr>
                        <td colspan="6"
                            class="empty">
                            Nhóm chưa nộp báo cáo nào.
                        </td>
                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

</section>

<%@ include file="../common/footer.jsp" %>
