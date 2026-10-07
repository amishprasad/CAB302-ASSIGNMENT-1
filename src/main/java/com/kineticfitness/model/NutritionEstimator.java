package com.kineticfitness.model;

import java.util.Optional;

public interface NutritionEstimator {
    Optional<Meal> estimate(String mealName);
}