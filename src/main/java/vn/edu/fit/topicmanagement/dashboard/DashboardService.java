package vn.edu.fit.topicmanagement.dashboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.major.MajorRepository;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final UserAccountRepository users;
    private final MajorRepository majors;

    public DashboardService(UserAccountRepository users, MajorRepository majors) {
        this.users = users;
        this.majors = majors;
    }

    public Summary getSummary() {
        return new Summary(users.count(), majors.count());
    }

    public record Summary(long userCount, long majorCount) {}
}
