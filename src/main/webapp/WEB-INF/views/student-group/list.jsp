
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Quản lý nhóm sinh viên"/>

<%@ include file="../common/header.jsp" %>

<!-- TIEU DE TRANG -->
<div class="page-heading">
    <div>
        <h1>Quản lý nhóm sinh viên</h1>
        <p>Quản lý các nhóm sinh viên đăng ký và thực hiện đề tài.</p>
    </div>

    <a class="button primary"
       href="<c:url value='/admin/student-groups/new'/>">
        <span aria-hidden="true">+</span> Thêm nhóm
    </a>
</div>

<!-- THONG KE -->
<div class="stats" aria-label="Thống kê nhóm sinh viên">

    <section>
        <span>Tổng số nhóm</span>

        <div class="stat-icon">
            <svg class="nav-icon" viewBox="0 0 24 24"
                 aria-hidden="true">
                <circle cx="9" cy="8" r="3"/>
                <path d="M3 21v-3a6 6 0 0 1 12 0v3"/>
                <path d="M16 5a3 3 0 0 1 0 6"/>
                <path d="M18 14a5 5 0 0 1 3 5v2"/>
            </svg>
        </div>

        <strong>
            <c:out value="${fn:length(groups)}"/>
        </strong>

        <small>Nhóm sinh viên trong hệ thống</small>
    </section>

    <section>
        <span>Hoạt động</span>

        <div class="stat-icon">
            <svg class="nav-icon" viewBox="0 0 24 24"
                 aria-hidden="true">
                <path d="M3 7V4h6l3 3h9v13H3z"/>
                <path d="M3 10h18"/>
            </svg>
        </div>

        <strong>Đề tài</strong>
        <small>Đăng ký và thực hiện đề tài</small>
    </section>

</div>

<!-- BANG DANH SACH -->
<section class="panel">

    <div class="panel-heading">
        <h2>Danh sách nhóm sinh viên</h2>

        <span class="neutral-badge">
            <c:out value="${fn:length(groups)}"/> nhóm
        </span>
    </div>

    <p>Thông tin các nhóm đã tạo trong từng đợt đăng ký.</p>

    <div class="table-wrap"
         role="region"
         aria-label="Danh sách nhóm sinh viên"
         tabindex="0">

        <table aria-label="Danh sách nhóm sinh viên">

            <thead>
            <tr>
                <th>Mã nhóm</th>
                <th>Tên nhóm</th>
                <th>Đợt đăng ký</th>
                <th>Thao tác</th>
            </tr>
            </thead>

            <tbody>

            <c:forEach items="${groups}" var="group">

                <tr>
                    <td>
                        <c:out value="${group.id}"/>
                    </td>

                    <td>
                        <strong>
                            <c:out value="${group.name}"/>
                        </strong>
                    </td>

                    <td>
                        <c:out value="${group.roundId}"/>
                    </td>

                    <td class="actions">

                        <!-- TV3: Xem chi tiet nhom -->
                        <a class="button"
                           href="<c:url value='/admin/student-groups/${group.id}'/>">
                            Chi tiết
                        </a>

                        <a class="button"
                           href="<c:url value='/admin/student-groups/${group.id}/edit'/>">
                            Sửa
                        </a>

                        <form
                            action="<c:url value='/admin/student-groups/${group.id}/delete'/>"
                            method="post"
                            class="inline-form"
                            onsubmit="return confirm('Bạn có chắc chắn muốn xóa nhóm này?');">

                            <input type="hidden"
                                   name="${_csrf.parameterName}"
                                   value="${_csrf.token}"/>

                            <button type="submit" class="button">
                                Xóa
                            </button>

                        </form>

                    </td>
                </tr>

            </c:forEach>

            <c:if test="${empty groups}">
                <tr>
                    <td colspan="4" class="empty">
                        Chưa có nhóm sinh viên nào.
                        Hãy chọn "Thêm nhóm" để tạo nhóm đầu tiên.
                    </td>
                </tr>
            </c:if>

            </tbody>
        </table>

    </div>

</section>

<%@ include file="../common/footer.jsp" %>
