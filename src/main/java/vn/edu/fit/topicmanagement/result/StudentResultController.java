package vn.edu.fit.topicmanagement.result;

import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student/my-results")
@PreAuthorize("hasRole('STUDENT')")
public class StudentResultController {

    private final ResultService results;

    public StudentResultController(ResultService results) {
        this.results = results;
    }

    @GetMapping
    public String myResult(Principal principal, Model model) {
        var views = results.getMyStudentResults(principal.getName());
        model.addAttribute("views", views);
        return "result/student-result";
    }
}
