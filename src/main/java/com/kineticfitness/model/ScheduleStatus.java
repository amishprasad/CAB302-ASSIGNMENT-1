package com.kineticfitness.model;

/**
 * Where a scheduled workout has got to. Replaces the old behaviour where
 * "Complete" and "Skip" both deleted the row, which made a finished session
 * indistinguishable from one that never existed.
 *
 * <p>US-32 &mdash; Manage scheduled workout.</p>
 */
public enum ScheduleStatus {

    /** Planned, not yet done. The only state a reminder can fire for. */
    SCHEDULED("Scheduled"),
    /** The user did the workout. */
    COMPLETED("Completed"),
    /** The user deliberately did not do it. */
    SKIPPED("Skipped");

    private final String display;

    ScheduleStatus(String display) {
        this.display = display;
    }

    /**
     * Whether this status is allowed to move to {@code target}.
     *
     * <p>A scheduled workout can be completed or skipped. A completed or skipped
     * one can only be put back to scheduled (an undo) &mdash; going straight from
     * completed to skipped is nonsense and is rejected. Moving to the status you
     * are already in is not a transition.</p>
     */
    public boolean canTransitionTo(ScheduleStatus target) {
        if (target == null || target == this) {
            return false;
        }
        if (this == SCHEDULED) {
            return true;
        }
        return target == SCHEDULED;
    }

    /** True when this workout is still waiting to happen. */
    public boolean isOpen() {
        return this == SCHEDULED;
    }

    @Override
    public String toString() {
        return display;
    }
}
