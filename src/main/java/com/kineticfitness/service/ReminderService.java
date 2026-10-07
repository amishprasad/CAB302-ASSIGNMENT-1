package com.kineticfitness.service;

import com.kineticfitness.model.ScheduleStatus;
import com.kineticfitness.model.ScheduledWorkout;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Decides which reminders are showing right now, and how to word them.
 *
 * <p>Every method takes {@code now} as a parameter instead of calling
 * {@link LocalDateTime#now()}. That single choice is what makes reminders
 * testable at all &mdash; a test can stand at any instant it likes and assert
 * exactly what the user would see.</p>
 *
 * <p>US-22 &mdash; Set an activity reminder. US-23 &mdash; See due reminders.</p>
 */
public final class ReminderService {

    private ReminderService() {
    }

    /**
     * Whether this workout's reminder should be on screen at {@code now}.
     *
     * <p>A reminder shows from its lead time until the session is over. It never
     * shows for a workout that has been completed or skipped, and never for one
     * the user did not ask to be reminded about.</p>
     */
    public static boolean isDue(ScheduledWorkout workout, LocalDateTime now) {
        if (workout == null || now == null) {
            return false;
        }
        if (workout.getStatus() != ScheduleStatus.SCHEDULED) {
            return false;
        }
        if (!workout.hasReminder()) {
            return false;
        }
        return !now.isBefore(workout.reminderAt()) && now.isBefore(workout.endsAt());
    }

    /**
     * Every reminder currently showing, soonest session first.
     *
     * @param workouts the user's schedule; may be null or empty
     */
    public static List<ScheduledWorkout> due(Collection<ScheduledWorkout> workouts, LocalDateTime now) {
        List<ScheduledWorkout> result = new ArrayList<>();
        if (workouts == null) {
            return result;
        }
        for (ScheduledWorkout workout : workouts) {
            if (isDue(workout, now)) {
                result.add(workout);
            }
        }
        result.sort(Comparator.comparing(ScheduledWorkout::startsAt));
        return result;
    }

    /**
     * The next session still to come, whether or not it has a reminder.
     *
     * <p>A session already under way does not count as upcoming.</p>
     */
    public static Optional<ScheduledWorkout> nextUpcoming(Collection<ScheduledWorkout> workouts, LocalDateTime now) {
        if (workouts == null || now == null) {
            return Optional.empty();
        }
        return workouts.stream()
                .filter(w -> w.getStatus() == ScheduleStatus.SCHEDULED)
                .filter(w -> w.startsAt().isAfter(now))
                .min(Comparator.comparing(ScheduledWorkout::startsAt));
    }

    /**
     * How the reminder reads, e.g. "Leg Day starts in 15 minutes" or
     * "Leg Day started 5 minutes ago".
     */
    public static String describe(ScheduledWorkout workout, LocalDateTime now) {
        if (workout == null || now == null) {
            throw new IllegalArgumentException("a workout and a time are required");
        }
        long minutesAway = Duration.between(now, workout.startsAt()).toMinutes();

        if (minutesAway > 0) {
            return workout.getName() + " starts in " + humanise(minutesAway) + ".";
        }
        if (minutesAway == 0) {
            return workout.getName() + " starts now.";
        }
        return workout.getName() + " started " + humanise(-minutesAway) + " ago.";
    }

    /**
     * A whole number of minutes written the way a person would say it:
     * "1 minute", "45 minutes", "1 hour", "2 hours 15 minutes".
     */
    public static String humanise(long minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes cannot be negative");
        }
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }
        long hours = minutes / 60;
        long remainder = minutes % 60;
        String hourPart = hours + (hours == 1 ? " hour" : " hours");
        if (remainder == 0) {
            return hourPart;
        }
        return hourPart + " " + remainder + (remainder == 1 ? " minute" : " minutes");
    }
}
