package com.kineticfitness.view;

import com.kineticfitness.model.BodyPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntryTest {

    private static Entry entry(String sets, String reps) {
        return new Entry(1, "1 Oct", "Bench Press", sets, reps, BodyPart.CHEST);
    }

    @Test
    void totalRepsMultipliesSetsByReps() {
        assertEquals(30, entry("3", "10").totalReps());
    }

    @Test
    void totalRepsIgnoresSurroundingWhitespace() {
        assertEquals(24, entry(" 4 ", " 6 ").totalReps());
    }

    @Test
    void totalRepsIsZeroWhenTextIsNotANumber() {
        assertEquals(0, entry("abc", "10").totalReps());
        assertEquals(0, entry("3", "").totalReps());
    }

    @Test
    void totalRepsFollowsEditedValues() {
        Entry e = entry("3", "10");
        e.setSets("5");
        e.setReps("5");
        assertEquals(25, e.totalReps());
    }
}