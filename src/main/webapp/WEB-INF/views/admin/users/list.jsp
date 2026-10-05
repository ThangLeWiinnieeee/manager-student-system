<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý tài khoản"/>
<%@ include file="../../common/header.jsp" %>
<div class="page-heading">
    <div><h1>Quản lý tài khoản</h1><p>Tạo tài khoản và phân quyền người dùng.</p></div>
    <a class="button primary" href="<c:url value='/admin/users/new'/>">Thêm tài khoản</a>
</div>
<div class="table-wrap">
<table>
    <thead><tr><th>Tên đăng nhập</th><th>Họ tên</th><th>Email</th><th>Vai trò</th><th>Bộ môn</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
    <tbody>
    <c:forEach items="${users}" var="user">
        <tr>
            <td><c:out value="${user.username}"/></td>
            <td><c:out value="${user.fullName}"/></td>
            <td><c:out value="${user.email}"/></td>
            <td><c:out value="${user.role.displayName}"/></td>
            <td><c:out value="${empty user.department ? '-' : user.department.name}"/></td>
            <td><span class="badge ${user.enabled ? 'active' : 'inactive'}">${user.enabled ? 'Hoạt động' : 'Đã khóa'}</span></td>
            <td class="actions">
                <a class="button" href="<c:url value='/admin/users/${user.id}/edit'/>">Sửa</a>
                <form action="<c:url value='/admin/users/${user.id}/toggle'/>" method="post" class="inline-form">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button type="submit" class="button">${user.enabled ? 'Khóa' : 'Mở khóa'}</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty users}"><tr><td colspan="7" class="empty">Chưa có tài khoản.</td></tr></c:if>
    </tbody>
</table>
</div>
<%@ include file="../../common/footer.jsp" %>
