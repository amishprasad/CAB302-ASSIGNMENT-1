package com.kineticfitness.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Behaviour tests for BMI and its WHO classification.
 *
 * <p>The "profile not filled in yet" case matters most: {@code LocalProfileStore} starts
 * height and weight at zero, so the dashboard asks for a BMI before the user has entered
 * anything. Without a guard that divides by zero and prints Infinity on screen.
 */
class BmiCalculatorTest {

    private static final double TOLERANCE = 0.05;

    @Test
    @DisplayName("BMI is weight in kilograms over height in metres squared")
    void bmi_calculatesFromHeightAndWeight() {
        assertEquals(25.0, BmiCalculator.bmi(180, 81), TOLERANCE);
        assertEquals(22.84, BmiCalculator.bmi(180, 74), TOLERANCE);
    }

    @Test
    @DisplayName("An unfilled profile reports zero rather than infinity")
    void bmi_returnsZeroWhenMeasurementsAreMissing() {
        assertEquals(0, BmiCalculator.bmi(0, 81), TOLERANCE, "no height recorded");
        assertEquals(0, BmiCalculator.bmi(180, 0), TOLERANCE, "no weight recorded");
    }

    @Test
    @DisplayName("Each WHO band is classified correctly, including its boundaries")
    void category_classifiesEveryWhoBand() {
        assertEquals("Underweight", BmiCalculator.category(18.4));
        assertEquals("Healthy weight", BmiCalculator.category(18.5), "lower boundary is healthy");
        assertEquals("Healthy weight", BmiCalculator.category(24.9), "upper boundary is healthy");
        assertEquals("Overweight", BmiCalculator.category(25.0), "25 starts overweight");
        assertEquals("Obese", BmiCalculator.category(30.0));
        assertEquals("Unknown", BmiCalculator.category(0), "empty profile");
    }

    @Test
    @DisplayName("The healthy band is shown in green and the extremes in red")
    void categoryColour_signalsHealthyAndUnhealthyBands() {
        assertEquals("#16A34A", BmiCalculator.categoryColour(22.0), "healthy → green");
        assertEquals("#DC2626", BmiCalculator.categoryColour(17.0), "underweight → red");
        assertEquals("#DC2626", BmiCalculator.categoryColour(31.0), "obese → red");
        assertEquals("#F59E0B", BmiCalculator.categoryColour(27.0), "overweight → amber");
        assertEquals("#64748B", BmiCalculator.categoryColour(0), "unknown → grey");
    }
}