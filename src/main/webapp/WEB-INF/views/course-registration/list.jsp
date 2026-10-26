<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Môn/lớp đăng ký" />
<%@ include file="../common/header.jsp" %>
<div class="page-heading">
    <div>
        <h1>Môn/lớp đăng ký</h1>
        <p>Quản lý môn/lớp tham gia các đợt do Khoa mở.</p>
    </div>
    <c:if test="${pageContext.request.isUserInRole('LECTURER')}"><a class="button primary" href="<c:url value='/course-registrations/new'/>">Đăng ký môn/lớp</a></c:if>
</div>
<div class="table-wrap" role="region" aria-label="Danh sách đợt đăng ký" tabindex="0">
    <table aria-label="Danh sách đợt đăng ký">
        <thead>
            <tr>
                <th scope="col">Môn/lớp</th>
                <th scope="col">Đợt chung</th>
                <th scope="col">Giai đoạn hiện tại</th>
                <th scope="col">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${periods}" var="period">
                <tr>
                    <td><strong><c:out value="${period.name}" /></strong></td>
                    <td><c:out value="${period.period.name}" /></td>
                    <td>
                        <span class="badge ${period.ended and not period.reviewerGradingOpen ? 'inactive' : 'active'}">
                            <c:out value="${period.stage}" />
                        </span>
                    </td>
                    <td class="actions">
                        <a class="button" href="<c:url value='/course-registrations/${period.id}'/>">Xem chi tiết</a>
                        <a class="button" href="<c:url value='/course-registrations/${period.id}/edit'/>">Sửa</a>
                        <c:if test="${not period.ended}">
                            <form action="<c:url value='/course-registrations/${period.id}/delete'/>" method="post"
                                  class="inline-form" onsubmit="return confirm('Bạn có chắc muốn xóa môn/lớp này?');">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                <button type="submit" class="button" style="color: #dc2626;">Xóa</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty periods}">
                <tr><td colspan="4" class="empty">Chưa có môn/lớp đăng ký.</td></tr>
            </c:if>
        </tbody>
    </table>
</div>
<%@ include file="../common/footer.jsp" %>
