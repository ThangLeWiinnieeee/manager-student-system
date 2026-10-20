package vn.edu.fit.topicmanagement.council;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodService;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistration;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationRepository;
import vn.edu.fit.topicmanagement.studentgroup.GroupTopicRegistrationStatus;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Controller
@RequestMapping("/admin/councils")
@PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
public class CouncilAdminController {

    private final CouncilService councils;
    private final RegistrationPeriodService periods;
    private final UserAccountService users;
    private final GroupTopicRegistrationRepository groupRegistrations;

    public CouncilAdminController(
            CouncilService councils,
            RegistrationPeriodService periods,
            UserAccountService users,
            GroupTopicRegistrationRepository groupRegistrations) {
        this.councils = councils;
        this.periods = periods;
        this.users = users;
        this.groupRegistrations = groupRegistrations;
    }

    @GetMapping
    public String list(
            @RequestParam(name = "roundId", required = false) Long roundId,
            Model model) {
        model.addAttribute("periods", periods.findAll());
        model.addAttribute("selectedRoundId", roundId);
        if (roundId != null) {
            model.addAttribute("councils", councils.findByRound(roundId));
        } else {
            model.addAttribute("councils", councils.findAll());
        }
        return "council/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("councilForm", new CouncilForm());
        model.addAttribute("periods", periods.findAll());
        model.addAttribute("isNew", true);
        return "council/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("councilForm") CouncilForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("periods", periods.findAll());
            model.addAttribute("isNew", true);
            return "council/form";
        }
        try {
            Council council = councils.create(form);
            redirect.addFlashAttribute("success", "Đã tạo hội đồng thành công");
            return "redirect:/admin/councils/" + council.getId();
        } catch (BusinessRuleException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("periods", periods.findAll());
            model.addAttribute("isNew", true);
            return "council/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Council council = councils.get(id);
        List<UserAccount> activeLecturers = users.findAll().stream()
                .filter(u -> u.getRole() == Role.LECTURER && u.isEnabled())
                .toList();

        model.addAttribute("council", council);
        model.addAttribute("members", councils.getMembers(id));
        model.addAttribute("assignedTopics", councils.getAssignmentsByCouncil(id));
        model.addAttribute("lecturers", activeLecturers);
        model.addAttribute("roles", CouncilRole.values());
        model.addAttribute("memberForm", new CouncilMemberForm());
        return "council/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Council council = councils.get(id);
        model.addAttribute("councilForm", councils.toForm(council));
        model.addAttribute("councilId", id);
        model.addAttribute("periods", periods.findAll());
        model.addAttribute("isNew", false);
        return "council/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("councilForm") CouncilForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("councilId", id);
            model.addAttribute("periods", periods.findAll());
            model.addAttribute("isNew", false);
            return "council/form";
        }
        try {
            councils.update(id, form);
            redirect.addFlashAttribute("success", "Đã cập nhật thông tin hội đồng");
            return "redirect:/admin/councils/" + id;
        } catch (BusinessRuleException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("councilId", id);
            model.addAttribute("periods", periods.findAll());
            model.addAttribute("isNew", false);
            return "council/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            councils.delete(id);
            redirect.addFlashAttribute("success", "Đã xóa hội đồng thành công");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/councils";
    }

    @PostMapping("/{id}/members")
    public String addMember(
            @PathVariable Long id,
            @Valid @ModelAttribute("memberForm") CouncilMemberForm form,
            BindingResult bindingResult,
            RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            redirect.addFlashAttribute("error", "Vui lòng chọn giảng viên và vai trò hợp lệ");
            return "redirect:/admin/councils/" + id;
        }
        try {
            councils.addOrUpdateMember(id, form);
            redirect.addFlashAttribute("success", "Đã cập nhật thành viên hội đồng thành công");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/councils/" + id;
    }

    @PostMapping("/{id}/members/{lecturerId}/remove")
    public String removeMember(
            @PathVariable Long id,
            @PathVariable Long lecturerId,
            RedirectAttributes redirect) {
        try {
            councils.removeMember(id, lecturerId);
            redirect.addFlashAttribute("success", "Đã xóa thành viên khỏi hội đồng");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/councils/" + id;
    }

    // ==========================================
    // Màn hình Phân công Đề tài & GVPB
    // ==========================================

    @GetMapping("/assignments")
    public String assignmentsPage(
            @RequestParam(name = "roundId", required = false) Long roundId,
            Model model) {
        var allPeriods = periods.findAll();
        Long currentRoundId = roundId != null ? roundId : (allPeriods.isEmpty() ? null : allPeriods.get(0).getId());

        List<GroupTopicRegistration> approvedRegistrations = List.of();
        List<Council> roundCouncils = List.of();
        if (currentRoundId != null) {
            approvedRegistrations = groupRegistrations.findAllByOrderBySubmittedAtDesc().stream()
                    .filter(r -> r.getStatus() == GroupTopicRegistrationStatus.APPROVED
                            && r.getTopic().getRound().getId().equals(currentRoundId))
                    .toList();
            roundCouncils = councils.findByRound(currentRoundId);
        }

        List<UserAccount> lecturers = users.findAll().stream()
                .filter(u -> u.getRole() == Role.LECTURER && u.isEnabled())
                .toList();

        java.util.Map<Long, TopicAssignment> assignmentsMap = new java.util.HashMap<>();
        for (GroupTopicRegistration reg : approvedRegistrations) {
            TopicAssignment asgn = councils.getAssignment(reg.getTopic().getId());
            if (asgn != null) {
                assignmentsMap.put(reg.getTopic().getId(), asgn);
            }
        }

        model.addAttribute("periods", allPeriods);
        model.addAttribute("selectedRoundId", currentRoundId);
        model.addAttribute("registrations", approvedRegistrations);
        model.addAttribute("councils", roundCouncils);
        model.addAttribute("lecturers", lecturers);
        model.addAttribute("assignmentsMap", assignmentsMap);
        model.addAttribute("councilService", councils);
        return "council/assign-topic";
    }

    @PostMapping("/assignments")
    public String saveAssignment(
            @RequestParam("topicId") Long topicId,
            @RequestParam(value = "councilId", required = false) Long councilId,
            @RequestParam(value = "reviewerId", required = false) Long reviewerId,
            @RequestParam(value = "roundId", required = false) Long roundId,
            RedirectAttributes redirect) {
        TopicAssignmentForm form = new TopicAssignmentForm();
        form.setTopicId(topicId);
        form.setCouncilId(councilId);
        form.setReviewerId(reviewerId);

        try {
            councils.assignTopic(form);
            redirect.addFlashAttribute("success", "Đã phân công hội đồng và phản biện thành công cho đề tài");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/councils/assignments" + (roundId != null ? "?roundId=" + roundId : "");
    }
}
