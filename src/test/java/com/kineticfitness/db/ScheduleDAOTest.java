package com.kineticfitness.db;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.ScheduleStatus;
import com.kineticfitness.model.ScheduledWorkout;
import com.kineticfitness.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistence for the schedule, against a throwaway in-memory database so the
 * suite never touches {@code kineticfitness.db}.
 *
 * <p>US-10, US-22, US-32.</p>
 */
class ScheduleDAOTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 20);
    private static final LocalTime SIX_PM = LocalTime.of(18, 0);

    private final UserDAO userDAO = new UserDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    @BeforeEach
    void setUp() {
        DatabaseConnection.configure("jdbc:sqlite::memory:");
        userDAO.save(new User("testuser", FitnessLevel.BEGINNER, 25, 180, 75));
    }

    @AfterEach
    void tearDown() {
        DatabaseConnection.reset();
    }

    private ScheduledWorkout saveLegDay() {
        return scheduleDAO.save("testuser",
                new ScheduledWorkout("Leg Day", DATE, SIX_PM, 45, 15));
    }

    @Test
    void savingReturnsTheWorkoutWithItsNewId() {
        ScheduledWorkout saved = saveLegDay();
        assertNotEquals(ScheduledWorkout.UNSAVED, saved.getId());
    }

    @Test
    @DisplayName("date, time, duration and reminder all survive the round trip")
    void everyFieldComesBackUnchanged() {
        saveLegDay();

        ScheduledWorkout loaded = scheduleDAO.findForUser("testuser").get(0);

        assertEquals("Leg Day", loaded.getName());
        assertEquals(DATE, loaded.getDate());
        assertEquals(SIX_PM, loaded.getStartTime());
        assertEquals(45, loaded.getDurationMinutes());
        assertEquals(15, loaded.getReminderLeadMinutes());
        assertEquals(ScheduleStatus.SCHEDULED, loaded.getStatus());
    }

    @Test
    @DisplayName("completing a workout keeps the record instead of deleting it")
    void statusIsPersisted() {
        int id = saveLegDay().getId();

        scheduleDAO.updateStatus(id, ScheduleStatus.COMPLETED);

        List<ScheduledWorkout> all = scheduleDAO.findForUser("testuser");
        assertEquals(1, all.size(), "a completed workout must still be in the history");
        assertEquals(ScheduleStatus.COMPLETED, all.get(0).getStatus());
    }

    @Test
    void completedWorkoutsDropOutOfTheOpenList() {
        int id = saveLegDay().getId();
        assertEquals(1, scheduleDAO.findOpenForUser("testuser").size());

        scheduleDAO.updateStatus(id, ScheduleStatus.COMPLETED);

        assertTrue(scheduleDAO.findOpenForUser("testuser").isEmpty());
    }

    @Test
    void reschedulingMovesTheWorkoutAndReopensIt() {
        int id = saveLegDay().getId();
        scheduleDAO.updateStatus(id, ScheduleStatus.SKIPPED);

        scheduleDAO.reschedule(id, LocalDate.of(2026, 9, 22), LocalTime.of(7, 30));

        ScheduledWorkout loaded = scheduleDAO.findForUser("testuser").get(0);
        assertEquals(LocalDate.of(2026, 9, 22), loaded.getDate());
        assertEquals(LocalTime.of(7, 30), loaded.getStartTime());
        assertEquals(ScheduleStatus.SCHEDULED, loaded.getStatus());
    }

    @Test
    void theReminderCanBeChangedAndSwitchedOff() {
        int id = saveLegDay().getId();

        scheduleDAO.updateReminderLead(id, 30);
        assertEquals(30, scheduleDAO.findForUser("testuser").get(0).getReminderLeadMinutes());

        scheduleDAO.updateReminderLead(id, ScheduledWorkout.NO_REMINDER);
        assertTrue(scheduleDAO.findForUser("testuser").get(0).hasReminder() == false);
    }

    @Test
    void deletingRemovesTheWorkoutEntirely() {
        scheduleDAO.delete(saveLegDay().getId());
        assertTrue(scheduleDAO.findForUser("testuser").isEmpty());
    }

    @Test
    void resultsComeBackSoonestFirst() {
        scheduleDAO.save("testuser", new ScheduledWorkout("Later", DATE.plusDays(2), SIX_PM, 30, 0));
        scheduleDAO.save("testuser", new ScheduledWorkout("Sooner", DATE, LocalTime.of(7, 0), 30, 0));
        scheduleDAO.save("testuser", new ScheduledWorkout("Same day, later", DATE, LocalTime.of(19, 0), 30, 0));

        List<String> order = scheduleDAO.findForUser("testuser").stream().map(ScheduledWorkout::getName).toList();

        assertEquals(List.of("Sooner", "Same day, later", "Later"), order);
    }

    @Test
    void oneUsersScheduleIsNotVisibleToAnother() {
        userDAO.save(new User("someoneelse", FitnessLevel.BEGINNER, 30, 170, 70));
        saveLegDay();

        assertTrue(scheduleDAO.findForUser("someoneelse").isEmpty());
    }

    @Test
    void anUnknownUserSimplyHasNothingScheduled() {
        assertTrue(scheduleDAO.findForUser("nobody").isEmpty());
    }
}
