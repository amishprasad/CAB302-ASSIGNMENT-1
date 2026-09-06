package com.kineticfitness.service;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.Goal;
import com.kineticfitness.model.User;
import com.kineticfitness.model.Workout;

/**
 * Core service layer for Kinetic Fitness.
 * Very early prototype — logic here is intentionally simple and will be
 * replaced/expanded (e.g. AI recommendations, real analytics) in later builds.
 */
public class FitnessTracker {

    /**
     * "Must have": Give realistic goals for user to achieve, based on
     * stated fitness level. Placeholder logic for the prototype stage.
     */
    public int suggestRealisticWeeklySessions(FitnessLevel level) {
        switch (level) {
            case BEGINNER:
                return 2;
            case INTERMEDIATE:
                return 4;
            case ADVANCED:
                return 6;
            default:
                return 3;
        }
    }

    /**
     * "Must have": Summarisation into graphs of user progress.
     * Prototype uses a simple ASCII bar chart in the console; a real
     * desktop UI build would replace this with an actual chart component.
     */
    public String buildProgressGraph(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Progress Summary for ").append(user.getName()).append(" ---\n");

        if (user.getGoals().isEmpty()) {
            sb.append("No goals set yet.\n");
            return sb.toString();
        }

        for (Goal goal : user.getGoals()) {
            sb.append(String.format("%-10s %-20s ", goal.getTimeframe(), goal.getDescription()));
            sb.append(renderBar(goal.getProgressPercentage()));
            sb.append(String.format(" %d/%d%n", goal.getCompletedSessions(), goal.getTargetSessions()));
        }

        sb.append(String.format("Total workouts logged: %d%n", user.getWorkouts().size()));
        return sb.toString();
    }

    private String renderBar(double percentage) {
        int totalBlocks = 20;
        int filledBlocks = (int) Math.round((percentage / 100.0) * totalBlocks);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < totalBlocks; i++) {
            bar.append(i < filledBlocks ? "#" : "-");
        }
        bar.append("]");
        return bar.toString();
    }

    public void logWorkout(User user, Workout workout) {
        user.logWorkout(workout);
    }
}
