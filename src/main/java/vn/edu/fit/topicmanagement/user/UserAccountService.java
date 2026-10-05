package vn.edu.fit.topicmanagement.user;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.department.Department;
import vn.edu.fit.topicmanagement.department.DepartmentRepository;

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
        return users.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public UserAccount get(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
    }

    @Transactional(readOnly = true)
    public UserAccount getByUsername(String username) {
        return users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
    }

    public UserAccount create(UserForm form) {
        validatePassword(form.getPassword());
        String username = normalizeUsername(form.getUsername());
        String email = normalizeEmail(form.getEmail());
        validateUnique(username, email, null);

        UserAccount account = new UserAccount();
        account.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        apply(account, form, username, email);
        return users.save(account);
    }

    public UserAccount update(Long id, UserForm form) {
        UserAccount account = get(id);
        String username = normalizeUsername(form.getUsername());
        String email = normalizeEmail(form.getEmail());
        validateUnique(username, email, id);
        apply(account, form, username, email);
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            validatePassword(form.getPassword());
            account.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        return account;
    }

    public void toggle(Long id, String currentUsername) {
        UserAccount account = get(id);
        if (account.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new IllegalArgumentException("Không thể khóa tài khoản đang đăng nhập");
        }
        account.setEnabled(!account.isEnabled());
    }

    public void changePassword(String username, ChangePasswordForm form) {
        UserAccount account = getByUsername(username);
        if (!passwordEncoder.matches(form.getCurrentPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp");
        }
        if (passwordEncoder.matches(form.getNewPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        account.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
    }

    public UserForm toForm(UserAccount account) {
        UserForm form = new UserForm();
        form.setUsername(account.getUsername());
        form.setFullName(account.getFullName());
        form.setEmail(account.getEmail());
        form.setRole(account.getRole());
        form.setEnabled(account.isEnabled());
        if (account.getDepartment() != null) {
            form.setDepartmentId(account.getDepartment().getId());
        }
        return form;
    }

    private void validateUnique(String username, String email, Long currentId) {
        boolean duplicateUsername = currentId == null
                ? users.existsByUsernameIgnoreCase(username)
                : users.existsByUsernameIgnoreCaseAndIdNot(username, currentId);
        boolean duplicateEmail = currentId == null
                ? users.existsByEmailIgnoreCase(email)
                : users.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (duplicateUsername) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (duplicateEmail) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
    }

    private void apply(UserAccount account, UserForm form, String username, String email) {
        account.setUsername(username);
        account.setFullName(form.getFullName().trim());
        account.setEmail(email);
        account.setRole(form.getRole());
        account.setEnabled(form.isEnabled());
        account.setDepartment(resolveDepartment(form.getDepartmentId()));
    }

    private Department resolveDepartment(Long id) {
        if (id == null) {
            return null;
        }
        Department department = departments.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bộ môn không tồn tại"));
        if (!department.isEnabled()) {
            throw new IllegalArgumentException("Bộ môn đã bị khóa");
        }
        return department;
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("Mật khẩu phải từ 8 đến 72 ký tự");
        }
    }

    private static String normalizeUsername(String value) {
        return value.trim().toLowerCase();
    }

    private static String normalizeEmail(String value) {
        return value.trim().toLowerCase();
    }
}
