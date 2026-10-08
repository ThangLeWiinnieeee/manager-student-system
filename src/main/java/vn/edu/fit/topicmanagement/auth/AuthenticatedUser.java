package vn.edu.fit.topicmanagement.auth;

import java.io.Serial;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import vn.edu.fit.topicmanagement.user.UserAccount;

public final class AuthenticatedUser extends User {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String fullName;

    public AuthenticatedUser(UserAccount account) {
        super(
                account.getEmail(),
                account.getPasswordHash(),
                account.isEnabled(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));
        this.fullName = account.getFullName();
    }

    public String getFullName() {
        return fullName;
    }
}
