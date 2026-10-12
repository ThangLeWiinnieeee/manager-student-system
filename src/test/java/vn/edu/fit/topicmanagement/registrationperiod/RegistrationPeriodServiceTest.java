package vn.edu.fit.topicmanagement.registrationperiod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class RegistrationPeriodServiceTest {
    @Mock RegistrationPeriodRepository periods;

    private RegistrationPeriodForm validKltnForm() {
        RegistrationPeriodForm form = new RegistrationPeriodForm();
        form.setName("Đợt đăng ký KLTN HK1 2026-2027");
        form.setType(RoundType.KLTN);
        form.setGvStart(LocalDateTime.of(2026, 10, 1, 8, 0));
        form.setGvEnd(LocalDateTime.of(2026, 10, 31, 17, 0));
        form.setSvStart(LocalDateTime.of(2026, 11, 1, 8, 0));
        form.setSvEnd(LocalDateTime.of(2026, 11, 20, 17, 0));
        form.setGvpbDeadline(LocalDateTime.of(2026, 12, 15, 17, 0));
        form.setCouncilReportDate(LocalDateTime.of(2027, 1, 10, 8, 0));
        return form;
    }

    @Test
    void acceptsValidKltnPeriod() {
        when(periods.save(any(RegistrationPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        RegistrationPeriodService service = new RegistrationPeriodService(periods);

        RegistrationPeriod period = service.create(validKltnForm());

        assertEquals(RoundType.KLTN, period.getType());
        verify(periods).save(any(RegistrationPeriod.class));
    }

    @Test
    void kltnRequiresCouncilReportDate() {
        RegistrationPeriodForm form = validKltnForm();
        form.setCouncilReportDate(null);
        RegistrationPeriodService service = new RegistrationPeriodService(periods);

        assertThrows(BusinessRuleException.class, () -> service.create(form));
    }

    @Test
    void monHocMustNotHaveCouncilReportDate() {
        RegistrationPeriodForm form = validKltnForm();
        form.setType(RoundType.MON_HOC);
        form.setGvpbDeadline(null);
        RegistrationPeriodService service = new RegistrationPeriodService(periods);

        assertThrows(BusinessRuleException.class, () -> service.create(form));
    }

    @Test
    void nckhMustNotHaveCouncilReportDate() {
        RegistrationPeriodForm form = validKltnForm();
        form.setType(RoundType.NCKH);
        form.setGvpbDeadline(null);
        RegistrationPeriodService service = new RegistrationPeriodService(periods);

        assertThrows(BusinessRuleException.class, () -> service.create(form));
    }

    @Test
    void monHocMustNotHaveGvpbDeadline() {
        RegistrationPeriodForm form = validKltnForm();
        form.setType(RoundType.MON_HOC);
        form.setCouncilReportDate(null);
        RegistrationPeriodService service = new RegistrationPeriodService(periods);

        assertThrows(BusinessRuleException.class, () -> service.create(form));
    }

    @Test
    void svWindowMustStartAfterGvWindowEnds() {
        RegistrationPeriodForm form = validKltnForm();
        form.setSvStart(LocalDateTime.of(2026, 10, 15, 8, 0));
        RegistrationPeriodService service = new RegistrationPeriodService(periods);

        assertThrows(BusinessRuleException.class, () -> service.create(form));
    }

    @Test
    void gvWindowMustHaveStartBeforeEnd() {
        RegistrationPeriodForm form = validKltnForm();
        form.setGvStart(LocalDateTime.of(2026, 11, 5, 8, 0));
        RegistrationPeriodService service = new RegistrationPeriodService(periods);

        assertThrows(BusinessRuleException.class, () -> service.create(form));
    }

    @Test
    void gvpbDeadlineMustBeAfterSvWindowEnds() {
        RegistrationPeriodForm form = validKltnForm();
        form.setGvpbDeadline(LocalDateTime.of(2026, 11, 10, 17, 0));
        RegistrationPeriodService service = new RegistrationPeriodService(periods);

        assertThrows(BusinessRuleException.class, () -> service.create(form));
    }

    @Test
    void councilDateMustBeAfterGvpbDeadline() {
        RegistrationPeriodForm form = validKltnForm();
        form.setCouncilReportDate(LocalDateTime.of(2026, 12, 1, 8, 0));
        RegistrationPeriodService service = new RegistrationPeriodService(periods);

        assertThrows(BusinessRuleException.class, () -> service.create(form));
    }
}