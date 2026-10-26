package vn.edu.fit.topicmanagement.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notifications;
    @Mock UserAccountService users;

    private NotificationService service;
    private UserAccount admin;
    private UserAccount student;

    @BeforeEach
    void setUp() {
        service = new NotificationService(notifications, users);
        admin = new UserAccount();
        admin.setEmail("admin@fit.edu.vn");
        admin.setRole(Role.ADMIN);

        student = new UserAccount();
        student.setEmail("student@fit.edu.vn");
        student.setRole(Role.STUDENT);
    }

    @Test
    void createNotification_success() {
        when(users.getByEmail("admin@fit.edu.vn")).thenReturn(admin);
        when(notifications.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationForm form = new NotificationForm();
        form.setTitle("Thông báo nộp khóa luận");
        form.setContent("Hạn chót ngày 30/11");
        form.setTargetRole(NotificationTarget.STUDENT);
        form.setPinned(true);
        form.setPublished(true);

        Notification result = service.create(form, "admin@fit.edu.vn");
        assertNotNull(result);
        assertEquals("Thông báo nộp khóa luận", result.getTitle());
        assertTrue(result.isPinned());
        assertTrue(result.isPublished());
        assertEquals(admin, result.getCreatedBy());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(Role.class)
    void automaticNoticeDetailsEnforceRecipients(Role role) {
        var user = new UserAccount();
        user.setRole(role);
        when(users.getByEmail("reader@test")).thenReturn(user);
        var notice = new Notification();
        notice.setAutomatic(true);
        when(notifications.findById(1L)).thenReturn(Optional.of(notice));
        for (var target : List.of(NotificationTarget.LECTURER, NotificationTarget.STUDENT)) {
            notice.setTargetRole(target);
            boolean allowed = target == NotificationTarget.LECTURER
                    ? role == Role.LECTURER || role == Role.DEAN : role == Role.STUDENT;
            if (allowed) assertSame(notice, service.getVisible(1L, "reader@test"));
            else assertThrows(vn.edu.fit.topicmanagement.common.exception.BusinessRuleException.class,
                    () -> service.getVisible(1L, "reader@test"));
        }
    }

    @Test
    void togglePublish_success() {
        Notification n = new Notification();
        n.setPublished(true);
        when(notifications.findById(1L)).thenReturn(Optional.of(n));

        service.togglePublish(1L);
        assertFalse(n.isPublished());

        service.togglePublish(1L);
        assertTrue(n.isPublished());
    }

    @Test
    void findVisibleForStudent_callsFindPublishedForRole() {
        when(users.getByEmail("student@fit.edu.vn")).thenReturn(student);
        when(notifications.findPublishedForRole(NotificationTarget.STUDENT)).thenReturn(List.of(new Notification()));

        List<Notification> list = service.findVisibleForUser("student@fit.edu.vn");
        assertEquals(1, list.size());
        verify(notifications).findPublishedForRole(NotificationTarget.STUDENT);
    }
}
