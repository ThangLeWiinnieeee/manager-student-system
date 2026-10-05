package vn.edu.fit.topicmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;

@Configuration
public class BootstrapData {

    @Bean
    CommandLineRunner createInitialAdmin(
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin-username}") String username,
            @Value("${app.bootstrap.admin-password}") String password) {
        return args -> {
            if (password.isBlank() || users.existsByUsernameIgnoreCase(username)) {
                return;
            }
            if (password.length() < 8 || password.length() > 72) {
                throw new IllegalStateException("ADMIN_PASSWORD phải từ 8 đến 72 ký tự");
            }
            UserAccount admin = new UserAccount();
            admin.setUsername(username.trim().toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setFullName("Quản trị hệ thống");
            admin.setEmail(username.trim().toLowerCase() + "@local.invalid");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            users.save(admin);
        };
    }
}
