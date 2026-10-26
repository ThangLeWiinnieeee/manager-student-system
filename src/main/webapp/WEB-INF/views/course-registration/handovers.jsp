<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Bàn giao nhiệm vụ"/>
<%@ include file="../common/header.jsp" %>
<div class="page-heading"><div><h1>Bàn giao nhiệm vụ</h1><p><c:out value="${course.courseLabel}"/></p></div>
<a class="button" href="<c:url value='/course-registrations/${course.id}'/>">Về môn/lớp</a></div>
<c:if test="${not empty success}"><p class="alert alert-success" role="status"><c:out value="${success}"/></p></c:if>
<c:if test="${not empty error}"><p class="alert alert-danger" role="alert"><c:out value="${error}"/></p></c:if>
<section class="panel">
<p>Chọn đúng nhiệm vụ cần bàn giao. Đổi GV phụ trách lớp không tự đổi GVHD. GV hỗ trợ TLCN phải là GVHD; bàn giao GVHD sẽ chuyển cả lượt hỗ trợ chưa nộp của người đó.</p>
<p>Không thay người đã nộp điểm hoặc hồ sơ đã chốt. Điểm nháp của người cũ được giữ trong dữ liệu lịch sử, người mới chấm độc lập. Thay thành viên hội đồng chỉ áp dụng cho nhóm được chọn.</p>
<c:forEach items="${tasks}" var="task" varStatus="row">
<details class="panel"><summary><c:out value="${task.label}"/> — <c:out value="${task.previous.fullName}"/></summary>
<form method="post" action="<c:url value='/admin/handovers'/>">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<input type="hidden" name="courseId" value="${course.id}"/>
<input type="hidden" name="duty" value="${task.duty}"/>
<input type="hidden" name="targetId" value="${task.targetId}"/>
<input type="hidden" name="previousId" value="${task.previous.id}"/>
<label for="replacement-${row.index}">Giảng viên nhận bàn giao</label>
<select id="replacement-${row.index}" name="replacementId" required><option value="">Chọn giảng viên</option>
<c:forEach items="${lecturers}" var="lecturer"><c:if test="${lecturer.id ne task.previous.id}">
<option value="${lecturer.id}"><c:out value="${lecturer.fullName}"/> · <c:out value="${lecturer.email}"/></option>
</c:if></c:forEach></select>
<label for="reason-${row.index}">Lý do bàn giao</label><textarea id="reason-${row.index}" name="reason" maxlength="500" required></textarea>
<button class="button primary" type="submit">Bàn giao nhiệm vụ này</button>
</form></details>
</c:forEach>
<c:if test="${empty tasks}"><p>Không có nhiệm vụ để bàn giao.</p></c:if>
</section>
<section class="panel"><h2>Lịch sử bàn giao</h2>
<div class="table-wrap"><table><thead><tr><th>Nhiệm vụ / mã hồ sơ</th><th>Từ</th><th>Sang</th><th>Người thực hiện</th><th>Lý do</th><th>Thời gian</th></tr></thead><tbody>
<c:forEach items="${history}" var="entry"><tr>
<td><c:out value="${entry.task}"/> #<c:out value="${entry.target_id}"/></td>
<td><c:out value="${entry.previous_name}"/></td><td><c:out value="${entry.replacement_name}"/></td>
<td><c:out value="${entry.actor_name}"/></td><td><c:out value="${entry.reason}"/></td><td><c:out value="${entry.created_at}"/></td>
</tr></c:forEach>
</tbody></table></div></section>
<%@ include file="../common/footer.jsp" %>
