package vn.edu.fit.topicmanagement.notification;

import jakarta.validation.Valid;
import java.security.Principal;
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
@RequestMapping("/admin/notifications")
@PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
public class NotificationAdminController {

    private final NotificationService notifications;

    public NotificationAdminController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("notifications", notifications.findAllForAdmin());
        return "notification/admin-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("notificationForm", new NotificationForm());
        model.addAttribute("targets", NotificationTarget.values());
        model.addAttribute("isNew", true);
        return "notification/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("notificationForm") NotificationForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("targets", NotificationTarget.values());
            model.addAttribute("isNew", true);
            return "notification/form";
        }
        try {
            notifications.create(form, principal.getName());
            redirect.addFlashAttribute("success", "Đã tạo thông báo thành công");
            return "redirect:/admin/notifications";
        } catch (BusinessRuleException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("targets", NotificationTarget.values());
            model.addAttribute("isNew", true);
            return "notification/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Notification notification = notifications.get(id);
        model.addAttribute("notificationForm", notifications.toForm(notification));
        model.addAttribute("notificationId", id);
        model.addAttribute("targets", NotificationTarget.values());
        model.addAttribute("isNew", false);
        return "notification/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("notificationForm") NotificationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("notificationId", id);
            model.addAttribute("targets", NotificationTarget.values());
            model.addAttribute("isNew", false);
            return "notification/form";
        }
        try {
            notifications.update(id, form);
            redirect.addFlashAttribute("success", "Đã cập nhật thông báo thành công");
            return "redirect:/admin/notifications";
        } catch (BusinessRuleException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("notificationId", id);
            model.addAttribute("targets", NotificationTarget.values());
            model.addAttribute("isNew", false);
            return "notification/form";
        }
    }

    @PostMapping("/{id}/toggle-publish")
    public String togglePublish(@PathVariable Long id, RedirectAttributes redirect) {
        notifications.togglePublish(id);
        redirect.addFlashAttribute("success", "Đã thay đổi trạng thái hiển thị thông báo");
        return "redirect:/admin/notifications";
    }

    @PostMapping("/{id}/toggle-pin")
    public String togglePin(@PathVariable Long id, RedirectAttributes redirect) {
        notifications.togglePin(id);
        redirect.addFlashAttribute("success", "Đã thay đổi trạng thái ghim thông báo");
        return "redirect:/admin/notifications";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        notifications.delete(id);
        redirect.addFlashAttribute("success", "Đã xóa thông báo thành công");
        return "redirect:/admin/notifications";
    }
}
