package com.kineticfitness.model;

import java.time.LocalDateTime;

public class Meal {
    private String name;
    private int calories;
    private double protein;
    private double carbs;
    private double fats;
    private LocalDateTime loggedAt;

    public Meal(String name, int calories, double protein, double carbs, double fats) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.loggedAt = LocalDateTime.now();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFats() { return fats; }
    public void setFats(double fats) { this.fats = fats; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
}