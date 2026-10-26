package vn.edu.fit.topicmanagement.notification;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.edu.fit.topicmanagement.registrationperiod.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:notice_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;LOCK_TIMEOUT=10000;INIT=CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE")
class RegistrationNoticePersistenceTest {
    @Autowired RegistrationNoticeService service;
    @Autowired RegistrationPeriodRepository periods;
    @Autowired NotificationRepository notifications;

    @BeforeEach
    void clean() {
        notifications.deleteAll();
        periods.deleteAll();
    }

    @Test
    void publishesEachPhaseOnceAndKeepsHistoryAfterClosing() {
        var period = openLecturerPeriod();
        service.publishOpenPeriods();
        service.publishOpenPeriods();
        assertEquals(1, notifications.count());
        var first = notifications.findAll().getFirst();
        assertEquals(NotificationTarget.LECTURER, first.getTargetRole());
        assertTrue(first.isAutomatic());
        assertNull(first.getCreatedBy());
        assertEquals("Hệ thống", first.getCreatorName());
        period = periods.findById(period.getId()).orElseThrow();
        assertTrue(period.isGvNoticeSent());
        assertFalse(period.isSvNoticeSent());

        period.setGvStart(LocalDateTime.now().minusDays(3));
        period.setGvEnd(LocalDateTime.now().minusDays(2));
        period.setSvStart(LocalDateTime.now().minusDays(1));
        periods.save(period);
        service.publishOpenPeriods();
        assertEquals(2, notifications.count());
        assertEquals(1, notifications.findAll().stream().filter(n -> n.getTargetRole() == NotificationTarget.STUDENT).count());
        period = periods.findById(period.getId()).orElseThrow();
        assertTrue(period.isSvNoticeSent());
        period.setSvEnd(LocalDateTime.now().minusHours(1));
        periods.save(period);
        service.publishOpenPeriods();
        assertEquals(2, notifications.count());

        // Deleting a notice intentionally must not make the scheduler recreate it.
        notifications.deleteAll();
        period.setSvEnd(LocalDateTime.now().plusDays(1));
        periods.save(period);
        service.publishOpenPeriods();
        assertEquals(0, notifications.count());
    }

    @Test
    void futureAndExpiredPhasesDoNotGenerateOpeningNotices() {
        var period = openLecturerPeriod();
        period.setGvStart(LocalDateTime.now().plusHours(1));
        periods.save(period);
        service.publishOpenPeriods();
        assertEquals(0, notifications.count());
        period.setGvStart(LocalDateTime.now().minusDays(4));
        period.setGvEnd(LocalDateTime.now().minusDays(3));
        period.setSvStart(LocalDateTime.now().minusDays(2));
        period.setSvEnd(LocalDateTime.now().minusDays(1));
        periods.save(period);
        service.publishOpenPeriods();
        assertEquals(0, notifications.count());
    }

    @Test
    void concurrentPublishersCreateOnlyOneNotice() throws Exception {
        openLecturerPeriod();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> service.publishOpenPeriods());
            var second = executor.submit(() -> service.publishOpenPeriods());
            first.get(15, TimeUnit.SECONDS);
            second.get(15, TimeUnit.SECONDS);
        }
        assertEquals(1, notifications.count());
        assertTrue(periods.findAll().getFirst().isGvNoticeSent());
    }

    private RegistrationPeriod openLecturerPeriod() {
        var period = new RegistrationPeriod();
        period.setName("Đợt kiểm thử thông báo");
        period.setType(RoundType.MON_HOC);
        period.setGvStart(LocalDateTime.now().minusDays(1));
        period.setGvEnd(LocalDateTime.now().plusDays(1));
        period.setSvStart(period.getGvEnd());
        period.setSvEnd(LocalDateTime.now().plusDays(2));
        return periods.save(period);
    }
}
