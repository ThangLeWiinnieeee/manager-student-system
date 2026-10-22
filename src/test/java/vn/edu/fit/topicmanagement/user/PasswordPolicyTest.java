package vn.edu.fit.topicmanagement.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

class PasswordPolicyTest {
    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("invalidPasswords")
    void rejectsInvalidPasswords(String password) {
        assertThrows(BusinessRuleException.class, () -> PasswordPolicy.validate(password));
    }

    static Stream<String> invalidPasswords() {
        return Stream.of("short", "        ", "a".repeat(73), "ấ".repeat(25));
    }

    @ParameterizedTest
    @MethodSource("validPasswords")
    void acceptsPasswordsAtTheLengthBoundaries(String password) {
        assertDoesNotThrow(() -> PasswordPolicy.validate(password));
    }

    static Stream<String> validPasswords() {
        return Stream.of("12345678", "a".repeat(72), "ấ".repeat(24));
    }
}
