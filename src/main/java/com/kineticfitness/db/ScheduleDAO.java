package com.kineticfitness.db;

import com.kineticfitness.model.ScheduledWorkout;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    private int getUserId(Connection connection, String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            throw new SQLException("No user found with username: " + username);
        }
    }

    public void save(String username, ScheduledWorkout workout) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "INSERT INTO scheduled_workouts (user_id, workout_name, workout_date, start_time, duration) VALUES (?, ?, ?, ?, ?)";

        try {
            int userId = getUserId(connection, username);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setString(2, workout.getName());
                statement.setString(3, workout.getDate());
                statement.setString(4, workout.getTime());
                statement.setString(5, workout.getDuration());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save scheduled workout: " + e.getMessage());
        }
    }

    public List<ScheduledWorkout> findForUser(String username) {
        List<ScheduledWorkout> results = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance();

        String sql = """
            SELECT s.workout_name, s.workout_date, s.start_time, s.duration
            FROM scheduled_workouts s
            JOIN users u ON s.user_id = u.id
            WHERE u.username = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                results.add(new ScheduledWorkout(
                        rs.getString("workout_name"),
                        rs.getString("workout_date"),
                        rs.getString("start_time"),
                        rs.getString("duration")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load scheduled workouts: " + e.getMessage());
        }

        return results;
    }
}