package vn.edu.fit.topicmanagement.user;

import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.department.Department;
import vn.edu.fit.topicmanagement.department.DepartmentRepository;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

@Service
@Transactional
public class UserAccountService {
    private final UserAccountRepository users;
    private final DepartmentRepository departments;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
            UserAccountRepository users,
            DepartmentRepository departments,
            PasswordEncoder passwordEncoder) {
        this.users = users;
        this.departments = departments;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findAll() {
        return users.findAllByOrderByFullNameAsc();
    }

    @Transactional(readOnly = true)
    public UserAccount get(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
    }

    @Transactional(readOnly = true)
    public UserAccount getByEmail(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
    }

    public UserAccount create(UserCreateForm form) {
        PasswordPolicy.validate(form.getPassword());
        String email = normalizeEmail(form.getEmail());
        validateUniqueEmail(email, null);

        UserAccount account = new UserAccount();
        account.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        apply(account, form, email);
        return users.save(account);
    }

    public UserAccount update(Long id, UserForm form, String currentEmail) {
        UserAccount account = lock(id);
        if (!form.isEnabled()) validateNotCurrentAccount(account, currentEmail);
        if (account.getRole() == Role.ADMIN
                && account.getEmail().equalsIgnoreCase(currentEmail)
                && form.getRole() != Role.ADMIN) {
            throw new BusinessRuleException("Quản trị viên không thể tự thay đổi vai trò của mình");
        }
        String email = normalizeEmail(form.getEmail());
        validateUniqueEmail(email, id);
        if (account.getRole() != form.getRole() || (account.isEnabled() && !form.isEnabled()))
            assertNoOpenResponsibilities(account);
        if (account.getRole() != form.getRole() || account.isEnabled() != form.isEnabled()
                || !account.getEmail().equalsIgnoreCase(email))
            account.invalidateSessions();
        apply(account, form, email);
        return account;
    }

    public void resetPassword(Long id, PasswordResetForm form) {
        form.validate();
        get(id).setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
    }

    public void toggle(Long id, String currentEmail) {
        UserAccount account = lock(id);
        validateNotCurrentAccount(account, currentEmail);
        if (account.isEnabled()) assertNoOpenResponsibilities(account);
        account.invalidateSessions();
        account.setEnabled(!account.isEnabled());
    }

    public void changePassword(String email, ChangePasswordForm form) {
        UserAccount account = getByEmail(email);
        if (!passwordEncoder.matches(form.getCurrentPassword(), account.getPasswordHash())) {
            throw new BusinessRuleException("Mật khẩu hiện tại không chính xác");
        }
        form.validate();
        if (passwordEncoder.matches(form.getNewPassword(), account.getPasswordHash())) {
            throw new BusinessRuleException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        account.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
    }

    public UserForm toForm(UserAccount account) {
        UserForm form = new UserForm();
        form.setFullName(account.getFullName());
        form.setEmail(account.getEmail());
        form.setRole(account.getRole());
        form.setEnabled(account.isEnabled());
        if (account.getDepartment() != null) {
            form.setDepartmentId(account.getDepartment().getId());
        }
        return form;
    }

    private UserAccount lock(Long id) {
        return users.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
    }

    private void assertNoOpenResponsibilities(UserAccount account) {
        if (users.hasOpenResponsibilities(account.getId(), java.time.LocalDateTime.now()))
            throw new BusinessRuleException("Tài khoản còn phụ trách lớp đang mở hoặc tham gia nhóm chưa chốt kết quả. "
                    + "Cần hoàn tất nhiệm vụ trước khi khóa / đổi vai trò.");
    }

    private void validateUniqueEmail(String email, Long currentId) {
        boolean duplicateEmail = currentId == null
                ? users.existsByEmailIgnoreCase(email)
                : users.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (duplicateEmail) {
            throw new BusinessRuleException("Email đã tồn tại");
        }
    }

    private void apply(UserAccount account, UserForm form, String email) {
        account.setFullName(form.getFullName().trim());
        account.setEmail(email);
        account.setRole(form.getRole());
        account.setEnabled(form.isEnabled());
        account.setDepartment(form.getRole() == Role.STUDENT ? resolveDepartment(form.getDepartmentId()) : null);
    }

    private Department resolveDepartment(Long id) {
        if (id == null) {
            return null;
        }
        Department department = departments.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bộ môn không tồn tại"));
        if (!department.isEnabled()) {
            throw new BusinessRuleException("Bộ môn đã bị khóa");
        }
        return department;
    }

    private static void validateNotCurrentAccount(UserAccount account, String currentEmail) {
        if (account.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new BusinessRuleException("Không thể khóa tài khoản đang đăng nhập");
        }
    }

    private static String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
