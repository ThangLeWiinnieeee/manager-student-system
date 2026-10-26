<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý đợt đăng ký" />
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
    <div>
        <h1>Quản lý đợt đăng ký</h1>
        <p>Theo dõi các đợt đăng ký và giai đoạn hiện tại.</p>
    </div>
    <c:if test="${manager}"><a class="button primary" href="<c:url value='/admin/registration-periods/new'/>">Mở đợt đăng ký</a></c:if>
</div>
<div class="table-wrap" role="region" aria-label="Danh sách đợt đăng ký" tabindex="0">
    <table aria-label="Danh sách đợt đăng ký">
        <thead>
            <tr>
                <th scope="col">Tên đợt</th>
                <th scope="col">Loại</th>
                <th scope="col">Giai đoạn hiện tại</th>
                <th scope="col">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${periods}" var="period">
                <tr>
                    <td><strong><c:out value="${period.name}" /> (#<c:out value="${period.id}" />)</strong></td>
                    <td><c:out value="${period.type.displayName}" /></td>
                    <td>
                        <span class="badge ${period.ended ? 'inactive' : 'active'}">
                            <c:out value="${period.stage}" />
                        </span>
                    </td>
                    <td class="actions">
                        <a class="button" href="<c:url value='/admin/registration-periods/${period.id}'/>">Xem chi tiết</a>
                        <c:if test="${manager}"><a class="button" href="<c:url value='/admin/registration-periods/${period.id}/edit'/>">Sửa</a>
                        <c:if test="${not period.ended}">
                            <form action="<c:url value='/admin/registration-periods/${period.id}/delete'/>" method="post"
                                  class="inline-form" onsubmit="return confirm('Bạn có chắc muốn xóa đợt đăng ký này?');">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                <button type="submit" class="button" style="color: #dc2626;">Xóa</button>
                            </form>
                        </c:if>
                        </c:if>
                        <c:if test="${pageContext.request.isUserInRole('LECTURER') and period.gvOpen}"><a class="button primary" href="<c:url value='/course-registrations/new?periodId=${period.id}'/>">Đăng ký môn/lớp</a></c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty periods}">
                <tr><td colspan="4" class="empty">Chưa có đợt đăng ký nào.</td></tr>
            </c:if>
        </tbody>
    </table>
</div>
<%@ include file="../common/footer.jsp" %>
