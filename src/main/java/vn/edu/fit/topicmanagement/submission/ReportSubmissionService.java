
package vn.edu.fit.topicmanagement.submission;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationService;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus;
import vn.edu.fit.topicmanagement.studentgroup.StudentGroup;
import vn.edu.fit.topicmanagement.studentgroup.StudentGroupService;

import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
@PreAuthorize("hasRole('STUDENT')")
public class ReportSubmissionService {

    private static final long MAX_FILE_SIZE =
            10L * 1024 * 1024;

    private static final byte[] PDF_SIGNATURE =
            "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final ReportSubmissionRepository reports;
    private final StudentGroupService groups;
    private final GroupTopicRegistrationService registrations;
    private final UserAccountService users;

    private final Path storageRoot;

    public ReportSubmissionService(
            ReportSubmissionRepository reports,
            StudentGroupService groups,
            GroupTopicRegistrationService registrations,
            UserAccountService users,
            @Value("${app.submission.storage-dir}")
            String storageDir) {

        this.reports = reports;
        this.groups = groups;
        this.registrations = registrations;
        this.users = users;

        this.storageRoot =
                Path.of(storageDir)
                        .toAbsolutePath()
                        .normalize();
    }

    // =========================================
    // LAY SINH VIEN DANG DANG NHAP
    // =========================================

    private UserAccount currentStudent() {

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
    // KIEM TRA NHOM DA DUOC DUYET DE TAI
    // =========================================

    private void assertApproved(Long groupId) {

        GroupTopicRegistration registration =
                registrations.findMyRegistration(groupId);

        if (registration == null ||
                registration.getStatus() !=
                        GroupTopicRegistrationStatus.APPROVED) {

            throw new BusinessRuleException(
                    "Nhóm phải được chấp thuận đề tài " +
                            "trước khi nộp báo cáo");
        }
    }

    // =========================================
    // KIEM TRA TEP PDF
    // =========================================

    private String validatePdf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException(
                    "Vui lòng chọn tệp báo cáo");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessRuleException(
                    "Dung lượng báo cáo không được vượt quá 10 MB");
        }

        String original = file.getOriginalFilename();

        if (original == null || original.isBlank()) {
            throw new BusinessRuleException(
                    "Tên tệp không hợp lệ");
        }

        // Chi giu ten tep, bo duong dan do client gui
        original = original.replace('\\', '/');

        original = original.substring(
                original.lastIndexOf('/') + 1
        ).trim();

        if (original.isBlank() ||
                original.length() > 200 ||
                original.chars().anyMatch(
                        c -> Character.isISOControl((char) c))) {

            throw new BusinessRuleException(
                    "Tên tệp báo cáo không hợp lệ");
        }

        if (!original.toLowerCase(Locale.ROOT)
                .endsWith(".pdf")) {

            throw new BusinessRuleException(
                    "Chỉ chấp nhận tệp PDF");
        }

        // Kiem tra chu ky PDF co ban
        try (InputStream input = file.getInputStream()) {

            byte[] header = input.readNBytes(5);

            if (!Arrays.equals(header, PDF_SIGNATURE)) {
                throw new BusinessRuleException(
                        "Nội dung tệp không đúng định dạng PDF");
            }

        } catch (IOException ex) {
            throw new BusinessRuleException(
                    "Không thể đọc tệp báo cáo");
        }

        return original;
    }

    // =========================================
    // TAO DUONG DAN AN TOAN
    // =========================================

    private Path resolveFile(String storedFilename) {

        Path target =
                storageRoot.resolve(storedFilename).normalize();

        if (!target.startsWith(storageRoot)) {
            throw new BusinessRuleException(
                    "Đường dẫn tệp không hợp lệ");
        }

        return target;
    }

    // =========================================
    // NHOM TRUONG NOP BAO CAO
    // =========================================

    @Transactional
    public ReportSubmission upload(
            Long groupId,
            MultipartFile file) {

        StudentGroup group = groups.getMyGroup(groupId);

        // Kiem tra quyen nhom truong tai backend
        if (!groups.isMyGroupLeader(groupId)) {
            throw new AccessDeniedException(
                    "Chỉ nhóm trưởng được nộp báo cáo");
        }

        // Phai duoc chap thuan de tai
        assertApproved(groupId);

        String originalFilename = validatePdf(file);

        UserAccount uploader = currentStudent();

        String storedFilename =
                UUID.randomUUID() + ".pdf";

        Path target = resolveFile(storedFilename);

        try {

            Files.createDirectories(storageRoot);

            try (InputStream input =
                         file.getInputStream()) {

                Files.copy(input, target);
            }

        } catch (IOException ex) {

            throw new BusinessRuleException(
                    "Không thể lưu tệp báo cáo");
        }

        // Xoa tep neu transaction database rollback
        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCompletion(int status) {

                                if (status != STATUS_COMMITTED) {

                                    try {
                                        Files.deleteIfExists(target);
                                    } catch (IOException ignored) {
                                        // Tep se can duoc don dep thu cong
                                    }
                                }
                            }
                        });

        ReportSubmission report = new ReportSubmission();

        report.setGroup(group);
        report.setUploadedBy(uploader);
        report.setOriginalFilename(originalFilename);
        report.setStoredFilename(storedFilename);
        report.setSizeBytes(file.getSize());
        report.setContentType("application/pdf");

        return reports.save(report);
    }

    // =========================================
    // LICH SU NOP BAO CAO
    // =========================================

    @Transactional(readOnly = true)
    public List<ReportSubmission> findMyGroupReports(
            Long groupId) {

        // Tu choi sinh vien khong thuoc nhom
        groups.getMyGroup(groupId);

        return reports
                .findByGroup_IdOrderByUploadedAtDesc(groupId);
    }

    // =========================================
    // DOI TUONG TRA VE KHI TAI BAO CAO
    // =========================================

    public record DownloadReport(
            Resource resource,
            String filename,
            long sizeBytes) {
    }

    // =========================================
    // TAI TEP BAO CAO
    // =========================================

    @Transactional(readOnly = true)
    public DownloadReport download(
            Long groupId,
            Long reportId) {

        // Chi thanh vien cua nhom moi duoc tai
        groups.getMyGroup(groupId);

        ReportSubmission report =
                reports.findByIdAndGroup_Id(
                        reportId,
                        groupId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy báo cáo"));

        Path target =
                resolveFile(report.getStoredFilename());

        if (!Files.isRegularFile(
                target, LinkOption.NOFOLLOW_LINKS)) {

            throw new ResourceNotFoundException(
                    "Tệp báo cáo không còn tồn tại");
        }

        return new DownloadReport(
                new FileSystemResource(target),
                report.getOriginalFilename(),
                report.getSizeBytes()
        );
    }
}
