package com.kineticfitness.model;

/**
 * How much training experience a user has. Chosen once when their profile is
 * created, and from then on the profile owns it &mdash; the goals screen shows
 * it but cannot change it.
 *
 * <p>Each constant carries its own label, the same split {@link Gender} and
 * {@link GoalType} use: the constant is the stored identity and the label is
 * what the user reads. That is why {@code ADVANCED} displays as "Pro" &mdash;
 * the wording can change without rewriting every profile already saved in the
 * database.</p>
 *
 * <p>US-04 &mdash; Create a profile. US-12 &mdash; Set a fitness goal.</p>
 */
public enum FitnessLevel {

    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Pro");

    private final String display;

    FitnessLevel(String display) {
        this.display = display;
    }

    /** The label shown in the UI, e.g. "Pro". */
    public String display() {
        return display;
    }
}
