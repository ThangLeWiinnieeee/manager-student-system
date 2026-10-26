
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
            đang tham gia trong những môn/lớp đăng ký đề tài.
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


<c:if test="${not empty invitations}">
<section class="panel">
<h2>Lời mời vào nhóm</h2>
<p>Chỉ khi đồng ý, bạn mới trở thành thành viên. Hệ thống kiểm tra lại thời hạn, chỗ trống và nhóm hiện tại khi xác nhận.</p>
<c:forEach items="${invitations}" var="invitation">
<form method="post" action="<c:url value='/student/my-groups/${invitation.group.id}/invitations/respond'/>">
<p><strong><c:out value="${invitation.group.name}"/></strong> · Mã lớp <c:out value="${invitation.group.roundId}"/></p>
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<button class="button primary" type="submit" name="accept" value="true">Đồng ý tham gia</button>
<button class="button" type="submit" name="accept" value="false">Từ chối</button>
</form>
</c:forEach>
</section>
</c:if>
<section class="panel">
<h2>Tham gia lớp đề tài</h2>
<p>Nhập mã giảng viên cung cấp. Đây là lớp thực hiện đề tài, không phải đăng ký học phần.</p>
<form method="post" action="<c:url value='/student/my-groups/join'/>">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<label for="joinCode">Mã tham gia</label><input id="joinCode" name="code" maxlength="16" minlength="16" required autocomplete="off"/>
<button class="button primary" type="submit">Tham gia lớp</button></form>
<c:if test="${not empty joinedCourses}"><h3>Lớp đã tham gia</h3><ul>
<c:forEach items="${joinedCourses}" var="course"><li><c:out value="${course.selectionLabel}"/> · <c:out value="${course.stage}"/></li></c:forEach>
</ul><p>Bạn có thể tạo nhóm hoặc nhận lời mời từ nhóm trưởng và xác nhận bên dưới.</p></c:if>
</section>
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
                    <th>Môn/lớp đăng ký</th>
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
