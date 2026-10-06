package com.kineticfitness.service;

import com.kineticfitness.util.PasswordUtil;

/**
 * Rules for changing a password from the Settings page, kept out of the view so they
 * can be unit tested and the error wording lives in one place.
 *
 * <p>The current password is checked first, so someone using an unattended signed-in
 * session cannot change the password without knowing the existing one. The remaining
 * rules are checked in the order a person would fix them: new password present, long
 * enough, confirmed, and actually different.</p>
 *
 * No JavaFX here, so it unit tests directly.
 */
public final class PasswordChangeValidator {

    /** Shortest new password accepted. */
    public static final int MIN_LENGTH = 6;

    private PasswordChangeValidator() {
    }

    /**
     * Checks a password-change request.
     *
     * @param storedHash      the signed-in user's saved hash (see {@link PasswordUtil#hash})
     * @param currentPassword what the user typed as their current password
     * @param newPassword     the proposed new password
     * @param confirmPassword the new password typed a second time
     * @return {@link ValidationResult#ok()}, or an error with a message safe to show in the UI
     */
    public static ValidationResult validate(String storedHash, String currentPassword,
                                            String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isEmpty()) {
            return ValidationResult.error("Enter your current password.");
        }
        if (!PasswordUtil.verify(currentPassword, storedHash)) {
            return ValidationResult.error("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ValidationResult.error("Enter a new password.");
        }
        if (newPassword.length() < MIN_LENGTH) {
            return ValidationResult.error("New password must be at least " + MIN_LENGTH + " characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ValidationResult.error("Passwords don't match.");
        }
        if (newPassword.equals(currentPassword)) {
            return ValidationResult.error("New password must be different from your current password.");
        }
        return ValidationResult.ok();
    }
}