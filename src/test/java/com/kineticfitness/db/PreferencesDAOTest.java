package com.kineticfitness.db;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import com.kineticfitness.util.UnitSystem;
import com.kineticfitness.view.LocalProfileStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistence for the Settings page's units and notification choices, against a
 * throwaway in-memory database so the suite never touches {@code kineticfitness.db}.
 */
class PreferencesDAOTest {

    private final UserDAO userDAO = new UserDAO();
    private final PreferencesDAO preferencesDAO = new PreferencesDAO();

    /**
     * {@link LocalProfileStore} is a one-per-app singleton: there is no public
     * constructor, only {@code getInstance()}. Every test below shares that single
     * instance, so each one resets it with {@link LocalProfileStore#clear()} first —
     * the same reset the app performs on sign-out — to stop one test's data leaking
     * into the next.
     */
    private final LocalProfileStore store = LocalProfileStore.getInstance();

    @BeforeEach
    void setUp() {
        DatabaseConnection.configure("jdbc:sqlite::memory:");
        userDAO.save(new User("testuser", FitnessLevel.BEGINNER, 25, 180, 75));
        store.clear();
    }

    @AfterEach
    void tearDown() {
        DatabaseConnection.reset();
        store.clear();
    }

    @Test
    @DisplayName("A user who has never saved Settings loads no row, so defaults stay in place")
    void load_returnsFalseAndLeavesDefaults_whenNothingSavedYet() {
        boolean found = preferencesDAO.load(store, "testuser");

        assertFalse(found);
        assertEquals(UnitSystem.METRIC, store.unitSystem);
        assertTrue(store.notifyWorkoutReminders);
        assertTrue(store.notifyGoalAlerts);
        assertFalse(store.notifyWeeklySummary);
    }

    @Test
    @DisplayName("Saved preferences are read back exactly as they were saved")
    void saveThenLoad_roundTripsEveryField() {
        store.unitSystem = UnitSystem.IMPERIAL;
        store.notifyWorkoutReminders = false;
        store.notifyGoalAlerts = false;
        store.notifyWeeklySummary = true;
        preferencesDAO.save(store, "testuser");

        store.clear(); // simulate a fresh load, e.g. after restarting the app
        boolean found = preferencesDAO.load(store, "testuser");

        assertTrue(found);
        assertEquals(UnitSystem.IMPERIAL, store.unitSystem);
        assertFalse(store.notifyWorkoutReminders);
        assertFalse(store.notifyGoalAlerts);
        assertTrue(store.notifyWeeklySummary);
    }

    @Test
    @DisplayName("Saving again updates the same row rather than inserting a second one")
    void save_overwritesPreviousChoice() {
        store.unitSystem = UnitSystem.IMPERIAL;
        preferencesDAO.save(store, "testuser");

        store.unitSystem = UnitSystem.METRIC;
        preferencesDAO.save(store, "testuser");

        store.clear();
        preferencesDAO.load(store, "testuser");
        assertEquals(UnitSystem.METRIC, store.unitSystem);
    }
}
