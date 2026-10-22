package com.kineticfitness.db;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import com.kineticfitness.view.LocalProfileStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Profile and goal persistence, against a throwaway in-memory database so the suite
 * never touches {@code kineticfitness.db}.
 *
 * <p>{@link LocalProfileStore} is a shared singleton, so every test resets it before
 * and after running. The pattern is: fill the store, {@code save}, {@code clear()} the
 * store, {@code load}, then assert on what came back from the database.
 */
class ProfileDAOTest {

    private final UserDAO userDAO = new UserDAO();
    private final ProfileDAO profileDAO = new ProfileDAO();
    private final LocalProfileStore store = LocalProfileStore.getInstance();

    @BeforeEach
    void setUp() {
        DatabaseConnection.configure("jdbc:sqlite::memory:");
        store.clear();
        userDAO.save(new User("alice", FitnessLevel.BEGINNER, 25, 180, 75));
        userDAO.save(new User("bob", FitnessLevel.BEGINNER, 30, 170, 70));
    }

    @AfterEach
    void tearDown() {
        store.clear();
        DatabaseConnection.reset();
    }

    private void fillAliceProfile() {
        store.firstName = "Alice";
        store.email = "alice@example.com";
        store.gender = LocalProfileStore.Gender.FEMALE;
        store.photoPath = "/photos/alice.png";
        store.dateOfBirth = LocalDate.of(2000, 5, 17);
        store.heightCm = 172.5;
        store.weightKg = 64.2;
        store.fitnessLevel = LocalProfileStore.FitnessLevel.INTERMEDIATE;
        store.primaryGoal = LocalProfileStore.PrimaryGoal.GAIN_MUSCLE;
        store.targetWeightKg = 68.0;
        store.weeklyWorkoutGoal = 5;
        store.weeklyExerciseDurationMinutes = 300;
        store.experienceLevel = LocalProfileStore.FitnessLevel.ADVANCED;
        store.preferredWorkoutTypes.add("Strength");
        store.preferredWorkoutTypes.add("Cardio");
        store.preferredWorkoutDays.add("Monday");
        store.preferredWorkoutDays.add("Thursday");
        store.goalStartDate = LocalDate.of(2026, 9, 1);
        store.goalTargetDate = LocalDate.of(2026, 12, 1);
        store.goalAchievedDate = LocalDate.of(2026, 11, 20);
    }

