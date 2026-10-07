package com.kineticfitness.model;

import com.kineticfitness.util.BmiCalculator;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * The personal half of a user's profile: who they are and their current body
 * measurements.
 *
 * <p>Immutable, and every invariant is checked in the constructor, so an object
 * of this type is always a profile that could legitimately be saved. A screen
 * cannot produce a half-valid one and persist it by accident.</p>
 *
 * <p>Derived figures are behaviour on the object rather than arithmetic repeated
 * in each view. BMI delegates to {@link BmiCalculator} so the WHO thresholds stay
 * written down exactly once. {@link #age(LocalDate)} takes today as a parameter
 * rather than calling {@link LocalDate#now()}, which keeps it testable.</p>
 *
 * <p>US-04 &mdash; Create a profile. US-05 &mdash; View and edit my profile.</p>
 */
public final class PersonalDetails {

    /** Bounds the form accepts, so a typo cannot be stored as a measurement. */
    public static final double MIN_HEIGHT_CM = 50;
    public static final double MAX_HEIGHT_CM = 300;
    public static final double MIN_WEIGHT_KG = 20;
    public static final double MAX_WEIGHT_KG = 500;

    private final String fullName;
    private final String email;
    private final Gender gender;
    private final String photoPath;
    private final LocalDate dateOfBirth;
    private final double heightCm;
    private final double weightKg;
    private final FitnessLevel fitnessLevel;

    /**
     * @param photoPath optional path to a profile picture; may be null
     * @throws IllegalArgumentException if any field is missing or out of range
     */
    public PersonalDetails(String fullName,
                           String email,
                           Gender gender,
                           String photoPath,
                           LocalDate dateOfBirth,
                           double heightCm,
                           double weightKg,
                           FitnessLevel fitnessLevel) {

        this.fullName = requireText(fullName, "full name");
        this.email = requireText(email, "email");
        this.gender = Objects.requireNonNull(gender, "gender");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "date of birth");
        this.fitnessLevel = Objects.requireNonNull(fitnessLevel, "fitness level");

        if (heightCm < MIN_HEIGHT_CM || heightCm > MAX_HEIGHT_CM) {
            throw new IllegalArgumentException("height must be between "
                    + (int) MIN_HEIGHT_CM + " and " + (int) MAX_HEIGHT_CM + " cm");
        }
        if (weightKg < MIN_WEIGHT_KG || weightKg > MAX_WEIGHT_KG) {
            throw new IllegalArgumentException("weight must be between "
                    + (int) MIN_WEIGHT_KG + " and " + (int) MAX_WEIGHT_KG + " kg");
        }

        this.photoPath = photoPath;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    // ---------- derived figures ----------

    /** Whole years between the date of birth and the given day; 0 if not yet born. */
    public int age(LocalDate today) {
        if (dateOfBirth.isAfter(today)) {
            return 0;
        }
        return Period.between(dateOfBirth, today).getYears();
    }

    /** Body Mass Index from the stored height and weight. */
    public double bmi() {
        return BmiCalculator.bmi(heightCm, weightKg);
    }

    /** WHO classification of {@link #bmi()}, e.g. "Healthy weight". */
    public String bmiCategory() {
        return BmiCalculator.category(bmi());
    }

    /**
     * A copy of these details with a new current weight — the one field the user
     * changes often, and the one the goals screen watches.
     */
    public PersonalDetails withWeight(double newWeightKg) {
        return new PersonalDetails(fullName, email, gender, photoPath,
                dateOfBirth, heightCm, newWeightKg, fitnessLevel);
    }

    // ---------- accessors ----------

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Gender getGender() { return gender; }
    public String getPhotoPath() { return photoPath; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public double getHeightCm() { return heightCm; }
    public double getWeightKg() { return weightKg; }
    public FitnessLevel getFitnessLevel() { return fitnessLevel; }

    @Override
    public String toString() {
        return "PersonalDetails[" + fullName + ", " + heightCm + "cm, " + weightKg + "kg]";
    }
}
