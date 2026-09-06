package com.kineticfitness.model;

/**
 * Represents a single fitness goal set by the user.
 * Supports the "Must have" requirement: user-defined goals across
 * daily / weekly / monthly / annual timeframes.
 */
public class Goal {

    private final GoalTimeframe timeframe;
    private final String description;
    private final int targetSessions;
    private int completedSessions;

    public Goal(GoalTimeframe timeframe, String description, int targetSessions) {
        this.timeframe = timeframe;
        this.description = description;
        this.targetSessions = targetSessions;
        this.completedSessions = 0;
    }

    public GoalTimeframe getTimeframe() {
        return timeframe;
    }

    public String getDescription() {
        return description;
    }

    public int getTargetSessions() {
        return targetSessions;
    }

    public int getCompletedSessions() {
        return completedSessions;
    }

    public void logSessionCompleted() {
        this.completedSessions++;
    }

    public double getProgressPercentage() {
        if (targetSessions == 0) return 0;
        double pct = (completedSessions * 100.0) / targetSessions;
        return Math.min(pct, 100.0);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %d/%d sessions (%.0f%%)",
                timeframe, description, completedSessions, targetSessions, getProgressPercentage());
    }
}
