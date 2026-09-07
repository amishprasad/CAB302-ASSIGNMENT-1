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
}
