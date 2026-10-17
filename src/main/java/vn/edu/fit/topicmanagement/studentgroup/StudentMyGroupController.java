
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


    public StudentMyGroupController(
            StudentGroupService groups,
            GroupTopicRegistrationService registrations) {

        this.groups = groups;
        this.registrations = registrations;
    }


    @GetMapping
    public String index(Model model) {

        model.addAttribute(
                "myGroups",
                groups.findMyGroups()
        );

        return "student-group/my-groups";
    }


    // GET: Hien thi form tao nhom
    @GetMapping("/new")
    public String createForm(Model model) {

        model.addAttribute(
                "groupForm", new StudentGroupForm());

        model.addAttribute(
                "openPeriods", groups.findOpenStudentPeriods());

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

        var memberList = groups.getMyGroupMembers(id);

        model.addAttribute("group", group);
        model.addAttribute("members", memberList);
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
                    "Thêm thành viên thành công");

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


    @GetMapping("/{id}/topics")
    public String chooseTopic(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        StudentGroup group = groups.getMyGroup(id);

        model.addAttribute("group", group);

        model.addAttribute(
                "isLeader",
                groups.isMyGroupLeader(id));

        model.addAttribute(
                "publishedTopics",
                registrations.findPublishedTopics(id, page));

        model.addAttribute(
                "registration",
                registrations.findMyRegistration(id));

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
                    "Đăng ký đề tài thành công. Vui lòng chờ Trưởng khoa xét duyệt.");

        } catch (BusinessRuleException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage());
        }

        return "redirect:/student/my-groups/" + id + "/topics";
    }


}
