package vn.edu.fit.topicmanagement.major;

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

@Controller
@RequestMapping("/admin/majors")
@PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
public class MajorController {
    private final MajorService majors;

    public MajorController(MajorService majors) {
        this.majors = majors;
    }

    @GetMapping
    String list(Model model) {
        model.addAttribute("majors", majors.findAll());
        return "admin/majors/list";
    }

    @GetMapping("/new")
    String createForm(Model model) {
        model.addAttribute("majorForm", new MajorForm());
        model.addAttribute("editing", false);
        return "admin/majors/form";
    }

    @PostMapping
    String create(
            @Valid @ModelAttribute MajorForm majorForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                majors.create(majorForm);
                redirectAttributes.addFlashAttribute("success", "Đã tạo chuyên ngành");
                return "redirect:/admin/majors";
            } catch (BusinessRuleException ex) {
                bindingResult.reject("major.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", false);
        return "admin/majors/form";
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable Long id, Model model) {
        Major major = majors.get(id);
        model.addAttribute("majorForm", majors.toForm(major));
        model.addAttribute("editing", true);
        model.addAttribute("majorId", id);
        return "admin/majors/form";
    }

    @PostMapping("/{id}")
    String update(
            @PathVariable Long id,
            @Valid @ModelAttribute MajorForm majorForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                majors.update(id, majorForm);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật chuyên ngành");
                return "redirect:/admin/majors";
            } catch (BusinessRuleException ex) {
                bindingResult.reject("major.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", true);
        model.addAttribute("majorId", id);
        return "admin/majors/form";
    }

    @PostMapping("/{id}/toggle")
    String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            majors.toggle(id);
            redirectAttributes.addFlashAttribute("success", "Đã thay đổi trạng thái chuyên ngành");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/majors";
    }
}
