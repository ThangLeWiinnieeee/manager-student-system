package vn.edu.fit.topicmanagement.major;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

@Service
@Transactional
public class MajorService {
    private final MajorRepository majors;
    private final UserAccountRepository users;

    public MajorService(MajorRepository majors, UserAccountRepository users) {
        this.majors = majors;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Major> findAll() {
        return majors.findAllByOrderByCodeAsc();
    }

    @Transactional(readOnly = true)
    public List<Major> findEnabled() {
        return majors.findByEnabledTrueOrderByCodeAsc();
    }

    @Transactional(readOnly = true)
    public Major get(Long id) {
        return majors.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên ngành"));
    }

    public Major create(MajorForm form) {
        String code = normalizeCode(form.getCode());
        String name = form.getName().trim();
        if (majors.existsByCodeIgnoreCase(code)) {
            throw new BusinessRuleException("Mã chuyên ngành đã tồn tại");
        }
        if (majors.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException("Tên chuyên ngành đã tồn tại");
        }
        Major major = new Major();
        apply(major, form, code, name);
        return majors.save(major);
    }

    public Major update(Long id, MajorForm form) {
        Major major = get(id);
        if (!form.isEnabled()) validateCanDisable(major, id);
        String code = normalizeCode(form.getCode());
        String name = form.getName().trim();
        if (majors.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessRuleException("Mã chuyên ngành đã tồn tại");
        }
        if (majors.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessRuleException("Tên chuyên ngành đã tồn tại");
        }
        apply(major, form, code, name);
        return major;
    }

    @Transactional(readOnly = true)
    public MajorForm toForm(Major major) {
        MajorForm form = new MajorForm();
        form.setCode(major.getCode());
        form.setName(major.getName());
        form.setEnabled(major.isEnabled());
        return form;
    }

    public void toggle(Long id) {
        Major major = get(id);
        validateCanDisable(major, id);
        major.setEnabled(!major.isEnabled());
    }

    private void validateCanDisable(Major major, Long id) {
        if (major.isEnabled() && users.existsByMajorId(id)) {
            throw new BusinessRuleException("Không thể khóa chuyên ngành đang có người dùng");
        }
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static void apply(Major major, MajorForm form, String code, String name) {
        major.setCode(code);
        major.setName(name);
        major.setEnabled(form.isEnabled());
    }
}
