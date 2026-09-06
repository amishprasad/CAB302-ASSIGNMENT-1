package com.kineticfitness.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoalTest {

    @Test
    void newGoalStartsWithNoProgress() {
        Goal goal = new Goal("Run 100 km", 100);
        assertEquals(0, goal.getCurrentValue());
        assertFalse(goal.isAchieved());
        assertEquals(0.0, goal.progressPercent(), 0.001);
    }

    @Test
    void progressAccumulates() {
        Goal goal = new Goal("Run 100 km", 100);
        goal.addProgress(30);
        goal.addProgress(20);
        assertEquals(50, goal.getCurrentValue());
        assertEquals(50.0, goal.progressPercent(), 0.001);
    }

    @Test
    void goalIsAchievedWhenTargetReached() {
        Goal goal = new Goal("Do 50 push-ups", 50);
        goal.addProgress(50);
        assertTrue(goal.isAchieved());
    }

    @Test
    void goalIsAchievedWhenTargetExceeded() {
        Goal goal = new Goal("Do 50 push-ups", 50);
        goal.addProgress(60);
        assertTrue(goal.isAchieved());
        assertEquals(120.0, goal.progressPercent(), 0.001);
    }

    @Test
    void progressPercentIsZeroForZeroTarget() {
        Goal goal = new Goal("Undefined target", 0);
        assertEquals(0.0, goal.progressPercent(), 0.001);
    }
}
