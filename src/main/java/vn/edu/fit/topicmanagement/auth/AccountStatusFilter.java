package vn.edu.fit.topicmanagement.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;

/** Recheck authorization before each request; a disabled/re-enabled account cannot reuse an old session. */
public class AccountStatusFilter extends OncePerRequestFilter {
    private final UserAccountRepository users;

    public AccountStatusFilter(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser principal) {
            var account = principal.getAccountId() == null ? null : users.findById(principal.getAccountId()).orElse(null);
            boolean valid = account != null && account.isEnabled()
                    && account.getAuthVersion() == principal.getAuthVersion()
                    && account.getEmail().equalsIgnoreCase(principal.getUsername())
                    && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + account.getRole().name()));
            if (!valid) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
                response.sendRedirect(request.getContextPath() + "/login?expired");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
