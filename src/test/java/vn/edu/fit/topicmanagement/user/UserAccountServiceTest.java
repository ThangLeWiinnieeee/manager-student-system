package vn.edu.fit.topicmanagement.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.fit.topicmanagement.major.MajorRepository;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {
    @Mock UserAccountRepository users;
    @Mock MajorRepository majors;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void cannotDisableCurrentAccountThroughEditForm() {
        UserAccount account = new UserAccount();
        account.setEmail("admin@fit.edu.vn");
        account.setRole(Role.ADMIN);
        when(users.findById(1L)).thenReturn(Optional.of(account));
        UserForm form = new UserForm();
        form.setEmail("changed@fit.edu.vn");
        form.setFullName("Admin");
        form.setRole(Role.ADMIN);
        form.setEnabled(false);

        assertThrows(BusinessRuleException.class,
                () -> new UserAccountService(users, majors, passwordEncoder)
                        .update(1L, form, "ADMIN@fit.edu.vn"));
        assertEquals("admin@fit.edu.vn", account.getEmail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "        ", "ấấấấấấấấấấấấấấấấấấấấấấấấấ"})
    void rejectsInvalidPasswordsWhenCreatingOrResetting(String password) {
        UserAccountService service = new UserAccountService(users, majors, passwordEncoder);
        UserCreateForm create = new UserCreateForm();
        create.setPassword(password);
        create.setEmail("student@fit.edu.vn");
        create.setFullName("Student");
        create.setRole(Role.STUDENT);
        PasswordResetForm reset = new PasswordResetForm();
        reset.setNewPassword(password);
        reset.setConfirmPassword(password);

        assertThrows(BusinessRuleException.class, () -> service.create(create));
        assertThrows(BusinessRuleException.class, () -> service.resetPassword(1L, reset));
        verifyNoInteractions(users, majors, passwordEncoder);
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "        ", "ấấấấấấấấấấấấấấấấấấấấấấấấấ"})
    void rejectsInvalidNewPasswordWhenChangingPassword(String password) {
        UserAccount account = new UserAccount();
        account.setPasswordHash("old-hash");
        when(users.findByEmailIgnoreCase("student@fit.edu.vn")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("old-password");
        form.setNewPassword(password);
        form.setConfirmPassword(password);

        assertThrows(BusinessRuleException.class,
                () -> new UserAccountService(users, majors, passwordEncoder)
                        .changePassword("student@fit.edu.vn", form));
        assertEquals("old-hash", account.getPasswordHash());
    }

    @Test
    void cannotDisableCurrentAccount() {
        UserAccount account = new UserAccount();
        account.setEmail("admin@fit.edu.vn");
        account.setEnabled(true);
        when(users.findById(1L)).thenReturn(Optional.of(account));

        UserAccountService service = new UserAccountService(users, majors, passwordEncoder);

        assertThrows(BusinessRuleException.class, () -> service.toggle(1L, "admin@fit.edu.vn"));
    }

    @Test
    void rejectsWrongCurrentPassword() {
        UserAccount account = new UserAccount();
        account.setEmail("student01@fit.edu.vn");
        account.setPasswordHash("encoded");
        when(users.findByEmailIgnoreCase("student01@fit.edu.vn")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong-password", "encoded")).thenReturn(false);

        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("wrong-password");
        form.setNewPassword("new-password");
        form.setConfirmPassword("new-password");
        UserAccountService service = new UserAccountService(users, majors, passwordEncoder);

        assertThrows(BusinessRuleException.class, () -> service.changePassword("student01@fit.edu.vn", form));
        verifyNoInteractions(majors);
    }

    @Test
    void hashesPasswordWhenCreatingAccount() {
        UserCreateForm form = new UserCreateForm();
        form.setEmail("student01@fit.edu.vn");
        form.setFullName("Nguyễn Văn A");
        form.setPassword("plain-password");
        form.setRole(Role.STUDENT);
        when(passwordEncoder.encode("plain-password")).thenReturn("{bcrypt}encoded");
        when(users.save(org.mockito.ArgumentMatchers.any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount account = new UserAccountService(users, majors, passwordEncoder).create(form);

        assertEquals("{bcrypt}encoded", account.getPasswordHash());
        assertNotEquals("plain-password", account.getPasswordHash());
    }

    @Test
    void adminCannotChangeOwnRole() {
        UserAccount account = new UserAccount();
        account.setEmail("admin@fit.edu.vn");
        account.setRole(Role.ADMIN);
        when(users.findById(1L)).thenReturn(Optional.of(account));
        UserForm form = new UserForm();
        form.setEmail("admin@fit.edu.vn");
        form.setFullName("Quản trị hệ thống");
        form.setRole(Role.DEAN);

        UserAccountService service = new UserAccountService(users, majors, passwordEncoder);

        assertThrows(BusinessRuleException.class,
                () -> service.update(1L, form, "admin@fit.edu.vn"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMIN", "DEAN", "LECTURER"})
    void nonStudentAccountNeverHasMajor(Role role) {
        UserCreateForm form = new UserCreateForm();
        form.setEmail("admin@fit.edu.vn");
        form.setFullName("Quản trị hệ thống");
        form.setPassword("plain-password");
        form.setRole(Role.ADMIN);
        form.setRole(role);
        form.setMajorId(99L);
        when(passwordEncoder.encode("plain-password")).thenReturn("{bcrypt}encoded");
        when(users.save(org.mockito.ArgumentMatchers.any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount account = new UserAccountService(users, majors, passwordEncoder).create(form);

        assertNull(account.getMajor());
        verifyNoInteractions(majors);
    }

    @Test
    void resetPasswordStoresOnlyEncodedValue() {
        UserAccount account = new UserAccount();
        account.setPasswordHash("{bcrypt}old");
        when(users.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("new-password")).thenReturn("{bcrypt}new");
        PasswordResetForm form = new PasswordResetForm();
        form.setNewPassword("new-password");
        form.setConfirmPassword("new-password");

        new UserAccountService(users, majors, passwordEncoder).resetPassword(1L, form);

        assertEquals("{bcrypt}new", account.getPasswordHash());
        assertNotEquals("new-password", account.getPasswordHash());
    }
}
