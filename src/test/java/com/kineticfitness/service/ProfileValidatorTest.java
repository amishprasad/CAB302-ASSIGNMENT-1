package com.kineticfitness.service;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the profile form's rules without starting JavaFX. */
class ProfileValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 6);
    private static final LocalDate DOB = LocalDate.of(1996, 5, 2);

    private ValidationResult validate(String name, String email, LocalDate dob,
                                      String height, String weight) {
        return ProfileValidator.validate(name, email, Gender.MALE, dob, height, weight,
                FitnessLevel.BEGINNER, TODAY);
    }

    @Test
    void acceptsACompleteProfile() {
        assertTrue(validate("Alex Taylor", "alex@example.com", DOB, "180", "75").valid());
    }

    @Test
    void acceptsAFullNameWithSpaces() {
        assertTrue(validate("Mary Jane Watson", "mj@example.com", DOB, "165", "60").valid());
    }

    @Test
    void rejectsAnyMissingField() {
        assertTrue(validate("", "alex@example.com", DOB, "180", "75").isInvalid());
        assertTrue(validate("Alex", "", DOB, "180", "75").isInvalid());
        assertTrue(validate("Alex", "alex@example.com", null, "180", "75").isInvalid());
        assertTrue(validate("Alex", "alex@example.com", DOB, "", "75").isInvalid());
    }

    @Test
    void rejectsMalformedEmailAddresses() {
        assertTrue(validate("Alex", "alex.example.com", DOB, "180", "75").isInvalid());
        assertTrue(validate("Alex", "@example.com", DOB, "180", "75").isInvalid());
        assertTrue(validate("Alex", "alex@example", DOB, "180", "75").isInvalid());
        assertTrue(validate("Alex", "a@b@c.com", DOB, "180", "75").isInvalid());
    }

    @Test
    void rejectsADateOfBirthInTheFuture() {
        assertTrue(validate("Alex", "a@b.com", TODAY.plusDays(1), "180", "75").isInvalid());
        assertTrue(validate("Alex", "a@b.com", TODAY, "180", "75").valid());
    }

    @Test
    void rejectsMeasurementsThatAreNotNumbers() {
        ValidationResult r = validate("Alex", "a@b.com", DOB, "tall", "75");
        assertTrue(r.isInvalid());
        assertEquals("Height and weight must be valid numbers.", r.message());
    }

    @Test
    void rejectsImplausibleMeasurements() {
        assertTrue(validate("Alex", "a@b.com", DOB, "15", "75").isInvalid());
        assertTrue(validate("Alex", "a@b.com", DOB, "180", "5").isInvalid());
        assertTrue(validate("Alex", "a@b.com", DOB, "180", "900").isInvalid());
    }

    @Test
    void rejectsNegativeMeasurements() {
        assertTrue(validate("Alex", "a@b.com", DOB, "-180", "75").isInvalid());
    }

    @Test
    void readsMeasurementsWithUnitsOrSpaces() {
        assertEquals(180, ProfileValidator.parseMeasurement(" 180 cm ").getAsDouble());
        assertEquals(75.5, ProfileValidator.parseMeasurement("75.5kg").getAsDouble());
        assertTrue(ProfileValidator.parseMeasurement("heavy").isEmpty());
        assertTrue(ProfileValidator.parseMeasurement(null).isEmpty());
    }
}
