package com.kineticfitness.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyLog {
    private LocalDate date;
    private final List<Meal> meals = new ArrayList<>();
    private double waterIntakeMl;
    private double proteinSupplementGrams;

    public DailyLog(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() { return date; }

    public List<Meal> getMeals() { return meals; }
    public void addMeal(Meal meal) { meals.add(meal); }

    public double getWaterIntakeMl() { return waterIntakeMl; }
    public void addWater(double ml) { this.waterIntakeMl += ml; }

    public double getProteinSupplementGrams() { return proteinSupplementGrams; }
    public void addProteinSupplement(double grams) { this.proteinSupplementGrams += grams; }

    public int getTotalCalories() {
        return meals.stream().mapToInt(Meal::getCalories).sum();
    }

    public double getTotalProtein() {
        return meals.stream().mapToDouble(Meal::getProtein).sum() + proteinSupplementGrams;
    }
}