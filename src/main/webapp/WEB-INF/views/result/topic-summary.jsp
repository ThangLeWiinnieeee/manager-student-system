<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
      <c:set var="pageTitle" value="Tổng hợp điểm & Kết quả: ${summary.topic.title}" />
      <%@ include file="../common/header.jsp" %>

        <div class="page-heading">
          <div>
            <a href="<c:url value='/admin/councils/${summary.council.id}'/>"
              style="text-decoration: none; color: #64748b; font-size: 0.9rem;">← Quay lại hội đồng
              ${summary.council.name}</a>
            <h1 style="margin-top: 8px;">Tổng hợp điểm & Kết quả bảo vệ</h1>
            <p>
              Đề tài: <strong>
                <c:out value="${summary.topic.title}" />
              </strong>
              · Hội đồng: <strong>
                <c:out value="${summary.council.name}" />
              </strong>
            </p>
          </div>
        </div>

        <style>
          .eval-box {
            margin-bottom: 24px;
            padding: 16px 20px;
            border-radius: 8px;
          }

          .eval-box-ok {
            background: #f0fdf4;
            border: 1px solid #bbf7d0;
          }

          .eval-box-wait {
            background: #fffbeb;
            border: 1px solid #fef08a;
          }

          .eval-title-ok {
            margin: 0 0 4px 0;
            color: #166534;
            font-size: 1rem;
          }

          .eval-title-wait {
            margin: 0 0 4px 0;
            color: #854d0e;
            font-size: 1rem;
          }

          .eval-desc-ok {
            margin: 0;
            color: #15803d;
            font-size: 0.875rem;
          }

          .eval-desc-wait {
            margin: 0;
            color: #a16207;
            font-size: 0.875rem;
          }
        </style>

        <!-- Trạng thái hoàn thành đánh giá -->
        <div class="card eval-box ${summary.isFullyEvaluated ? 'eval-box-ok' : 'eval-box-wait'}">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <h3 class="${summary.isFullyEvaluated ? 'eval-title-ok' : 'eval-title-wait'}">
                <c:choose>
                  <c:when test="${summary.isFullyEvaluated}">
                    ✓ Đã thu thập đủ điểm từ toàn bộ Thành viên Hội đồng và Giảng viên phản biện
                  </c:when>
                  <c:otherwise>
                    ⏳ Đang chờ hoàn tất chấm điểm: Một số thành viên hoặc phản biện chưa nộp điểm
                  </c:otherwise>
                </c:choose>
              </h3>
              <p class="${summary.isFullyEvaluated ? 'eval-desc-ok' : 'eval-desc-wait'}">
                Quy định: Điểm chỉ được Chủ tịch hội đồng chốt và tính chính thức khi tất cả thành viên đã xác nhận nộp.
              </p>
            </div>
          </div>
        </div>

        <!-- Bảng điểm chi tiết phản biện và hội đồng -->
        <div style="margin-bottom: 24px;">
          <h2>1. Điểm Giảng viên phản biện (GVPB)</h2>
          <div class="table-wrap" role="region" aria-label="Điểm GVPB" tabindex="0">
            <table>
              <thead>
                <tr>
                  <th>Họ tên GVPB</th>
                  <th>Trạng thái</th>
                  <th>Nội dung (4.0)</th>
                  <th>Trình bày (3.0)</th>
                  <th>Phản biện (3.0)</th>
                  <th>Tổng điểm (10.0)</th>
                  <th>Nhận xét</th>
                </tr>
              </thead>
              <tbody>
                <c:choose>
                  <c:when test="${summary.reviewer != null}">
                    <tr>
                      <td><strong>
                          <c:out value="${summary.reviewer.fullName}" />
                        </strong></td>
                      <td>
                        <span
                          class="badge ${summary.reviewerEvaluation != null && summary.reviewerEvaluation.status == 'SUBMITTED' ? 'active' : 'warn'}">
                          <c:out
                            value="${summary.reviewerEvaluation != null ? summary.reviewerEvaluation.status.displayName : 'Chưa chấm'}" />
                        </span>
                      </td>
                      <td>
                        <c:out
                          value="${summary.reviewerEvaluation != null ? summary.reviewerEvaluation.scoreContent : '-'}" />
                      </td>
                      <td>
                        <c:out
                          value="${summary.reviewerEvaluation != null ? summary.reviewerEvaluation.scorePresentation : '-'}" />
                      </td>
                      <td>
                        <c:out
                          value="${summary.reviewerEvaluation != null ? summary.reviewerEvaluation.scoreQa : '-'}" />
                      </td>
                      <td><strong style="color: #1e3a8a;">
                          <c:out
                            value="${summary.reviewerEvaluation != null ? summary.reviewerEvaluation.totalScore : '-'}" />
                        </strong></td>
                      <td>
                        <c:out
                          value="${summary.reviewerEvaluation != null ? summary.reviewerEvaluation.comments : '-'}" />
                      </td>
                    </tr>
                  </c:when>
                  <c:otherwise>
                    <tr>
                      <td colspan="7" class="empty">Đề tài chưa được phân công Giảng viên phản biện.</td>
                    </tr>
                  </c:otherwise>
                </c:choose>
              </tbody>
            </table>
          </div>
        </div>

        <div style="margin-bottom: 32px;">
          <h2>2. Điểm các Thành viên Hội đồng</h2>
          <div class="table-wrap" role="region" aria-label="Điểm Hội đồng" tabindex="0">
            <table>
              <thead>
                <tr>
                  <th>Thành viên hội đồng</th>
                  <th>Vai trò</th>
                  <th>Trạng thái</th>
                  <th>Nội dung (4.0)</th>
                  <th>Trình bày (3.0)</th>
                  <th>Trả lời (3.0)</th>
                  <th>Tổng điểm (10.0)</th>
                  <th>Nhận xét</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach items="${summary.memberScores}" var="ms">
                  <tr>
                    <td><strong>
                        <c:out value="${ms.lecturer.fullName}" />
                      </strong></td>
                    <td>
                      <span
                        class="badge ${ms.role == 'PRESIDENT' ? 'active' : (ms.role == 'SECRETARY' ? 'info' : 'warn')}">
                        <c:out value="${ms.role.displayName}" />
                      </span>
                    </td>
                    <td>
                      <span
                        class="badge ${ms.evaluation != null && ms.evaluation.status == 'SUBMITTED' ? 'active' : 'warn'}">
                        <c:out value="${ms.evaluation != null ? ms.evaluation.status.displayName : 'Chưa chấm'}" />
                      </span>
                    </td>
                    <td>
                      <c:out value="${ms.evaluation != null ? ms.evaluation.scoreContent : '-'}" />
                    </td>
                    <td>
                      <c:out value="${ms.evaluation != null ? ms.evaluation.scorePresentation : '-'}" />
                    </td>
                    <td>
                      <c:out value="${ms.evaluation != null ? ms.evaluation.scoreQa : '-'}" />
                    </td>
                    <td><strong style="color: #1e3a8a;">
                        <c:out value="${ms.evaluation != null ? ms.evaluation.totalScore : '-'}" />
                      </strong></td>
                    <td>
                      <c:out value="${ms.evaluation != null ? ms.evaluation.comments : '-'}" />
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Bảng tổng hợp điểm và xếp loại -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 32px;">
          <div class="card"
            style="background: #fff; padding: 20px; border-radius: 8px; border: 1px solid var(--border-color, #e2e8f0);">
            <h3 style="margin-top: 0;">Tổng kết tính điểm</h3>
            <table style="width: 100%; border-collapse: collapse;">
              <tr style="border-bottom: 1px solid #f1f5f9;">
                <td style="padding: 10px 0; color: #64748b;">Điểm Giảng viên phản biện (30%):</td>
                <td style="padding: 10px 0; text-align: right; font-weight: 600;">
                  <c:out value="${summary.reviewerScore != null ? summary.reviewerScore : 'Chưa có'}" />
                </td>
              </tr>
              <tr style="border-bottom: 1px solid #f1f5f9;">
                <td style="padding: 10px 0; color: #64748b;">Điểm Trung bình Hội đồng (70%):</td>
                <td style="padding: 10px 0; text-align: right; font-weight: 600;">
                  <c:out value="${summary.councilAvgScore}" />
                </td>
              </tr>
              <tr style="border-bottom: 2px solid #e2e8f0;">
                <td style="padding: 14px 0; font-weight: 700; font-size: 1.1rem; color: #1e293b;">ĐIỂM TỔNG KẾT:</td>
                <td style="padding: 14px 0; text-align: right; font-weight: 800; font-size: 1.35rem; color: #2563eb;">
                  <c:out value="${summary.finalScore}" /> / 10.0
                </td>
              </tr>
              <tr>
                <td style="padding: 12px 0; font-weight: 600; color: #1e293b;">Xếp loại tốt nghiệp:</td>
                <td style="padding: 12px 0; text-align: right;">
                  <span
                    class="badge ${summary.grade == 'XUẤT SẮC' || summary.grade == 'GIỎI' ? 'active' : (summary.grade == 'KHÁ' ? 'info' : 'warn')}">
                    <c:out value="${summary.grade}" />
                  </span>
                </td>
              </tr>
            </table>
          </div>

          <div class="card"
            style="background: #fff; padding: 20px; border-radius: 8px; border: 1px solid var(--border-color, #e2e8f0);">
            <h3 style="margin-top: 0;">Xác nhận của Chủ tịch Hội đồng</h3>
            <c:choose>
              <c:when test="${summary.finalResult != null && summary.finalResult.presidentConfirmed}">
                <div
                  style="background: #f0fdf4; border: 1px solid #bbf7d0; padding: 16px; border-radius: 6px; margin-bottom: 16px;">
                  <p style="margin: 0 0 8px 0; color: #166534; font-weight: 600;">✓ Đã được Chủ tịch hội đồng xác nhận
                  </p>
                  <p style="margin: 0; font-size: 0.875rem; color: #15803d;">
                    Thời gian xác nhận:
                    <c:out value="${summary.finalResult.presidentConfirmedAtText}" />
                  </p>
                </div>
                <div>
                  <label style="font-weight: 600; display: block; margin-bottom: 4px;">Kết luận của Hội đồng:</label>
                  <div
                    style="padding: 10px; background: #f8fafc; border-radius: 6px; border: 1px solid #e2e8f0; font-style: italic;">
                    "
                    <c:out value="${summary.finalResult.conclusion}" />"
                  </div>
                </div>
              </c:when>
              <c:when test="${isPresident}">
                <form action="<c:url value='/council/results/topic/${summary.topic.id}/confirm'/>" method="post">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                  <div class="form-group" style="margin-bottom: 16px;">
                    <label for="conclusion" style="display: block; font-weight: 600; margin-bottom: 6px;">Ghi kết luận
                      của Hội đồng *</label>
                    <textarea id="conclusion" name="conclusion" rows="4" required maxlength="1000"
                      placeholder="Đề tài đáp ứng yêu cầu khóa luận, sinh viên bảo vệ thành công..."
                      style="width: 100%; padding: 8px; border: 1px solid #cbd5e1; border-radius: 6px;"></textarea>
                  </div>
                  <c:choose>
                    <c:when test="${!summary.isFullyEvaluated}">
                      <button type="submit" class="button primary" style="width: 100%;" disabled="disabled">
                        Xác nhận kết quả cuối cùng
                      </button>
                    </c:when>
                    <c:otherwise>
                      <button type="submit" class="button primary" style="width: 100%;"
                        onclick="return confirm('Chủ tịch xác nhận chốt kết quả của đề tài này?');">
                        Xác nhận kết quả cuối cùng
                      </button>
                    </c:otherwise>
                  </c:choose>
                  <c:if test="${!summary.isFullyEvaluated}">
                    <small style="color: #ef4444; display: block; margin-top: 6px; text-align: center;">Chưa thể xác
                      nhận vì chưa đủ điểm từ các thành viên.</small>
                  </c:if>
                </form>
              </c:when>
              <c:otherwise>
                <div class="empty" style="padding: 24px; text-align: center; color: #64748b;">
                  Đang chờ Chủ tịch hội đồng chốt và xác nhận kết quả sau khi đủ điểm.
                </div>
              </c:otherwise>
            </c:choose>
          </div>
        </div>

        <%@ include file="../common/footer.jsp" %>