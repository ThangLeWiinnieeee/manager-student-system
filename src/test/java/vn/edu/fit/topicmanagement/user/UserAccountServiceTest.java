package vn.edu.fit.topicmanagement.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.fit.topicmanagement.department.DepartmentRepository;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {
    @Mock UserAccountRepository users;
    @Mock DepartmentRepository departments;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void cannotDisableCurrentAccount() {
        UserAccount account = new UserAccount();
        account.setUsername("admin");
        account.setEnabled(true);
        when(users.findById(1L)).thenReturn(Optional.of(account));

        UserAccountService service = new UserAccountService(users, departments, passwordEncoder);

        assertThrows(IllegalArgumentException.class, () -> service.toggle(1L, "admin"));
    }

    @Test
    void rejectsWrongCurrentPassword() {
        UserAccount account = new UserAccount();
        account.setUsername("student01");
        account.setPasswordHash("encoded");
        when(users.findByUsernameIgnoreCase("student01")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong-password", "encoded")).thenReturn(false);

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("wrong-password");
        form.setNewPassword("new-password");
        form.setConfirmPassword("new-password");
        UserAccountService service = new UserAccountService(users, departments, passwordEncoder);

        assertThrows(IllegalArgumentException.class, () -> service.changePassword("student01", form));
        verifyNoInteractions(departments);
    }
}
