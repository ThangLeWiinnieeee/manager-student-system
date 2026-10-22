package vn.edu.fit.topicmanagement.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class BootstrapDataTest {
    @Mock UserAccountRepository users;
    @Mock PasswordEncoder encoder;

    @Test
    void normalizesEmailBeforeCheckingForExistingAdmin() throws Exception {
        when(users.existsByEmailIgnoreCase("admin@example.test")).thenReturn(true);

        new BootstrapData().createInitialAdmin(users, encoder, " ADMIN@example.test ", "Password123!").run();

        verify(users).existsByEmailIgnoreCase("admin@example.test");
        verifyNoInteractions(encoder);
    }

    @Test
    void rejectsAdminPasswordAboveBcryptByteLimit() {
        var runner = new BootstrapData().createInitialAdmin(users, encoder, "admin@example.test", "ấ".repeat(25));

        assertThrows(BusinessRuleException.class, runner::run);
        verifyNoInteractions(encoder);
    }
}
