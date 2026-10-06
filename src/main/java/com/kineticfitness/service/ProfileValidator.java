package com.kineticfitness.service;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.Gender;
import com.kineticfitness.model.PersonalDetails;

import java.time.LocalDate;
import java.util.OptionalDouble;

/**
 * The rules the profile form must satisfy, and the parsing of its free-text
 * height and weight fields.
 *
 * <p>Both the create form and the edit form call this, so the two screens can no
 * longer drift apart on what counts as a valid profile. Pure functions, with
 * {@code today} passed in so the "not in the future" rule is testable.</p>
 *
 * <p>US-04 &mdash; Create a profile. US-05 &mdash; View and edit my profile.</p>
 */
public final class ProfileValidator {

    private ProfileValidator() {
    }

    /**
     * Validates the whole form in the order the fields appear, so the user is
     * told about the first problem rather than the last.
     *
     * @param heightText the raw text typed into the height field
     * @param weightText the raw text typed into the weight field
     * @return {@link ValidationResult#ok()} when every rule passes
     */
    public static ValidationResult validate(String fullName,
                                            String email,
                                            Gender gender,
                                            LocalDate dateOfBirth,
                                            String heightText,
                                            String weightText,
                                            FitnessLevel fitnessLevel,
                                            LocalDate today) {

        if (isBlank(fullName) || isBlank(email) || gender == null || dateOfBirth == null
                || isBlank(heightText) || isBlank(weightText) || fitnessLevel == null) {
            return ValidationResult.error("Please fill in every field before continuing.");
        }

        if (!looksLikeEmail(email)) {
            return ValidationResult.error("Enter a valid email address.");
        }

        if (dateOfBirth.isAfter(today)) {
            return ValidationResult.error("Date of birth can't be in the future.");
        }

        OptionalDouble height = parseMeasurement(heightText);
        OptionalDouble weight = parseMeasurement(weightText);
        if (height.isEmpty() || weight.isEmpty()) {
            return ValidationResult.error("Height and weight must be valid numbers.");
        }
        if (height.getAsDouble() <= 0 || weight.getAsDouble() <= 0) {
            return ValidationResult.error("Height and weight must be positive numbers.");
        }
        if (height.getAsDouble() < PersonalDetails.MIN_HEIGHT_CM
                || height.getAsDouble() > PersonalDetails.MAX_HEIGHT_CM) {
            return ValidationResult.error("Height must be between "
                    + (int) PersonalDetails.MIN_HEIGHT_CM + " and "
                    + (int) PersonalDetails.MAX_HEIGHT_CM + " cm.");
        }
        if (weight.getAsDouble() < PersonalDetails.MIN_WEIGHT_KG
                || weight.getAsDouble() > PersonalDetails.MAX_WEIGHT_KG) {
            return ValidationResult.error("Weight must be between "
                    + (int) PersonalDetails.MIN_WEIGHT_KG + " and "
                    + (int) PersonalDetails.MAX_WEIGHT_KG + " kg.");
        }

        return ValidationResult.ok();
    }

    /**
     * Reads a measurement from free text, tolerating spaces and a unit suffix
     * such as "cm" or "kg".
     *
     * @return the number, or empty when the text is not one
     */
    public static OptionalDouble parseMeasurement(String text) {
        if (text == null) {
            return OptionalDouble.empty();
        }
        String cleaned = text.trim().toLowerCase().replace("cm", "").replace("kg", "").trim();
        try {
            return OptionalDouble.of(Double.parseDouble(cleaned));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    /**
     * A deliberately permissive check: one "@" with something either side, and a
     * dot in the domain. Rejecting unusual but legal addresses annoys users more
     * than accepting a typo does.
     */
    private static boolean looksLikeEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@') || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1);
        return domain.contains(".") && !domain.startsWith(".") && !domain.endsWith(".");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
