package vn.edu.fit.topicmanagement.result;

import java.util.List;
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
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodService;

@Controller
@RequestMapping("/dean/results")
@PreAuthorize("hasAnyRole('DEAN', 'ADMIN')")
public class DeanResultController {

    private final ResultService results;
    private final RegistrationPeriodService periods;

    public DeanResultController(ResultService results, RegistrationPeriodService periods) {
        this.results = results;
        this.periods = periods;
    }

    @GetMapping
    public String list(
            @RequestParam(name = "roundId", required = false) Long roundId,
            Model model) {
        var allPeriods = periods.findAll();
        Long currentRoundId = roundId != null ? roundId : (allPeriods.isEmpty() ? null : allPeriods.get(0).getId());

        List<TopicFinalResult> roundResults = List.of();
        if (currentRoundId != null) {
            roundResults = results.findByRound(currentRoundId);
        }

        model.addAttribute("periods", allPeriods);
        model.addAttribute("selectedRoundId", currentRoundId);
        model.addAttribute("results", roundResults);
        return "result/dean-list";
    }

    @PostMapping("/topic/{topicId}/publish")
    public String publish(
            @PathVariable Long topicId,
            @RequestParam(value = "roundId", required = false) Long roundId,
            RedirectAttributes redirect) {
        try {
            results.publish(topicId);
            redirect.addFlashAttribute("success", "Đã công bố kết quả đề tài cho sinh viên xem");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dean/results" + (roundId != null ? "?roundId=" + roundId : "");
    }

    @PostMapping("/topic/{topicId}/revoke")
    public String revoke(
            @PathVariable Long topicId,
            @RequestParam(value = "roundId", required = false) Long roundId,
            RedirectAttributes redirect) {
        try {
            results.revoke(topicId);
            redirect.addFlashAttribute("success", "Đã thu hồi kết quả đề tài");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dean/results" + (roundId != null ? "?roundId=" + roundId : "");
    }
}
