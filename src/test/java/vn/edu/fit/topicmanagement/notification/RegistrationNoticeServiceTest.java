package vn.edu.fit.topicmanagement.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodRepository;
import vn.edu.fit.topicmanagement.user.Role;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

class RegistrationNoticeServiceTest {
    private final RegistrationPeriodRepository periods = mock(RegistrationPeriodRepository.class);
    private final UserAccountService users = mock(UserAccountService.class);
    private final NotificationRepository notifications = mock(NotificationRepository.class);
    private final RegistrationNoticeService service = new RegistrationNoticeService(periods, users, notifications);

    @ParameterizedTest
    @EnumSource(Role.class)
    void noticesFollowRoleAndRegistrationBoundaries(Role role) {
        var period = period();
        var user = user(role);
        when(users.getByEmail("user@test")).thenReturn(user);
        when(periods.findAllByOrderByGvStartDesc()).thenReturn(List.of(period));
        var times = List.of(period.getGvStart().minusSeconds(1), period.getGvStart(),
                period.getGvEnd().minusSeconds(1), period.getGvEnd(), period.getSvEnd(),
                period.getSvEnd().plusSeconds(1));
        try (var clock = mockStatic(LocalDateTime.class)) {
            for (int i = 0; i < times.size(); i++) {
                clock.when(LocalDateTime::now).thenReturn(times.get(i));
                boolean expected = (role == Role.LECTURER || role == Role.DEAN) && (i == 1 || i == 2)
                        || role == Role.STUDENT && (i == 3 || i == 4);
                assertEquals(expected ? List.of(period) : List.of(), service.findOpenForUser("user@test"));
            }
        }
    }

    @Test
    void editedSchedulesAndRepeatedReadsDoNotLeaveStaleOrDuplicateNotices() {
        var period = period();
        var now = period.getGvStart();
        when(users.getByEmail("user@test")).thenReturn(user(Role.LECTURER));
        when(periods.findAllByOrderByGvStartDesc()).thenReturn(List.of(period));
        try (var clock = mockStatic(LocalDateTime.class)) {
            clock.when(LocalDateTime::now).thenReturn(now);
            assertEquals(List.of(period), service.findOpenForUser("user@test"));
            assertEquals(List.of(period), service.findOpenForUser("user@test"));
            period.setGvStart(now.plusDays(1));
            assertTrue(service.findOpenForUser("user@test").isEmpty());
            when(periods.findAllByOrderByGvStartDesc()).thenReturn(List.of());
            assertTrue(service.findOpenForUser("user@test").isEmpty());
        }
        verify(periods, never()).save(any());
    }

    @Test
    void studentsDoNotReceiveEarlyNoticesForLegacyOverlappingSchedules() {
        var period = period();
        period.setSvStart(period.getGvStart());
        var now = period.getGvStart();
        when(users.getByEmail("user@test")).thenReturn(user(Role.STUDENT));
        when(periods.findAllByOrderByGvStartDesc()).thenReturn(List.of(period));
        try (var clock = mockStatic(LocalDateTime.class)) {
            clock.when(LocalDateTime::now).thenReturn(now);
            assertTrue(service.findOpenForUser("user@test").isEmpty());
        }
    }

    private UserAccount user(Role role) {
        var user = new UserAccount();
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }

    private RegistrationPeriod period() {
        var period = new RegistrationPeriod();
        period.setGvStart(LocalDateTime.of(2026, 10, 1, 8, 0));
        period.setGvEnd(period.getGvStart().plusDays(1));
        period.setSvStart(period.getGvEnd());
        period.setSvEnd(period.getSvStart().plusDays(1));
        return period;
    }
}
