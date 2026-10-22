package vn.edu.fit.topicmanagement.topic;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import vn.edu.fit.topicmanagement.major.MajorService;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodService;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccountService;

/**
 * TV2 - Điều phối đề tài.
 * /topics: danh sách mọi vai trò đều xem được (giảng viên/sinh viên chỉ thấy
 * đề tài của mình + đề tài đã công bố - lọc ở TopicRepository.search).
 * Thao tác chỉnh sửa: chỉ LECTURER; thao tác duyệt/công bố: ADMIN hoặc DEAN.
 */
@Controller
@RequestMapping("/topics")
public class TopicController {
    private final TopicService topics;
    private final RegistrationPeriodService periods;
    private final MajorService majors;
    private final UserAccountService accounts;

    public TopicController(
            TopicService topics,
            RegistrationPeriodService periods,
            MajorService majors,
            UserAccountService accounts) {
        this.topics = topics;
        this.periods = periods;
        this.majors = majors;
        this.accounts = accounts;
    }

    /** Dữ liệu dùng chung cho form lọc và form đăng ký. */
    @ModelAttribute
    void referenceData(Model model) {
        model.addAttribute("periods", periods.findAll());
        model.addAttribute("majors", majors.findEnabled());
        model.addAttribute("lecturers", accounts.findAll().stream()
                .filter(u -> u.getRole() == Role.LECTURER && u.isEnabled())
                .toList());
        model.addAttribute("statuses", TopicStatus.values());
    }

    @GetMapping
    String list(
            @RequestParam(required = false) Long roundId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) TopicStatus status,
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model) {
        String viewerEmail = isManager(authentication) ? null : authentication.getName();
        Page<Topic> topicPage = topics.search(roundId, majorId, status, viewerEmail, page);
        model.addAttribute("topicPage", topicPage);
        model.addAttribute("filterRoundId", roundId);
        model.addAttribute("filterMajorId", majorId);
        model.addAttribute("filterStatus", status);
        return "topic/list";
    }

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Authentication authentication, Model model) {
        Topic topic = topics.get(id);
        model.addAttribute("topic", topic);
        model.addAttribute("rejectForm", new RejectForm());
        model.addAttribute("owner",
                topic.getCreatedBy().getEmail().equalsIgnoreCase(authentication.getName()));
        return "topic/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('LECTURER')")
    String createForm(Model model) {
        model.addAttribute("topicForm", new TopicForm());
        model.addAttribute("editing", false);
        return "topic/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    String create(
            @Valid @ModelAttribute("topicForm") TopicForm topicForm,
            BindingResult bindingResult,
            @RequestParam(name = "action", required = false) String action,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        boolean submitNow = "submit".equals(action);
        if (!bindingResult.hasErrors()) {
            try {
                Topic topic = topics.create(authentication.getName(), topicForm, submitNow);
                redirectAttributes.addFlashAttribute("success",
                        submitNow ? "Đã gửi đề tài chờ duyệt" : "Đã lưu đề tài nháp");
                return "redirect:/topics/" + topic.getId();
            } catch (BusinessRuleException ex) {
                bindingResult.reject("topic.invalid", ex.getMessage());
            }
        }
        model.addAttribute("editing", false);
        return "topic/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('LECTURER')")
    String editForm(@PathVariable Long id, Model model) {
        Topic topic = topics.get(id);
        model.addAttribute("topicForm", topics.toForm(topic));
        model.addAttribute("rejectionReason", topic.getRejectionReason());
        model.addAttribute("editing", true);
        model.addAttribute("topicId", id);
        return "topic/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("topicForm") TopicForm topicForm,
            BindingResult bindingResult,
            @RequestParam(name = "action", required = false) String action,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        boolean submitNow = "submit".equals(action);
        if (!bindingResult.hasErrors()) {
            try {
                topics.update(id, authentication.getName(), topicForm, submitNow);
                redirectAttributes.addFlashAttribute("success",
                        submitNow ? "Đã gửi đề tài chờ duyệt" : "Đã cập nhật đề tài");
                return "redirect:/topics/" + id;
            } catch (BusinessRuleException ex) {
                bindingResult.reject("topic.invalid", ex.getMessage());
            }
        }
        model.addAttribute("rejectionReason", topics.get(id).getRejectionReason());
        model.addAttribute("editing", true);
        model.addAttribute("topicId", id);
        return "topic/form";
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('LECTURER')")
    String submit(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            topics.submit(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Đã gửi đề tài chờ duyệt");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/topics/" + id;
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            topics.approve(id);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt đề tài");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/topics/" + id;
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    String reject(
            @PathVariable Long id,
            @Valid @ModelAttribute RejectForm rejectForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                topics.reject(id, rejectForm.getReason());
                redirectAttributes.addFlashAttribute("success", "Đã từ chối đề tài");
                return "redirect:/topics/" + id;
            } catch (BusinessRuleException ex) {
                bindingResult.reject("reject.invalid", ex.getMessage());
            }
        }
        Topic topic = topics.get(id);
        model.addAttribute("topic", topic);
        model.addAttribute("owner",
                topic.getCreatedBy().getEmail().equalsIgnoreCase(authentication.getName()));
        return "topic/detail";
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    String publish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            topics.publish(id);
            redirectAttributes.addFlashAttribute("success", "Đã công bố đề tài");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/topics/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('LECTURER')")
    String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            topics.delete(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Đã xóa đề tài");
            return "redirect:/topics";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/topics/" + id;
    }

    private static boolean isManager(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority ->
                "ROLE_ADMIN".equals(authority.getAuthority()) || "ROLE_DEAN".equals(authority.getAuthority()));
    }
}