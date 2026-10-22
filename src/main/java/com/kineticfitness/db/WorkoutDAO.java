package com.kineticfitness.db;

import com.kineticfitness.model.BodyPart;
import com.kineticfitness.model.Exercise;
import com.kineticfitness.model.User;
import com.kineticfitness.model.Workout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves, loads, updates and deletes workouts and their exercises in the SQLite database.
 *
 * <p>Follows the DAO (Data Access Object) pattern: all SQL for the {@code workouts}
 * and {@code exercises} tables lives here, so the screens never touch the database
 * directly. Multi-step writes run inside a single transaction, so a failure part-way
 * through leaves the database unchanged.
 */
public class WorkoutDAO {

    /**
     * Saves a workout and every one of its exercises for the given user, in one transaction.
     * The user must already exist in the users table.
     *
     * @param user    the user the workout belongs to
     * @param workout the workout to save, including its exercises
     * @throws DataAccessException if the workout or any exercise cannot be saved
     */
    public void save(User user, Workout workout) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
        INSERT INTO workouts (user_id, workout_date, name)
        VALUES ((SELECT id FROM users WHERE username = ?), ?, ?)
    """;
        inTransaction(connection, "Failed to save workout", () -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getUsername());
                statement.setString(2, workout.getDate().toString());
                if (workout.getName() != null) {
                    statement.setString(3, workout.getName());
                } else {
                    statement.setNull(3, java.sql.Types.VARCHAR);
                }
                statement.executeUpdate();
            }
            int workoutId = lastInsertRowId(connection);
            saveExercises(connection, workoutId, workout.getExercises());
        });
    }

    @FunctionalInterface
    private interface SqlWork {
        void run() throws SQLException;
    }

    private void inTransaction(Connection connection, String failureMessage, SqlWork work) {
        try {
            boolean previous = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                work.run();
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previous);
            }
        } catch (SQLException e) {
            throw new DataAccessException(failureMessage + ": " + e.getMessage(), e);
        }
    }

    private int lastInsertRowId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT last_insert_rowid()")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void saveExercises(Connection connection, int workoutId, List<Exercise> exercises) throws SQLException {
        String sql = """
            INSERT INTO exercises (workout_id, name, sets, reps, body_part)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Exercise exercise : exercises) {
                statement.setInt(1, workoutId);
                statement.setString(2, exercise.getName());
                statement.setInt(3, exercise.getSets());
                statement.setInt(4, exercise.getReps());
                if (exercise.getBodyPart() != null) {
                    statement.setString(5, exercise.getBodyPart().name());
                } else {
                    statement.setNull(5, java.sql.Types.VARCHAR);
                }
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * Loads every workout a user has logged, with its exercises, newest first.
     *
     * @param username the user whose workouts to load
     * @return the user's workouts, or an empty list if none or if loading fails
     */
    public List<Workout> findAllByUsername(String username) {
        List<Workout> workouts = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            SELECT w.id, w.workout_date, w.name
            FROM workouts w
            JOIN users u ON u.id = w.user_id
            WHERE u.username = ?
            ORDER BY w.id DESC
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int workoutId = rs.getInt("id");
                    Workout workout = new Workout(LocalDate.parse(rs.getString("workout_date")));
                    workout.setId(workoutId);
                    workout.setName(rs.getString("name"));
                    for (Exercise exercise : findExercises(connection, workoutId)) {
                        workout.addExercise(exercise);
                    }
                    workouts.add(workout);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load workouts: " + e.getMessage());
        }
        return workouts;
    }

    private List<Exercise> findExercises(Connection connection, int workoutId) throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT id, name, sets, reps, body_part FROM exercises WHERE workout_id = ? ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, workoutId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String bodyPartName = rs.getString("body_part");
                    BodyPart bodyPart = bodyPartName != null ? BodyPart.valueOf(bodyPartName) : null;
                    Exercise exercise = new Exercise(
                            rs.getString("name"),
                            rs.getInt("sets"),
                            rs.getInt("reps"),
                            bodyPart);
                    exercise.setId(rs.getInt("id"));
                    exercises.add(exercise);
                }
            }
        }
        return exercises;
    }

    /**
     * Changes the sets and reps of a logged exercise.
     *
     * @param exerciseId the id of the exercise to update
     * @param sets       the new number of sets
     * @param reps       the new number of reps
     */
    public void updateExercise(int exerciseId, int sets, int reps) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "UPDATE exercises SET sets = ?, reps = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sets);
            statement.setInt(2, reps);
            statement.setInt(3, exerciseId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update exercise: " + e.getMessage());
        }
    }

    /**
     * Renames a workout session.
     *
     * @param workoutId the id of the workout to rename
     * @param name      the new name, or {@code null} to clear it
     */
    public void updateWorkoutName(int workoutId, String name) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "UPDATE workouts SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (name != null) {
                statement.setString(1, name);
            } else {
                statement.setNull(1, java.sql.Types.VARCHAR);
            }
            statement.setInt(2, workoutId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to rename workout: " + e.getMessage());
        }
    }

    /**
     * Deletes one exercise from a logged workout.
     *
     * @param exerciseId the id of the exercise to delete
     */
    public void deleteExercise(int exerciseId) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "DELETE FROM exercises WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, exerciseId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete exercise: " + e.getMessage());
        }
    }

    /**
     * Deletes every workout and exercise belonging to the given user, in one transaction.
     * Used by the Settings page's "Clear All Data" action. Does not touch their account,
     * goals (those live in LocalProfileStore, not the database), or scheduled workouts.
     *
     * @param username the user whose workout data to delete
     * @throws DataAccessException if the data cannot be deleted
     */
    public void deleteAllForUser(String username) {
        Connection connection = DatabaseConnection.getInstance();
        String deleteExercises = """
        DELETE FROM exercises WHERE workout_id IN (
            SELECT w.id FROM workouts w JOIN users u ON u.id = w.user_id WHERE u.username = ?
        )
        """;
        String deleteWorkouts = "DELETE FROM workouts WHERE user_id = (SELECT id FROM users WHERE username = ?)";

        inTransaction(connection, "Failed to clear workout data", () -> {
            try (PreparedStatement statement = connection.prepareStatement(deleteExercises)) {
                statement.setString(1, username);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(deleteWorkouts)) {
                statement.setString(1, username);
                statement.executeUpdate();
            }
        });
    }

    /**
     * Adds the name column to a workouts table created before sessions could be named.
     * Safe to run on every startup; does nothing if the column already exists.
     *
     * @param connection the open database connection
     * @throws SQLException if the table cannot be inspected or altered
     */
    public static void migrate(Connection connection) throws SQLException {
        boolean hasName = false;
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(workouts)")) {
            while (rs.next()) {
                if ("name".equalsIgnoreCase(rs.getString("name"))) {
                    hasName = true;
                }
            }
        }
        if (!hasName) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE workouts ADD COLUMN name TEXT");
            }
        }
    }
}