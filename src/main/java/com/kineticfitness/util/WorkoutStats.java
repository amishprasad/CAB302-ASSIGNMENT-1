package com.kineticfitness.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns raw workout rows into the numbers the Dashboard and Workout History pages display.
 *
 * <p>Contains no JavaFX types, so every method here can be unit tested with plain JUnit —
 * no test harness, no visible window.
 */
public final class WorkoutStats {

    private WorkoutStats() {
    }

    /** One exercise as logged: the session it belongs to, and the reps it contributed. */
    public record Sample(String date, int totalReps) {}

    /** One whole session: every exercise on that date, added up. Becomes one bar on a chart. */
    public record DatedVolume(String date, int totalReps) {}

    /**
     * Groups samples into one entry per session date and adds up the reps.
     *
     * <p>Samples arrive newest-first (WorkoutDAO sorts by id descending). The result is
     * flipped to oldest-first so a chart reads left to right, then trimmed to the most
     * recent {@code limit} sessions so the bars stay wide enough to read.
     */
    public static List<DatedVolume> volumeByDate(List<Sample> samples, int limit) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Sample sample : samples) {
            totals.merge(sample.date(), sample.totalReps(), Integer::sum);
        }

        List<DatedVolume> volumes = new ArrayList<>();
        for (Map.Entry<String, Integer> total : totals.entrySet()) {
            volumes.add(new DatedVolume(total.getKey(), total.getValue()));
        }
        Collections.reverse(volumes);

        if (limit > 0 && volumes.size() > limit) {
            return new ArrayList<>(volumes.subList(volumes.size() - limit, volumes.size()));
        }
        return volumes;
    }

    /**
     * How many of the given session dates fall in the same week as {@code today},
     * counting a week as Monday through Sunday.
     *
     * <p>{@code today} is a parameter rather than being read from the clock inside, so a
     * test can pin it to a known date instead of behaving differently on a Sunday.
     */
    public static int sessionsThisWeek(List<LocalDate> dates, LocalDate today) {
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        int count = 0;
        for (LocalDate date : dates) {
            if (date != null && !date.isBefore(monday) && !date.isAfter(sunday)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Progress towards a target as a percentage, capped at 100 and never negative.
     * A target of zero or less returns 0 rather than dividing by zero.
     */
    public static int percentOf(double current, double target) {
        if (target <= 0) {
            return 0;
        }
        int percent = (int) Math.round((current / target) * 100);
        return Math.max(0, Math.min(100, percent));
    }
}