<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Phiếu đánh giá đề tài: ${topic.title}" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <a href="<c:url value='/lecturer/evaluations'/>" style="text-decoration: none; color: #64748b; font-size: 0.9rem;">← Quay lại danh sách đề tài cần chấm</a>
    <h1 style="margin-top: 8px;">Phiếu đánh giá & Chấm điểm đề tài</h1>
    <p>
      Đề tài: <strong><c:out value="${topic.title}" /></strong>
      · Môn/lớp: <strong><c:out value="${topic.round.name}" /></strong>
      · Trạng thái phiếu:
      <c:choose>
        <c:when test="${currentEval != null && currentEval.status == 'SUBMITTED'}">
          <span class="badge active">Đã xác nhận nộp (Khóa chỉnh sửa)</span>
        </c:when>
        <c:when test="${currentEval != null && currentEval.status == 'DRAFT'}">
          <span class="badge warn">Bản nháp</span>
        </c:when>
        <c:otherwise>
          <span class="badge inactive">Chưa nhập điểm</span>
        </c:otherwise>
      </c:choose>
    </p>
  </div>
</div>

<c:set var="isLocked" value="${currentEval != null && currentEval.status == 'SUBMITTED'}" />

<div class="card" style="background: #fff; padding: 24px; border-radius: 8px; border: 1px solid var(--border-color, #e2e8f0); max-width: 750px; margin-bottom: 24px;">
  <p>Nhóm: <strong><c:out value="${registration.group.name}"/></strong> · <a href="<c:url value='/review/submissions/groups/${registration.group.id}'/>">Xem báo cáo để chấm</a></p>
