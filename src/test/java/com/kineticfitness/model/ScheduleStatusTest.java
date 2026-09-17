package com.kineticfitness.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The transition rules for a scheduled workout's status.
 *
 * <p>US-32 &mdash; Manage scheduled workout.</p>
 */
class ScheduleStatusTest {

    @Test
    void aScheduledWorkoutCanBeCompleted() {
        assertTrue(ScheduleStatus.SCHEDULED.canTransitionTo(ScheduleStatus.COMPLETED));
    }

    @Test
    void aScheduledWorkoutCanBeSkipped() {
        assertTrue(ScheduleStatus.SCHEDULED.canTransitionTo(ScheduleStatus.SKIPPED));
    }

    @Test
    @DisplayName("a finished workout can be reopened, which is how Reschedule works")
    void completedAndSkippedCanGoBackToScheduled() {
        assertTrue(ScheduleStatus.COMPLETED.canTransitionTo(ScheduleStatus.SCHEDULED));
        assertTrue(ScheduleStatus.SKIPPED.canTransitionTo(ScheduleStatus.SCHEDULED));
    }

    @Test
    @DisplayName("you cannot skip something you already completed")
    void completedCannotBecomeSkipped() {
        assertFalse(ScheduleStatus.COMPLETED.canTransitionTo(ScheduleStatus.SKIPPED));
        assertFalse(ScheduleStatus.SKIPPED.canTransitionTo(ScheduleStatus.COMPLETED));
    }

    @Test
    void movingToTheStatusYouAreAlreadyInIsNotATransition() {
        for (ScheduleStatus status : ScheduleStatus.values()) {
            assertFalse(status.canTransitionTo(status), status + " -> " + status);
        }
    }

    @Test
    void aNullTargetIsRejectedRatherThanThrowing() {
        assertFalse(ScheduleStatus.SCHEDULED.canTransitionTo(null));
    }

    @Test
    void onlyScheduledCountsAsStillOpen() {
        assertTrue(ScheduleStatus.SCHEDULED.isOpen());
        assertFalse(ScheduleStatus.COMPLETED.isOpen());
        assertFalse(ScheduleStatus.SKIPPED.isOpen());
    }
}
