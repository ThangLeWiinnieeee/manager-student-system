
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Chi tiết nhóm sinh viên"/>

<%@ include file="../common/header.jsp" %>

<!-- TIEU DE -->
<div class="page-heading">
    <div>
        <h1>Chi tiết nhóm sinh viên</h1>
        <p>
            Nhóm:
            <c:out value="${group.name}"/>
            | Môn/lớp đăng ký:
            <c:out value="${group.roundId}"/>
        </p>
    </div>

    <a class="button"
       href="<c:url value='/admin/student-groups'/>">
        Về danh sách
    </a>
</div>

<!-- THONG KE NHOM -->
<div class="stats">

    <section>
        <span>Mã nhóm</span>

        <div class="stat-icon">
            <svg class="nav-icon"
                 viewBox="0 0 24 24"
                 aria-hidden="true">
                <path d="M3 7V4h6l3 3h9v13H3z"/>
                <path d="M3 10h18"/>
            </svg>
        </div>

        <strong>
            <c:out value="${group.id}"/>
        </strong>

        <small>Mã định danh nhóm</small>
    </section>

    <section>
        <span>Tổng thành viên</span>

        <div class="stat-icon">
            <svg class="nav-icon"
                 viewBox="0 0 24 24"
                 aria-hidden="true">
                <circle cx="9" cy="8" r="3"/>
                <path d="M3 21v-3a6 6 0 0 1 12 0v3"/>
                <path d="M16 5a3 3 0 0 1 0 6"/>
            </svg>
        </div>

        <strong>
            <c:out value="${memberCount}"/>
        </strong>

        <small>Sinh viên trong nhóm</small>
    </section>

</div>

<!-- THEM THANH VIEN -->
<c:if test="${membershipOpen}">
<section class="panel">

    <div class="panel-heading">
        <h2>Mời thành viên</h2>
    </div>

    <p>
        Nhập email của sinh viên đã có tài khoản
        trong hệ thống.
    </p>

    <form method="post"
          action="<c:url value='/admin/student-groups/${group.id}/members'/>"
          class="form-card narrow">

        <label for="email">
            Email sinh viên
        </label>

        <input type="email"
               id="email"
               name="email"
               maxlength="150"
               placeholder="sinhvien@example.com"
               required>

        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}">

        <div class="form-actions">
            <button type="submit" class="primary">
                + Mời thành viên
            </button>
        </div>

    </form>

</section>

</c:if>
<p>GV phụ trách có thể chuyển nhóm trưởng cho thành viên hiện hữu sau khi nhận đề tài, trước khi chốt kết quả. Danh sách thành viên và báo cáo cũ được giữ nguyên.</p>
<!-- DANH SACH THANH VIEN -->
<section class="panel">

    <div class="panel-heading">
        <h2>Danh sách thành viên</h2>

        <span class="neutral-badge">
            <c:out value="${memberCount}"/> thành viên
        </span>
    </div>

    <div class="table-wrap"
         role="region"
         aria-label="Danh sách thành viên"
         tabindex="0">

        <table aria-label="Danh sách thành viên">

            <thead>
                <tr>
                    <th>Họ tên</th>
                    <th>Email</th>
                    <th>Vai trò</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach items="${members}" var="member">

                    <tr>
                        <td>
                            <c:out value="${member.fullName}"/>
                        </td>

                        <td>
                            <c:out value="${member.email}"/>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${member.leader}">
                                    <span class="badge active">
                                        Nhóm trưởng
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    Thành viên
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td class="actions">

                            <c:choose>

                                <c:when test="${member.leader}">
                                    <span class="neutral-badge">
                                        Nhóm trưởng
                                    </span>
                                </c:when>

                                <c:otherwise>

                                    <!-- CHON NHOM TRUONG -->
                                    <form method="post"
                                          class="inline-form"
                                          action="<c:url value='/admin/student-groups/${group.id}/leader/${member.studentId}'/>">

                                        <input type="hidden"
                                               name="${_csrf.parameterName}"
                                               value="${_csrf.token}">

                                        <button type="submit"
                                                class="button">
                                            Chọn trưởng
                                        </button>
                                    </form>

                                    <c:if test="${membershipOpen}">
                                    <!-- XOA THANH VIEN -->
                                    <form method="post"
                                          class="inline-form"
                                          action="<c:url value='/admin/student-groups/${group.id}/members/${member.studentId}/remove'/>"
                                          onsubmit="return confirm('Bạn muốn xóa thành viên này?');">

                                        <input type="hidden"
                                               name="${_csrf.parameterName}"
                                               value="${_csrf.token}">

                                        <button type="submit"
                                                class="button">
                                            Xóa
                                        </button>
                                    </form>
                                    </c:if>

                                </c:otherwise>

                            </c:choose>

                        </td>
                    </tr>

                </c:forEach>

                <c:if test="${empty members}">
                    <tr>
                        <td colspan="4" class="empty">
                            Nhóm hiện chưa có thành viên.
                            Hãy thêm sinh viên đầu tiên.
                        </td>
                    </tr>
                </c:if>

            </tbody>
        </table>
    </div>

</section>

<c:set var="invitationBase" value="/admin/student-groups"/>
<%@ include file="invitations.jsp" %>
<%@ include file="../common/footer.jsp" %>
