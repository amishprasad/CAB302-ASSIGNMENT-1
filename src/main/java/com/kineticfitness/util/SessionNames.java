package com.kineticfitness.util;

public final class SessionNames {

    private SessionNames() {
    }

    public static String normalize(String raw) {
        return (raw == null || raw.isBlank()) ? null : raw.strip();
    }
}