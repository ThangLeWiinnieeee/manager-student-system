<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Chi tiết Hội đồng: ${council.name}" />
<c:set var="isAdminOrDean" value="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}" />
<%@ include file="../common/header.jsp" %>

<div class="page-heading">
  <div>
    <c:choose>
      <c:when test="${isAdminOrDean}">
        <a href="<c:url value='/admin/councils'/>" style="text-decoration: none; color: #64748b; font-size: 0.9rem;">← Quay lại danh sách hội đồng</a>
      </c:when>
      <c:otherwise>
        <a href="<c:url value='/lecturer/councils'/>" style="text-decoration: none; color: #64748b; font-size: 0.9rem;">← Quay lại hội đồng của tôi</a>
      </c:otherwise>
    </c:choose>
    <h1 style="margin-top: 8px;"><c:out value="${council.name}" /></h1>
    <p>
      Môn/lớp: <strong><c:out value="${council.round.name}" /></strong>
      · Địa điểm: <strong><c:out value="${empty council.location ? 'Chưa xếp phòng' : council.location}" /></strong>
      · Thời gian: <strong><c:out value="${council.reportDateText}" /></strong>
      · Trạng thái:
      <span class="badge ${council.status == 'ACTIVE' ? 'active' : (council.status == 'COMPLETED' ? 'info' : 'inactive')}">
        <c:out value="${council.status.displayName}" />
      </span>
    </p>
  </div>
  <c:if test="${isAdminOrDean}">
    <div style="display: flex; gap: 8px;">
      <a class="button" href="<c:url value='/admin/councils/${council.id}/edit'/>">Chỉnh sửa hội đồng</a>
      <a class="button primary" href="<c:url value='/admin/councils/assignments?roundId=${council.round.id}'/>">Phân công đề tài</a>
    </div>
  </c:if>
</div>

<style>
  .council-box {
    margin-bottom: 24px;
    padding: 16px 20px;
    border-radius: 8px;
  }
  .council-box-ok {
    background: #f0fdf4;
    border: 1px solid #bbf7d0;
  }
  .council-box-warn {
    background: #fffbeb;
    border: 1px solid #fef08a;
  }
  .council-title-ok {
    margin: 0 0 4px 0;
    color: #166534;
    font-size: 1rem;
  }
  .council-title-warn {
    margin: 0 0 4px 0;
    color: #854d0e;
    font-size: 1rem;
  }
  .council-desc-ok {
    margin: 0;
    color: #15803d;
    font-size: 0.875rem;
  }
  .council-desc-warn {
    margin: 0;
    color: #a16207;
    font-size: 0.875rem;
  }
  .grid-admin {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 24px;
    margin-bottom: 32px;
  }
  .grid-standard {
    display: grid;
    grid-template-columns: 1fr;
    gap: 24px;
    margin-bottom: 32px;
  }
</style>

<!-- Khối cảnh báo quy chuẩn hội đồng -->
<div class="card council-box ${council.eligible ? 'council-box-ok' : 'council-box-warn'}">
  <div style="display: flex; justify-content: space-between; align-items: center;">
    <div>
      <h3 class="${council.eligible ? 'council-title-ok' : 'council-title-warn'}">
        <c:choose>
          <c:when test="${council.eligible}">
            ✓ Hội đồng đạt chuẩn (3 - 5 GV, đúng 1 Chủ tịch, đúng 1 Thư ký)
          </c:when>
          <c:otherwise>
            ⚠️ Hội đồng chưa hoàn thiện yêu cầu chuẩn
          </c:otherwise>
        </c:choose>
      </h3>
      <p class="${council.eligible ? 'council-desc-ok' : 'council-desc-warn'}">
        Quy định: Mỗi hội đồng có từ 3 đến 5 giảng viên, trong đó có đúng 1 Chủ tịch, đúng 1 Thư ký và các Ủy viên.
      </p>
    </div>
    <span class="badge ${council.eligible ? 'active' : 'warn'}">
      Hiện có: <c:out value="${fn:length(members)}" /> / 5 GV
    </span>
  </div>
</div>

