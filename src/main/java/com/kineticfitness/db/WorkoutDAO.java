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

public class WorkoutDAO {

    /**
     * Saves a workout and every one of its exercises for the given user.
     * The user must already exist in the users table
     **/

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
        } catch (SQLException e) {
            System.err.println("Failed to save workout: " + e.getMessage());
            return;
        }

        int workoutId = -1;
        try (Statement idStmt = connection.createStatement();
             ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {

            if (rs.next()) {
                workoutId = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve workout ID: " + e.getMessage());
            return;
        }

        try {
            saveExercises(connection, workoutId, workout.getExercises());
        } catch (SQLException e) {
            System.err.println("Failed to save exercises: " + e.getMessage());
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
}