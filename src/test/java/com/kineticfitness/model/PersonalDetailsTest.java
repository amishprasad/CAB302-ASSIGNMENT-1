package com.kineticfitness.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the rules and derived figures a profile owns. */
class PersonalDetailsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 6);

    private PersonalDetails details(double heightCm, double weightKg, LocalDate dob) {
        return new PersonalDetails("Alex Taylor", "alex@example.com", Gender.OTHER,
                null, dob, heightCm, weightKg, FitnessLevel.BEGINNER);
    }

    @Test
    void ageCountsWholeYearsOnly() {
        assertEquals(30, details(180, 75, LocalDate.of(1996, 10, 6)).age(TODAY));
        assertEquals(29, details(180, 75, LocalDate.of(1996, 10, 7)).age(TODAY));
    }

    @Test
    void ageIsZeroForAFutureDateOfBirth() {
        assertEquals(0, details(180, 75, LocalDate.of(2030, 1, 1)).age(TODAY));
    }

    @Test
    void bmiAndCategoryComeFromTheSharedCalculator() {
        PersonalDetails d = details(180, 75, LocalDate.of(1996, 10, 6));
        assertEquals(23.1, d.bmi(), 0.05);
        assertEquals("Healthy weight", d.bmiCategory());
    }

    @Test
    void bmiCategoryTracksTheWhoBands() {
        assertEquals("Underweight", details(180, 55, TODAY.minusYears(30)).bmiCategory());
        assertEquals("Overweight", details(180, 90, TODAY.minusYears(30)).bmiCategory());
        assertEquals("Obese", details(180, 105, TODAY.minusYears(30)).bmiCategory());
    }

    @Test
    void changingWeightLeavesTheOriginalAlone() {
        PersonalDetails before = details(180, 85, TODAY.minusYears(30));
        PersonalDetails after = before.withWeight(78);

        assertEquals(85, before.getWeightKg());
        assertEquals(78, after.getWeightKg());
        assertEquals(before.getFullName(), after.getFullName());
    }

    @Test
    void blankNameOrEmailIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new PersonalDetails("  ", "a@b.com", Gender.MALE, null,
                        TODAY.minusYears(20), 180, 75, FitnessLevel.BEGINNER));
        assertThrows(IllegalArgumentException.class, () ->
                new PersonalDetails("Alex", "", Gender.MALE, null,
                        TODAY.minusYears(20), 180, 75, FitnessLevel.BEGINNER));
    }

    @Test
    void measurementsOutsideTheAcceptedRangeAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> details(10, 75, TODAY.minusYears(20)));
        assertThrows(IllegalArgumentException.class,
                () -> details(180, 5, TODAY.minusYears(20)));
        assertThrows(IllegalArgumentException.class,
                () -> details(400, 75, TODAY.minusYears(20)));
    }

    @Test
    void nameIsStoredTrimmed() {
        assertEquals("Alex Taylor",
                new PersonalDetails("  Alex Taylor  ", "a@b.com", Gender.MALE, null,
                        TODAY.minusYears(20), 180, 75, FitnessLevel.BEGINNER).getFullName());
    }
}
