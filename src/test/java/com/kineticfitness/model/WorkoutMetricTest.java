package com.kineticfitness.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests that each metric reads itself off a workout and aggregates correctly. */
class WorkoutMetricTest {

    private Workout workout(LocalDate date, Exercise... exercises) {
        Workout w = new Workout(date);
        for (Exercise e : exercises) {
            w.addExercise(e);
        }
        return w;
    }

    /** 3 sets of 10 and 4 sets of 5: 7 sets, 50 reps. */
    private Workout mixedWorkout() {
        return workout(LocalDate.of(2026, 10, 1),
                new Exercise("Squat", 3, 10),
                new Exercise("Press", 4, 5));
    }

    @Test
    void repsCountsEverySetsRepetitions() {
        assertEquals(50, WorkoutMetric.REPS.of(mixedWorkout()));
    }

    @Test
    void setsCountsSetsRatherThanRepetitions() {
        assertEquals(7, WorkoutMetric.SETS.of(mixedWorkout()));
    }

    @Test
    void aWorkoutWithNoExercisesIsZeroOnEveryMetric() {
        Workout empty = workout(LocalDate.of(2026, 10, 1));
        for (WorkoutMetric metric : WorkoutMetric.values()) {
            assertEquals(0, metric.of(empty), metric.name() + " should be zero");
        }
    }

    @Test
    void totalAddsUpEveryWorkout() {
        List<Workout> workouts = List.of(mixedWorkout(), mixedWorkout());
        assertEquals(100, WorkoutMetric.REPS.totalAcross(workouts));
        assertEquals(14, WorkoutMetric.SETS.totalAcross(workouts));
    }

    @Test
    void highestIsTheLargestSingleWorkout() {
        List<Workout> workouts = List.of(
                workout(LocalDate.of(2026, 10, 1), new Exercise("A", 2, 10)),   // 20 reps
                workout(LocalDate.of(2026, 10, 2), new Exercise("B", 5, 10)));  // 50 reps
        assertEquals(50, WorkoutMetric.REPS.highestIn(workouts));
        assertEquals(5, WorkoutMetric.SETS.highestIn(workouts));
    }

    @Test
    void averageIsRoundedDown() {
        List<Workout> workouts = List.of(
                workout(LocalDate.of(2026, 10, 1), new Exercise("A", 1, 10)),   // 10 reps
                workout(LocalDate.of(2026, 10, 2), new Exercise("B", 1, 11)));  // 11 reps
        assertEquals(10, WorkoutMetric.REPS.averageAcross(workouts));
    }

    @Test
    void aggregatesOfNothingAreZeroRatherThanAnError() {
        List<Workout> none = List.of();
        for (WorkoutMetric metric : WorkoutMetric.values()) {
            assertEquals(0, metric.totalAcross(none));
            assertEquals(0, metric.highestIn(none));
            assertEquals(0, metric.averageAcross(none), "average of nothing must not divide by zero");
        }
    }

    @Test
    void everyMetricHasALabelAndAUnit() {
        for (WorkoutMetric metric : WorkoutMetric.values()) {
            assertFalse(metric.tabLabel().isBlank());
            assertFalse(metric.unit().isBlank());
        }
    }
}