    private int profileRowCount() throws SQLException {
        Connection connection = DatabaseConnection.getInstance();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM profiles")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    @DisplayName("load returns false when the user has no saved profile")
    void loadWithNothingSaved() {
        assertFalse(profileDAO.load(store, "alice"));
    }

    @Test
    @DisplayName("load returns false for an unknown username")
    void loadUnknownUser() {
        fillAliceProfile();
        profileDAO.save(store, "alice");
        store.clear();

        assertFalse(profileDAO.load(store, "ghost"));
    }

    @Test
    @DisplayName("every personal-detail field survives save then load")
    void personalDetailsRoundTrip() {
        fillAliceProfile();
        profileDAO.save(store, "alice");
        store.clear();

        assertTrue(profileDAO.load(store, "alice"));

        assertEquals("Alice", store.firstName);
        assertEquals("alice@example.com", store.email);
        assertEquals(LocalProfileStore.Gender.FEMALE, store.gender);
        assertEquals("/photos/alice.png", store.photoPath);
        assertEquals(LocalDate.of(2000, 5, 17), store.dateOfBirth);
        assertEquals(172.5, store.heightCm, 0.001);
        assertEquals(64.2, store.weightKg, 0.001);
        assertEquals(LocalProfileStore.FitnessLevel.INTERMEDIATE, store.fitnessLevel);
    }

    @Test
    @DisplayName("every goal field survives save then load")
    void goalsRoundTrip() {
        fillAliceProfile();
        profileDAO.save(store, "alice");
        store.clear();

        profileDAO.load(store, "alice");

        assertEquals(LocalProfileStore.PrimaryGoal.GAIN_MUSCLE, store.primaryGoal);
        assertEquals(68.0, store.targetWeightKg, 0.001);
        assertEquals(5, store.weeklyWorkoutGoal);
        assertEquals(300, store.weeklyExerciseDurationMinutes);
        assertEquals(LocalProfileStore.FitnessLevel.ADVANCED, store.experienceLevel);
        assertEquals(LocalDate.of(2026, 9, 1), store.goalStartDate);
        assertEquals(LocalDate.of(2026, 12, 1), store.goalTargetDate);
        assertEquals(LocalDate.of(2026, 11, 20), store.goalAchievedDate);
    }

    @Test
    @DisplayName("preferred workout types and days keep their values and order")
    void preferencesRoundTrip() {
        fillAliceProfile();
        profileDAO.save(store, "alice");
        store.clear();

        profileDAO.load(store, "alice");

        assertEquals(List.of("Strength", "Cardio"), List.copyOf(store.preferredWorkoutTypes));
        assertEquals(List.of("Monday", "Thursday"), List.copyOf(store.preferredWorkoutDays));
    }

    @Test
    @DisplayName("an untouched default profile loads back with null optionals and empty sets")
    void defaultProfileRoundTrip() {
        profileDAO.save(store, "alice");
        store.clear();

        assertTrue(profileDAO.load(store, "alice"));

        assertEquals("", store.firstName);
        assertNull(store.gender);
        assertNull(store.photoPath);
        assertNull(store.dateOfBirth);
        assertNull(store.primaryGoal);
        assertNull(store.goalStartDate);
        assertNull(store.goalTargetDate);
        assertNull(store.goalAchievedDate);
        assertTrue(store.preferredWorkoutTypes.isEmpty());
        assertTrue(store.preferredWorkoutDays.isEmpty());
        assertEquals(4, store.weeklyWorkoutGoal);
    }

    @Test
    @DisplayName("saving again updates the profile instead of adding a second row")
    void saveTwiceUpdates() throws SQLException {
        fillAliceProfile();
        profileDAO.save(store, "alice");

        store.firstName = "Alicia";
        store.weeklyWorkoutGoal = 3;
        profileDAO.save(store, "alice");
        store.clear();

        profileDAO.load(store, "alice");

        assertEquals("Alicia", store.firstName);
        assertEquals(3, store.weeklyWorkoutGoal);
        assertEquals(1, profileRowCount());
    }

    @Test
    @DisplayName("load replaces preferred sets already in the store instead of merging into them")
    void loadReplacesExistingSets() {
        profileDAO.save(store, "alice");
        store.clear();

        store.preferredWorkoutTypes.add("Leftover");
        store.preferredWorkoutDays.add("Sunday");
        profileDAO.load(store, "alice");

        assertTrue(store.preferredWorkoutTypes.isEmpty());
        assertTrue(store.preferredWorkoutDays.isEmpty());
    }

    @Test
    @DisplayName("each user's profile is kept separate")
    void profilesAreIsolatedPerUser() {
        fillAliceProfile();
        profileDAO.save(store, "alice");
        store.clear();

        store.firstName = "Bob";
        store.dateOfBirth = LocalDate.of(1995, 1, 2);
        store.primaryGoal = LocalProfileStore.PrimaryGoal.LOSE_WEIGHT;
        profileDAO.save(store, "bob");
        store.clear();

        profileDAO.load(store, "alice");
        assertEquals("Alice", store.firstName);
        assertEquals(LocalProfileStore.PrimaryGoal.GAIN_MUSCLE, store.primaryGoal);

        store.clear();
        profileDAO.load(store, "bob");
        assertEquals("Bob", store.firstName);
        assertEquals(LocalProfileStore.PrimaryGoal.LOSE_WEIGHT, store.primaryGoal);
        assertEquals(LocalDate.of(1995, 1, 2), store.dateOfBirth);
    }
}