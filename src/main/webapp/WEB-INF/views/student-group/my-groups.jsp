
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Nhóm của tôi"/>

<%@ include file="../common/header.jsp" %>

<!-- ============================= -->
<!-- TV3: TIEU DE TRANG            -->
<!-- ============================= -->

<div class="page-heading">

    <div>
        <h1>Nhóm của tôi</h1>

        <p>
            Xem và quản lý các nhóm sinh viên mà bạn
            đang tham gia trong những đợt đăng ký đề tài.
        </p>
    </div>

    <!-- Nut tao nhom -->
    <a class="button"
       href="<c:url value='/student/my-groups/new'/>">
        + Tạo nhóm
    </a>

</div>


<!-- ============================= -->
<!-- THONG BAO THANH CONG / LOI    -->
<!-- ============================= -->

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


<!-- ============================= -->
<!-- DANH SACH NHOM CUA TOI        -->
<!-- ============================= -->

<section class="panel">

    <div class="panel-heading">

        <h2>Danh sách nhóm của tôi</h2>

        <span class="neutral-badge">
            Nhóm sinh viên
        </span>

    </div>


    <div class="table-wrap"
         role="region"
         aria-label="Danh sách nhóm của tôi"
         tabindex="0">

        <table aria-label="Danh sách nhóm của tôi">

            <!-- TIEU DE BANG -->

            <thead>
                <tr>
                    <th>Mã nhóm</th>
                    <th>Tên nhóm</th>
                    <th>Đợt đăng ký</th>
                    <th>Vai trò của tôi</th>
                    <th>Thao tác</th>
                </tr>
            </thead>


            <!-- NOI DUNG BANG -->

            <tbody>

                <c:forEach items="${myGroups}"
                           var="group">

                    <tr>

                        <!-- MA NHOM -->
                        <td>
                            <c:out value="${group.id}"/>
                        </td>


                        <!-- TEN NHOM -->
                        <td>
                            <strong>
                                <c:out value="${group.name}"/>
                            </strong>
                        </td>


                        <!-- DOT DANG KY -->
                        <td>
                            <c:out value="${group.roundId}"/>
                        </td>


                        <!-- VAI TRO SINH VIEN -->
                        <td>

                            <c:choose>

                                <c:when test="${group.leader}">

                                    <span class="badge active">
                                        Nhóm trưởng
                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <span class="neutral-badge">
                                        Thành viên
                                    </span>

                                </c:otherwise>

                            </c:choose>

                        </td>


                        <!-- THAO TAC -->
                        <td class="actions">

                            <!-- TV3: Xem chi tiet nhom -->
                            <a class="button"
                               href="<c:url value='/student/my-groups/${group.id}'/>">
                                Chi tiết
                            </a>

                        </td>

                    </tr>

                </c:forEach>


                <!-- CHUA THAM GIA NHOM NAO -->

                <c:if test="${empty myGroups}">

                    <tr>
                        <td colspan="5" class="empty">

                            Bạn chưa tham gia nhóm sinh viên nào.
                            Hãy nhấn "+ Tạo nhóm"
                            để tạo nhóm đầu tiên.

                        </td>
                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

</section>


<!-- ============================= -->
<!-- FOOTER                        -->
<!-- ============================= -->

<%@ include file="../common/footer.jsp" %>
