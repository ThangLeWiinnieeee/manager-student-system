<%@ page pageEncoding="UTF-8" %>
<c:if test="${not empty invitations}">
<section class="panel">
<h2>Lời mời đang chờ</h2>
<p>Lời mời chưa tính vào số thành viên. Cần xử lý hết lời mời trước khi nhóm đăng ký đề tài.</p>
<ul>
<c:forEach items="${invitations}" var="invitation">
<li><c:out value="${invitation.student.fullName}"/> · <c:out value="${invitation.student.email}"/>
<form method="post" class="inline-form" action="<c:url value='${invitationBase}/${group.id}/invitations/${invitation.student.id}/cancel'/>">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<button class="button" type="submit">Thu hồi lời mời</button></form>
</li>
</c:forEach>
</ul>
</section>
</c:if>
