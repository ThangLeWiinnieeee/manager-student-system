
package vn.edu.fit.topicmanagement.studentgroup;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

@Controller
@RequestMapping("/dean/group-topic-registrations")
@PreAuthorize("hasAnyRole('DEAN','ADMIN','LECTURER')")
public class DeanGroupTopicRegistrationController {

    private final GroupTopicRegistrationService registrations;

    public DeanGroupTopicRegistrationController(
            GroupTopicRegistrationService registrations) {

        this.registrations = registrations;
    }


    // =========================================
    // DEAN: DANH SACH DANG KY DE TAI
    // =========================================

    @GetMapping
    public String index(Model model) {

        model.addAttribute(
                "registrations",
                registrations.findReviewable());

        return "student-group/dean-topic-registrations";
    }


    // =========================================
    // DEAN: CHAP THUAN
    // =========================================

    @PostMapping("/{id}/approve")
    public String approve(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            registrations.approve(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã chấp thuận đăng ký đề tài thành công");

        } catch (BusinessRuleException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage());
        }

        return "redirect:/dean/group-topic-registrations";
    }


    // =========================================
    // DEAN: TU CHOI
    // =========================================

    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable Long id,
            @RequestParam(
                    value = "reason",
                    required = false
            ) String reason,
            RedirectAttributes redirectAttributes) {

        try {

            registrations.reject(id, reason);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã từ chối đăng ký đề tài");

        } catch (BusinessRuleException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage());
        }

        return "redirect:/dean/group-topic-registrations";
    }
}
