package com.kineticfitness.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WorkoutTest {

    @Test
    void totalRepsSumsAllExercises() {
        Workout workout = new Workout(LocalDate.now());
        workout.addExercise(new Exercise("Push-ups", 3, 10)); // 30
        workout.addExercise(new Exercise("Squats", 4, 12));    // 48
        assertEquals(78, workout.totalReps());
    }

    @Test
    void totalRepsGrowsAsExercisesAreAdded() {
        Workout workout = new Workout(LocalDate.now());
        workout.addExercise(new Exercise("Push-ups", 3, 10));
        assertEquals(30, workout.totalReps());

        workout.addExercise(new Exercise("Squats", 4, 12));
        assertEquals(78, workout.totalReps(), "adding an exercise updates the total");
    }

    @Test
    void emptyWorkoutHasZeroReps() {
        Workout workout = new Workout(LocalDate.now());
        assertEquals(0, workout.totalReps());
    }
}
