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
 * Saves a workout and every one of its exercises for the given user.
 * The user must already exist in the users table
 **/

public class WorkoutDAO {

    public void save(User user, Workout workout) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            INSERT INTO workouts (user_id, workout_date)
            VALUES ((SELECT id FROM users WHERE username = ?), ?)
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, workout.getDate().toString());
            statement.executeUpdate();

            int workoutId = lastInsertRowId(connection);
            saveExercises(connection, workoutId, workout.getExercises());
        } catch (SQLException e) {
            System.err.println("Failed to save workout: " + e.getMessage());
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

    public List<Workout> findAllByUsername(String username) {
        List<Workout> workouts = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            SELECT w.id, w.workout_date
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
}