package vn.edu.fit.topicmanagement.department;

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
class DepartmentServiceTest {
    @Mock DepartmentRepository departments;
    @Mock UserAccountRepository users;

    @Test
    void cannotDisableOccupiedDepartmentThroughEitherEndpoint() {
        Department department = new Department();
        when(departments.findById(1L)).thenReturn(Optional.of(department));
        when(users.existsByDepartmentId(1L)).thenReturn(true);
        DepartmentService service = new DepartmentService(departments, users);
        DepartmentForm form = form(false);

        assertThrows(BusinessRuleException.class, () -> service.toggle(1L));
        assertThrows(BusinessRuleException.class, () -> service.update(1L, form));
        assertTrue(department.isEnabled());
    }

    @Test
    void canDisableEmptyDepartmentAndEnableItAgain() {
        Department department = new Department();
        when(departments.findById(1L)).thenReturn(Optional.of(department));
        DepartmentService service = new DepartmentService(departments, users);

        service.update(1L, form(false));
        assertFalse(department.isEnabled());
        service.toggle(1L);
        assertTrue(department.isEnabled());
    }

    private static DepartmentForm form(boolean enabled) {
        DepartmentForm form = new DepartmentForm();
        form.setCode("CNPM");
        form.setName("Công nghệ phần mềm");
        form.setEnabled(enabled);
        return form;
    }
}
