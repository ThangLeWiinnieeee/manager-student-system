
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Chi tiết nhóm của tôi"/>

<%@ include file="../common/header.jsp" %>


<%-- ========================================= --%>
<%-- TV3: TIEU DE TRANG                        --%>
<%-- ========================================= --%>

<div class="page-heading">
     <a class="button"
           href="<c:url value='/student/my-groups/${group.id}/topics'/>">
            Xem / Đăng ký đề tài
     </a>

     <a class="button"
        href="<c:url value='/student/my-groups/${group.id}/submissions'/>">
         Báo cáo đề tài
     </a>

    <div>
        <h1>Chi tiết nhóm của tôi</h1>

        <p>
            Nhóm:
            <c:out value="${group.name}"/>

            | Môn/lớp đăng ký:
            <c:out value="${round.courseLabel}"/>
        </p>
    </div>

    <a class="button"
       href="<c:url value='/student/my-groups'/>">
        Quay lại
    </a>

</div>


<%-- ========================================= --%>
<%-- THONG BAO THANH CONG                       --%>
<%-- ========================================= --%>

<c:if test="${not empty success}">

    <p class="alert alert-success"
       role="status">

        <c:out value="${success}"/>

    </p>

</c:if>


<%-- ========================================= --%>
<%-- THONG BAO LOI                              --%>
<%-- ========================================= --%>

<c:if test="${not empty error}">

    <p class="alert alert-danger"
       role="alert">

        <c:out value="${error}"/>

    </p>

</c:if>


<%-- ========================================= --%>
<%-- THONG TIN NHOM                             --%>
<%-- ========================================= --%>

<section class="panel">

    <div class="panel-heading">

        <h2>Thông tin nhóm</h2>

        <c:choose>

            <c:when test="${isLeader}">
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

    </div>


    <p>
        <strong>Mã nhóm:</strong>
        <c:out value="${group.id}"/>
    </p>


    <p>
        <strong>Tên nhóm:</strong>
        <c:out value="${group.name}"/>
    </p>


    <p>
        <strong>Môn/lớp đăng ký:</strong>
        <c:out value="${round.courseLabel}"/>
    </p>


    <p>
        <strong>Số thành viên:</strong>
        <c:out value="${memberCount}"/>
    </p>


    <p>
        <strong>Vai trò của bạn:</strong>

        <c:choose>

            <c:when test="${isLeader}">
                Nhóm trưởng
            </c:when>

            <c:otherwise>
                Thành viên
            </c:otherwise>

        </c:choose>

    </p>

</section>


<%-- ========================================= --%>
<%-- FORM THEM THANH VIEN                       --%>
<%-- CHI NHOM TRUONG MOI THAY FORM NAY          --%>
<%-- ========================================= --%>

<c:if test="${isLeader and membershipOpen}">

    <section class="panel">

        <div class="panel-heading">
            <h2>Mời thành viên</h2>
        </div>


        <p>
            Nhập email của sinh viên đã có tài khoản
            trong hệ thống và chưa tham gia nhóm khác
            trong cùng môn/lớp đăng ký.
        </p>


        <form method="post"
              class="form-card narrow"
              action="<c:url value='/student/my-groups/${group.id}/members'/>">


            <label for="email">
                Email sinh viên
            </label>


            <input type="email"
                   id="email"
                   name="email"
                   maxlength="150"
                   placeholder="sinhvien@example.com"
                   required>


            <%-- CSRF TOKEN --%>

            <input type="hidden"
                   name="${_csrf.parameterName}"
                   value="${_csrf.token}">


            <div class="form-actions">

                <button type="submit"
                        class="primary">

                    + Mời thành viên

                </button>

            </div>

        </form>

    </section>

</c:if>


<%-- ========================================= --%>
<%-- DANH SACH THANH VIEN                       --%>
<%-- ========================================= --%>

<section class="panel">

    <div class="panel-heading">

        <h2>Danh sách thành viên</h2>

        <span class="neutral-badge">
            <c:out value="${memberCount}"/>
            thành viên
        </span>

    </div>


    <div class="table-wrap"
         role="region"
         aria-label="Danh sách thành viên"
         tabindex="0">


        <table aria-label="Danh sách thành viên">


            <%-- TIEU DE BANG --%>

            <thead>

                <tr>

                    <th>Họ tên</th>

                    <th>Email</th>

                    <th>Vai trò</th>

                    <c:if test="${isLeader and membershipOpen}">
                        <th>Thao tác</th>
                    </c:if>

                </tr>

            </thead>


            <%-- NOI DUNG BANG --%>

            <tbody>


                <c:forEach items="${members}"
                           var="member">

                    <tr>


                        <%-- HO TEN SINH VIEN --%>

                        <td>
                            <c:out value="${member.fullName}"/>
                        </td>


                        <%-- EMAIL SINH VIEN --%>

                        <td>
                            <c:out value="${member.email}"/>
                        </td>


                        <%-- VAI TRO TRONG NHOM --%>

                        <td>

                            <c:choose>

                                <c:when test="${member.leader}">

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


                        <%-- ================================= --%>
                        <%-- NUT XOA CHI DANH CHO NHOM TRUONG   --%>
                        <%-- ================================= --%>

                        <c:if test="${isLeader and membershipOpen}">

                            <td class="actions">

                                <c:choose>


                                    <%-- KHONG CHO XOA NHOM TRUONG --%>

                                    <c:when test="${member.leader}">

                                        <span class="neutral-badge">
                                            Không thể xóa
                                        </span>

                                    </c:when>


                                    <%-- XOA THANH VIEN THUONG --%>

                                    <c:otherwise>

                                        <form method="post"
                                              class="inline-form"
                                              action="<c:url value='/student/my-groups/${group.id}/members/${member.studentId}/remove'/>"
                                              onsubmit="return confirm('Bạn có chắc chắn muốn xóa thành viên này khỏi nhóm?');">


                                            <%-- CSRF TOKEN --%>

                                            <input type="hidden"
                                                   name="${_csrf.parameterName}"
                                                   value="${_csrf.token}">


                                            <button type="submit"
                                                    class="button">

                                                Xóa

                                            </button>

                                        </form>

                                    </c:otherwise>


                                </c:choose>

                            </td>

                        </c:if>


                    </tr>

                </c:forEach>


                <%-- ================================= --%>
                <%-- TRUONG HOP NHOM CHUA CO THANH VIEN --%>
                <%-- ================================= --%>

                <c:if test="${empty members}">

                    <tr>

                        <td colspan="${isLeader ? 4 : 3}"
                            class="empty">

                            Nhóm hiện chưa có thành viên.

                        </td>

                    </tr>

                </c:if>


            </tbody>

        </table>

    </div>

</section>


<%-- ========================================= --%>
<%-- FOOTER                                    --%>
<%-- ========================================= --%>

<c:if test="${isLeader}">
<c:set var="invitationBase" value="/student/my-groups"/>
<%@ include file="invitations.jsp" %>
</c:if>
<%@ include file="../common/footer.jsp" %>
