package com.kineticfitness.model;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LookupNutritionEstimatorTest {

    private final NutritionEstimator estimator = new LookupNutritionEstimator();

    @Test
    void knownFoodIsEstimatedIgnoringCase() {
        Optional<Meal> result = estimator.estimate("Chicken And Rice");
        assertTrue(result.isPresent());
        assertEquals(520, result.get().getCalories());
    }

    @Test
    void partialNameStillMatches() {
        assertTrue(estimator.estimate("grilled chicken and rice bowl").isPresent());
    }

    @Test
    void unknownFoodReturnsEmpty() {
        assertTrue(estimator.estimate("zzzz").isEmpty());
    }

    @Test
    void blankInputReturnsEmpty() {
        assertTrue(estimator.estimate("   ").isEmpty());
    }
}