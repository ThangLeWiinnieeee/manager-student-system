<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý bộ môn"/>
<%@ include file="../../common/header.jsp" %>
<div class="page-heading">
    <div><h1>Quản lý bộ môn</h1><p>Danh mục bộ môn dùng khi quản lý giảng viên và đề tài.</p></div>
    <a class="button primary" href="<c:url value='/admin/departments/new'/>">Thêm bộ môn</a>
</div>
<div class="table-wrap">
<table>
    <thead><tr><th>Mã</th><th>Tên bộ môn</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
    <tbody>
    <c:forEach items="${departments}" var="department">
        <tr>
            <td><c:out value="${department.code}"/></td>
            <td><c:out value="${department.name}"/></td>
            <td><span class="badge ${department.enabled ? 'active' : 'inactive'}">${department.enabled ? 'Hoạt động' : 'Đã khóa'}</span></td>
            <td class="actions">
                <a class="button" href="<c:url value='/admin/departments/${department.id}/edit'/>">Sửa</a>
                <form action="<c:url value='/admin/departments/${department.id}/toggle'/>" method="post" class="inline-form">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button type="submit" class="button">${department.enabled ? 'Khóa' : 'Mở khóa'}</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty departments}"><tr><td colspan="4" class="empty">Chưa có bộ môn.</td></tr></c:if>
    </tbody>
</table>
</div>
<%@ include file="../../common/footer.jsp" %>
