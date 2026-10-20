<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${isNew ? 'Tạo thông báo mới' : 'Chỉnh sửa thông báo'}" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <h1><c:out value="${pageTitle}" /></h1>
    <p>Nhập nội dung thông báo và chọn nhóm đối tượng nhận thông báo.</p>
  </div>
  <a class="button" href="<c:url value='/admin/notifications'/>">Quay lại danh sách</a>
</div>

<div class="card" style="background: #fff; padding: 24px; border-radius: 8px; border: 1px solid var(--border-color, #e2e8f0); max-width: 800px;">
  <form action="<c:url value='${isNew ? \"/admin/notifications\" : \"/admin/notifications/\".concat(notificationId)}'/>" method="post">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

    <div class="form-group" style="margin-bottom: 16px;">
      <label for="title" style="display: block; font-weight: 600; margin-bottom: 6px;">Tiêu đề thông báo *</label>
      <input type="text" id="title" name="title" value="<c:out value='${notificationForm.title}'/>" required maxlength="200" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px;" />
    </div>

    <div class="form-group" style="margin-bottom: 16px;">
      <label for="targetRole" style="display: block; font-weight: 600; margin-bottom: 6px;">Đối tượng nhận thông báo *</label>
      <select id="targetRole" name="targetRole" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px;">
        <c:forEach items="${targets}" var="t">
          <option value="${t.name()}" ${notificationForm.targetRole == t ? 'selected' : ''}>
            <c:out value="${t.displayName}" />
          </option>
        </c:forEach>
      </select>
    </div>

    <div class="form-group" style="margin-bottom: 16px;">
      <label for="content" style="display: block; font-weight: 600; margin-bottom: 6px;">Nội dung chi tiết *</label>
      <textarea id="content" name="content" rows="10" required maxlength="4000" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-family: inherit;"><c:out value='${notificationForm.content}'/></textarea>
    </div>

    <div style="display: flex; gap: 24px; margin-bottom: 24px;">
      <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
        <input type="checkbox" name="pinned" value="true" ${notificationForm.pinned ? 'checked' : ''} />
        <span>Ghim thông báo lên đầu trang</span>
      </label>

      <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
        <input type="checkbox" name="published" value="true" ${notificationForm.published ? 'checked' : ''} />
        <span>Công bố ngay lập tức</span>
      </label>
    </div>

    <div style="display: flex; gap: 12px;">
      <button type="submit" class="button primary">
        <c:out value="${isNew ? 'Đăng thông báo' : 'Lưu thay đổi'}" />
      </button>
      <a class="button" href="<c:url value='/admin/notifications'/>">Hủy bỏ</a>
    </div>
  </form>
</div>

<%@ include file="../common/footer.jsp" %>
