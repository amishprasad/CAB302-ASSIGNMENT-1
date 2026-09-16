package com.kineticfitness.service;

import com.kineticfitness.model.ScheduleStatus;
import com.kineticfitness.model.ScheduledWorkout;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Warns the user when a workout they are scheduling overlaps one already booked.
 *
 * <p>Written test-first: this class exists before {@code ScheduleConflictDetector}
 * does, so the first run fails to compile. That failure is the "red" step.</p>
 */
class ScheduleConflictDetectorTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 21);

    @Test
    void aWorkoutOverlappingAnExistingOneClashes() {
        // Cardio runs 18:00-19:00.
        ScheduledWorkout booked = new ScheduledWorkout(
                "Cardio", DATE, LocalTime.of(18, 0), 60, ScheduledWorkout.NO_REMINDER);

        // Leg Day would start at 18:30, half an hour into it.
        ScheduledWorkout candidate = new ScheduledWorkout(
                "Leg Day", DATE, LocalTime.of(18, 30), 45, ScheduledWorkout.NO_REMINDER);

        assertTrue(ScheduleConflictDetector.clashes(candidate, List.of(booked)));
    }

    @Test
    void aCompletedWorkoutDoesNotBlockTheSlot() {
        // Cardio was booked 18:00-19:00 but has already been marked done.
        ScheduledWorkout done = new ScheduledWorkout(
                "Cardio", DATE, LocalTime.of(18, 0), 60, ScheduledWorkout.NO_REMINDER)
                .withStatus(ScheduleStatus.COMPLETED);

        // Finishing it frees the slot, so an 18:30 session is not a clash.
        ScheduledWorkout candidate = new ScheduledWorkout(
                "Leg Day", DATE, LocalTime.of(18, 30), 45, ScheduledWorkout.NO_REMINDER);

        assertFalse(ScheduleConflictDetector.clashes(candidate, List.of(done)));
    }
}
