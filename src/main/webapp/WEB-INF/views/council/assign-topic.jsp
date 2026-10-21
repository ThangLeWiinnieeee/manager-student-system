<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <c:set var="pageTitle" value="Phân công Hội đồng & Giảng viên phản biện" />
    <%@ include file="../common/header.jsp" %>

      <div class="page-heading">
        <div>
          <a href="<c:url value='/admin/councils'/>" style="text-decoration: none; color: #64748b; font-size: 0.9rem;">←
            Quay lại danh sách hội đồng</a>
          <h1 style="margin-top: 8px;">Phân công Hội đồng & Giảng viên phản biện</h1>
          <p>Chỉ định Giảng viên phản biện (GVPB) và xếp đề tài vào Hội đồng bảo vệ phù hợp.</p>
        </div>
      </div>

      <form method="get" action="<c:url value='/admin/councils/assignments'/>"
        style="margin-bottom: 20px; display: flex; gap: 12px; align-items: center;">
        <label for="roundId" style="font-weight: 600;">Chọn đợt đăng ký:</label>
        <select id="roundId" name="roundId" onchange="this.form.submit()"
          style="padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1;">
          <c:forEach items="${periods}" var="p">
            <option value="${p.id}" ${selectedRoundId==p.id ? 'selected' : '' }>
              <c:out value="${p.name}" /> (
              <c:out value="${p.type.displayName}" />)
            </option>
          </c:forEach>
        </select>
      </form>

      <div class="table-wrap" role="region" aria-label="Phân công đề tài" tabindex="0">
        <table>
          <thead>
            <tr>
              <th style="width: 25%;">Đề tài & Nhóm SV</th>
              <th style="width: 15%;">GV Hướng dẫn</th>
              <th style="width: 25%;">Hội đồng bảo vệ</th>
              <th style="width: 20%;">Giảng viên phản biện</th>
              <th style="width: 15%;">Lưu phân công</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${registrations}" var="reg">
              <c:set var="topic" value="${reg.topic}" />
              <c:set var="asgn" value="${assignmentsMap[topic.id]}" />
              <tr>
                <td>
                  <a href="<c:url value='/topics/${topic.id}'/>"
                    style="font-weight: 600; color: #1d4ed8; text-decoration: none;">
                    <c:out value="${topic.title}" />
                  </a>
                  <div style="font-size: 0.85rem; color: #64748b; margin-top: 4px;">
                    Nhóm: <strong>
                      <c:out value="${reg.group.name}" />
                    </strong>
                  </div>
                </td>
                <td>
                  <c:forEach items="${topic.advisors}" var="adv" varStatus="vs">
                    <div>
                      <c:out value="${adv.fullName}" />
                    </div>
                  </c:forEach>
                </td>
                <td>
                  <select form="assign-form-${topic.id}" name="councilId"
                    style="width: 100%; padding: 6px 8px; border-radius: 6px; border: 1px solid #cbd5e1;">
                    <option value="">-- Chưa xếp hội đồng --</option>
                    <c:forEach items="${councils}" var="c">
                      <option value="${c.id}" ${asgn !=null && asgn.council !=null && asgn.council.id==c.id ? 'selected'
                        : '' }>
                        <c:out value="${c.name}" />
                      </option>
                    </c:forEach>
                  </select>
                </td>
                <td>
                  <select form="assign-form-${topic.id}" name="reviewerId"
                    style="width: 100%; padding: 6px 8px; border-radius: 6px; border: 1px solid #cbd5e1;">
                    <option value="">-- Chưa gán GVPB --</option>
                    <c:forEach items="${lecturers}" var="lec">
                      <%-- Chặn chọn GVHD làm phản biện ngay trên UI --%>
                        <c:set var="isAdv" value="false" />
                        <c:forEach items="${topic.advisors}" var="adv">
                          <c:if test="${adv.id == lec.id}">
                            <c:set var="isAdv" value="true" />
                          </c:if>
                        </c:forEach>
                        <c:if test="${!isAdv}">
                          <option value="${lec.id}" ${asgn !=null && asgn.reviewer !=null && asgn.reviewer.id==lec.id
                            ? 'selected' : '' }>
                            <c:out value="${lec.fullName}" />
                          </option>
                        </c:if>
                    </c:forEach>
                  </select>
                </td>
                <td>
                  <form id="assign-form-${topic.id}" action="<c:url value='/admin/councils/assignments'/>" method="post"
                    style="margin: 0;">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <input type="hidden" name="topicId" value="${topic.id}" />
                    <input type="hidden" name="roundId" value="${selectedRoundId}" />
                    <button type="submit" class="button primary" style="width: 100%;">Lưu</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty registrations}">
              <tr>
                <td colspan="5" class="empty">
                  Không có đề tài nào đã được duyệt đăng ký nhóm trong đợt này.
                </td>
              </tr>
            </c:if>
          </tbody>
        </table>
      </div>

      <%@ include file="../common/footer.jsp" %>