package com.kineticfitness.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the rules a goal owns: when its target counts as reached, how long is
 * left, and how early it was completed.
 */
class FitnessGoalTest {

    private static final LocalDate START = LocalDate.of(2026, 8, 15);
    private static final LocalDate TARGET = LocalDate.of(2026, 11, 15);

    private FitnessGoal goal(GoalType type, double targetWeight) {
        return new FitnessGoal(type, targetWeight, 3, 150, FitnessLevel.BEGINNER,
                Set.of("Cardio"), Set.of("Mon"), START, TARGET, null);
    }

    @Test
    void loseWeightIsReachedAtOrBelowTarget() {
        FitnessGoal g = goal(GoalType.LOSE_WEIGHT, 70);
        assertTrue(g.isReachedBy(69.9));
        assertTrue(g.isReachedBy(70));
        assertFalse(g.isReachedBy(70.1));
    }

    @Test
    void gainMuscleIsReachedAtOrAboveTarget() {
        FitnessGoal g = goal(GoalType.GAIN_MUSCLE, 80);
        assertTrue(g.isReachedBy(80.5));
        assertFalse(g.isReachedBy(79.5));
    }

    @Test
    void maintainWeightAllowsATolerance() {
        FitnessGoal g = goal(GoalType.MAINTAIN_WEIGHT, 75);
        assertTrue(g.isReachedBy(75.9));
        assertTrue(g.isReachedBy(74.1));
        assertFalse(g.isReachedBy(76.5));
    }

    @Test
    void improveFitnessIsNeverReachedByWeightAlone() {
        FitnessGoal g = goal(GoalType.IMPROVE_FITNESS, 75);
        assertFalse(g.isReachedBy(75));
        assertFalse(g.getType().isMeasurable());
    }

    @Test
    void achievingReturnsANewGoalAndLeavesTheOriginalActive() {
        FitnessGoal active = goal(GoalType.LOSE_WEIGHT, 70);
        FitnessGoal done = active.achievedOn(LocalDate.of(2026, 11, 3));

        assertFalse(active.isAchieved());
        assertTrue(done.isAchieved());
        assertEquals(12, done.daysAheadOfTarget());
    }

    @Test
    void completingLateGivesANegativeMargin() {
        FitnessGoal done = goal(GoalType.LOSE_WEIGHT, 70)
                .achievedOn(LocalDate.of(2026, 11, 20));
        assertEquals(-5, done.daysAheadOfTarget());
    }

    @Test
    void anAchievedGoalCannotBeAchievedTwice() {
        FitnessGoal done = goal(GoalType.LOSE_WEIGHT, 70).achievedOn(TARGET);
        assertThrows(IllegalStateException.class, () -> done.achievedOn(TARGET));
    }

    @Test
    void daysRemainingGoesNegativeOnceTheTargetHasPassed() {
        FitnessGoal g = goal(GoalType.LOSE_WEIGHT, 70);
        assertEquals(10, g.daysRemaining(LocalDate.of(2026, 11, 5)));
        assertEquals(-2, g.daysRemaining(LocalDate.of(2026, 11, 17)));
        assertTrue(g.isOverdue(LocalDate.of(2026, 11, 17)));
        assertFalse(g.isOverdue(LocalDate.of(2026, 11, 1)));
    }

    @Test
    void durationIsTheWholeSpanTheUserChose() {
        assertEquals(92, goal(GoalType.LOSE_WEIGHT, 70).durationDays());
    }

    @Test
    void aTargetDateOnOrBeforeTheStartIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new FitnessGoal(GoalType.LOSE_WEIGHT, 70, 3, 150, FitnessLevel.BEGINNER,
                        Set.of("Cardio"), Set.of("Mon"), TARGET, START, null));
    }

    @Test
    void aNonPositiveTargetWeightIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new FitnessGoal(GoalType.LOSE_WEIGHT, 0, 3, 150, FitnessLevel.BEGINNER,
                        Set.of("Cardio"), Set.of("Mon"), START, TARGET, null));
    }

    @Test
    void preferredSetsCannotBeChangedThroughTheGetter() {
        FitnessGoal g = goal(GoalType.LOSE_WEIGHT, 70);
        assertThrows(UnsupportedOperationException.class,
                () -> g.getPreferredWorkoutTypes().add("Yoga"));
    }
}
