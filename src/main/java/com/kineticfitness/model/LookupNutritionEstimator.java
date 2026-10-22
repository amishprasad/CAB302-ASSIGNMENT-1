package com.kineticfitness.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class LookupNutritionEstimator implements NutritionEstimator {

    private final Map<String, Meal> foods = new LinkedHashMap<>();

    public LookupNutritionEstimator() {
        add("chicken and rice", 520, 40, 55, 12);
        add("protein shake", 180, 30, 5, 2);
        add("oats and banana", 350, 10, 60, 6);
        add("scrambled eggs", 220, 14, 2, 17);
        add("greek yoghurt", 150, 15, 8, 5);
        add("tuna sandwich", 380, 25, 40, 12);
        add("salad", 120, 4, 12, 6);
        add("pasta", 480, 16, 85, 8);
        add("banana", 105, 1, 27, 0);
        add("peanut butter toast", 300, 12, 28, 16);
    }

    private void add(String name, int calories, double protein, double carbs, double fats) {
        foods.put(name, new Meal(name, calories, protein, carbs, fats));
    }

    @Override
    public Optional<Meal> estimate(String mealName) {
        if (mealName == null || mealName.isBlank()) {
            return Optional.empty();
        }
        String key = mealName.trim().toLowerCase();
        if (foods.containsKey(key)) {
            return Optional.of(foods.get(key));
        }
        for (Map.Entry<String, Meal> entry : foods.entrySet()) {
            if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}