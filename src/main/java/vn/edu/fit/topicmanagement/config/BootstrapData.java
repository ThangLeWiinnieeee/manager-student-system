package vn.edu.fit.topicmanagement.config;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.fit.topicmanagement.user.PasswordPolicy;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;

@Configuration
public class BootstrapData {

    @Bean
    CommandLineRunner createInitialAdmin(
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin-email}") String email,
            @Value("${app.bootstrap.admin-password}") String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return args -> {
            if (password.isBlank() || users.existsByEmailIgnoreCase(normalizedEmail)) {
                return;
            }
            PasswordPolicy.validate(password);
            UserAccount admin = new UserAccount();
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setFullName("Quản trị hệ thống");
            admin.setEmail(normalizedEmail);
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            users.save(admin);
        };
    }
}
