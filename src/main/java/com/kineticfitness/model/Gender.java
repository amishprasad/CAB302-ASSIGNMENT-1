package com.kineticfitness.model;

/** How a user describes their gender on their profile. */
public enum Gender {

    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer not to say");

    private final String display;

    Gender(String display) {
        this.display = display;
    }

    /** The label shown in the UI. */
    public String display() {
        return display;
    }

    @Override
    public String toString() {
        return display;
    }
}