<!-- Danh sách thành viên hội đồng -->
<div class="${isAdminOrDean ? 'grid-admin' : 'grid-standard'}">
  <div>
    <h2>Danh sách thành viên hội đồng</h2>
    <div class="table-wrap" role="region" aria-label="Thành viên hội đồng" tabindex="0">
      <table>
        <thead>
          <tr>
            <th>Họ và tên giảng viên</th>
            <th>Email</th>
            <th>Khoa</th>
            <th>Vai trò</th>
            <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
              <th>Thao tác</th>
            </c:if>
          </tr>
        </thead>
        <tbody>
          <c:forEach items="${members}" var="m">
            <tr>
              <td><strong><c:out value="${m.lecturer.fullName}" /></strong></td>
              <td><c:out value="${m.lecturer.email}" /></td>
              <td>Công nghệ thông tin</td>
              <td>
                <span class="badge ${m.role == 'PRESIDENT' ? 'active' : (m.role == 'SECRETARY' ? 'info' : 'warn')}">
                  <c:out value="${m.role.displayName}" />
                </span>
              </td>
              <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
                <td class="actions">
                  <form action="<c:url value='/admin/councils/${council.id}/members/${m.lecturer.id}/remove'/>" method="post" style="display: inline;" onsubmit="return confirm('Bạn có chắc muốn xóa thành viên này khỏi hội đồng?');">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <button type="submit" class="button" style="color: #dc2626;">Xóa</button>
                  </form>
                </td>
              </c:if>
            </tr>
          </c:forEach>
          <c:if test="${empty members}">
            <tr>
              <td colspan="${isAdminOrDean ? 5 : 4}" class="empty">
                Chưa có thành viên nào trong hội đồng.
              </td>
            </tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>

  <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">
    <div class="card" style="background: #fff; padding: 20px; border-radius: 8px; border: 1px solid var(--border-color, #e2e8f0); height: fit-content;">
      <h3 style="margin-top: 0;">Thêm / Gán thành viên</h3>
      <form action="<c:url value='/admin/councils/${council.id}/members'/>" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

        <div class="form-group" style="margin-bottom: 12px;">
          <label for="lecturerId" style="display: block; font-weight: 600; margin-bottom: 4px;">Chọn Giảng viên *</label>
          <select id="lecturerId" name="lecturerId" required style="width: 100%; padding: 8px 10px; border: 1px solid #cbd5e1; border-radius: 6px;">
            <option value="">-- Chọn giảng viên --</option>
            <c:forEach items="${lecturers}" var="lec">
              <option value="${lec.id}">
                <c:out value="${lec.fullName}" /> (<c:out value="${lec.email}" />)
              </option>
            </c:forEach>
          </select>
        </div>

        <div class="form-group" style="margin-bottom: 16px;">
          <label for="role" style="display: block; font-weight: 600; margin-bottom: 4px;">Vai trò *</label>
          <select id="role" name="role" required style="width: 100%; padding: 8px 10px; border: 1px solid #cbd5e1; border-radius: 6px;">
            <c:forEach items="${roles}" var="r">
              <option value="${r.name()}"><c:out value="${r.displayName}" /></option>
            </c:forEach>
          </select>
        </div>

        <button type="submit" class="button primary" style="width: 100%;">Thêm vào hội đồng</button>
      </form>
    </div>
  </c:if>
</div>

<!-- Danh sách đề tài được phân công vào hội đồng này -->
<div>
  <h2>Danh sách đề tài được phân công báo cáo</h2>
  <div class="table-wrap" role="region" aria-label="Đề tài phân công" tabindex="0">
    <table>
      <thead>
        <tr>
          <th>Tên đề tài</th>
          <th>Bộ môn</th>
          <th>GV Hướng dẫn</th>
          <th>GV Phản biện (GVPB)</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${assignedTopics}" var="ta">
          <tr>
            <td>
              <a href="<c:url value='/topics/${ta.topic.id}'/>" style="font-weight: 600; color: #1d4ed8; text-decoration: none;">
                <c:out value="${ta.registration.group.name}"/> — <c:out value="${ta.topic.title}" />
              </a>
            </td>
            <td><c:out value="${ta.topic.department.name}" default="Không giới hạn" /></td>
            <td>
              <c:forEach items="${ta.topic.advisors}" var="adv" varStatus="vs">
                <c:out value="${adv.fullName}" /><c:if test="${not vs.last}">, </c:if>
              </c:forEach>
            </td>
            <td>
              <c:choose>
                <c:when test="${not empty ta.reviewer}">
                  <strong><c:out value="${ta.reviewer.fullName}" /></strong>
                </c:when>
                <c:otherwise>
                  <span style="color: #94a3b8;">Chưa gán GVPB</span>
                </c:otherwise>
              </c:choose>
            </td>
            <td class="actions">
              <c:if test="${pageContext.request.isUserInRole('LECTURER')}">
                <a class="button primary" href="<c:url value='/lecturer/evaluations/registration/${ta.registration.id}'/>">Chấm điểm</a>
              </c:if>
              <a class="button" href="<c:url value='/council/results/registration/${ta.registration.id}'/>">Xem điểm & Kết quả</a>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty assignedTopics}">
          <tr>
            <td colspan="5" class="empty">
              Chưa có đề tài nào được phân công vào hội đồng này.
            </td>
          </tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>

<%@ include file="../common/footer.jsp" %>
