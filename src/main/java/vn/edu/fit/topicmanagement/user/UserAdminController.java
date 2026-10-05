package vn.edu.fit.topicmanagement.user;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fit.topicmanagement.department.DepartmentService;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
public class UserAdminController {
    private final UserAccountService accounts;
    private final DepartmentService departments;

    public UserAdminController(UserAccountService accounts, DepartmentService departments) {
        this.accounts = accounts;
        this.departments = departments;
    }

    @ModelAttribute
    void commonModel(Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("departments", departments.findEnabled());
    }

    @GetMapping
    String list(Model model) {
        model.addAttribute("users", accounts.findAll());
        return "admin/users/list";
    }

    @GetMapping("/new")
    String createForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("editing", false);
        return "admin/users/form";
    }

    @PostMapping
    String create(
            @Valid @ModelAttribute UserForm userForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                accounts.create(userForm);
                redirectAttributes.addFlashAttribute("success", "Đã tạo tài khoản");
                return "redirect:/admin/users";
            } catch (IllegalArgumentException ex) {
                bindingResult.reject("user.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", false);
        return "admin/users/form";
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("userForm", accounts.toForm(accounts.get(id)));
        model.addAttribute("editing", true);
        model.addAttribute("userId", id);
        return "admin/users/form";
    }

    @PostMapping("/{id}")
    String update(
            @PathVariable Long id,
            @Valid @ModelAttribute UserForm userForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                accounts.update(id, userForm);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật tài khoản");
                return "redirect:/admin/users";
            } catch (IllegalArgumentException ex) {
                bindingResult.reject("user.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", true);
        model.addAttribute("userId", id);
        return "admin/users/form";
    }

    @PostMapping("/{id}/toggle")
    String toggle(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            accounts.toggle(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Đã thay đổi trạng thái tài khoản");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
