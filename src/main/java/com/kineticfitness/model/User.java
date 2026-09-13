package com.kineticfitness.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private LocalDate dateOfBirth;
    private FitnessLevel fitnessLevel;
    private double heightCm;
    private double weightKg;
    private final List<Workout> workouts = new ArrayList<>();

    public User(String username, FitnessLevel fitnessLevel) {
        this.username = username;
        this.fitnessLevel = fitnessLevel;
    }

    public User(String username, String firstName, String lastName, String gender, String email,
                LocalDate dateOfBirth, FitnessLevel fitnessLevel, double heightCm, double weightKg) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.fitnessLevel = fitnessLevel;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    /** Age computed from dateOfBirth. Returns 0 if dateOfBirth hasn't been set. */
    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public FitnessLevel getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(FitnessLevel fitnessLevel) { this.fitnessLevel = fitnessLevel; }

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

    /**
     * Standard WHO classification of Body Mass Index.
     */
    public String getBmiCategory() {
        double bmi = getBmi();
        if (bmi <= 0) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Healthy weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    public List<Workout> getWorkouts() { return workouts; }
    public void addWorkout(Workout workout) { workouts.add(workout); }

    /** Total repetitions summed across all logged workouts. */
    public int getTotalRepsAllWorkouts() {
        return workouts.stream().mapToInt(Workout::totalReps).sum();
    }
}
