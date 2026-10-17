
package vn.edu.fit.topicmanagement.submission;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationService;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus;
import vn.edu.fit.topicmanagement.studentgroup.StudentGroup;
import vn.edu.fit.topicmanagement.studentgroup.StudentGroupService;

@Controller
@RequestMapping("/student/my-groups/{groupId}/submissions")
@PreAuthorize("hasRole('STUDENT')")
public class StudentSubmissionController {

    private final ReportSubmissionService reports;
    private final StudentGroupService groups;
    private final GroupTopicRegistrationService registrations;

    public StudentSubmissionController(
            ReportSubmissionService reports,
            StudentGroupService groups,
            GroupTopicRegistrationService registrations) {

        this.reports = reports;
        this.groups = groups;
        this.registrations = registrations;
    }

    // =========================================
    // TRANG NOP VA XEM BAO CAO
    // =========================================

    @GetMapping
    public String index(
            @PathVariable Long groupId,
            Model model) {

        StudentGroup group = groups.getMyGroup(groupId);

        GroupTopicRegistration registration =
                registrations.findMyRegistration(groupId);

        boolean approved =
                registration != null &&
                        registration.getStatus() ==
                                GroupTopicRegistrationStatus.APPROVED;

        model.addAttribute("group", group);

        model.addAttribute(
                "isLeader",
                groups.isMyGroupLeader(groupId));

        model.addAttribute("approved", approved);

        model.addAttribute(
                "reports",
                reports.findMyGroupReports(groupId));

        return "submission/student-submissions";
    }

    // =========================================
    // NOP BAO CAO
    // =========================================

    @PostMapping
    public String upload(
            @PathVariable Long groupId,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {

            reports.upload(groupId, file);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Nộp báo cáo thành công");

        } catch (BusinessRuleException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage());
        }

        return "redirect:/student/my-groups/"
                + groupId + "/submissions";
    }

    // =========================================
    // TAI BAO CAO
    // =========================================

    @GetMapping("/{reportId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long groupId,
            @PathVariable Long reportId) {

        ReportSubmissionService.DownloadReport file =
                reports.download(groupId, reportId);

        String disposition =
                ContentDisposition.attachment()
                        .filename(
                                file.filename(),
                                StandardCharsets.UTF_8)
                        .build()
                        .toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition)
                .contentLength(file.sizeBytes())
                .body(file.resource());
    }
}
