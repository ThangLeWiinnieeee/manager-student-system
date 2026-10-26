<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${isNew ? 'Thành lập Hội đồng mới' : 'Chỉnh sửa Hội đồng'}" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <h1><c:out value="${pageTitle}" /></h1>
    <p>Thiết lập thông tin cơ bản của hội đồng, đợt áp dụng, địa điểm và thời gian báo cáo.</p>
  </div>
  <a class="button" href="<c:url value='/admin/councils'/>">Quay lại danh sách</a>
</div>

<div class="card" style="background: #fff; padding: 24px; border-radius: 8px; border: 1px solid var(--border-color, #e2e8f0); max-width: 700px;">
  <form action="<c:url value='${isNew ? \"/admin/councils\" : \"/admin/councils/\".concat(councilId)}'/>" method="post">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

    <div class="form-group" style="margin-bottom: 16px;">
      <label for="name" style="display: block; font-weight: 600; margin-bottom: 6px;">Tên hội đồng *</label>
      <input type="text" id="name" name="name" value="<c:out value='${councilForm.name}'/>" required maxlength="150" placeholder="Ví dụ: Hội đồng KLTN số 01 - Bộ môn HTTT" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px;" />
    </div>

    <div class="form-group" style="margin-bottom: 16px;">
      <label for="roundId" style="display: block; font-weight: 600; margin-bottom: 6px;">Môn/lớp đăng ký *</label>
      <select id="roundId" name="roundId" required style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px;">
        <c:forEach items="${periods}" var="p">
          <option value="${p.id}" ${councilForm.roundId == p.id ? 'selected' : ''}>
            <c:out value="${p.selectionLabel}" /> (<c:out value="${p.type.displayName}" />)
          </option>
        </c:forEach>
      </select>
    </div>

    <div class="form-group" style="margin-bottom: 16px;">
      <label for="location" style="display: block; font-weight: 600; margin-bottom: 6px;">Địa điểm báo cáo</label>
      <input type="text" id="location" name="location" value="<c:out value='${councilForm.location}'/>" maxlength="200" placeholder="Ví dụ: Phòng H.501, Tòa H" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px;" />
    </div>

    <div class="form-group" style="margin-bottom: 24px;">
      <label for="reportDate" style="display: block; font-weight: 600; margin-bottom: 6px;">Ngày giờ báo cáo</label>
      <input type="datetime-local" id="reportDate" name="reportDate" value="<c:out value='${councilForm.reportDate}'/>" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px;" />
      <small style="color: #64748b; display: block; margin-top: 4px;">Thời gian tổ chức các buổi thuyết trình và chấm điểm của hội đồng.</small>
    </div>

    <div style="display: flex; gap: 12px;">
      <button type="submit" class="button primary">
        <c:out value="${isNew ? 'Thành lập hội đồng' : 'Lưu thay đổi'}" />
      </button>
      <a class="button" href="<c:url value='/admin/councils'/>">Hủy bỏ</a>
    </div>
  </form>
</div>

<%@ include file="../common/footer.jsp" %>
