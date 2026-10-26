package vn.edu.fit.topicmanagement.registrationperiod;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

/** Giảng viên đăng ký và quản lý môn/lớp tham gia một đợt chung. */
@Controller
@RequestMapping("/course-registrations")
@PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
public class CourseRegistrationController {
    private final CourseRegistrationService periods;
    private final RegistrationPeriodService windows;
    private final vn.edu.fit.topicmanagement.user.UserAccountService accounts;
    private final vn.edu.fit.topicmanagement.department.DepartmentService departments;

    public CourseRegistrationController(CourseRegistrationService periods, RegistrationPeriodService windows,
            vn.edu.fit.topicmanagement.user.UserAccountService accounts,
            vn.edu.fit.topicmanagement.department.DepartmentService departments) {
        this.periods = periods;
        this.windows = windows;
        this.accounts = accounts;
        this.departments = departments;
    }

    @GetMapping
    String list(Model model) {
        model.addAttribute("periods", periods.findManageable());
        return "course-registration/list";
    }

    @ModelAttribute
    void roundTypes(Model model) {
        model.addAttribute("registrationModes", RegistrationMode.values());
        model.addAttribute("departments", departments.findEnabled());
        model.addAttribute("lecturers", accounts.findAll().stream().filter(u -> u.isEnabled()
                && u.getRole() == vn.edu.fit.topicmanagement.user.Role.LECTURER).toList());
        model.addAttribute("manager", CourseRegistrationService.isManager());
        model.addAttribute("windows", windows.findAll().stream().filter(RegistrationPeriod::isGvOpen).toList());
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('LECTURER')")
    String createForm(@org.springframework.web.bind.annotation.RequestParam(required = false) Long periodId, Model model) {
        var form = new CourseRegistrationForm();
        form.setPeriodId(periodId);
        model.addAttribute("periodForm", form);
        model.addAttribute("editing", false);
        return "course-registration/form";
    }
    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    String create(
            @Valid @ModelAttribute("periodForm") CourseRegistrationForm periodForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                periods.create(periodForm);
                redirectAttributes.addFlashAttribute("success", "Đã đăng ký môn/lớp");
                return "redirect:/course-registrations";
            } catch (BusinessRuleException ex) {
                bindingResult.reject("period.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", false);
        return "course-registration/form";
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable Long id, Model model) {
        CourseRegistration period = periods.get(id);
        periods.assertCanManage(period);
        model.addAttribute("periodForm", periods.toForm(period));
        model.addAttribute("window", period.getPeriod());
        model.addAttribute("editing", true);
        model.addAttribute("periodId", id);
        return "course-registration/form";
    }

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        CourseRegistration period = periods.get(id);
        periods.assertCanManage(period);
        model.addAttribute("period", period);
        return "course-registration/detail";
    }

    @PostMapping("/{id}/join-code")
    String joinCode(@PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "false") boolean renew,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "true") boolean enabled,
            RedirectAttributes redirect) {
        periods.updateJoinCode(id, renew, enabled);
        redirect.addFlashAttribute("success", "Đã cập nhật mã tham gia lớp");
        return "redirect:/course-registrations/" + id;
    }

    @PostMapping("/{id}/delete")
    String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            periods.delete(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa môn/lớp đăng ký");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Môn/lớp đang có dữ liệu liên quan, không thể xóa");
        }
        return "redirect:/course-registrations";
    }

    @PostMapping("/{id}")
    String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("periodForm") CourseRegistrationForm periodForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                periods.update(id, periodForm);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật môn/lớp");
                return "redirect:/course-registrations";
            } catch (BusinessRuleException ex) {
                bindingResult.reject("period.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", true);
        model.addAttribute("periodId", id);
        var course = periods.get(id);
        periods.assertCanManage(course);
        model.addAttribute("window", course.getPeriod());
        return "course-registration/form";
    }
}
