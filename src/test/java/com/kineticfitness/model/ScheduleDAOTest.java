package com.kineticfitness.db;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.ScheduledWorkout;
import com.kineticfitness.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleDAOTest {

    private final UserDAO userDAO = new UserDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    @BeforeEach
    void setUp() {
        // fresh in-memory DB per test — never touches kineticfitness.db
        DatabaseConnection.configure("jdbc:sqlite::memory:");
        userDAO.save(new User("testuser", FitnessLevel.BEGINNER, 25, 180, 75));
    }

    @Test
    void savedWorkoutIsRetrievedForUser() {
        scheduleDAO.save("testuser",
                new ScheduledWorkout("Leg Day", "15 Sep 2026", "18:00", "45 mins"));

        List<ScheduledWorkout> found = scheduleDAO.findForUser("testuser");

        assertEquals(1, found.size());
        assertEquals("Leg Day", found.get(0).getName());
    }

    @Test
    void deletedWorkoutIsRemoved() {
        scheduleDAO.save("testuser",
                new ScheduledWorkout("Push Day", "16 Sep 2026", "07:00", "60 mins"));
        ScheduledWorkout saved = scheduleDAO.findForUser("testuser").get(0);

        scheduleDAO.delete(saved.getId());

        assertTrue(scheduleDAO.findForUser("testuser").isEmpty());
    }

    @Test
    void userPersistsAndReloads() {
        User loaded = userDAO.findByUsername("testuser");

        assertNotNull(loaded);
        assertEquals(FitnessLevel.BEGINNER, loaded.getFitnessLevel());
        assertEquals(25, loaded.getAge());
    }
}