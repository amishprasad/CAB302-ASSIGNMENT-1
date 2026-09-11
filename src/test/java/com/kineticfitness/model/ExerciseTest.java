package com.kineticfitness.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExerciseTest {

    @Test
    void constructorSetsInitialValues() {
        Exercise exercise = new Exercise("Push-ups", 3, 12);
        assertEquals("Push-ups", exercise.getName());
        assertEquals(3, exercise.getSets());
        assertEquals(12, exercise.getReps());
    }

    @Test
    void totalRepsCalculatesSetsMultipliedByReps() {
        Exercise exercise = new Exercise("Bench Press", 4, 8);
        assertEquals(32, exercise.totalReps());
    }

    @Test
    void settersUpdateValues() {
        Exercise exercise = new Exercise("Squat", 3, 10);
        exercise.setName("Goblet Squat");
        exercise.setSets(5);
        exercise.setReps(15);

        assertEquals("Goblet Squat", exercise.getName());
        assertEquals(5, exercise.getSets());
        assertEquals(15, exercise.getReps());
        assertEquals(75, exercise.totalReps());
    }
}
