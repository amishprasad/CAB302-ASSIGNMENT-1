package com.kineticfitness.util;

import java.util.Locale;

/**
 * Converts stored metric values (kg, cm) into whatever the user has chosen to see, and
 * formats them for display.
 *
 * <p>Kept separate from any one screen because Settings, Profile, Analytics and the
 * Dashboard all show weight and height, and the conversion factors should only be
 * written down once. Missing values (zero or below, the {@code LocalProfileStore}
 * default for an unfilled profile) format as "—" instead of "0.0 kg".
 */
public final class UnitConverter {

    /** Shown in place of a measurement the user has not entered yet. */
    public static final String NOT_SET = "—";

    private static final double POUNDS_PER_KILOGRAM = 2.20462262185;
    private static final double CENTIMETRES_PER_INCH = 2.54;
    private static final int INCHES_PER_FOOT = 12;

    private UnitConverter() {
    }

    public static double kgToPounds(double kilograms) {
        return kilograms * POUNDS_PER_KILOGRAM;
    }

    public static double poundsToKg(double pounds) {
        return pounds / POUNDS_PER_KILOGRAM;
    }

    public static double cmToInches(double centimetres) {
        return centimetres / CENTIMETRES_PER_INCH;
    }

    public static double inchesToCm(double inches) {
        return inches * CENTIMETRES_PER_INCH;
    }

    /** A stored weight in kilograms, as a number in the chosen system's unit. */
    public static double weightFor(double kilograms, UnitSystem system) {
        return system == UnitSystem.IMPERIAL ? kgToPounds(kilograms) : kilograms;
    }

    /** A stored height in centimetres, as a number in the chosen system's unit. */
    public static double heightFor(double centimetres, UnitSystem system) {
        return system == UnitSystem.IMPERIAL ? cmToInches(centimetres) : centimetres;
    }

    /** "72.5 kg" or "159.8 lb", or "—" when no weight has been entered. */
    public static String formatWeight(double kilograms, UnitSystem system) {
        if (kilograms <= 0) {
            return NOT_SET;
        }
        return String.format(Locale.ROOT, "%.1f %s",
                weightFor(kilograms, system), system.weightUnit());
    }

    /**
     * "180 cm" for metric, or feet and inches such as "5 ft 11 in" for imperial;
     * "—" when no height has been entered.
     *
     * <p>Imperial rounds to the nearest whole inch <em>before</em> splitting into feet,
     * so 182.9 cm reads "6 ft 0 in" rather than the impossible "5 ft 12 in".
     */
    public static String formatHeight(double centimetres, UnitSystem system) {
        if (centimetres <= 0) {
            return NOT_SET;
        }
        if (system == UnitSystem.METRIC) {
            return String.format(Locale.ROOT, "%.0f cm", centimetres);
        }
        long totalInches = Math.round(cmToInches(centimetres));
        return (totalInches / INCHES_PER_FOOT) + " ft " + (totalInches % INCHES_PER_FOOT) + " in";
    }
}
