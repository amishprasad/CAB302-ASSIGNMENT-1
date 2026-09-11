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
        assertEquals("Unknown", user.getBmiCategory());
    }

    @Test
    void bmiCategoryIdentifiesUnderweight() {
        // 50 / (1.80 * 1.80) = 15.43
        User user = new User("abin", FitnessLevel.BEGINNER, 20, 180, 50);
        assertEquals("Underweight", user.getBmiCategory());
    }

    @Test
    void bmiCategoryIdentifiesHealthyWeight() {
        // 70 / (1.80 * 1.80) = 21.60
        User user = new User("abin", FitnessLevel.BEGINNER, 20, 180, 70);
        assertEquals("Healthy weight", user.getBmiCategory());
    }

    @Test
    void bmiCategoryIdentifiesOverweight() {
        // 85 / (1.80 * 1.80) = 26.23
        User user = new User("abin", FitnessLevel.BEGINNER, 20, 180, 85);
        assertEquals("Overweight", user.getBmiCategory());
    }

    @Test
    void bmiCategoryIdentifiesObese() {
        // 110 / (1.80 * 1.80) = 33.95
        User user = new User("abin", FitnessLevel.BEGINNER, 20, 180, 110);
        assertEquals("Obese", user.getBmiCategory());
    }

    @Test
    void totalRepsAllWorkoutsSumsCorrectly() {
        User user = new User("abin", FitnessLevel.BEGINNER);
        Workout w1 = new Workout(LocalDate.now());
        w1.addExercise(new Exercise("Push-ups", 2, 10)); // 20
        Workout w2 = new Workout(LocalDate.now());
        w2.addExercise(new Exercise("Squats", 3, 10)); // 30

        user.addWorkout(w1);
        user.addWorkout(w2);
        assertEquals(50, user.getTotalRepsAllWorkouts());
    }

    @Test
    void achievedGoalsCountCalculatesCorrectly() {
        User user = new User("abin", FitnessLevel.BEGINNER);
        Goal g1 = new Goal("Goal 1", 50);
        g1.addProgress(50); // achieved

        Goal g2 = new Goal("Goal 2", 100);
        g2.addProgress(30); // in progress

        user.addGoal(g1);
        user.addGoal(g2);

        assertEquals(1, user.getAchievedGoalsCount());
    }

    @Test
    void settersUpdateProfileDetails() {
        User user = new User("abin", FitnessLevel.BEGINNER);
        user.setUsername("alex");
        user.setAge(25);
        user.setHeightCm(175);
        user.setWeightKg(68);

        assertEquals("alex", user.getUsername());
        assertEquals(25, user.getAge());
        assertEquals(175, user.getHeightCm(), 0.001);
        assertEquals(68, user.getWeightKg(), 0.001);
    }
}

