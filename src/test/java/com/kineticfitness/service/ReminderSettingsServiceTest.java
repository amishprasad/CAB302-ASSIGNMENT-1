package com.kineticfitness.service;

import com.kineticfitness.db.DatabaseConnection;
import com.kineticfitness.db.ScheduleDAO;
import com.kineticfitness.db.UserDAO;
import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.ScheduledWorkout;
import com.kineticfitness.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

class ReminderSettingsServiceTest {
    private final ScheduleDAO repository = new ScheduleDAO();
    private final LocalDateTime now = LocalDateTime.of(2026, 10, 6, 18, 0);
    private ReminderSettingsService service;
    private ScheduledWorkout workout;

    @BeforeEach
    void setUp() {
        DatabaseConnection.configure("jdbc:sqlite::memory:");
        new UserDAO().save(new User("testuser", FitnessLevel.BEGINNER, 25, 180, 75));
        workout = repository.save("testuser", new ScheduledWorkout(
                "Leg Day", now.toLocalDate(), now.plusMinutes(10).toLocalTime(), 45, 0));
        service = new ReminderSettingsService(repository);
    }

    @AfterEach
    void tearDown() {
        DatabaseConnection.reset();
    }

    @Test
    void enablingAReminderPersistsBeforeNotifyingAndMakesItDueImmediately() {
        List<Integer> notifiedIds = new ArrayList<>();
        service.addListener(id -> {
            notifiedIds.add(id);
            assertTrue(ReminderService.isDue(repository.findForUser("testuser").get(0), now));
        });

        service.updateReminder(workout.getId(), 15);

        ScheduledWorkout saved = repository.findForUser("testuser").get(0);
        assertEquals(15, saved.getReminderLeadMinutes());
        assertEquals(workout.getId(), saved.getId());
        assertEquals(workout.startsAt(), saved.startsAt());
        assertEquals(List.of(workout.getId()), notifiedIds);
    }

    @Test
    void disablingAReminderNotifiesListenersAfterItStopsBeingDue() {
        service.updateReminder(workout.getId(), 15);
        List<Boolean> dueAfterChange = new ArrayList<>();
        service.addListener(id -> dueAfterChange.add(
                ReminderService.isDue(repository.findForUser("testuser").get(0), now)));

        service.updateReminder(workout.getId(), 0);

        assertEquals(List.of(false), dueAfterChange);
        assertFalse(repository.findForUser("testuser").get(0).hasReminder());
    }

    @Test
    void allRegisteredObserversReceiveTheChangeAndRemovedObserversDoNot() {
        List<Integer> first = new ArrayList<>();
        List<Integer> second = new ArrayList<>();
        IntConsumer firstListener = first::add;
        service.addListener(firstListener);
        service.addListener(second::add);
        service.updateReminder(workout.getId(), 15);
        service.removeListener(firstListener);
        service.updateReminder(workout.getId(), 30);

        assertEquals(List.of(workout.getId()), first);
        assertEquals(List.of(workout.getId(), workout.getId()), second);
    }

    @Test
    void invalidLeadTimeDoesNotChangeStorageOrNotify() {
        List<Integer> notifications = new ArrayList<>();
        service.addListener(notifications::add);

        assertThrows(IllegalArgumentException.class, () -> service.updateReminder(workout.getId(), -1));

        assertEquals(0, repository.findForUser("testuser").get(0).getReminderLeadMinutes());
        assertTrue(notifications.isEmpty());
    }

    @Test
    void missingWorkoutDoesNotNotify() {
        List<Integer> notifications = new ArrayList<>();
        service.addListener(notifications::add);

        assertThrows(IllegalArgumentException.class, () -> service.updateReminder(99999, 15));

        assertTrue(notifications.isEmpty());
    }

    @Test
    void storageFailureDoesNotNotify() {
        ReminderSettingsService failing = new ReminderSettingsService(new ScheduleDAO() {
            @Override
            public void updateReminderLead(int id, int minutes) {
                throw new IllegalStateException("Database unavailable");
            }
        });
        List<Integer> notifications = new ArrayList<>();
        failing.addListener(notifications::add);

        assertThrows(IllegalStateException.class, () -> failing.updateReminder(workout.getId(), 15));

        assertTrue(notifications.isEmpty());
    }
}
