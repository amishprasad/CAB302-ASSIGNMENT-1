package com.kineticfitness.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the labels each fitness level supplies for itself. */
class FitnessLevelTest {

    @Test
    void eachLevelHasItsOwnLabel() {
        assertEquals("Beginner", FitnessLevel.BEGINNER.display());
        assertEquals("Intermediate", FitnessLevel.INTERMEDIATE.display());
        assertEquals("Pro", FitnessLevel.ADVANCED.display());
    }

    @Test
    void everyLevelSuppliesANonBlankLabel() {
        for (FitnessLevel level : FitnessLevel.values()) {
            assertNotNull(level.display(), level.name() + " has no label");
            assertFalse(level.display().isBlank(), level.name() + " has a blank label");
        }
    }

    /**
     * The constant names are what {@code ProfileDAO} writes to the database, so
     * renaming one would silently orphan every profile already saved.
     */
    @Test
    void storedNamesAreUnchanged() {
        assertEquals("BEGINNER", FitnessLevel.BEGINNER.name());
        assertEquals("INTERMEDIATE", FitnessLevel.INTERMEDIATE.name());
        assertEquals("ADVANCED", FitnessLevel.ADVANCED.name());
    }
}
