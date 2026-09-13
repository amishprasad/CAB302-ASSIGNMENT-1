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

    public boolean hasPersonalDetails() {
        return dateOfBirth != null;
    }

    public boolean hasGoals() {
        return primaryGoal != null;
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