package com.kineticfitness.service;

import com.kineticfitness.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordChangeValidatorTest {

    private static final String CURRENT = "oldpass1";
    private static final String STORED_HASH = PasswordUtil.hash(CURRENT);

    private static ValidationResult validate(String current, String newPassword, String confirm) {
        return PasswordChangeValidator.validate(STORED_HASH, current, newPassword, confirm);
    }

    private static void assertError(String expectedMessage, ValidationResult result) {
        assertTrue(result.isInvalid());
        assertEquals(expectedMessage, result.message());
    }

    @Test
    void acceptsCorrectCurrentPasswordAndMatchingNewPassword() {
        ValidationResult result = validate(CURRENT, "newpass1", "newpass1");
        assertTrue(result.valid());
        assertEquals("", result.message());
    }

    @Test
    void rejectsBlankCurrentPassword() {
        assertError("Enter your current password.", validate("", "newpass1", "newpass1"));
    }

    @Test
    void rejectsNullCurrentPassword() {
        assertError("Enter your current password.", validate(null, "newpass1", "newpass1"));
    }

    @Test
    void rejectsWrongCurrentPassword() {
        assertError("Current password is incorrect.", validate("wrongpass", "newpass1", "newpass1"));
    }

    @Test
    void rejectsWhenThereIsNoStoredHashToCheckAgainst() {
        ValidationResult result = PasswordChangeValidator.validate(null, CURRENT, "newpass1", "newpass1");
        assertError("Current password is incorrect.", result);
    }

    @Test
    void rejectsBlankNewPassword() {
        assertError("Enter a new password.", validate(CURRENT, "   ", "   "));
    }

    @Test
    void rejectsNewPasswordShorterThanSixCharacters() {
        assertError("New password must be at least 6 characters.", validate(CURRENT, "abc12", "abc12"));
    }

    @Test
    void acceptsNewPasswordOfExactlyMinimumLength() {
        assertTrue(validate(CURRENT, "abc123", "abc123").valid());
    }

    @Test
    void rejectsWhenConfirmationDoesNotMatch() {
        assertError("Passwords don't match.", validate(CURRENT, "newpass1", "newpass2"));
    }

    @Test
    void rejectsMissingConfirmation() {
        assertError("Passwords don't match.", validate(CURRENT, "newpass1", null));
    }

    @Test
    void rejectsNewPasswordThatIsTheSameAsTheCurrentOne() {
        assertError("New password must be different from your current password.",
                validate(CURRENT, CURRENT, CURRENT));
    }

    @Test
    void wrongCurrentPasswordIsReportedBeforeAnyOtherProblem() {
        assertError("Current password is incorrect.", validate("wrongpass", "abc", "xyz"));
    }
}