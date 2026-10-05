package vn.edu.fit.topicmanagement.dashboard;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fit.topicmanagement.department.DepartmentRepository;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;

@Controller
public class HomeController {
    private final UserAccountRepository users;
    private final DepartmentRepository departments;

    public HomeController(UserAccountRepository users, DepartmentRepository departments) {
        this.users = users;
        this.departments = departments;
    }

    @GetMapping("/")
    String dashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("userCount", users.count());
        model.addAttribute("departmentCount", departments.count());
        return "home";
    }
}
