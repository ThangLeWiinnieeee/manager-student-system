package vn.edu.fit.topicmanagement.dashboard;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fit.topicmanagement.auth.AuthenticatedUser;

@Controller
public class HomeController {
    private final DashboardService dashboard;

    public HomeController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/")
    String dashboard(Authentication authentication, Model model) {
        model.addAttribute("fullName", ((AuthenticatedUser) authentication.getPrincipal()).getFullName());
        DashboardService.Summary summary = dashboard.getSummary();
        model.addAttribute("userCount", summary.userCount());
        model.addAttribute("departmentCount", summary.departmentCount());
        return "home";
    }
}
