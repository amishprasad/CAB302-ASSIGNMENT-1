package com.kineticfitness.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behaviour of a planned training session: when it starts and ends, when its
 * reminder fires, and how it moves between statuses.
 *
 * <p>US-10, US-22, US-32.</p>
 */
class ScheduledWorkoutTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 20);
    private static final LocalTime SIX_PM = LocalTime.of(18, 0);

    private ScheduledWorkout legDay(int reminderLeadMinutes) {
        return new ScheduledWorkout("Leg Day", DATE, SIX_PM, 45, reminderLeadMinutes);
    }

    @Test
    void aNewWorkoutStartsScheduledAndUnsaved() {
        ScheduledWorkout workout = legDay(15);
        assertEquals(ScheduleStatus.SCHEDULED, workout.getStatus());
        assertEquals(ScheduledWorkout.UNSAVED, workout.getId());
        assertTrue(workout.isOpen());
    }

    @Test
    void startAndEndAreDerivedFromDateTimeAndDuration() {
        ScheduledWorkout workout = legDay(15);
        assertEquals(LocalDateTime.of(2026, 9, 20, 18, 0), workout.startsAt());
        assertEquals(LocalDateTime.of(2026, 9, 20, 18, 45), workout.endsAt());
    }

    @Test
    void theReminderFiresItsLeadTimeBeforeTheStart() {
        ScheduledWorkout workout = legDay(15);
        assertTrue(workout.hasReminder());
        assertEquals(LocalDateTime.of(2026, 9, 20, 17, 45), workout.reminderAt());
    }

    @Test
    @DisplayName("a lead time that crosses midnight still resolves correctly")
    void aReminderCanFallOnThePreviousDay() {
        ScheduledWorkout earlyStart = new ScheduledWorkout("Sunrise Run", DATE, LocalTime.of(0, 30), 30, 60);
        assertEquals(LocalDateTime.of(2026, 9, 19, 23, 30), earlyStart.reminderAt());
    }

    @Test
    void aZeroLeadTimeMeansNoReminderAtAll() {
        ScheduledWorkout workout = legDay(ScheduledWorkout.NO_REMINDER);
        assertFalse(workout.hasReminder());
        assertThrows(IllegalStateException.class, workout::reminderAt);
    }

    @Test
    void completingAWorkoutLeavesTheOriginalUntouched() {
        ScheduledWorkout scheduled = legDay(15);
        ScheduledWorkout completed = scheduled.withStatus(ScheduleStatus.COMPLETED);

        assertEquals(ScheduleStatus.COMPLETED, completed.getStatus());
        assertEquals(ScheduleStatus.SCHEDULED, scheduled.getStatus(), "the original must be immutable");
        assertFalse(completed.isOpen());
    }

    @Test
    void anIllegalTransitionIsRefused() {
        ScheduledWorkout completed = legDay(15).withStatus(ScheduleStatus.COMPLETED);
        assertThrows(IllegalArgumentException.class, () -> completed.withStatus(ScheduleStatus.SKIPPED));
    }

    @Test
    @DisplayName("rescheduling a skipped workout reopens it")
    void reschedulingMovesTheTimeAndReturnsItToScheduled() {
        ScheduledWorkout skipped = legDay(15).withStatus(ScheduleStatus.SKIPPED);

        ScheduledWorkout moved = skipped.rescheduledTo(LocalDate.of(2026, 9, 22), LocalTime.of(7, 30));

        assertEquals(LocalDate.of(2026, 9, 22), moved.getDate());
        assertEquals(LocalTime.of(7, 30), moved.getStartTime());
        assertEquals(ScheduleStatus.SCHEDULED, moved.getStatus());
        assertEquals(15, moved.getReminderLeadMinutes(), "the reminder setting should survive a reschedule");
    }

    @Test
    void theReminderCanBeChangedOrSwitchedOff() {
        ScheduledWorkout workout = legDay(15);
        assertEquals(30, workout.withReminderLead(30).getReminderLeadMinutes());
        assertFalse(workout.withReminderLead(ScheduledWorkout.NO_REMINDER).hasReminder());
    }

    @Test
    void savingAttachesTheDatabaseId() {
        assertEquals(7, legDay(15).withId(7).getId());
    }

    @Test
    void durationIsWrittenForPeople() {
        assertEquals("45 mins", legDay(0).durationLabel());
        assertEquals("1 hr", new ScheduledWorkout("A", DATE, SIX_PM, 60, 0).durationLabel());
        assertEquals("1 hr 30 mins", new ScheduledWorkout("A", DATE, SIX_PM, 90, 0).durationLabel());
        assertEquals("2 hrs", new ScheduledWorkout("A", DATE, SIX_PM, 120, 0).durationLabel());
    }

    @Test
    void impossibleWorkoutsAreRejectedAtConstruction() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledWorkout("  ", DATE, SIX_PM, 45, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledWorkout("Leg Day", null, SIX_PM, 45, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledWorkout("Leg Day", DATE, null, 45, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledWorkout("Leg Day", DATE, SIX_PM, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledWorkout("Leg Day", DATE, SIX_PM, 45, -5));
    }

    @Test
    void theNameIsTrimmed() {
        assertEquals("Leg Day", new ScheduledWorkout("  Leg Day  ", DATE, SIX_PM, 45, 0).getName());
    }
}
