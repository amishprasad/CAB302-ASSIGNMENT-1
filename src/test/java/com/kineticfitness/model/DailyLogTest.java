package com.kineticfitness.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DailyLogTest {

    @Test
    void totalCaloriesSumsAllMeals() {
        DailyLog log = new DailyLog(LocalDate.now());
        log.addMeal(new Meal("Chicken and rice", 520, 40, 55, 12));
        log.addMeal(new Meal("Protein shake", 180, 30, 5, 2));

        assertEquals(700, log.getTotalCalories());
    }

    @Test
    void totalProteinIncludesSupplements() {
        DailyLog log = new DailyLog(LocalDate.now());
        log.addMeal(new Meal("Chicken and rice", 520, 40, 55, 12));
        log.addProteinSupplement(25);

        assertEquals(65.0, log.getTotalProtein());
    }

    @Test
    void waterIntakeAccumulates() {
        DailyLog log = new DailyLog(LocalDate.now());
        log.addWater(500);
        log.addWater(300);

        assertEquals(800.0, log.getWaterIntakeMl());
    }
}