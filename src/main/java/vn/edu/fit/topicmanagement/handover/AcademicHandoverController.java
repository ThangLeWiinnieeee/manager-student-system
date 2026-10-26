package vn.edu.fit.topicmanagement.handover;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistrationService;
import vn.edu.fit.topicmanagement.user.*;

@Controller
@RequestMapping("/admin/handovers")
@PreAuthorize("hasAnyRole('ADMIN','DEAN')")
public class AcademicHandoverController {
    private final AcademicHandoverService handovers;
    private final CourseRegistrationService courses;
    private final UserAccountService users;
    private final HandoverLog history;
    public AcademicHandoverController(AcademicHandoverService handovers, CourseRegistrationService courses,
            UserAccountService users, HandoverLog history) {
        this.handovers=handovers; this.courses=courses; this.users=users; this.history=history;
    }
    @GetMapping
    public String page(@RequestParam Long courseId, Model model) {
        model.addAttribute("course", courses.get(courseId));
        model.addAttribute("tasks", handovers.findTasks(courseId));
        model.addAttribute("lecturers", users.findAll().stream().filter(u -> u.isEnabled() && u.getRole()==Role.LECTURER).toList());
        model.addAttribute("history", history.findByCourse(courseId));
        return "course-registration/handovers";
    }
    @PostMapping
    public String transfer(@RequestParam Long courseId, @RequestParam AcademicHandoverService.Duty duty,
            @RequestParam Long targetId, @RequestParam Long previousId, @RequestParam Long replacementId,
            @RequestParam String reason, RedirectAttributes redirect) {
        try {
            handovers.transfer(courseId, duty, targetId, previousId, replacementId, reason);
            redirect.addFlashAttribute("success", "Đã bàn giao nhiệm vụ và lưu lịch sử. Điểm cũ không được chuyển cho người mới.");
        } catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/handovers?courseId=" + courseId;
    }
}
