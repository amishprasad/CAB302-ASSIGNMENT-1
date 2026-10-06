package com.kineticfitness.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkoutStatsAnalyticsTest {

    /** A Wednesday, pinned so the tests behave the same on any day. Its Monday is 2026-10-05. */
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 7);

    // ---- currentStreak ----

    @Test
    void streakIsZeroWithNoWorkouts() {
        assertEquals(0, WorkoutStats.currentStreak(List.of(), TODAY));
    }

    @Test
    void workoutTodayCountsAsOneDay() {
        assertEquals(1, WorkoutStats.currentStreak(List.of(TODAY), TODAY));
    }

    @Test
    void workoutOnlyYesterdayKeepsStreakAlive() {
        assertEquals(1, WorkoutStats.currentStreak(List.of(TODAY.minusDays(1)), TODAY));
    }

    @Test
    void streakIsZeroWhenLastWorkoutWasTwoDaysAgo() {
        assertEquals(0, WorkoutStats.currentStreak(List.of(TODAY.minusDays(2)), TODAY));
    }

    @Test
    void streakIsZeroWhenLastWorkoutWasWeeksAgo() {
        List<LocalDate> old = List.of(TODAY.minusDays(21), TODAY.minusDays(22), TODAY.minusDays(23));
        assertEquals(0, WorkoutStats.currentStreak(old, TODAY));
    }

    @Test
    void consecutiveDaysEndingTodayAreAllCounted() {
        List<LocalDate> dates = List.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(2));
        assertEquals(3, WorkoutStats.currentStreak(dates, TODAY));
    }

    @Test
    void consecutiveDaysEndingYesterdayAreAllCounted() {
        List<LocalDate> dates = List.of(TODAY.minusDays(1), TODAY.minusDays(2));
        assertEquals(2, WorkoutStats.currentStreak(dates, TODAY));
    }

    @Test
    void gapBreaksTheStreak() {
        List<LocalDate> dates = List.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(3));
        assertEquals(2, WorkoutStats.currentStreak(dates, TODAY));
    }

    @Test
    void multipleWorkoutsOnOneDayCountOnce() {
        List<LocalDate> dates = List.of(TODAY, TODAY, TODAY.minusDays(1));
        assertEquals(2, WorkoutStats.currentStreak(dates, TODAY));
    }

    @Test
    void futureAndNullDatesAreIgnored() {
        List<LocalDate> dates = Arrays.asList(TODAY.plusDays(1), null, TODAY);
        assertEquals(1, WorkoutStats.currentStreak(dates, TODAY));
    }

    // ---- weeklyBuckets ----

    @Test
    void emptyInputGivesOneZeroBucketPerWeekOldestFirst() {
        List<WorkoutStats.WeekBucket> buckets = WorkoutStats.weeklyBuckets(List.of(), TODAY, 8);

        assertEquals(8, buckets.size());
        assertEquals(LocalDate.of(2026, 8, 17), buckets.get(0).weekStart());
        assertEquals(LocalDate.of(2026, 10, 5), buckets.get(7).weekStart());
        assertTrue(buckets.stream().allMatch(b -> b.totalReps() == 0));
    }

    @Test
    void repsAcrossDaysOfTheSameWeekAreAdded() {
        List<WorkoutStats.DayVolume> days = List.of(
                new WorkoutStats.DayVolume(LocalDate.of(2026, 9, 28), 10),   // Monday
                new WorkoutStats.DayVolume(LocalDate.of(2026, 10, 4), 20));  // Sunday, same week

        List<WorkoutStats.WeekBucket> buckets = WorkoutStats.weeklyBuckets(days, TODAY, 2);

        assertEquals(30, buckets.get(0).totalReps());
        assertEquals(0, buckets.get(1).totalReps());
    }

    @Test
    void mondayStartsANewWeek() {
        List<WorkoutStats.DayVolume> days = List.of(
                new WorkoutStats.DayVolume(LocalDate.of(2026, 10, 4), 5),    // Sunday, last week
                new WorkoutStats.DayVolume(LocalDate.of(2026, 10, 5), 7));   // Monday, this week

        List<WorkoutStats.WeekBucket> buckets = WorkoutStats.weeklyBuckets(days, TODAY, 2);

        assertEquals(5, buckets.get(0).totalReps());
        assertEquals(7, buckets.get(1).totalReps());
    }

    @Test
    void workoutsOutsideTheWindowAreExcluded() {
        List<WorkoutStats.DayVolume> days = List.of(
                new WorkoutStats.DayVolume(LocalDate.of(2026, 9, 27), 99),   // before the 2-week window
                new WorkoutStats.DayVolume(LocalDate.of(2026, 10, 12), 99)); // next week

        List<WorkoutStats.WeekBucket> buckets = WorkoutStats.weeklyBuckets(days, TODAY, 2);

        assertTrue(buckets.stream().allMatch(b -> b.totalReps() == 0));
    }

    @Test
    void zeroOrNegativeWeeksGivesNoBuckets() {
        assertTrue(WorkoutStats.weeklyBuckets(List.of(), TODAY, 0).isEmpty());
        assertTrue(WorkoutStats.weeklyBuckets(List.of(), TODAY, -3).isEmpty());
    }
}