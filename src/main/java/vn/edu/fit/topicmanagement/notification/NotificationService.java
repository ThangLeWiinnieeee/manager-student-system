package vn.edu.fit.topicmanagement.notification;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notifications;
    private final UserAccountService users;

    public NotificationService(NotificationRepository notifications, UserAccountService users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Notification> findVisibleForUser(String email) {
        UserAccount user = users.getByEmail(email);
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.DEAN) {
            return notifications.findAllPublished().stream()
                    .filter(notification -> notification.isAutomaticRecipient(user.getRole())).toList();
        } else if (user.getRole() == Role.LECTURER) {
            return notifications.findPublishedForRole(NotificationTarget.LECTURER);
        } else {
            return notifications.findPublishedForRole(NotificationTarget.STUDENT);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    @Transactional(readOnly = true)
    public List<Notification> findAllForAdmin() {
        return notifications.findAllByOrderByPinnedDescCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Notification get(Long id) {
        return notifications.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
    }

    @Transactional(readOnly = true)
    public Notification getVisible(Long id, String email) {
        Notification notification = get(id);
        UserAccount user = users.getByEmail(email);
        if (!notification.isAutomaticRecipient(user.getRole())) {
            throw new BusinessRuleException("Bạn không có quyền xem thông báo này");
        }
        if (!notification.isPublished() && user.getRole() != Role.ADMIN && user.getRole() != Role.DEAN) {
            throw new ResourceNotFoundException("Thông báo không tồn tại hoặc đã bị ẩn");
        }
        if (notification.getTargetRole() == NotificationTarget.LECTURER && user.getRole() == Role.STUDENT) {
            throw new BusinessRuleException("Bạn không có quyền xem thông báo này");
        }
        if (notification.getTargetRole() == NotificationTarget.STUDENT && user.getRole() == Role.LECTURER) {
            throw new BusinessRuleException("Bạn không có quyền xem thông báo này");
        }
        return notification;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public Notification create(NotificationForm form, String creatorEmail) {
        UserAccount creator = users.getByEmail(creatorEmail);
        Notification notification = new Notification();
        notification.setCreatedBy(creator);
        apply(notification, form);
        return notifications.save(notification);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public Notification update(Long id, NotificationForm form) {
        Notification notification = get(id);
        apply(notification, form);
        return notifications.save(notification);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void togglePublish(Long id) {
        Notification notification = get(id);
        notification.setPublished(!notification.isPublished());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void togglePin(Long id) {
        Notification notification = get(id);
        notification.setPinned(!notification.isPinned());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void delete(Long id) {
        Notification notification = get(id);
        notifications.delete(notification);
    }

    @Transactional(readOnly = true)
    public NotificationForm toForm(Notification notification) {
        NotificationForm form = new NotificationForm();
        form.setTitle(notification.getTitle());
        form.setContent(notification.getContent());
        form.setTargetRole(notification.getTargetRole());
        form.setPinned(notification.isPinned());
        form.setPublished(notification.isPublished());
        return form;
    }

    private void apply(Notification notification, NotificationForm form) {
        if (notification.isAutomatic() && notification.getTargetRole() != form.getTargetRole()) {
            throw new BusinessRuleException("Không thể đổi đối tượng nhận thông báo mở đợt tự động");
        }
        notification.setTitle(form.getTitle().trim());
        notification.setContent(form.getContent().trim());
        notification.setTargetRole(form.getTargetRole() == null ? NotificationTarget.ALL : form.getTargetRole());
        notification.setPinned(form.isPinned());
        notification.setPublished(form.isPublished());
    }
}
