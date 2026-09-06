package com.kineticfitness.model;

import java.time.LocalDateTime;

/**
 * Represents a single logged workout entry.
 * Supports the "Must have" requirement: user-inputted tracking of
 * workouts, steps, working sets, and reps.
 */
public class Workout {

    private final String activityType; // e.g. "Run", "Strength", "Swim", "Steps"
    private final int sets;
    private final int reps;
    private final int steps;
    private final int durationMinutes;
    private final LocalDateTime loggedAt;

    public Workout(String activityType, int sets, int reps, int steps, int durationMinutes) {
        this.activityType = activityType;
        this.sets = sets;
        this.reps = reps;
        this.steps = steps;
        this.durationMinutes = durationMinutes;
        this.loggedAt = LocalDateTime.now();
    }

    public String getActivityType() {
        return activityType;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    public int getSteps() {
        return steps;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | sets=%d reps=%d steps=%d duration=%dmin",
                loggedAt.toLocalDate(), activityType, sets, reps, steps, durationMinutes);
    }
}
