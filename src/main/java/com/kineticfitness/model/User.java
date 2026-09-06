package com.kineticfitness.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Kinetic Fitness user profile.
 * Supports the "Must have" requirement: user enters fitness level and goals.
 * Basic version of "Should have" User Profiles feature.
 */
public class User {

    private final String name;
    private FitnessLevel fitnessLevel;
    private final List<Goal> goals = new ArrayList<>();
    private final List<Workout> workouts = new ArrayList<>();

    public User(String name, FitnessLevel fitnessLevel) {
        this.name = name;
        this.fitnessLevel = fitnessLevel;
    }

    public String getName() {
        return name;
    }

    public FitnessLevel getFitnessLevel() {
        return fitnessLevel;
    }

    public void setFitnessLevel(FitnessLevel fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public void addGoal(Goal goal) {
        goals.add(goal);
    }

    public List<Workout> getWorkouts() {
        return workouts;
    }

    public void logWorkout(Workout workout) {
        workouts.add(workout);
        // naive: crediting every active goal with one completed session
        for (Goal goal : goals) {
            goal.logSessionCompleted();
        }
    }
}
