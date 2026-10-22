package com.kineticfitness;

import com.kineticfitness.model.MealValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MealValidatorTest {

    @Test
    void validInputHasNoProblem() {
        assertTrue(MealValidator.validate("Eggs", "150", "12.5", "1", "10").isEmpty());
    }

    @Test
    void blankNameIsRejected() {
        assertEquals("Enter a meal name.", MealValidator.validate("  ", "150", "12", "1", "10").orElseThrow());
    }

    @Test
    void negativeCaloriesAreRejected() {
        assertEquals("Calories can't be negative.", MealValidator.validate("Eggs", "-5", "12", "1", "10").orElseThrow());
    }

    @Test
    void caloriesMustBeWholeNumber() {
        assertEquals("Calories must be a whole number.", MealValidator.validate("Eggs", "150.5", "12", "1", "10").orElseThrow());
    }

    @Test
    void nonNumericMacroIsRejected() {
        assertEquals("Protein (g) must be a number.", MealValidator.validate("Eggs", "150", "abc", "1", "10").orElseThrow());
    }

    @Test
    void notANumberIsRejected() {
        assertEquals("Carbs (g) must be a number.", MealValidator.validate("Eggs", "150", "12", "NaN", "10").orElseThrow());
    }

    @Test
    void negativeMacroIsRejected() {
        assertEquals("Fats (g) can't be negative.", MealValidator.validate("Eggs", "150", "12", "1", "-2").orElseThrow());
    }

    @Test
    void blankValueAsksForIt() {
        assertEquals("Enter carbs (g).", MealValidator.validate("Eggs", "150", "12", "", "10").orElseThrow());
    }
}