<form action="<c:url value='/lecturer/evaluations/registration/${registration.id}'/>" method="post" id="evalForm">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    <input type="hidden" name="action" id="formAction" value="draft" />

    <div style="background: #f8fafc; padding: 16px; border-radius: 6px; margin-bottom: 20px; border-left: 4px solid #3b82f6;">
      <h3 style="margin: 0 0 8px 0; font-size: 1rem;">Quy định thang điểm (Thang điểm 10.0)</h3>
      <ul style="margin: 0; padding-left: 20px; font-size: 0.875rem; color: #475569; line-height: 1.6;">
        <li><strong>Nội dung & Chuyên môn:</strong> Tối đa 4.0 điểm.</li>
        <li><strong>Báo cáo & Trình bày:</strong> Tối đa 3.0 điểm.</li>
        <li><strong>Trả lời câu hỏi & Phản biện:</strong> Tối đa 3.0 điểm.</li>
        <li><strong>Lưu ý:</strong> Sau khi nhấn <em>"Xác nhận nộp điểm"</em>, phiếu điểm sẽ bị <strong>khóa hoàn toàn</strong> và không thể thay đổi!</li>
      </ul>
    </div>

    <div class="form-group" style="margin-bottom: 16px;">
      <label for="scoreContent" style="display: block; font-weight: 600; margin-bottom: 6px;">1. Điểm Nội dung & Chuyên môn (Tối đa 4.0) *</label>
      <input type="number" step="0.1" min="0.0" max="4.0" id="scoreContent" name="scoreContent" value="<c:out value='${evaluationForm.scoreContent}'/>" required ${isLocked ? 'readonly' : ''} oninput="calculateSum()" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px;" />
    </div>

    <div class="form-group" style="margin-bottom: 16px;">
      <label for="scorePresentation" style="display: block; font-weight: 600; margin-bottom: 6px;">2. Điểm Báo cáo & Trình bày (Tối đa 3.0) *</label>
      <input type="number" step="0.1" min="0.0" max="3.0" id="scorePresentation" name="scorePresentation" value="<c:out value='${evaluationForm.scorePresentation}'/>" required ${isLocked ? 'readonly' : ''} oninput="calculateSum()" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px;" />
    </div>

    <div class="form-group" style="margin-bottom: 16px;">
      <label for="scoreQa" style="display: block; font-weight: 600; margin-bottom: 6px;">3. Điểm Trả lời câu hỏi & Phản biện (Tối đa 3.0) *</label>
      <input type="number" step="0.1" min="0.0" max="3.0" id="scoreQa" name="scoreQa" value="<c:out value='${evaluationForm.scoreQa}'/>" required ${isLocked ? 'readonly' : ''} oninput="calculateSum()" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px;" />
    </div>

    <div style="background: #f1f5f9; padding: 12px 16px; border-radius: 6px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
      <span style="font-weight: 600; font-size: 1.05rem;">Tổng điểm đánh giá:</span>
      <span id="totalDisplay" style="font-size: 1.35rem; font-weight: 700; color: #1e3a8a;">
        <c:out value="${currentEval != null ? currentEval.totalScore : '0.00'}" /> / 10.0
      </span>
    </div>

    <c:if test="${!assignment.legacyAssessment && topic.round.type == 'TLCN'}"><p>GV hỗ trợ nộp điểm trước; GVPB chỉ chấm sau khi GV hỗ trợ đã nộp chính thức.</p></c:if>
    <c:if test="${!assignment.legacyAssessment && topic.round.type == 'KLTN' && assignment.reviewer.email == pageContext.request.userPrincipal.name}">
    <div class="form-group">
      <label for="defenseAllowed">Quyết định cho phép bảo vệ (bắt buộc khi nộp điểm)</label>
      <select id="defenseAllowed" name="defenseAllowed" ${isLocked ? 'disabled' : ''}>
        <option value="" ${evaluationForm.defenseAllowed == null ? 'selected' : ''}>Chưa quyết định</option>
        <option value="true" ${evaluationForm.defenseAllowed != null && evaluationForm.defenseAllowed ? 'selected' : ''}>Được bảo vệ trước hội đồng</option>
        <option value="false" ${evaluationForm.defenseAllowed != null && !evaluationForm.defenseAllowed ? 'selected' : ''}>Không được bảo vệ — điểm cuối cùng bằng 0</option>
      </select>
      <p>Nếu không cho phép bảo vệ, cần ghi rõ lý do trong nhận xét. Khoa xác nhận trước khi công bố.</p>
    </div>
    </c:if>
    <c:if test="${topic.round.type == 'NCKH'}"><p>Nhận xét nghiệm thu cần nêu kết quả so với mục tiêu, phương pháp, tính mới và khả năng ứng dụng.</p></c:if>
    <div class="form-group" style="margin-bottom: 24px;">
      <label for="comments" style="display: block; font-weight: 600; margin-bottom: 6px;">Nhận xét & Đánh giá chi tiết</label>
      <textarea id="comments" name="comments" rows="6" maxlength="2000" ${isLocked ? 'readonly' : ''} placeholder="Nhập nhận xét về ưu điểm, khuyết điểm, tính thực tiễn..." style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-family: inherit;"><c:out value='${evaluationForm.comments}'/></textarea>
    </div>

    <c:if test="${!isLocked}">
      <div style="display: flex; gap: 12px;">
        <button type="submit" class="button" onclick="document.getElementById('formAction').value='draft'">
          💾 Lưu nháp
        </button>
        <button type="submit" class="button primary" onclick="document.getElementById('formAction').value='submit'; return confirm('Bạn có chắc chắn muốn XÁC NHẬN NỘP điểm? Sau khi nộp, phiếu điểm sẽ bị KHÓA không thể sửa đổi.');">
          ✓ Xác nhận nộp điểm
        </button>
        <a class="button" href="<c:url value='/lecturer/evaluations'/>">Quay lại</a>
      </div>
    </c:if>

    <c:if test="${isLocked}">
      <div style="color: #15803d; font-weight: 600; display: flex; align-items: center; gap: 8px;">
        <span>🔒 Điểm đã được xác nhận vào lúc: <c:out value="${currentEval.submittedAtText}" /></span>
      </div>
    </c:if>
  </form>
</div>

<script>
function calculateSum() {
  const c = parseFloat(document.getElementById('scoreContent').value) || 0;
  const p = parseFloat(document.getElementById('scorePresentation').value) || 0;
  const q = parseFloat(document.getElementById('scoreQa').value) || 0;
  const total = (c + p + q).toFixed(2);
  document.getElementById('totalDisplay').textContent = total + ' / 10.0';
}
</script>

<%@ include file="../common/footer.jsp" %>
