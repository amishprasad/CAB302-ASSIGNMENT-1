package com.kineticfitness.util;

/**
 * Body Mass Index and its standard WHO classification.
 *
 * <p>Kept separate from any one screen because the profile store and the {@code User}
 * model both hold height and weight, and the thresholds should only be written down once.
 */
public final class BmiCalculator {

    private BmiCalculator() {
    }

    public static double bmi(double heightCm, double weightKg) {
        return 0;
    }

    public static String category(double bmi) {
        return "";
    }

    public static String categoryColour(double bmi) {
        return "";
    }
}
