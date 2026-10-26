package vn.edu.fit.topicmanagement.notification;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodRepository;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
@Transactional(readOnly = true)
public class RegistrationNoticeService {
    private final RegistrationPeriodRepository periods;
    private final UserAccountService users;
    private final NotificationRepository notifications;

    public RegistrationNoticeService(RegistrationPeriodRepository periods, UserAccountService users,
            NotificationRepository notifications) {
        this.periods = periods;
        this.users = users;
        this.notifications = notifications;
    }

    @Transactional
    public void publishOpenPeriods() {
        var now = LocalDateTime.now();
        for (Long id : periods.findIdsAwaitingNotice(now)) {
            // Lock the schedule so concurrent page requests and scheduler runs publish only once.
            var period = periods.findLockedById(id).orElse(null);
            if (period == null) continue;
            if (!period.isGvNoticeSent() && period.isGvWindowOpen(now)) {
                publish(period, NotificationTarget.LECTURER);
                period.setGvNoticeSent(true);
            }
            if (!period.isSvNoticeSent() && period.isSvWindowOpen(now) && !now.isBefore(period.getGvEnd())) {
                publish(period, NotificationTarget.STUDENT);
                period.setSvNoticeSent(true);
            }
        }
    }

    private void publish(RegistrationPeriod period, NotificationTarget target) {
        boolean student = target == NotificationTarget.STUDENT;
        var notice = new Notification();
        notice.setAutomatic(true);
        notice.setTargetRole(target);
        notice.setTitle((student ? "Mở đăng ký sinh viên: " : "Mở đăng ký giảng viên: ")
                + period.getType().getDisplayName() + " (#" + period.getId() + ")");
        notice.setContent("Đợt đăng ký: " + period.getName() + ".\n"
                + (student ? "Sinh viên có thể tham gia lớp, tạo hoặc tham gia nhóm và đăng ký đề tài.\n"
                        : "Giảng viên có thể đăng ký môn/lớp và đề tài. Trưởng khoa theo dõi và quản lý đợt đăng ký.\n")
                + "Thời gian: từ " + (student ? period.getSvStartText() : period.getGvStartText())
                + (student ? " đến " : " đến trước ")
                + (student ? period.getSvEndText() : period.getGvEndText()) + ".\n"
                + "Vui lòng xem lịch đợt đăng ký hiện tại nếu có thay đổi thời gian.");
        notifications.save(notice);
    }

    public List<RegistrationPeriod> findOpenForUser(String email) {
        var user = users.getByEmail(email);
        var role = user.getRole();
        if (!user.isEnabled() || (role != Role.LECTURER && role != Role.DEAN && role != Role.STUDENT)) {
            return List.of();
        }
        var now = LocalDateTime.now();
        return periods.findAllByOrderByGvStartDesc().stream()
                .filter(period -> role == Role.STUDENT
                        ? period.isSvWindowOpen(now) && !now.isBefore(period.getGvEnd())
                        : period.isGvWindowOpen(now))
                .toList();
    }
}
