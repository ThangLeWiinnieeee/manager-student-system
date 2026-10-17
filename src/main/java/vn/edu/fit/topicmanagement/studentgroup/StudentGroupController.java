
package vn.edu.fit.topicmanagement.studentgroup;

import jakarta.validation.Valid;
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
import java.util.List;

@Controller
@RequestMapping("/admin/student-groups")
@PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
public class StudentGroupController {

    private final StudentGroupService groups;

    public StudentGroupController(StudentGroupService groups) {
        this.groups = groups;
    }

    // Danh sach nhom
    @GetMapping
    public String list(Model model) {
        model.addAttribute("groups", groups.findAll());
        return "student-group/list";
    }

    // Form tao nhom
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("groupForm", new StudentGroupForm());
        model.addAttribute("editing", false);
        return "student-group/form";
    }

    // Xu ly tao nhom

    @PostMapping
    public String create(
            @Valid @ModelAttribute("groupForm") StudentGroupForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            return "student-group/form";
        }

        try {
            groups.create(form);
        } catch (BusinessRuleException ex) {
            bindingResult.reject("business", ex.getMessage());
            model.addAttribute("editing", false);
            return "student-group/form";
        }

        redirectAttributes.addFlashAttribute(
                "success", "Tạo nhóm thành công");

        return "redirect:/admin/student-groups";
    }


    // Form chinh sua nhom
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model) {

        StudentGroup group = groups.get(id);

        model.addAttribute("groupForm", groups.toForm(group));
        model.addAttribute("groupId", id);
        model.addAttribute("editing", true);

        return "student-group/form";
    }

    // Xu ly cap nhat nhom

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("groupForm") StudentGroupForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("groupId", id);
            model.addAttribute("editing", true);
            return "student-group/form";
        }

        try {
            groups.update(id, form);
        } catch (BusinessRuleException ex) {
            bindingResult.reject("business", ex.getMessage());
            model.addAttribute("groupId", id);
            model.addAttribute("editing", true);
            return "student-group/form";
        }

        redirectAttributes.addFlashAttribute(
                "success", "Cập nhật nhóm thành công");

        return "redirect:/admin/student-groups";
    }


    // Xoa nhom
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            groups.delete(id);
            redirectAttributes.addFlashAttribute(
                    "success", "Xóa nhóm thành công");

        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());
        }

        return "redirect:/admin/student-groups";
    }


    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model) {

        StudentGroup group = groups.get(id);

        List<StudentGroupMemberView> memberList =
                groups.getMembers(id);

        model.addAttribute("group", group);
        model.addAttribute("members", memberList);
        model.addAttribute(
                "memberCount", memberList.size());
        model.addAttribute(
                "memberForm", new StudentGroupMemberForm());

        return "student-group/detail";
    }


    // TV3: Them sinh vien vao nhom
    @PostMapping("/{id}/members")
    public String addMember(
            @PathVariable Long id,
            @Valid @ModelAttribute("memberForm")
            StudentGroupMemberForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // Kiem tra email
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Email sinh viên không hợp lệ"
            );

            return "redirect:/admin/student-groups/" + id;
        }

        try {
            // Goi Service them thanh vien
            groups.addMember(id, form.getEmail());

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Thêm thành viên thành công"
            );

        } catch (BusinessRuleException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/student-groups/" + id;
    }


    // TV3: Xóa thành viên khỏi nhóm
    @PostMapping("/{id}/members/{studentId}/remove")
    public String removeMember(
            @PathVariable Long id,
            @PathVariable Long studentId,
            RedirectAttributes redirectAttributes) {

        try {
            groups.removeMember(id, studentId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Xóa thành viên thành công"
            );

        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/student-groups/" + id;
    }


    // TV3: Chỉ định nhóm trưởng
    @PostMapping("/{id}/leader/{studentId}")
    public String assignLeader(
            @PathVariable Long id,
            @PathVariable Long studentId,
            RedirectAttributes redirectAttributes) {

        try {
            groups.assignLeader(id, studentId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Chỉ định nhóm trưởng thành công"
            );

        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/student-groups/" + id;
    }


}
