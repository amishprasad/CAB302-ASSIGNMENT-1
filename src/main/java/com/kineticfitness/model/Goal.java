package com.kineticfitness.model;

import java.util.LinkedHashSet;
import java.util.Set;


public class Goal {

    public enum GoalType {
        LOSE_WEIGHT("Lose weight"), GAIN_MUSCLE("Gain muscle"),
        IMPROVE_FITNESS("Improve fitness"), MAINTAIN_WEIGHT("Maintain weight");

        public final String display;

        GoalType(String display) {
            this.display = display;
        }
    }

    private GoalType goalType;
    private double targetWeightKg;
    private int weeklyWorkoutGoal;
    private int weeklyExerciseDurationMinutes;
    private FitnessLevel experienceLevel;
    private final Set<String> preferredWorkoutTypes = new LinkedHashSet<>();
    private final Set<String> preferredWorkoutDays = new LinkedHashSet<>();

    public Goal(GoalType goalType, double targetWeightKg, int weeklyWorkoutGoal,
                int weeklyExerciseDurationMinutes, FitnessLevel experienceLevel) {
        this.goalType = goalType;
        this.targetWeightKg = targetWeightKg;
        this.weeklyWorkoutGoal = weeklyWorkoutGoal;
        this.weeklyExerciseDurationMinutes = weeklyExerciseDurationMinutes;
        this.experienceLevel = experienceLevel;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public double getTargetWeightKg() {
        return targetWeightKg;
    }

    public int getWeeklyWorkoutGoal() {
        return weeklyWorkoutGoal;
    }

    public int getWeeklyExerciseDurationMinutes() {
        return weeklyExerciseDurationMinutes;
    }

    public FitnessLevel getExperienceLevel() {
        return experienceLevel;
    }

    public Set<String> getPreferredWorkoutTypes() {
        return preferredWorkoutTypes;
    }

    public Set<String> getPreferredWorkoutDays() {
        return preferredWorkoutDays;
    }
}
