package vn.edu.fit.topicmanagement.dashboard;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fit.topicmanagement.auth.AuthenticatedUser;
import vn.edu.fit.topicmanagement.notification.RegistrationNoticeService;

@Controller
public class HomeController {
    private final DashboardService dashboard;
    private final RegistrationNoticeService registrationNotices;

    public HomeController(DashboardService dashboard, RegistrationNoticeService registrationNotices) {
        this.dashboard = dashboard;
        this.registrationNotices = registrationNotices;
    }

    @GetMapping("/")
    String dashboard(Authentication authentication, Model model) {
        model.addAttribute("fullName", ((AuthenticatedUser) authentication.getPrincipal()).getFullName());
        DashboardService.Summary summary = dashboard.getSummary();
        model.addAttribute("userCount", summary.userCount());
        model.addAttribute("departmentCount", summary.departmentCount());
        model.addAttribute("registrationNotices", registrationNotices.findOpenForUser(authentication.getName()));
        return "home";
    }
}
