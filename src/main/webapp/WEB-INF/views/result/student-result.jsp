<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Kết quả Đề tài của tôi" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <h1>Kết quả Đề tài & Khóa luận</h1>
    <p>Xem điểm đánh giá, xếp loại và kết luận chính thức từ Hội đồng bảo vệ sau khi được Khoa công bố.</p>
  </div>
</div>

<c:choose>
  <c:when test="${view != null && view.result != null}">
    <div class="card" style="background: #fff; padding: 28px; border-radius: 8px; border: 1px solid var(--border-color, #e2e8f0); max-width: 750px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">
      <div style="border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; margin-bottom: 20px;">
        <span class="badge active" style="margin-bottom: 8px;">KẾT QUẢ CHÍNH THỨC</span>
        <h2 style="margin: 6px 0; font-size: 1.35rem; color: #1e293b;">
          <c:out value="${view.topic.title}" />
        </h2>
        <p style="margin: 0; color: #64748b; font-size: 0.9rem;">
          Nhóm thực hiện: <strong><c:out value="${view.group.name}" /></strong>
          · Đợt: <strong><c:out value="${view.topic.round.name}" /></strong>
          · Ngày công bố: <strong><c:out value="${view.result.publishedAtText}" /></strong>
        </p>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px;">
        <div style="background: #eff6ff; padding: 20px; border-radius: 8px; border: 1px solid #bfdbfe; text-align: center;">
          <div style="color: #1e40af; font-size: 0.9rem; font-weight: 600; margin-bottom: 4px;">ĐIỂM TỔNG KẾT</div>
          <div style="font-size: 2.5rem; font-weight: 800; color: #1d4ed8;">
            <c:out value="${view.result.finalScore}" />
          </div>
          <div style="color: #64748b; font-size: 0.8rem;">Thang điểm 10.0</div>
        </div>

        <div style="background: #f0fdf4; padding: 20px; border-radius: 8px; border: 1px solid #bbf7d0; text-align: center;">
          <div style="color: #166534; font-size: 0.9rem; font-weight: 600; margin-bottom: 8px;">XẾP LOẠI</div>
          <div style="margin-top: 6px;">
            <span class="badge ${view.result.grade == 'XUẤT SẮC' || view.result.grade == 'GIỎI' ? 'active' : (view.result.grade == 'KHÁ' ? 'info' : 'warn')}" style="font-size: 1.15rem; padding: 6px 16px;">
              <c:out value="${view.result.grade}" />
            </span>
          </div>
        </div>
      </div>

      <div style="background: #f8fafc; padding: 16px 20px; border-radius: 6px; border: 1px solid #e2e8f0; margin-bottom: 20px;">
        <h4 style="margin: 0 0 6px 0; color: #334155; font-size: 0.95rem;">Kết luận của Hội đồng đánh giá:</h4>
        <p style="margin: 0; color: #1e293b; font-style: italic; line-height: 1.6;">
          "<c:out value="${view.result.conclusion}" />"
        </p>
      </div>

      <div style="text-align: right; color: #64748b; font-size: 0.85rem;">
        Hội đồng chấm: <strong><c:out value="${view.result.council.name}" /></strong>
      </div>
    </div>
  </c:when>

  <c:when test="${view != null}">
    <div class="empty" style="padding: 48px 24px; text-align: center; background: #fff; border-radius: 8px; border: 1px dashed #cbd5e1; max-width: 700px;">
      <h3 style="color: #475569; margin-top: 0;">Kết quả chưa được công bố</h3>
      <p style="color: #64748b; line-height: 1.6; margin-bottom: 0;">
        Đề tài <strong>"<c:out value="${view.topic.title}" />"</strong> của nhóm bạn đang trong quá trình chấm điểm hoặc đang chờ Khoa duyệt công bố chính thức. Vui lòng quay lại sau!
      </p>
    </div>
  </c:when>

  <c:otherwise>
    <div class="empty" style="padding: 48px 24px; text-align: center; background: #fff; border-radius: 8px; border: 1px dashed #cbd5e1; max-width: 700px;">
      <h3 style="color: #475569; margin-top: 0;">Bạn chưa có kết quả đề tài</h3>
      <p style="color: #64748b; line-height: 1.6; margin-bottom: 0;">
        Bạn chưa tham gia nhóm đề tài nào đã được duyệt hoặc đợt bảo vệ chưa diễn ra.
      </p>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="../common/footer.jsp" %>
