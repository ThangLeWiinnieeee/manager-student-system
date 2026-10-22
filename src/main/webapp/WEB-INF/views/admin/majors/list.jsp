<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý chuyên ngành"/>
<%@ include file="../../common/header.jsp" %>
<div class="page-heading">
    <div><h1>Quản lý chuyên ngành</h1><p>Danh mục chuyên ngành dùng khi quản lý sinh viên và phân loại đề tài.</p></div>
    <a class="button primary" href="<c:url value='/admin/majors/new'/>">Thêm chuyên ngành</a>
</div>
<div class="table-wrap" role="region" aria-label="Danh sách chuyên ngành" tabindex="0">
<table aria-label="Danh sách chuyên ngành">
    <thead><tr><th>Mã</th><th>Tên chuyên ngành</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
    <tbody>
    <c:forEach items="${majors}" var="major">
        <tr>
            <td><c:out value="${major.code}"/></td>
            <td><c:out value="${major.name}"/></td>
            <td><span class="badge ${major.enabled ? 'active' : 'inactive'}">${major.enabled ? 'Hoạt động' : 'Đã khóa'}</span></td>
            <td class="actions">
                <a class="button" href="<c:url value='/admin/majors/${major.id}/edit'/>">Sửa</a>
                <form action="<c:url value='/admin/majors/${major.id}/toggle'/>" method="post" class="inline-form">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button type="submit" class="button">${major.enabled ? 'Khóa' : 'Mở khóa'}</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty majors}"><tr><td colspan="4" class="empty">Chưa có chuyên ngành. Chọn “Thêm chuyên ngành” để tạo chuyên ngành đầu tiên.</td></tr></c:if>
    </tbody>
</table>
</div>
<%@ include file="../../common/footer.jsp" %>
