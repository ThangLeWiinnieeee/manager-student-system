
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
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/review/submissions")
@PreAuthorize("hasAnyRole('DEAN', 'LECTURER')")
public class ReportReviewController {

    private final ReportReviewService reviews;

    public ReportReviewController(
            ReportReviewService reviews) {
        this.reviews = reviews;
    }

    // =========================================
    // DANH SACH NHOM CO DE TAI DA DUOC DUYET
    // =========================================

    @GetMapping
    public String index(Model model) {

        model.addAttribute(
                "groupReports",
                reviews.listVisibleGroups()
        );

        return "submission/review-list";
    }

    // =========================================
    // CHI TIET LICH SU BAO CAO CUA MOT NHOM
    // =========================================

    @GetMapping("/groups/{groupId}")
    public String detail(
            @PathVariable Long groupId,
            Model model) {

        model.addAttribute(
                "groupInfo",
                reviews.getGroupInfo(groupId)
        );

        model.addAttribute(
                "reports",
                reviews.getGroupReports(groupId)
        );

        return "submission/review-detail";
    }

    // =========================================
    // TAI BAO CAO PDF
    // =========================================

    @GetMapping("/groups/{groupId}/files/{reportId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long groupId,
            @PathVariable Long reportId) {

        ReportReviewService.DownloadReport file =
                reviews.download(groupId, reportId);

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
                        disposition
                )
                .header(
                        "X-Content-Type-Options",
                        "nosniff"
                )
                .contentLength(file.sizeBytes())
                .body(file.resource());
    }
}
