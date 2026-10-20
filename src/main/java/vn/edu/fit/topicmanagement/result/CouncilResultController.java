package vn.edu.fit.topicmanagement.result;

import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.council.CouncilRole;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Controller
@RequestMapping("/council/results")
@PreAuthorize("hasAnyRole('LECTURER', 'DEAN', 'ADMIN')")
public class CouncilResultController {

    private final ResultService results;
    private final UserAccountService users;

    public CouncilResultController(ResultService results, UserAccountService users) {
        this.results = results;
        this.users = users;
    }

    @GetMapping("/topic/{topicId}")
    public String topicSummary(@PathVariable Long topicId, Principal principal, Model model) {
        ResultService.TopicSummaryView summary = results.getSummaryView(topicId);
        UserAccount current = users.getByEmail(principal.getName());

        boolean isPresident = summary.council().getMembers().stream()
                .anyMatch(m -> m.getRole() == CouncilRole.PRESIDENT && m.getLecturer().getId().equals(current.getId()));

        model.addAttribute("summary", summary);
        model.addAttribute("isPresident", isPresident);
        return "result/topic-summary";
    }

    @PostMapping("/topic/{topicId}/confirm")
    public String confirm(
            @PathVariable Long topicId,
            @RequestParam("conclusion") String conclusion,
            Principal principal,
            RedirectAttributes redirect) {
        try {
            results.confirmByPresident(topicId, conclusion, principal.getName());
            redirect.addFlashAttribute("success", "Chủ tịch hội đồng đã xác nhận kết quả bảo vệ thành công");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/council/results/topic/" + topicId;
    }
}
