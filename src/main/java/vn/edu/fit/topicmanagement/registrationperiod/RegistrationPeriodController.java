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

/**
 * TV2 - Quản lý đợt đăng ký.
 * Nằm dưới /admin/** nên vừa được bảo vệ bởi SecurityConfig (/admin/** = ADMIN, DEAN),
 * vừa được bảo vệ thêm bằng @PreAuthorize ở cấp phương thức (defense-in-depth).
 */
@Controller
@RequestMapping("/admin/registration-periods")
@PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
public class RegistrationPeriodController {
    private final RegistrationPeriodService periods;

    public RegistrationPeriodController(RegistrationPeriodService periods) {
        this.periods = periods;
    }

    @GetMapping
    String list(Model model) {
        model.addAttribute("periods", periods.findAll());
        return "registration-period/list";
    }

    @ModelAttribute
    void roundTypes(Model model) {
        model.addAttribute("roundTypes", RoundType.values());
    }

    @GetMapping("/new")
    String createForm(Model model) {
        model.addAttribute("periodForm", new RegistrationPeriodForm());
        model.addAttribute("editing", false);
        return "registration-period/form";
    }
        @PostMapping
    String create(
            @Valid @ModelAttribute("periodForm") RegistrationPeriodForm periodForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                periods.create(periodForm);
                redirectAttributes.addFlashAttribute("success", "Đã tạo đợt đăng ký");
                return "redirect:/admin/registration-periods";
            } catch (BusinessRuleException ex) {
                bindingResult.reject("period.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", false);
        return "registration-period/form";
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable Long id, Model model) {
        RegistrationPeriod period = periods.get(id);
        model.addAttribute("periodForm", periods.toForm(period));
        model.addAttribute("editing", true);
        model.addAttribute("periodId", id);
        return "registration-period/form";
    }

    @PostMapping("/{id}")
    String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("periodForm") RegistrationPeriodForm periodForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                periods.update(id, periodForm);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật đợt đăng ký");
                return "redirect:/admin/registration-periods";
            } catch (BusinessRuleException ex) {
                bindingResult.reject("period.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", true);
        model.addAttribute("periodId", id);
        return "registration-period/form";
    }
}