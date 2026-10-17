
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Nộp báo cáo"/>

<%@ include file="../common/header.jsp" %>


<%-- TIEU DE TRANG --%>

<div class="page-heading">

    <div>
        <h1>Nộp báo cáo đề tài</h1>

        <p>
            Nhóm:
            <c:out value="${group.name}"/>

            | Đợt:
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


<%-- KIEM TRA TRANG THAI DE TAI --%>

<c:if test="${not approved}">

    <section class="panel">

        <p class="alert alert-danger">
            Nhóm chưa được chấp thuận đăng ký đề tài.
            Hiện chưa thể nộp báo cáo.
        </p>

    </section>

</c:if>


<%-- FORM UPLOAD CHI DANH CHO NHOM TRUONG --%>

<c:if test="${approved and isLeader}">

    <section class="panel">

        <div class="panel-heading">
            <h2>Nộp báo cáo mới</h2>

            <span class="badge active">
                Được phép nộp
            </span>
        </div>

        <p>
            Chọn báo cáo định dạng PDF,
            dung lượng tối đa 10 MB.
            Mỗi lần nộp sẽ được lưu vào lịch sử.
        </p>

        <form method="post"
              enctype="multipart/form-data"
              class="form-card narrow"
              action="<c:url value='/student/my-groups/${group.id}/submissions'/>">

            <label for="reportFile">
                Chọn tệp báo cáo PDF
            </label>

            <input type="file"
                   id="reportFile"
                   name="file"
                   accept=".pdf,application/pdf"
                   required>

            <input type="hidden"
                   name="${_csrf.parameterName}"
                   value="${_csrf.token}">

            <div class="form-actions">

                <button type="submit"
                        class="primary">
                    Nộp báo cáo
                </button>

            </div>

        </form>

    </section>

</c:if>


<%-- THANH VIEN CHI DUOC XEM --%>

<c:if test="${approved and not isLeader}">

    <section class="panel">

        <p>
            Bạn là thành viên của nhóm.
            Chỉ nhóm trưởng mới được nộp báo cáo.
            Bạn có thể xem và tải các báo cáo đã nộp.
        </p>

    </section>

</c:if>


<%-- LICH SU BAO CAO --%>

<section class="panel">

    <div class="panel-heading">

        <h2>Lịch sử nộp báo cáo</h2>

        <span class="neutral-badge">
            Danh sách báo cáo
        </span>

    </div>

    <div class="table-wrap"
         role="region"
         aria-label="Lịch sử nộp báo cáo"
         tabindex="0">

        <table>

            <thead>
                <tr>
                    <th>Mã báo cáo</th>
                    <th>Tên tệp</th>
                    <th>Người nộp</th>
                    <th>Dung lượng</th>
                    <th>Thời gian</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach items="${reports}"
                           var="report">

                    <tr>

                        <td>
                            <c:out value="${report.id}"/>
                        </td>

                        <td>
                            <strong>
                                <c:out value="${report.originalFilename}"/>
                            </strong>
                        </td>

                        <td>
                            <c:out value="${report.uploadedBy.email}"/>
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
                               href="<c:url value='/student/my-groups/${group.id}/submissions/${report.id}/download'/>">
                                Tải xuống
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
