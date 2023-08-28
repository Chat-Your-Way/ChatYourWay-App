package com.chat.yourway.unit.annotation.validator;

import com.chat.yourway.annotation.validator.PasswordValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {
    public static final PasswordValidator PASSWORD_VALIDATOR = new PasswordValidator();

    @Mock
    public ConstraintValidatorContext context;

    @DisplayName("PasswordValidator should pass validator when user input correct password")
    @Test
    public void shouldPassValidator_whenUserInputCorrectPassword() {
        // Given
        var password = "P!ssw0rd";

        // When
        var isValid = PASSWORD_VALIDATOR.isValid(password, context);

        // Then
        assertTrue(isValid);
    }

    @DisplayName("PasswordValidator should fail validator when user input blank password")
    @Test
    public void shouldFailValidator_whenUserInputBlankPassword() {
        // Given
        var password = "";

        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> PASSWORD_VALIDATOR.isValid(password, context));
    }

    @DisplayName("PasswordValidator should fail validator when user input short password")
    @Test
    public void shouldFailValidator_whenUserInputShortPassword() {
        // Given
        var password = "abc";

        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> PASSWORD_VALIDATOR.isValid(password, context));
    }

    @DisplayName("PasswordValidator should fail validator when user input long password")
    @Test
    public void shouldFailValidator_whenUserInputLongPassword() {
        // Given
        var password = "verylongpassword";

        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> PASSWORD_VALIDATOR.isValid(password, context));
    }

    @DisplayName("PasswordValidator should fail validator when user input password without special symbol")
    @Test
    public void shouldFailValidator_whenUserInputPasswordWithoutSpecialSymbol() {
        // Given
        var password = "Password123";

        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> PASSWORD_VALIDATOR.isValid(password, context));
    }

    @DisplayName("PasswordValidator should fail validator when user input password without upper-case character")
    @Test
    public void shouldFailValidator_whenUserInputPasswordWithoutUpperCase() {
        // Given
        var password = "password123!";

        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> PASSWORD_VALIDATOR.isValid(password, context));
    }

    @DisplayName("PasswordValidator should fail validator when user input password without upper-case character")
    @Test
    public void shouldFailValidator_whenUserInputPasswordWithoutDigit() {
        // Given
        var password = "Password!";

        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> PASSWORD_VALIDATOR.isValid(password, context));
    }
}
