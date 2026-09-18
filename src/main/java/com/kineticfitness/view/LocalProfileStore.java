package com.kineticfitness.view;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;


public class LocalProfileStore {

    private static final LocalProfileStore INSTANCE = new LocalProfileStore();

    public static LocalProfileStore getInstance() {
        return INSTANCE;
    }

    private LocalProfileStore() {
    }

    // ---- Personal details (Profile page) ----
    public String firstName = "";
    public String email = "";
    public Gender gender = null;
    public String photoPath = null;
    public LocalDate dateOfBirth = null;
    public double heightCm = 0;
    public double weightKg = 0;
    public FitnessLevel fitnessLevel = FitnessLevel.BEGINNER;

    // ---- Fitness goals (Goals page) ----
    public PrimaryGoal primaryGoal = null;
    public double targetWeightKg = 0;
    public int weeklyWorkoutGoal = 4;
    public int weeklyExerciseDurationMinutes = 240;
    public FitnessLevel experienceLevel = FitnessLevel.BEGINNER;
    public final Set<String> preferredWorkoutTypes = new LinkedHashSet<>();
    public final Set<String> preferredWorkoutDays = new LinkedHashSet<>();

    /** Set automatically the first time a goal is saved; preserved across edits. */
    public LocalDate goalStartDate = null;
    /** The deadline the user picked for the goal. */
    public LocalDate goalTargetDate = null;
    /** Set when the goal is completed; null while it is still active. */
    public LocalDate goalAchievedDate = null;

    /**
     * Resets every field to its default. Called when a user signs in or out, so one
     * account's details can never be shown to the next person to log in.
     */
    public void clear() {
        firstName = "";
        email = "";
        gender = null;
        photoPath = null;
        dateOfBirth = null;
        heightCm = 0;
        weightKg = 0;
        fitnessLevel = FitnessLevel.BEGINNER;
        primaryGoal = null;
        targetWeightKg = 0;
        weeklyWorkoutGoal = 4;
        weeklyExerciseDurationMinutes = 240;
        experienceLevel = FitnessLevel.BEGINNER;
        preferredWorkoutTypes.clear();
        preferredWorkoutDays.clear();
        goalStartDate = null;
        goalTargetDate = null;
        goalAchievedDate = null;
        milestones.clear();
    }

    public boolean hasPersonalDetails() {
        return dateOfBirth != null;
    }

    public boolean isGoalAchieved() {
        return primaryGoal != null && goalAchievedDate != null;
    }

    public boolean hasGoals() {
        return primaryGoal != null;
    }

    // ---- Milestone goals (Goals page) ----
    public final java.util.List<Milestone> milestones = new java.util.ArrayList<>();

    public static class Milestone {
        public String description;
        public double targetValue;
        public String unit;
        public double currentValue;

        public Milestone(String description, double targetValue, String unit, double currentValue) {
            this.description = description;
            this.targetValue = targetValue;
            this.unit = unit;
            this.currentValue = currentValue;
        }

        public int progressPercent() {
            if (targetValue <= 0) return 0;
            int pct = (int) Math.round((currentValue / targetValue) * 100);
            return Math.max(0, Math.min(pct, 100));
        }

        public boolean isAchieved() {
            return currentValue >= targetValue;
        }
    }

    public enum FitnessLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    public enum Gender {
        MALE("Male"), FEMALE("Female"), OTHER("Other"), PREFER_NOT_TO_SAY("Prefer not to say");

        public final String display;

        Gender(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    public enum PrimaryGoal {
        LOSE_WEIGHT("Lose weight"), GAIN_MUSCLE("Gain muscle"),
        IMPROVE_FITNESS("Improve fitness"), MAINTAIN_WEIGHT("Maintain weight");

        public final String display;

        PrimaryGoal(String display) {
            this.display = display;
        }
    }
}