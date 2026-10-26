
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Tạo nhóm sinh viên"/>

<%@ include file="../common/header.jsp" %>

<div class="page-heading">
    <div>
        <h1>Tạo nhóm sinh viên</h1>
        <p>
            Tạo nhóm đăng ký đề tài trong đợt đang mở.
            Bạn sẽ tự động trở thành nhóm trưởng.
        </p>
    </div>
</div>

<section class="panel">

    <c:if test="${not empty error}">
        <p class="alert alert-danger">
            <c:out value="${error}"/>
        </p>
    </c:if>

    <c:choose>
        <c:when test="${empty openPeriods}">
            <p>
                Bạn chưa tham gia lớp đề tài nào đang mở đăng ký nhóm. Hãy về “Nhóm của tôi” để nhập mã GV cung cấp.
            </p>

            <a class="button"
               href="<c:url value='/student/my-groups'/>">
                Quay lại
            </a>
        </c:when>

        <c:otherwise>
            <form method="post"
                  action="<c:url value='/student/my-groups'/>"
                  class="form-card">

                <label for="name">
                    Tên nhóm
                </label>

                <input type="text"
                       id="name"
                       name="name"
                       value="<c:out value='${groupForm.name}'/>"
                       maxlength="150"
                       placeholder="Nhóm CNTT 01"
                       required>

                <label for="roundId">
                    Môn/lớp đăng ký
                </label>

                <select id="roundId"
                        name="roundId"
                        required>

                    <option value="">
                        -- Chọn môn/lớp đăng ký --
                    </option>

                    <c:forEach items="${openPeriods}"
                               var="period">

                        <option value="${period.id}"
                            ${groupForm.roundId == period.id
                              ? 'selected="selected"' : ''}>

                            <c:out value="${period.selectionLabel}"/> · Tối đa <c:out value="${period.maxMembers}"/> SV · <c:out value="${period.registrationMode.displayName}"/>

                        </option>

                    </c:forEach>

                </select>

                <input type="hidden"
                       name="${_csrf.parameterName}"
                       value="${_csrf.token}">

                <div class="form-actions">

                    <a class="button"
                       href="<c:url value='/student/my-groups'/>">
                        Hủy
                    </a>

                    <button type="submit" class="primary">
                        Tạo nhóm
                    </button>

                </div>

            </form>
        </c:otherwise>
    </c:choose>

</section>

<%@ include file="../common/footer.jsp" %>
