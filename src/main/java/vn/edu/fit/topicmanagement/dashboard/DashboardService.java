package vn.edu.fit.topicmanagement.dashboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.department.DepartmentRepository;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final UserAccountRepository users;
    private final DepartmentRepository departments;

    public DashboardService(UserAccountRepository users, DepartmentRepository departments) {
        this.users = users;
        this.departments = departments;
    }

    public Summary getSummary() {
        return new Summary(users.count(), departments.count());
    }

    public record Summary(long userCount, long departmentCount) {}
}
