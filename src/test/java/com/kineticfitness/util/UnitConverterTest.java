package com.kineticfitness.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Behaviour tests for the metric/imperial display conversion.
 *
 * <p>The cases that matter most are the ones a user would notice on screen: an unfilled
 * profile must show "—" rather than "0.0 lb", and a height just under a whole number of
 * feet must not print "5 ft 12 in".
 */
class UnitConverterTest {

    private static final double TOLERANCE = 0.01;

    @Test
    @DisplayName("Kilograms and pounds convert using the standard factor")
    void weightConversion_usesStandardFactor() {
        assertEquals(2.2046, UnitConverter.kgToPounds(1), 0.001);
        assertEquals(165.35, UnitConverter.kgToPounds(75), TOLERANCE);
        assertEquals(75.0, UnitConverter.poundsToKg(165.3467), TOLERANCE);
    }

    @Test
    @DisplayName("Centimetres and inches convert using 2.54")
    void heightConversion_uses254() {
        assertEquals(1.0, UnitConverter.cmToInches(2.54), TOLERANCE);
        assertEquals(70.87, UnitConverter.cmToInches(180), TOLERANCE);
        assertEquals(180.0, UnitConverter.inchesToCm(70.8661), TOLERANCE);
    }

    @Test
    @DisplayName("Converting there and back returns the original value")
    void conversions_roundTrip() {
        assertEquals(68.4, UnitConverter.poundsToKg(UnitConverter.kgToPounds(68.4)), 1e-9);
        assertEquals(172.5, UnitConverter.inchesToCm(UnitConverter.cmToInches(172.5)), 1e-9);
    }

    @Test
    @DisplayName("Metric leaves stored values untouched; imperial converts them")
    void weightFor_andHeightFor_respectTheChosenSystem() {
        assertEquals(80.0, UnitConverter.weightFor(80, UnitSystem.METRIC), 1e-9);
        assertEquals(176.37, UnitConverter.weightFor(80, UnitSystem.IMPERIAL), TOLERANCE);
        assertEquals(180.0, UnitConverter.heightFor(180, UnitSystem.METRIC), 1e-9);
        assertEquals(70.87, UnitConverter.heightFor(180, UnitSystem.IMPERIAL), TOLERANCE);
    }

    @Test
    @DisplayName("Weight is formatted with one decimal and the right unit")
    void formatWeight_showsValueAndUnit() {
        assertEquals("72.5 kg", UnitConverter.formatWeight(72.5, UnitSystem.METRIC));
        assertEquals("159.8 lb", UnitConverter.formatWeight(72.5, UnitSystem.IMPERIAL));
    }

    @Test
    @DisplayName("Height is shown in centimetres, or as feet and inches")
    void formatHeight_showsCentimetresOrFeetAndInches() {
        assertEquals("180 cm", UnitConverter.formatHeight(180, UnitSystem.METRIC));
        assertEquals("5 ft 11 in", UnitConverter.formatHeight(180, UnitSystem.IMPERIAL));
    }

    @Test
    @DisplayName("Rounding up to a whole foot never produces 12 inches")
    void formatHeight_roundsBeforeSplittingIntoFeet() {
        // 182.9 cm is 72.008 in: rounds to 72 in = 6 ft 0 in, not "5 ft 12 in".
        assertEquals("6 ft 0 in", UnitConverter.formatHeight(182.9, UnitSystem.IMPERIAL));
        // 182.3 cm is 71.77 in: rounds to 72 in as well.
        assertEquals("6 ft 0 in", UnitConverter.formatHeight(182.3, UnitSystem.IMPERIAL));
        assertEquals("5 ft 11 in", UnitConverter.formatHeight(181.0, UnitSystem.IMPERIAL));
    }

    @Test
    @DisplayName("An unfilled profile shows a dash instead of zero")
    void formatting_showsDashWhenMeasurementMissing() {
        assertEquals("—", UnitConverter.formatWeight(0, UnitSystem.METRIC));
        assertEquals("—", UnitConverter.formatWeight(0, UnitSystem.IMPERIAL));
        assertEquals("—", UnitConverter.formatHeight(0, UnitSystem.METRIC));
        assertEquals("—", UnitConverter.formatHeight(-5, UnitSystem.IMPERIAL));
    }
}
