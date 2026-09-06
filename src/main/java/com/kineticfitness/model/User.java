package com.kineticfitness.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private FitnessLevel fitnessLevel;
    private int age;
    private double heightCm;
    private double weightKg;
    private final List<Workout> workouts = new ArrayList<>();
    private final List<Goal> goals = new ArrayList<>();

    public User(String username, FitnessLevel fitnessLevel) {
        this.username = username;
        this.fitnessLevel = fitnessLevel;
    }

    public User(String username, FitnessLevel fitnessLevel, int age, double heightCm, double weightKg) {
        this(username, fitnessLevel);
        this.age = age;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public FitnessLevel getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(FitnessLevel fitnessLevel) { this.fitnessLevel = fitnessLevel; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    /**
     * Body Mass Index derived from the stored height and weight.
     * Returns 0 when height has not been set, to avoid dividing by zero.
     */
    public double getBmi() {
        if (heightCm <= 0) return 0;
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    public List<Workout> getWorkouts() { return workouts; }
    public void addWorkout(Workout workout) { workouts.add(workout); }

    public List<Goal> getGoals() { return goals; }
    public void addGoal(Goal goal) { goals.add(goal); }
}
