package com.kineticfitness.service;

import com.kineticfitness.model.ScheduleStatus;
import com.kineticfitness.model.ScheduledWorkout;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * When a reminder shows, when it stops, and how it is worded.
 *
 * <p>Every case fixes {@code now} explicitly, so these assertions describe
 * exactly what a user would see at a given instant.</p>
 *
 * <p>US-22 &mdash; Set an activity reminder. US-23 &mdash; See due reminders.</p>
 */
class ReminderServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 20);
    private static final LocalTime SIX_PM = LocalTime.of(18, 0);

    /** Leg Day: 18:00-18:45, remind 15 minutes ahead (so from 17:45). */
    private ScheduledWorkout legDay() {
        return new ScheduledWorkout("Leg Day", DATE, SIX_PM, 45, 15);
    }

    private LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(DATE, LocalTime.of(hour, minute));
    }

    @Test
    void notDueBeforeTheLeadTime() {
        assertFalse(ReminderService.isDue(legDay(), at(17, 44)));
    }

    @Test
    @DisplayName("due from the exact moment the lead time is reached")
    void dueAtTheLeadTimeBoundary() {
        assertTrue(ReminderService.isDue(legDay(), at(17, 45)));
    }

    @Test
    void stillDueWhileTheSessionIsRunning() {
        assertTrue(ReminderService.isDue(legDay(), at(18, 30)));
    }

    @Test
    @DisplayName("the reminder clears the moment the session is over")
    void notDueOnceTheSessionHasFinished() {
        assertFalse(ReminderService.isDue(legDay(), at(18, 45)));
        assertFalse(ReminderService.isDue(legDay(), at(19, 0)));
    }

    @Test
    void neverDueForAWorkoutWithNoReminderSet() {
        ScheduledWorkout noReminder = new ScheduledWorkout("Leg Day", DATE, SIX_PM, 45,
                ScheduledWorkout.NO_REMINDER);
        assertFalse(ReminderService.isDue(noReminder, at(17, 50)));
    }

    @Test
    @DisplayName("finishing a workout silences its reminder")
    void neverDueForACompletedOrSkippedWorkout() {
        assertFalse(ReminderService.isDue(legDay().withStatus(ScheduleStatus.COMPLETED), at(17, 50)));
        assertFalse(ReminderService.isDue(legDay().withStatus(ScheduleStatus.SKIPPED), at(17, 50)));
    }

    @Test
    void nullsAreTreatedAsNotDueRatherThanThrowing() {
        assertFalse(ReminderService.isDue(null, at(17, 50)));
        assertFalse(ReminderService.isDue(legDay(), null));
    }

    @Test
    void dueRemindersComeBackSoonestFirst() {
        ScheduledWorkout later = new ScheduledWorkout("Cardio", DATE, LocalTime.of(18, 30), 30, 60);
        ScheduledWorkout earlier = legDay();
        ScheduledWorkout tomorrow = new ScheduledWorkout("Rest", DATE.plusDays(1), SIX_PM, 30, 15);

        List<ScheduledWorkout> due = ReminderService.due(List.of(later, earlier, tomorrow), at(17, 50));

        assertEquals(2, due.size());
        assertEquals("Leg Day", due.get(0).getName());
        assertEquals("Cardio", due.get(1).getName());
    }

    @Test
    void anEmptyOrNullScheduleProducesNoReminders() {
        assertTrue(ReminderService.due(List.of(), at(17, 50)).isEmpty());
        assertTrue(ReminderService.due(null, at(17, 50)).isEmpty());
    }

    @Test
    void theNextUpcomingSessionIsTheEarliestStillToStart() {
        ScheduledWorkout later = new ScheduledWorkout("Cardio", DATE, LocalTime.of(20, 0), 30, 0);

        assertEquals("Leg Day",
                ReminderService.nextUpcoming(List.of(later, legDay()), at(12, 0)).orElseThrow().getName());
    }

    @Test
    @DisplayName("a session already under way is not 'upcoming'")
    void aRunningSessionIsNotUpcoming() {
        assertTrue(ReminderService.nextUpcoming(List.of(legDay()), at(18, 10)).isEmpty());
    }

    @Test
    void completedSessionsAreNeverUpcoming() {
        assertTrue(ReminderService.nextUpcoming(
                List.of(legDay().withStatus(ScheduleStatus.COMPLETED)), at(12, 0)).isEmpty());
    }

    @Test
    void theWordingCountsDownThenCountsUp() {
        assertEquals("Leg Day starts in 15 minutes.", ReminderService.describe(legDay(), at(17, 45)));
        assertEquals("Leg Day starts in 1 minute.", ReminderService.describe(legDay(), at(17, 59)));
        assertEquals("Leg Day starts now.", ReminderService.describe(legDay(), at(18, 0)));
        assertEquals("Leg Day started 10 minutes ago.", ReminderService.describe(legDay(), at(18, 10)));
    }

    @Test
    void longWaitsAreWordedInHours() {
        assertEquals("1 minute", ReminderService.humanise(1));
        assertEquals("45 minutes", ReminderService.humanise(45));
        assertEquals("1 hour", ReminderService.humanise(60));
        assertEquals("1 hour 1 minute", ReminderService.humanise(61));
        assertEquals("2 hours 15 minutes", ReminderService.humanise(135));
        assertEquals("0 minutes", ReminderService.humanise(0));
    }

    @Test
    void negativeDurationsAreARejectedProgrammingError() {
        assertThrows(IllegalArgumentException.class, () -> ReminderService.humanise(-1));
        assertThrows(IllegalArgumentException.class, () -> ReminderService.describe(null, at(18, 0)));
    }
}
