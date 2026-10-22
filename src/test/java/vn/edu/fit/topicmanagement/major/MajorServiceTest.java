package vn.edu.fit.topicmanagement.major;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class MajorServiceTest {
    @Mock MajorRepository majors;
    @Mock UserAccountRepository users;

    @Test
    void cannotDisableOccupiedMajorThroughEitherEndpoint() {
        Major major = new Major();
        when(majors.findById(1L)).thenReturn(Optional.of(major));
        when(users.existsByMajorId(1L)).thenReturn(true);
        MajorService service = new MajorService(majors, users);
        MajorForm form = form(false);

        assertThrows(BusinessRuleException.class, () -> service.toggle(1L));
        assertThrows(BusinessRuleException.class, () -> service.update(1L, form));
        assertTrue(major.isEnabled());
    }

    @Test
    void canDisableEmptyMajorAndEnableItAgain() {
        Major major = new Major();
        when(majors.findById(1L)).thenReturn(Optional.of(major));
        MajorService service = new MajorService(majors, users);

        service.update(1L, form(false));
        assertFalse(major.isEnabled());
        service.toggle(1L);
        assertTrue(major.isEnabled());
    }

    private static MajorForm form(boolean enabled) {
        MajorForm form = new MajorForm();
        form.setCode("CNPM");
        form.setName("Công nghệ phần mềm");
        form.setEnabled(enabled);
        return form;
    }
}
