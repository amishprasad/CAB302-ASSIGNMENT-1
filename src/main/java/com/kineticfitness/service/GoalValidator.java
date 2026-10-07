package com.kineticfitness.service;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.GoalType;

import java.time.LocalDate;
import java.util.OptionalDouble;

/**
 * The rules the "Set a New Goal" form must satisfy, and the parsing of its
 * free-text target weight.
 *
 * <p>Every method is a pure function and {@code today} arrives as a parameter
 * rather than from {@link LocalDate#now()}, so the "target date must be in the
 * future" rule can be tested without waiting for tomorrow.</p>
 *
 * <p>US-12 &mdash; Set a fitness goal.</p>
 */
public final class GoalValidator {

    /** Heaviest target the form will accept, in kilograms. */
    public static final double MAX_TARGET_WEIGHT_KG = 400;

    private GoalValidator() {
    }

    /**
     * Validates the whole form in the order the fields appear, so the user is
     * told about the first problem rather than the last.
     *
     * @param targetWeightText the raw text typed into the target weight field
     * @param weeklyMinutes    the chosen weekly exercise duration, or null if unchosen
     * @param weeklyWorkouts   the chosen number of workouts per week, or null if unchosen
     * @param startDate        the goal's existing start date, or today for a new goal
     * @return {@link ValidationResult#ok()} when every rule passes
     */
    public static ValidationResult validate(GoalType type,
                                            String targetWeightText,
                                            Integer weeklyMinutes,
                                            Integer weeklyWorkouts,
                                            LocalDate targetDate,
                                            FitnessLevel experienceLevel,
                                            boolean anyWorkoutTypeSelected,
                                            boolean anyWorkoutDaySelected,
                                            LocalDate startDate) {

        if (type == null || experienceLevel == null
                || targetWeightText == null || targetWeightText.isBlank()) {
            return ValidationResult.error(
                    "Please choose a goal type, target weight, and experience level.");
        }

        OptionalDouble weight = parseTargetWeight(targetWeightText);
        if (weight.isEmpty()) {
            return ValidationResult.error("Target weight must be a valid number.");
        }
        if (weight.getAsDouble() <= 0) {
            return ValidationResult.error("Target weight must be a positive number.");
        }
        if (weight.getAsDouble() > MAX_TARGET_WEIGHT_KG) {
            return ValidationResult.error(
                    "Target weight must be " + (int) MAX_TARGET_WEIGHT_KG + " kg or less.");
        }

        // Null means the user never opened the dropdown. The form pre-selects
        // nothing, so an unanswered question has to be caught here rather than
        // sailing through as somebody else's default.
        if (weeklyMinutes == null) {
            return ValidationResult.error("Choose how long you want to exercise each week.");
        }
        if (weeklyMinutes <= 0) {
            return ValidationResult.error("Weekly exercise duration must be more than zero.");
        }
        if (weeklyWorkouts == null) {
            return ValidationResult.error("Choose how many workouts you want to do each week.");
        }
        if (weeklyWorkouts <= 0) {
            return ValidationResult.error("Weekly workout goal must be more than zero.");
        }

        if (targetDate == null) {
            return ValidationResult.error("Choose a target date for this goal.");
        }
        if (!targetDate.isAfter(startDate)) {
            return ValidationResult.error("Target date must be after " + startDate + ".");
        }

        if (!anyWorkoutTypeSelected || !anyWorkoutDaySelected) {
            return ValidationResult.error(
                    "Pick at least one workout type and one workout day.");
        }

        return ValidationResult.ok();
    }

    /**
     * Reads a target weight from free text, tolerating surrounding spaces and a
     * trailing "kg".
     *
     * @return the weight, or empty when the text is not a number
     */
    public static OptionalDouble parseTargetWeight(String text) {
        if (text == null) {
            return OptionalDouble.empty();
        }
        String cleaned = text.trim().toLowerCase();
        if (cleaned.endsWith("kg")) {
            cleaned = cleaned.substring(0, cleaned.length() - 2).trim();
        }
        try {
            return OptionalDouble.of(Double.parseDouble(cleaned));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }
}
