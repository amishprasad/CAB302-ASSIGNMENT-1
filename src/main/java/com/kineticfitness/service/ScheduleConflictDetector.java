package com.kineticfitness.service;

import com.kineticfitness.model.ScheduledWorkout;

import java.util.Collection;

/**
 * Detects whether a workout the user is about to schedule overlaps one they
 * have already booked.
 *
 * <p>US-10 &mdash; Schedule a workout.</p>
 */
public final class ScheduleConflictDetector {

    private ScheduleConflictDetector() {
    }

    /**
     * Whether {@code candidate} overlaps any workout in {@code existing}.
     *
     * <p>Two sessions overlap when each starts before the other finishes. Workouts
     * that have been completed or skipped are ignored &mdash; they are history, not
     * a commitment, so they do not block the slot.</p>
     *
     * @param candidate the workout being scheduled
     * @param existing  the workouts already in the schedule
     */
    public static boolean clashes(ScheduledWorkout candidate, Collection<ScheduledWorkout> existing) {
        for (ScheduledWorkout other : existing) {
            // Saved workouts keep their identity when moved. Never compare a
            // moved session with its original row; unsaved rows all have id 0.
            if (candidate.getId() != ScheduledWorkout.UNSAVED
                    && candidate.getId() == other.getId()) {
                continue;
            }
            if (!other.isOpen()) {
                continue;   // a completed or skipped session no longer holds its slot
            }
            if (candidate.startsAt().isBefore(other.endsAt())
                    && other.startsAt().isBefore(candidate.endsAt())) {
                return true;
            }
        }
        return false;
    }
}
