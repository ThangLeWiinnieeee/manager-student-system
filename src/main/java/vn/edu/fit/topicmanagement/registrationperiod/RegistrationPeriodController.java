package vn.edu.fit.topicmanagement.registrationperiod;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

@Controller
@RequestMapping("/admin/registration-periods")
@PreAuthorize("hasAnyRole('ADMIN','DEAN','LECTURER')")
public class RegistrationPeriodController {
    private final RegistrationPeriodService periods;
    private final CourseRegistrationRepository courses;
    public RegistrationPeriodController(RegistrationPeriodService periods, CourseRegistrationRepository courses) {
        this.periods = periods;
        this.courses = courses;
    }
    @ModelAttribute
    void referenceData(Model model) {
        model.addAttribute("roundTypes", RoundType.values());
        model.addAttribute("manager", CourseRegistrationService.isManager());
    }
    @GetMapping
    String list(Model model) {
        model.addAttribute("periods", periods.findAll());
        return "registration-period/list";
    }
    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        model.addAttribute("period", periods.get(id));
        model.addAttribute("courses", courses.findByPeriod_IdOrderByCourseNameAsc(id));
        return "registration-period/detail";
    }
    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','DEAN')")
    String createForm(Model model) {
        model.addAttribute("periodForm", new RegistrationPeriodForm());
        model.addAttribute("editing", false);
        return "registration-period/form";
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEAN')")
    String create(@Valid @ModelAttribute("periodForm") RegistrationPeriodForm form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                var period = periods.create(form);
                redirect.addFlashAttribute("success", "Đã mở đợt đăng ký");
                return "redirect:/admin/registration-periods/" + period.getId();
            } catch (BusinessRuleException ex) { errors.reject("period.invalid", ex.getMessage()); }
        }
        model.addAttribute("editing", false);
        return "registration-period/form";
    }
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','DEAN')")
    String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("periodForm", periods.toForm(periods.get(id)));
        model.addAttribute("editing", true);
        model.addAttribute("periodId", id);
        return "registration-period/form";
    }
    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEAN')")
    String update(@PathVariable Long id, @Valid @ModelAttribute("periodForm") RegistrationPeriodForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                periods.update(id, form);
                redirect.addFlashAttribute("success", "Đã cập nhật lịch đợt đăng ký");
                return "redirect:/admin/registration-periods/" + id;
            } catch (BusinessRuleException ex) { errors.reject("period.invalid", ex.getMessage()); }
        }
        model.addAttribute("editing", true);
        model.addAttribute("periodId", id);
        return "registration-period/form";
    }
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','DEAN')")
    String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            periods.delete(id);
            redirect.addFlashAttribute("success", "Đã xóa đợt đăng ký");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "Đợt đã có dữ liệu liên quan, không thể xóa");
        }
        return "redirect:/admin/registration-periods";
    }
}
