package vn.edu.fit.topicmanagement.council;

import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Controller
@RequestMapping("/lecturer/councils")
@PreAuthorize("hasRole('LECTURER')")
public class CouncilLecturerController {

    private final CouncilService councils;
    private final UserAccountService users;

    public CouncilLecturerController(CouncilService councils, UserAccountService users) {
        this.councils = councils;
        this.users = users;
    }

    @GetMapping
    public String myCouncils(Principal principal, Model model) {
        List<Council> myCouncils = councils.findMyCouncils(principal.getName());
        model.addAttribute("councils", myCouncils);
        return "council/lecturer-councils";
    }

    @GetMapping("/{id}")
    public String councilDetail(@PathVariable Long id, Principal principal, Model model) {
        Council council = councils.get(id);
        UserAccount currentLecturer = users.getByEmail(principal.getName());

        boolean isMember = council.getMembers().stream()
                .anyMatch(m -> m.getLecturer().getId().equals(currentLecturer.getId()));
        if (!isMember) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không phải thành viên hội đồng này");
        }

        model.addAttribute("council", council);
        model.addAttribute("members", councils.getMembers(id));
        model.addAttribute("assignedTopics", councils.getAssignmentsByCouncil(id));
        return "council/detail";
    }
}
