package com.kineticfitness.service;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.GoalType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the goal form's rules without starting JavaFX. */
class GoalValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 6);
    private static final LocalDate LATER = LocalDate.of(2027, 1, 6);

    private ValidationResult validate(String weight, LocalDate target,
                                      boolean types, boolean days) {
        return validate(weight, 150, 3, target, types, days);
    }

    private ValidationResult validate(String weight, Integer minutes, Integer workouts,
                                      LocalDate target, boolean types, boolean days) {
        return GoalValidator.validate(GoalType.LOSE_WEIGHT, weight, minutes, workouts,
                target, FitnessLevel.BEGINNER, types, days, TODAY);
    }

    @Test
    void acceptsACompleteForm() {
        assertTrue(validate("70", LATER, true, true).valid());
    }

    @Test
    void rejectsAMissingTargetWeight() {
        assertTrue(validate("  ", LATER, true, true).isInvalid());
    }

    @Test
    void rejectsTextThatIsNotANumber() {
        ValidationResult r = validate("heavy", LATER, true, true);
        assertTrue(r.isInvalid());
        assertEquals("Target weight must be a valid number.", r.message());
    }

    @Test
    void rejectsZeroAndNegativeWeights() {
        assertTrue(validate("0", LATER, true, true).isInvalid());
        assertTrue(validate("-5", LATER, true, true).isInvalid());
    }

    @Test
    void rejectsAnImplausiblyHeavyTarget() {
        assertTrue(validate("500", LATER, true, true).isInvalid());
    }

    @Test
    void rejectsAWeeklyDurationTheUserNeverChose() {
        ValidationResult r = validate("70", null, 3, LATER, true, true);
        assertTrue(r.isInvalid());
        assertEquals("Choose how long you want to exercise each week.", r.message());
    }

    @Test
    void rejectsAWeeklyWorkoutGoalTheUserNeverChose() {
        ValidationResult r = validate("70", 150, null, LATER, true, true);
        assertTrue(r.isInvalid());
        assertEquals("Choose how many workouts you want to do each week.", r.message());
    }

    @Test
    void rejectsZeroOrNegativeWeeklyFigures() {
        assertTrue(validate("70", 0, 3, LATER, true, true).isInvalid());
        assertTrue(validate("70", 150, 0, LATER, true, true).isInvalid());
        assertTrue(validate("70", -60, 3, LATER, true, true).isInvalid());
    }

    @Test
    void rejectsAMissingTargetDate() {
        assertTrue(validate("70", null, true, true).isInvalid());
    }

    @Test
    void rejectsATargetDateThatIsNotInTheFuture() {
        assertTrue(validate("70", TODAY, true, true).isInvalid());
        assertTrue(validate("70", TODAY.minusDays(1), true, true).isInvalid());
    }

    @Test
    void requiresAtLeastOneWorkoutTypeAndOneDay() {
        assertTrue(validate("70", LATER, false, true).isInvalid());
        assertTrue(validate("70", LATER, true, false).isInvalid());
    }

    @Test
    void readsAWeightWithSpacesOrAKgSuffix() {
        assertEquals(72.5, GoalValidator.parseTargetWeight(" 72.5 kg ").getAsDouble());
        assertEquals(70, GoalValidator.parseTargetWeight("70").getAsDouble());
        assertTrue(GoalValidator.parseTargetWeight("seventy").isEmpty());
        assertTrue(GoalValidator.parseTargetWeight(null).isEmpty());
    }
}
