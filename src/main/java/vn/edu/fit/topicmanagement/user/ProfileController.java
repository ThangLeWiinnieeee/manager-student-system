package vn.edu.fit.topicmanagement.user;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserAccountService accounts;

    public ProfileController(UserAccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    String profile(Authentication authentication, Model model) {
        model.addAttribute("account", accounts.getByEmail(authentication.getName()));
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "profile/index";
    }

    @PostMapping("/password")
    String changePassword(
            Authentication authentication,
            @Valid @ModelAttribute ChangePasswordForm changePasswordForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                accounts.changePassword(authentication.getName(), changePasswordForm);
                redirectAttributes.addFlashAttribute("success", "Đã đổi mật khẩu");
                return "redirect:/profile";
            } catch (BusinessRuleException ex) {
                bindingResult.reject("password.invalid", ex.getMessage());
            }
        }
        model.addAttribute("account", accounts.getByEmail(authentication.getName()));
        return "profile/index";
    }
}
