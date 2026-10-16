package com.kineticfitness.db;

import com.kineticfitness.model.BodyPart;
import com.kineticfitness.model.Exercise;
import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import com.kineticfitness.model.Workout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistence for workouts and their exercises, against a throwaway in-memory
 * database so the suite never touches {@code kineticfitness.db}.
 */
class WorkoutDAOTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 20);

    private final UserDAO userDAO = new UserDAO();
    private final WorkoutDAO workoutDAO = new WorkoutDAO();

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        DatabaseConnection.configure("jdbc:sqlite::memory:");
        testUser = new User("testuser", FitnessLevel.BEGINNER, 25, 180, 75);
        otherUser = new User("someoneelse", FitnessLevel.BEGINNER, 30, 170, 70);
        userDAO.save(testUser);
        userDAO.save(otherUser);
    }

    @AfterEach
    void tearDown() {
        DatabaseConnection.reset();
    }

    private Workout workout(LocalDate date, String name, Exercise... exercises) {
        Workout workout = new Workout(date);
        workout.setName(name);
        for (Exercise exercise : exercises) {
            workout.addExercise(exercise);
        }
        return workout;
    }

    private Workout savePushDay() {
        workoutDAO.save(testUser, workout(DATE, "Push Day",
                new Exercise("Bench Press", 4, 8, BodyPart.CHEST),
                new Exercise("Overhead Press", 3, 10, BodyPart.SHOULDERS)));
        return workoutDAO.findAllByUsername("testuser").get(0);
    }

    @Test
    @DisplayName("name, date and every exercise survive the round trip")
    void everyFieldComesBackUnchanged() {
        savePushDay();

        Workout loaded = workoutDAO.findAllByUsername("testuser").get(0);

        assertEquals("Push Day", loaded.getName());
        assertEquals(DATE, loaded.getDate());
        assertEquals(2, loaded.getExercises().size());

        Exercise first = loaded.getExercises().get(0);
        assertEquals("Bench Press", first.getName());
        assertEquals(4, first.getSets());
        assertEquals(8, first.getReps());
        assertEquals(BodyPart.CHEST, first.getBodyPart());

        Exercise second = loaded.getExercises().get(1);
        assertEquals("Overhead Press", second.getName());
        assertEquals(3, second.getSets());
        assertEquals(10, second.getReps());
        assertEquals(BodyPart.SHOULDERS, second.getBodyPart());
    }

    @Test
    void loadedWorkoutsAndExercisesHaveDatabaseIds() {
        Workout loaded = savePushDay();

        assertTrue(loaded.getId() > 0);
        assertTrue(loaded.getExercises().get(0).getId() > 0);
        assertNotEquals(loaded.getExercises().get(0).getId(), loaded.getExercises().get(1).getId());
    }

    @Test
    @DisplayName("a workout with no name and an exercise with no body part are both allowed")
    void optionalFieldsCanBeNull() {
        workoutDAO.save(testUser, workout(DATE, null, new Exercise("Mystery Move", 3, 12)));

        Workout loaded = workoutDAO.findAllByUsername("testuser").get(0);

        assertNull(loaded.getName());
        assertNull(loaded.getExercises().get(0).getBodyPart());
    }

    @Test
    void aWorkoutWithNoExercisesIsStillSaved() {
        workoutDAO.save(testUser, workout(DATE, "Stretch"));

        Workout loaded = workoutDAO.findAllByUsername("testuser").get(0);

        assertEquals("Stretch", loaded.getName());
        assertTrue(loaded.getExercises().isEmpty());
    }

    @Test
    @DisplayName("saving for a user who has no account stores nothing")
    void savingForAnUnknownUserStoresNothing() {
        User ghost = new User("ghost", FitnessLevel.BEGINNER, 40, 175, 80);

        workoutDAO.save(ghost, workout(DATE, "Ghost Day", new Exercise("Squat", 5, 5, BodyPart.LEGS)));

        assertTrue(workoutDAO.findAllByUsername("ghost").isEmpty());
    }

    @Test
    void resultsComeBackNewestFirst() {
        workoutDAO.save(testUser, workout(DATE, "First"));
        workoutDAO.save(testUser, workout(DATE.plusDays(1), "Second"));
        workoutDAO.save(testUser, workout(DATE.plusDays(2), "Third"));

        List<String> order = workoutDAO.findAllByUsername("testuser").stream().map(Workout::getName).toList();

        assertEquals(List.of("Third", "Second", "First"), order);
    }

    @Test
    void oneUsersWorkoutsAreNotVisibleToAnother() {
        savePushDay();

        assertTrue(workoutDAO.findAllByUsername("someoneelse").isEmpty());
    }

    @Test
    void anUnknownUserSimplyHasNoWorkouts() {
        assertTrue(workoutDAO.findAllByUsername("nobody").isEmpty());
    }

    @Test
    void exerciseSetsAndRepsCanBeUpdated() {
        Exercise bench = savePushDay().getExercises().get(0);

        workoutDAO.updateExercise(bench.getId(), 5, 3);

        Exercise updated = workoutDAO.findAllByUsername("testuser").get(0).getExercises().get(0);
        assertEquals(5, updated.getSets());
        assertEquals(3, updated.getReps());
        assertEquals("Bench Press", updated.getName());
    }

    @Test
    void theWorkoutNameCanBeChangedAndCleared() {
        int id = savePushDay().getId();

        workoutDAO.updateWorkoutName(id, "Chest Day");
        assertEquals("Chest Day", workoutDAO.findAllByUsername("testuser").get(0).getName());

        workoutDAO.updateWorkoutName(id, null);
        assertNull(workoutDAO.findAllByUsername("testuser").get(0).getName());
    }

    @Test
    void deletingAnExerciseLeavesTheRestOfTheWorkout() {
        Exercise bench = savePushDay().getExercises().get(0);

        workoutDAO.deleteExercise(bench.getId());

        List<Exercise> remaining = workoutDAO.findAllByUsername("testuser").get(0).getExercises();
        assertEquals(1, remaining.size());
        assertEquals("Overhead Press", remaining.get(0).getName());
    }

    @Test
    void deletingAllForAUserClearsEveryWorkoutTheyHave() {
        savePushDay();
        workoutDAO.save(testUser, workout(DATE.plusDays(1), "Pull Day", new Exercise("Row", 4, 8, BodyPart.BACK)));

        workoutDAO.deleteAllForUser("testuser");

        assertTrue(workoutDAO.findAllByUsername("testuser").isEmpty());
    }

    @Test
    void deletingAllForAUserLeavesOtherUsersAlone() {
        savePushDay();
        workoutDAO.save(otherUser, workout(DATE, "Theirs", new Exercise("Squat", 5, 5, BodyPart.LEGS)));

        workoutDAO.deleteAllForUser("testuser");

        assertTrue(workoutDAO.findAllByUsername("testuser").isEmpty());
        List<Workout> theirs = workoutDAO.findAllByUsername("someoneelse");
        assertEquals(1, theirs.size());
        assertEquals(1, theirs.get(0).getExercises().size());
    }

    @Test
    void deletingAllForAUserKeepsTheirAccount() {
        savePushDay();

        workoutDAO.deleteAllForUser("testuser");

        assertNotNull(userDAO.findByUsername("testuser"));
    }

    @Test
    void deletingAllForAnUnknownUserDoesNothing() {
        savePushDay();

        assertDoesNotThrow(() -> workoutDAO.deleteAllForUser("nobody"));

        assertEquals(1, workoutDAO.findAllByUsername("testuser").size());
    }

    @Test
    @DisplayName("migrate adds the name column to a workouts table that predates it")
    void migrateAddsTheNameColumn() throws SQLException {
        try (Connection old = oldStyleDatabase()) {
            assertFalse(hasColumn(old, "name"));

            WorkoutDAO.migrate(old);

            assertTrue(hasColumn(old, "name"));
        }
    }

    @Test
    void migrateCanSafelyRunTwice() throws SQLException {
        try (Connection old = oldStyleDatabase()) {
            WorkoutDAO.migrate(old);

            assertDoesNotThrow(() -> WorkoutDAO.migrate(old));
            assertTrue(hasColumn(old, "name"));
        }
    }

    private Connection oldStyleDatabase() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE workouts (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "user_id INTEGER, workout_date TEXT)");
        }
        return connection;
    }

    private boolean hasColumn(Connection connection, String column) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(workouts)")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }
}