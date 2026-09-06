package com.kineticfitness.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void newUserStartsWithNoWorkouts() {
        User user = new User("abin", FitnessLevel.BEGINNER);
        assertEquals(0, user.getWorkouts().size());
    }

    @Test
    void canAddWorkoutToUser() {
        User user = new User("abin", FitnessLevel.BEGINNER);
        user.addWorkout(new Workout(LocalDate.now()));
        assertEquals(1, user.getWorkouts().size());
    }

    @Test
    void fitnessLevelCanBeUpdated() {
        User user = new User("abin", FitnessLevel.BEGINNER);
        user.setFitnessLevel(FitnessLevel.ADVANCED);
        assertEquals(FitnessLevel.ADVANCED, user.getFitnessLevel());
    }

    @Test
    void profileConstructorStoresDetails() {
        User user = new User("abin", FitnessLevel.INTERMEDIATE, 27, 180, 81);
        assertEquals(27, user.getAge());
        assertEquals(180, user.getHeightCm(), 0.001);
        assertEquals(81, user.getWeightKg(), 0.001);
    }

    @Test
    void bmiIsCalculatedFromHeightAndWeight() {
        // 81 / (1.80 * 1.80) == 25.0
        User user = new User("abin", FitnessLevel.INTERMEDIATE, 27, 180, 81);
        assertEquals(25.0, user.getBmi(), 0.001);
    }

    @Test
    void bmiIsZeroWhenHeightNotSet() {
        User user = new User("abin", FitnessLevel.BEGINNER);
        assertEquals(0.0, user.getBmi(), 0.001);
    }
}
