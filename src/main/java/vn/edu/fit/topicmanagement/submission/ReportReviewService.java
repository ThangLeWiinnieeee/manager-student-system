
package vn.edu.fit.topicmanagement.submission;


import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationRepository;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus;

import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
public class ReportReviewService {

    private final ReportSubmissionRepository reports;
    private final GroupTopicRegistrationRepository registrations;
    private final UserAccountService users;
    private final ReportStorage storage;
    private final vn.edu.fit.topicmanagement.council.TopicAssignmentRepository assignments;

    public ReportReviewService(
            ReportSubmissionRepository reports,
            GroupTopicRegistrationRepository registrations,
            UserAccountService users,
            ReportStorage storage,
            vn.edu.fit.topicmanagement.council.TopicAssignmentRepository assignments) {

        this.reports = reports;
        this.registrations = registrations;
        this.users = users;
        this.assignments = assignments;

        this.storage = storage;
    }

    // =========================================
    // TAI KHOAN DANG DANG NHAP
    // =========================================

    private UserAccount currentUser() {

        Authentication auth =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException(
                    "Bạn chưa đăng nhập");
        }

        return users.getByEmail(auth.getName());
    }

    // =========================================
    // KIEM TRA ROLE DEAN
    // =========================================

    private boolean isDean() {

        Authentication auth =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        return auth != null &&
                auth.getAuthorities().stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_DEAN") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    // =========================================
    // KIEM TRA QUYEN XEM DANG KY
    // =========================================

    private boolean canView(
            GroupTopicRegistration registration,
            UserAccount user) {

        if (registration.getStatus()
                != GroupTopicRegistrationStatus.APPROVED) {
            return false;
        }

        // Truong khoa duoc xem nhom da duoc duyet
        if (isDean()) {
            return true;
        }

        var lecturer = registration.getTopic().getRound().getLecturer();
        if (lecturer != null && lecturer.getId().equals(user.getId())) return true;

        // Giang vien chi xem de tai minh huong dan
        var assignment = assignments.findByRegistration_Id(registration.getId()).orElse(null);
        if (assignment != null && ((assignment.getReviewer() != null && assignment.getReviewer().getId().equals(user.getId()))
                || (assignment.getCouncil() != null && assignment.getCouncil().getMembers().stream()
                .anyMatch(m -> m.getLecturer().getId().equals(user.getId()))))) return true;
        return registration.getTopic()
                .getAdvisors()
                .stream()
                .anyMatch(advisor ->
                        advisor.getId().equals(user.getId()));
    }

    // =========================================
    // KIEM TRA QUYEN TRUY CAP MOT NHOM
    // =========================================

    private GroupTopicRegistration requireAccess(
            Long groupId) {

        GroupTopicRegistration registration =
                registrations.findByGroup_Id(groupId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy đăng ký của nhóm"));

        UserAccount user = currentUser();

        if (!canView(registration, user)) {
            throw new AccessDeniedException(
                    "Bạn không có quyền xem báo cáo nhóm này");
        }

        return registration;
    }

    // =========================================
    // DANH SACH NHOM DUOC PHEP XEM
    // =========================================

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listVisibleGroups() {

        UserAccount user = currentUser();

        return registrations
                .findAllByOrderBySubmittedAtDesc()
                .stream()
                .filter(registration ->
                        canView(registration, user))
                .map(registration -> {

                    Long groupId =
                            registration.getGroup().getId();

                    return Map.<String, Object>of(
                            "groupId", groupId,
                            "groupName",
                            registration.getGroup().getName(),
                            "topicTitle",
                            registration.getTopic().getTitle(),
                            "roundId",
                            registration.getGroup().getRoundId(),
                            "reportCount",
                            reports.countByGroup_Id(groupId)
                    );
                })
                .toList();
    }

    // =========================================
    // THONG TIN NHOM
    // =========================================

    @Transactional(readOnly = true)
    public Map<String, Object> getGroupInfo(Long groupId) {

        GroupTopicRegistration registration =
                requireAccess(groupId);

        return Map.of(
                "groupId", registration.getGroup().getId(),
                "groupName", registration.getGroup().getName(),
                "topicTitle", registration.getTopic().getTitle(),
                "roundId", registration.getGroup().getRoundId()
        );
    }

    // =========================================
    // LICH SU BAO CAO CUA NHOM
    // =========================================

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getGroupReports(
            Long groupId) {

        requireAccess(groupId);

        return reports
                .findByGroup_IdOrderByUploadedAtDesc(groupId)
                .stream()
                .map(report -> Map.<String, Object>of(
                        "reportId", report.getId(),
                        "originalFilename",
                        report.getOriginalFilename(),
                        "uploadedByEmail",
                        report.getUploadedBy().getEmail(),
                        "sizeBytes", report.getSizeBytes(),
                        "uploadedAtText",
                        report.getUploadedAtText()
                ))
                .toList();
    }

    // =========================================
    // DOI TUONG TRA VE KHI TAI FILE
    // =========================================

    public record DownloadReport(
            Resource resource,
            String filename,
            long sizeBytes) {
    }

    // =========================================
    // DEAN / LECTURER TAI BAO CAO
    // =========================================

    @Transactional(readOnly = true)
    public DownloadReport download(
            Long groupId,
            Long reportId) {

        // Kiem tra quyen TRUOC khi lay file
        requireAccess(groupId);

        ReportSubmission report =
                reports.findByIdAndGroup_Id(
                        reportId,
                        groupId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy báo cáo"));

        return new DownloadReport(
                storage.read(report.getStoredFilename()),
                report.getOriginalFilename(),
                report.getSizeBytes()
        );
    }
}
