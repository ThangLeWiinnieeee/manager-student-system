package vn.edu.fit.topicmanagement.evaluation;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.council.CouncilService;
import vn.edu.fit.topicmanagement.council.TopicAssignment;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.topic.TopicService;

@Controller
@RequestMapping("/lecturer/evaluations")
@PreAuthorize("hasRole('LECTURER')")
public class EvaluationController {

    private final EvaluationService evaluations;
    private final TopicService topics;
    private final CouncilService councils;

    public EvaluationController(
            EvaluationService evaluations,
            TopicService topics,
            CouncilService councils) {
        this.evaluations = evaluations;
        this.topics = topics;
        this.councils = councils;
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("items", evaluations.findAssignedTopicsForLecturer(principal.getName()));
        return "evaluation/lecturer-list";
    }

    @GetMapping("/registration/{registrationId}")
    public String form(@PathVariable Long registrationId, Principal principal, Model model) {
        councils.assertCanViewAssessment(registrationId, principal.getName());
        Topic topic = councils.getRegistration(registrationId).getTopic();
        TopicAssignment assignment = councils.getAssignment(registrationId);
        Evaluation currentEval = evaluations.getMyEvaluation(registrationId, principal.getName());

        EvaluationForm form = new EvaluationForm();
        if (currentEval != null) {
            form.setScoreContent(currentEval.getScoreContent());
            form.setScorePresentation(currentEval.getScorePresentation());
            form.setScoreQa(currentEval.getScoreQa());
            form.setComments(currentEval.getComments());
            form.setDefenseAllowed(currentEval.getDefenseAllowed());
        }

        model.addAttribute("registration", councils.getRegistration(registrationId));
        model.addAttribute("topic", topic);
        model.addAttribute("assignment", assignment);
        model.addAttribute("currentEval", currentEval);
        model.addAttribute("evaluationForm", form);
        return "evaluation/form";
    }

    @PostMapping("/registration/{registrationId}")
    public String submit(
            @PathVariable Long registrationId,
            @Valid @ModelAttribute("evaluationForm") EvaluationForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            councils.assertCanViewAssessment(registrationId, principal.getName());
            Topic topic = councils.getRegistration(registrationId).getTopic();
            TopicAssignment assignment = councils.getAssignment(registrationId);
            Evaluation currentEval = evaluations.getMyEvaluation(registrationId, principal.getName());
            model.addAttribute("registration", councils.getRegistration(registrationId));
            model.addAttribute("topic", topic);
            model.addAttribute("assignment", assignment);
            model.addAttribute("currentEval", currentEval);
            return "evaluation/form";
        }

        try {
            evaluations.evaluate(registrationId, form, principal.getName());
            String msg = "submit".equalsIgnoreCase(form.getAction())
                    ? "Đã xác nhận nộp điểm và khóa chỉnh sửa thành công"
                    : "Đã lưu nháp điểm đánh giá";
            redirect.addFlashAttribute("success", msg);
            return "redirect:/lecturer/evaluations/registration/" + registrationId;
        } catch (BusinessRuleException ex) {
            councils.assertCanViewAssessment(registrationId, principal.getName());
            Topic topic = councils.getRegistration(registrationId).getTopic();
            TopicAssignment assignment = councils.getAssignment(registrationId);
            Evaluation currentEval = evaluations.getMyEvaluation(registrationId, principal.getName());
            model.addAttribute("registration", councils.getRegistration(registrationId));
            model.addAttribute("topic", topic);
            model.addAttribute("assignment", assignment);
            model.addAttribute("currentEval", currentEval);
            model.addAttribute("error", ex.getMessage());
            return "evaluation/form";
        }
    }
}
