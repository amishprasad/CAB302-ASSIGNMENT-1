package com.kineticfitness.util;

import com.kineticfitness.util.WorkoutStats.DatedVolume;
import com.kineticfitness.util.WorkoutStats.Sample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behaviour tests for the dashboard and workout-history statistics.
 *
 * <p>Each test states a rule the chart depends on, rather than checking a getter.
 * {@link WorkoutStats} holds no JavaFX types, which is what lets these run headless.
 */
class WorkoutStatsTest {

    // WorkoutDAO returns newest session first, so the test data is built that way.
    private List<Sample> threeSessionsNewestFirst() {
        return Arrays.asList(
                new Sample("14 Sep", 40),   // two exercises logged
                new Sample("14 Sep", 30),   // on the same day
                new Sample("12 Sep", 40),
                new Sample("9 Sep", 36));
    }

    @Test
    @DisplayName("Exercises logged on the same day become a single session")
    void volumeByDate_groupsExercisesLoggedOnTheSameDay() {
        List<DatedVolume> volumes = WorkoutStats.volumeByDate(threeSessionsNewestFirst(), 8);

        assertEquals(3, volumes.size(), "four exercises across three dates should give three bars");
    }

    @Test
    @DisplayName("A session's reps are the sum of every exercise logged that day")
    void volumeByDate_sumsRepsWithinASession() {
        List<DatedVolume> volumes = WorkoutStats.volumeByDate(threeSessionsNewestFirst(), 8);

        DatedVolume mostRecent = volumes.get(volumes.size() - 1);
        assertEquals(70, mostRecent.totalReps(), "40 + 30 logged on 14 Sep");
    }

    @Test
    @DisplayName("Sessions are returned oldest first so a chart reads left to right")
    void volumeByDate_reversesDaoOrderForDisplay() {
        List<DatedVolume> volumes = WorkoutStats.volumeByDate(threeSessionsNewestFirst(), 8);

        assertEquals("9 Sep", volumes.get(0).date(), "oldest session should be first");
        assertEquals("14 Sep", volumes.get(2).date(), "newest session should be last");
    }

    @Test
    @DisplayName("Only the most recent sessions are kept when the limit is exceeded")
    void volumeByDate_keepsTheNewestSessionsNotTheOldest() {
        List<Sample> twelveSessions = new ArrayList<>();
        for (int day = 12; day >= 1; day--) {          // newest first, as the DAO returns
            twelveSessions.add(new Sample(day + " Sep", day));
        }

        List<DatedVolume> volumes = WorkoutStats.volumeByDate(twelveSessions, 6);

        assertEquals(6, volumes.size(), "should trim to the limit");
        assertEquals("7 Sep", volumes.get(0).date(), "window should start at the 7th-newest session");
        assertEquals("12 Sep", volumes.get(5).date(), "newest session must survive the trim");
    }

    @Test
    @DisplayName("A limit of zero keeps every session")
    void volumeByDate_treatsZeroLimitAsNoLimit() {
        List<Sample> samples = Arrays.asList(
                new Sample("3 Sep", 10), new Sample("2 Sep", 10), new Sample("1 Sep", 10));

        assertEquals(3, WorkoutStats.volumeByDate(samples, 0).size());
    }

    @Test
    @DisplayName("A user with no workouts produces no bars rather than an error")
    void volumeByDate_handlesAnEmptyHistory() {
        assertTrue(WorkoutStats.volumeByDate(new ArrayList<>(), 6).isEmpty());
    }

    @Test
    @DisplayName("Only sessions in the current Monday-to-Sunday week are counted")
    void sessionsThisWeek_countsMondayToSundayInclusive() {
        LocalDate wednesday = LocalDate.of(2026, 9, 16);
        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2026, 9, 14),   // Monday       - counts
                LocalDate.of(2026, 9, 16),   // Wednesday    - counts
                LocalDate.of(2026, 9, 20),   // Sunday       - counts
                LocalDate.of(2026, 9, 13),   // previous Sun - excluded
                LocalDate.of(2026, 9, 21));  // next Monday  - excluded

        assertEquals(3, WorkoutStats.sessionsThisWeek(dates, wednesday));
    }

    @Test
    @DisplayName("The weekly count is the same whichever day of that week it is asked on")
    void sessionsThisWeek_isStableAcrossTheWeek() {
        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 16), LocalDate.of(2026, 9, 20));

        assertEquals(3, WorkoutStats.sessionsThisWeek(dates, LocalDate.of(2026, 9, 14)), "asked on Monday");
        assertEquals(3, WorkoutStats.sessionsThisWeek(dates, LocalDate.of(2026, 9, 20)), "asked on Sunday");
    }

    @Test
    @DisplayName("A workout with no recorded date is skipped instead of crashing")
    void sessionsThisWeek_ignoresMissingDates() {
        List<LocalDate> dates = Arrays.asList(LocalDate.of(2026, 9, 16), null);

        assertEquals(1, WorkoutStats.sessionsThisWeek(dates, LocalDate.of(2026, 9, 16)));
    }

    @Test
    @DisplayName("Progress is reported as a whole percentage of the target")
    void percentOf_reportsProgressTowardsTheTarget() {
        assertEquals(75, WorkoutStats.percentOf(3, 4));
        assertEquals(0, WorkoutStats.percentOf(0, 4), "nothing done yet");
    }

    @Test
    @DisplayName("Beating the target caps the progress bar at 100 percent")
    void percentOf_capsAtOneHundred() {
        assertEquals(100, WorkoutStats.percentOf(9, 4));
    }

    @Test
    @DisplayName("A goal of zero reports no progress instead of dividing by zero")
    void percentOf_handlesAZeroTarget() {
        assertEquals(0, WorkoutStats.percentOf(5, 0));
    }
}