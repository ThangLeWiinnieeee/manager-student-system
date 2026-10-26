package vn.edu.fit.topicmanagement.registrationperiod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RegistrationPeriodTest {
    @Test
    void registrationWindowsHandOverWithoutOverlap() {
        var period = period();
        var transition = period.getGvEnd();

        assertFalse(period.isGvWindowOpen(period.getGvStart().minusNanos(1)));
        assertTrue(period.isGvWindowOpen(period.getGvStart()));
        assertTrue(period.isGvWindowOpen(transition.minusNanos(1)));
        assertFalse(period.isSvWindowOpen(transition.minusNanos(1)));
        assertFalse(period.isGvWindowOpen(transition));
        assertTrue(period.isSvWindowOpen(transition));
        assertFalse(period.isGvWindowOpen(transition.plusNanos(1)));
        assertTrue(period.isSvWindowOpen(transition.plusNanos(1)));
        assertTrue(period.isSvWindowOpen(period.getSvEnd()));
        assertFalse(period.isSvWindowOpen(period.getSvEnd().plusNanos(1)));
    }

    @Test
    void displayedStageMatchesPermissionsAtTransition() {
        var period = period();
        var transition = period.getGvEnd();
        try (var time = mockStatic(LocalDateTime.class)) {
            time.when(LocalDateTime::now).thenReturn(transition);
            assertFalse(period.isGvOpen());
            assertEquals("SV đang đăng ký", period.getStage());

            period.setSvStart(transition.plusHours(1));
            assertEquals("Chờ giai đoạn SV", period.getStage());
            assertFalse(period.isSvWindowOpen(transition));
        }
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(value = RoundType.class, names = {"TLCN", "KLTN"})
    void extendingReviewerDeadlineReopensGradingStageOnly(RoundType type) {
        var period = period();
        period.setType(type);
        var now = period.getSvEnd().plusDays(2);
        period.setGvpbDeadline(now.minusDays(1));
        try (var time = mockStatic(LocalDateTime.class)) {
            time.when(LocalDateTime::now).thenReturn(now);
            assertEquals("Đã hết hạn chấm phản biện", period.getStage());
            assertFalse(period.isReviewerGradingOpen());
            period.setGvpbDeadline(now.plusDays(1));
            assertEquals("Đang chấm phản biện", period.getStage());
            assertTrue(period.isReviewerGradingOpen());
            assertFalse(period.isSvWindowOpen(now));
            assertTrue(period.isEnded());
            time.when(LocalDateTime::now).thenReturn(period.getGvpbDeadline());
            assertTrue(period.isReviewerGradingOpen());
            time.when(LocalDateTime::now).thenReturn(period.getGvpbDeadline().plusNanos(1));
            assertFalse(period.isReviewerGradingOpen());
        }
    }

    private RegistrationPeriod period() {
        var period = new RegistrationPeriod();
        period.setGvStart(LocalDateTime.of(2026, 10, 1, 8, 0));
        period.setGvEnd(LocalDateTime.of(2026, 10, 10, 8, 0));
        period.setSvStart(period.getGvEnd());
        period.setSvEnd(LocalDateTime.of(2026, 10, 20, 17, 0));
        return period;
    }
}
