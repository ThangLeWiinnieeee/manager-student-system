
package vn.edu.fit.topicmanagement.studentgroup;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/student/my-groups")
@PreAuthorize("hasRole('STUDENT')")
public class StudentMyGroupController {

    private final StudentGroupService groups;
    private final GroupTopicRegistrationService registrations;
    private final vn.edu.fit.topicmanagement.registrationperiod.CourseRegistrationService periods;


    public StudentMyGroupController(
            StudentGroupService groups,
            GroupTopicRegistrationService registrations,
            vn.edu.fit.topicmanagement.registrationperiod.CourseRegistrationService periods) {

        this.groups = groups;
        this.registrations = registrations;
        this.periods = periods;
    }


    @PostMapping("/join")
    public String join(@RequestParam String code, RedirectAttributes redirect) {
        try {
            var course = periods.join(code);
            redirect.addFlashAttribute("success", "Đã tham gia lớp " + course.getCourseLabel() + ". Bạn có thể tạo nhóm hoặc nhận và xác nhận lời mời vào nhóm.");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/student/my-groups";
    }

    @GetMapping
    public String index(Model model) {
        var joinedCourses = periods.findJoinedCourses();
        model.addAttribute("joinedCourses", joinedCourses);
        var now = java.time.LocalDateTime.now();
        model.addAttribute("openCourseIds", joinedCourses.stream()
                .filter(course -> course.isSvWindowOpen(now))
                .map(course -> course.getId()).collect(java.util.stream.Collectors.toSet()));
        model.addAttribute("invitations", groups.findMyInvitations());

        model.addAttribute(
                "myGroups",
                groups.findMyGroups()
        );

        return "student-group/my-groups";
    }


    // GET: Hien thi form tao nhom
    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long roundId, Model model) {
        var openPeriods = groups.findOpenStudentPeriods();
        var form = new StudentGroupForm();
        if (roundId != null && openPeriods.stream().anyMatch(course -> course.getId().equals(roundId))) {
            form.setRoundId(roundId);
        }
        model.addAttribute("groupForm", form);
        model.addAttribute("openPeriods", openPeriods);

        return "student-group/student-create";
    }

    // POST: Xu ly tao nhom
    @PostMapping
    public String create(
            @Valid @ModelAttribute("groupForm")
            StudentGroupForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "openPeriods",
                    groups.findOpenStudentPeriods());

            return "student-group/student-create";
        }

        try {
            StudentGroup group = groups.createMyGroup(form);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Tạo nhóm thành công. Bạn là nhóm trưởng!");

            return "redirect:/student/my-groups";

        } catch (BusinessRuleException ex) {

            model.addAttribute("error", ex.getMessage());

            model.addAttribute(
                    "openPeriods",
                    groups.findOpenStudentPeriods());

            return "student-group/student-create";
        }
    }


    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model) {

        StudentGroup group = groups.getMyGroup(id);
        model.addAttribute("round", periods.get(group.getRoundId()));

        var memberList = groups.getMyGroupMembers(id);

        model.addAttribute("group", group);
        model.addAttribute("members", memberList);
        model.addAttribute("invitations", groups.findInvitations(id));
        model.addAttribute("membershipOpen", groups.isMembershipOpen(id));
        model.addAttribute("memberCount", memberList.size());

        model.addAttribute(
                "isLeader",
                groups.isMyGroupLeader(id));

        return "student-group/student-detail";
    }


    @PostMapping("/{id}/members")
    public String addMember(
            @PathVariable Long id,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        try {
            groups.addMemberByLeader(id, email);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã gửi lời mời. Sinh viên cần xác nhận tại Nhóm của tôi.");

        } catch (BusinessRuleException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage());
        }

        return "redirect:/student/my-groups/" + id;
    }


    @PostMapping("/{id}/members/{studentId}/remove")
    public String removeMember(
            @PathVariable Long id,
            @PathVariable Long studentId,
            RedirectAttributes redirectAttributes) {

        try {
            groups.removeMemberByLeader(id, studentId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Xóa thành viên thành công");

        } catch (BusinessRuleException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage());
        }

        return "redirect:/student/my-groups/" + id;
    }


    @PostMapping("/{id}/invitations/respond")
    public String respond(@PathVariable Long id, @RequestParam boolean accept, RedirectAttributes redirect) {
        try {
            groups.respondToInvitation(id, accept);
            redirect.addFlashAttribute("success", accept ? "Đã tham gia nhóm" : "Đã từ chối lời mời");
        } catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/student/my-groups";
    }

    @PostMapping("/{id}/invitations/{studentId}/cancel")
    public String cancelInvitation(@PathVariable Long id, @PathVariable Long studentId, RedirectAttributes redirect) {
        groups.cancelInvitation(id, studentId);
        redirect.addFlashAttribute("success", "Đã thu hồi lời mời");
        return "redirect:/student/my-groups/" + id;
    }

    @GetMapping("/{id}/topics")
    public String chooseTopic(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        StudentGroup group = groups.getMyGroup(id);
        var round = periods.get(group.getRoundId());
        model.addAttribute("round", round);
        model.addAttribute("registrationOpen", round.isSvWindowOpen(java.time.LocalDateTime.now()));
        var registration = registrations.findMyRegistration(id);
        if (!model.containsAttribute("proposalForm")) {
            var form = new ProposalForm();
            if (registration != null && registration.getStatus() == GroupTopicRegistrationStatus.REJECTED) {
                form.setTitle(registration.getTopic().getTitle());
                form.setDescription(registration.getTopic().getDescription());
            }
            model.addAttribute("proposalForm", form);
        }

        model.addAttribute("group", group);

        model.addAttribute(
                "isLeader",
                groups.isMyGroupLeader(id));

        model.addAttribute(
                "publishedTopics",
                registrations.findPublishedTopics(id, page));

        model.addAttribute(
                "registration",
                registration);

        return "student-group/topic-registration";
    }


    @PostMapping("/{id}/topics/register")
    public String registerTopic(
            @PathVariable Long id,
            @RequestParam("topicId") Long topicId,
            RedirectAttributes redirectAttributes) {

        try {

            registrations.register(id, topicId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đăng ký thành công. Nhóm đã nhận đề tài.");

        } catch (BusinessRuleException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage());
        }

        return "redirect:/student/my-groups/" + id + "/topics";
    }

    @PostMapping("/{id}/topics/propose")
    public String propose(@PathVariable Long id, @Valid @ModelAttribute("proposalForm") ProposalForm form,
            BindingResult binding, Model model, RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            try {
                registrations.propose(id, form);
                redirect.addFlashAttribute("success", "Đã gửi nội dung đề xuất cho khoa xét duyệt");
                return "redirect:/student/my-groups/" + id + "/topics";
            } catch (BusinessRuleException ex) { binding.reject("proposal.invalid", ex.getMessage()); }
        }
        return chooseTopic(id, 0, model);
    }


}
