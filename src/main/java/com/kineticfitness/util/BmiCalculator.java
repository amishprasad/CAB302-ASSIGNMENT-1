package com.kineticfitness.util;

/**
 * Body Mass Index and its standard WHO classification.
 *
 * <p>Kept separate from any one screen because the profile store and the {@code User}
 * model both hold height and weight, and the thresholds should only be written down once.
 * No JavaFX here, so it unit tests directly.
 */
public final class BmiCalculator {

    /** Utility class; not meant to be instantiated. */
    private BmiCalculator() {
    }

    /**
     * Calculates BMI from height and weight.
     * Returns 0 when either figure is missing, so a half-filled profile shows
     * "—" on screen instead of infinity or a crash.
     *
     * @param heightCm height in centimetres
     * @param weightKg weight in kilograms
     * @return the BMI value, or 0 if height or weight is zero or negative
     */
    public static double bmi(double heightCm, double weightKg) {
        if (heightCm <= 0 || weightKg <= 0) {
            return 0;
        }
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /**
     * Returns the WHO category for a BMI value.
     *
     * @param bmi the BMI value
     * @return "Underweight", "Healthy weight", "Overweight", "Obese",
     *         or "Unknown" if the value is zero or negative
     */
    public static String category(double bmi) {
        if (bmi <= 0) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Healthy weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    /**
     * Returns the display colour for a BMI category: green inside the healthy band,
     * amber for overweight, red for underweight or obese, and grey when unknown.
     *
     * @param bmi the BMI value
     * @return a hex colour string, e.g. {@code "#16A34A"}
     */
    public static String categoryColour(double bmi) {
        if (bmi <= 0) return "#64748B";                       // unknown → grey
        if (bmi < 18.5 || bmi >= 30.0) return "#DC2626";       // under / obese → red
        if (bmi < 25.0) return "#16A34A";                      // healthy → green
        return "#F59E0B";                                      // overweight → amber
    }
}