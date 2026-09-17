package com.kineticfitness.service;

/**
 * The outcome of validating user input: either valid, or invalid with a message
 * that is safe to show directly in the UI.
 *
 * <p>Returning this instead of a bare {@code boolean} keeps the error wording in
 * one testable place rather than scattered through button handlers.</p>
 *
 * @param valid   true when the input passed every rule
 * @param message the message to display; empty when {@code valid} is true
 */
public record ValidationResult(boolean valid, String message) {

    private static final ValidationResult OK = new ValidationResult(true, "");

    /** A successful result with no message. */
    public static ValidationResult ok() {
        return OK;
    }

    /**
     * A failed result carrying the message to show the user.
     *
     * @param message human-readable explanation; must not be blank
     */
    public static ValidationResult error(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("an error result needs a message");
        }
        return new ValidationResult(false, message);
    }

    /** Convenience inverse of {@link #valid()}, for readable guard clauses. */
    public boolean isInvalid() {
        return !valid;
    }
}
