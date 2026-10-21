package vn.edu.fit.topicmanagement.notification;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("notifications", notifications.findVisibleForUser(principal.getName()));
        return "notification/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("notification", notifications.getVisible(id, principal.getName()));
        return "notification/detail";
    }
}
