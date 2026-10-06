package com.kineticfitness.util;

/**
 * The measurement system a user has chosen on the Settings page.
 *
 * <p>Saved data is always stored in metric (kilograms and centimetres), so switching
 * system only changes how numbers are <em>shown</em>, never what is stored. That keeps
 * a change of preference from ever corrupting a logged weight or height.
 *
 * <p>No JavaFX here, so it unit tests directly.
 */
public enum UnitSystem {

    METRIC("Metric", "kg", "cm"),
    IMPERIAL("Imperial", "lb", "in");

    private final String displayName;
    private final String weightUnit;
    private final String heightUnit;

    UnitSystem(String displayName, String weightUnit, String heightUnit) {
        this.displayName = displayName;
        this.weightUnit = weightUnit;
        this.heightUnit = heightUnit;
    }

    /** Label shown on the Settings toggle, e.g. "Metric". */
    public String displayName() {
        return displayName;
    }

    /** Short weight unit for this system: "kg" or "lb". */
    public String weightUnit() {
        return weightUnit;
    }

    /** Short height unit for this system: "cm" or "in". */
    public String heightUnit() {
        return heightUnit;
    }

    /**
     * Reads a system back from saved text, ignoring case and surrounding spaces.
     * Anything missing or unrecognised falls back to {@link #METRIC}, so a blank or
     * damaged preference can never stop the app from loading.
     */
    public static UnitSystem parse(String text) {
        if (text == null) {
            return METRIC;
        }
        String cleaned = text.trim();
        for (UnitSystem system : values()) {
            if (system.name().equalsIgnoreCase(cleaned) || system.displayName.equalsIgnoreCase(cleaned)) {
                return system;
            }
        }
        return METRIC;
    }
}
