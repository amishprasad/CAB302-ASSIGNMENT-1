package com.kineticfitness.db;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.Goal;

import java.sql.*;


public class GoalsDAO {

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

    public void save(String username, Goal goal) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            INSERT INTO goals (user_id, goal_type, target_weight_kg, weekly_workout_goal,
                weekly_exercise_duration_minutes, experience_level, preferred_workout_types, preferred_workout_days)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(user_id) DO UPDATE SET
                goal_type = excluded.goal_type,
                target_weight_kg = excluded.target_weight_kg,
                weekly_workout_goal = excluded.weekly_workout_goal,
                weekly_exercise_duration_minutes = excluded.weekly_exercise_duration_minutes,
                experience_level = excluded.experience_level,
                preferred_workout_types = excluded.preferred_workout_types,
                preferred_workout_days = excluded.preferred_workout_days
        """;

        try {
            int userId = getUserId(connection, username);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setString(2, goal.getGoalType().name());
                statement.setDouble(3, goal.getTargetWeightKg());
                statement.setInt(4, goal.getWeeklyWorkoutGoal());
                statement.setInt(5, goal.getWeeklyExerciseDurationMinutes());
                statement.setString(6, goal.getExperienceLevel().name());
                statement.setString(7, String.join(",", goal.getPreferredWorkoutTypes()));
                statement.setString(8, String.join(",", goal.getPreferredWorkoutDays()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save goal: " + e.getMessage());
        }
    }

    /** Returns the user's current goal, or null if they haven't set one. */
    public Goal findForUser(String username) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            SELECT g.goal_type, g.target_weight_kg, g.weekly_workout_goal,
                   g.weekly_exercise_duration_minutes, g.experience_level,
                   g.preferred_workout_types, g.preferred_workout_days
            FROM goals g
            JOIN users u ON g.user_id = u.id
            WHERE u.username = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Goal goal = new Goal(
                        Goal.GoalType.valueOf(rs.getString("goal_type")),
                        rs.getDouble("target_weight_kg"),
                        rs.getInt("weekly_workout_goal"),
                        rs.getInt("weekly_exercise_duration_minutes"),
                        FitnessLevel.valueOf(rs.getString("experience_level")));

                String types = rs.getString("preferred_workout_types");
                if (types != null && !types.isEmpty()) {
                    for (String t : types.split(",")) goal.getPreferredWorkoutTypes().add(t);
                }
                String days = rs.getString("preferred_workout_days");
                if (days != null && !days.isEmpty()) {
                    for (String d : days.split(",")) goal.getPreferredWorkoutDays().add(d);
                }
                return goal;
            }
        } catch (SQLException e) {
            System.err.println("Failed to load goal: " + e.getMessage());
        }
        return null;
    }

    public void delete(String username) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "DELETE FROM goals WHERE user_id = (SELECT id FROM users WHERE username = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete goal: " + e.getMessage());
        }
    }
}