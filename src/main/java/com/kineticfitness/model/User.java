package com.kineticfitness.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private FitnessLevel fitnessLevel;
    private final List<Workout> workouts = new ArrayList<>();
    private final List<Goal> goals = new ArrayList<>();

    public User(String username, FitnessLevel fitnessLevel) {
        this.username = username;
        this.fitnessLevel = fitnessLevel;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public FitnessLevel getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(FitnessLevel fitnessLevel) { this.fitnessLevel = fitnessLevel; }

    public List<Workout> getWorkouts() { return workouts; }
    public void addWorkout(Workout workout) { workouts.add(workout); }

    public List<Goal> getGoals() { return goals; }
    public void addGoal(Goal goal) { goals.add(goal); }
}
