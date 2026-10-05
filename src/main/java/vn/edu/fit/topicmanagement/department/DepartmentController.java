package vn.edu.fit.topicmanagement.department;

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

@Controller
@RequestMapping("/admin/departments")
@PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
public class DepartmentController {
    private final DepartmentService departments;

    public DepartmentController(DepartmentService departments) {
        this.departments = departments;
    }

    @GetMapping
    String list(Model model) {
        model.addAttribute("departments", departments.findAll());
        return "admin/departments/list";
    }

    @GetMapping("/new")
    String createForm(Model model) {
        model.addAttribute("departmentForm", new DepartmentForm());
        model.addAttribute("editing", false);
        return "admin/departments/form";
    }

    @PostMapping
    String create(
            @Valid @ModelAttribute DepartmentForm departmentForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                departments.create(departmentForm);
                redirectAttributes.addFlashAttribute("success", "Đã tạo bộ môn");
                return "redirect:/admin/departments";
            } catch (IllegalArgumentException ex) {
                bindingResult.reject("department.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", false);
        return "admin/departments/form";
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable Long id, Model model) {
        Department department = departments.get(id);
        DepartmentForm form = new DepartmentForm();
        form.setCode(department.getCode());
        form.setName(department.getName());
        form.setEnabled(department.isEnabled());
        model.addAttribute("departmentForm", form);
        model.addAttribute("editing", true);
        model.addAttribute("departmentId", id);
        return "admin/departments/form";
    }

    @PostMapping("/{id}")
    String update(
            @PathVariable Long id,
            @Valid @ModelAttribute DepartmentForm departmentForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                departments.update(id, departmentForm);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật bộ môn");
                return "redirect:/admin/departments";
            } catch (IllegalArgumentException ex) {
                bindingResult.reject("department.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", true);
        model.addAttribute("departmentId", id);
        return "admin/departments/form";
    }

    @PostMapping("/{id}/toggle")
    String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departments.toggle(id);
            redirectAttributes.addFlashAttribute("success", "Đã thay đổi trạng thái bộ môn");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/departments";
    }
}
