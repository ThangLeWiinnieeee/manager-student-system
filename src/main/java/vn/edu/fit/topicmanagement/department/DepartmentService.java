package vn.edu.fit.topicmanagement.department;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

@Service
@Transactional
public class DepartmentService {
    private final DepartmentRepository departments;
    private final UserAccountRepository users;

    public DepartmentService(DepartmentRepository departments, UserAccountRepository users) {
        this.departments = departments;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return departments.findAllByOrderByCodeAsc();
    }

    @Transactional(readOnly = true)
    public List<Department> findEnabled() {
        return departments.findByEnabledTrueOrderByCodeAsc();
    }

    @Transactional(readOnly = true)
    public Department get(Long id) {
        return departments.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bộ môn"));
    }

    public Department create(DepartmentForm form) {
        String code = normalizeCode(form.getCode());
        String name = form.getName().trim();
        if (departments.existsByCodeIgnoreCase(code)) {
            throw new BusinessRuleException("Mã bộ môn đã tồn tại");
        }
        if (departments.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException("Tên bộ môn đã tồn tại");
        }
        Department department = new Department();
        apply(department, form, code, name);
        return departments.save(department);
    }

    public Department update(Long id, DepartmentForm form) {
        Department department = get(id);
        String code = normalizeCode(form.getCode());
        String name = form.getName().trim();
        if (departments.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessRuleException("Mã bộ môn đã tồn tại");
        }
        if (departments.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessRuleException("Tên bộ môn đã tồn tại");
        }
        apply(department, form, code, name);
        return department;
    }

    @Transactional(readOnly = true)
    public DepartmentForm toForm(Department department) {
        DepartmentForm form = new DepartmentForm();
        form.setCode(department.getCode());
        form.setName(department.getName());
        form.setEnabled(department.isEnabled());
        return form;
    }

    public void toggle(Long id) {
        Department department = get(id);
        if (department.isEnabled() && users.existsByDepartmentId(id)) {
            throw new BusinessRuleException("Không thể khóa bộ môn đang có người dùng");
        }
        department.setEnabled(!department.isEnabled());
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private static void apply(Department department, DepartmentForm form, String code, String name) {
        department.setCode(code);
        department.setName(name);
        department.setEnabled(form.isEnabled());
    }
}
