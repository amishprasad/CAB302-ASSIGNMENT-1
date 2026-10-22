package com.kineticfitness.model;

import java.util.Optional;

public final class MealValidator {

    private MealValidator() {
    }

    public static Optional<String> validate(String name, String calories, String protein,
                                            String carbs, String fats) {
        if (name == null || name.isBlank()) {
            return Optional.of("Enter a meal name.");
        }
        Optional<String> problem = checkWholeNumber("Calories", calories);
        if (problem.isPresent()) {
            return problem;
        }
        problem = checkDecimal("Protein (g)", protein);
        if (problem.isPresent()) {
            return problem;
        }
        problem = checkDecimal("Carbs (g)", carbs);
        if (problem.isPresent()) {
            return problem;
        }
        return checkDecimal("Fats (g)", fats);
    }

    private static Optional<String> checkWholeNumber(String label, String text) {
        if (text == null || text.isBlank()) {
            return Optional.of("Enter " + label.toLowerCase() + ".");
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value < 0) {
                return Optional.of(label + " can't be negative.");
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.of(label + " must be a whole number.");
        }
    }

    private static Optional<String> checkDecimal(String label, String text) {
        if (text == null || text.isBlank()) {
            return Optional.of("Enter " + label.toLowerCase() + ".");
        }
        try {
            double value = Double.parseDouble(text.trim());
            if (!Double.isFinite(value)) {
                return Optional.of(label + " must be a number.");
            }
            if (value < 0) {
                return Optional.of(label + " can't be negative.");
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.of(label + " must be a number.");
        }
    }
}