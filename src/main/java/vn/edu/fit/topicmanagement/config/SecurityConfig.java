
package vn.edu.fit.topicmanagement.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import vn.edu.fit.topicmanagement.user.UserAccountRepository;
import vn.edu.fit.topicmanagement.auth.AuthenticatedUser;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            UserAccountRepository users) {

        return email -> users
                .findByEmailIgnoreCase(email.trim())
                .map(AuthenticatedUser::new)
                .orElseThrow(() ->
                        new org.springframework.security.core.userdetails
                                .UsernameNotFoundException(
                                "Không tìm thấy tài khoản"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        return http

                .authorizeHttpRequests(authorize -> authorize

                        // Cho phep render JSP va trang loi
                        .dispatcherTypeMatchers(
                                DispatcherType.FORWARD,
                                DispatcherType.ERROR
                        ).permitAll()

                        // Tai nguyen cong khai
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/error"
                        ).permitAll()

                        // TV3:
                        // ADMIN, DEAN, LECTURER
                        // duoc quan ly nhom sinh vien
                        //
                        // PHAI nam TRUOC /admin/**
                        .requestMatchers(
                                "/admin/student-groups",
                                "/admin/student-groups/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "DEAN",
                                "LECTURER"
                        )

                        // Cac chuc nang admin khac:
                        // giu nguyen quyen ADMIN va DEAN
                        .requestMatchers("/admin/**")
                        .hasAnyRole(
                                "ADMIN",
                                "DEAN"
                        )

                        // TV4:
                        .requestMatchers("/dean/**")
                        .hasAnyRole("DEAN", "ADMIN")

                        .requestMatchers("/lecturer/**")
                        .hasRole("LECTURER")

                        .requestMatchers("/student/**")
                        .hasRole("STUDENT")

                        // Cac URL con lai yeu cau dang nhap.
                        // Phan quyen chi tiet duoc kiem tra
                        // bang @PreAuthorize tai Controller/Service
                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form

                        .loginPage("/login")

                        .usernameParameter("email")

                        .defaultSuccessUrl("/", true)

                        .failureUrl("/login?error")

                        .permitAll()
                )

                .logout(logout -> logout

                        .logoutSuccessUrl("/login?logout")

                        .invalidateHttpSession(true)

                        .deleteCookies("JSESSIONID")
                )

                .build();
    }
}
