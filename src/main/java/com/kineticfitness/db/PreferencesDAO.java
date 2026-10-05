package com.kineticfitness.db;

import com.kineticfitness.util.UnitSystem;
import com.kineticfitness.view.LocalProfileStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Persists the Settings page's unit system and notification choices for the signed-in
 * user, mirroring how {@link ProfileDAO} persists {@link LocalProfileStore}. One row
 * per user, keyed on the same id as {@code users} and {@code profiles}.
 */
public class PreferencesDAO {

    public void save(LocalProfileStore store, String username) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            INSERT INTO preferences (id, unit_system, notify_workout_reminders,
                                      notify_goal_alerts, notify_weekly_summary)
            VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                unit_system = excluded.unit_system,
                notify_workout_reminders = excluded.notify_workout_reminders,
                notify_goal_alerts = excluded.notify_goal_alerts,
                notify_weekly_summary = excluded.notify_weekly_summary
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, store.unitSystem.name());
            statement.setInt(3, store.notifyWorkoutReminders ? 1 : 0);
            statement.setInt(4, store.notifyGoalAlerts ? 1 : 0);
            statement.setInt(5, store.notifyWeeklySummary ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save preferences: " + e.getMessage());
        }
    }

    /**
     * Loads saved preferences into {@code store}. Leaves the store's existing (default)
     * values untouched — and returns {@code false} — when the user has never saved
     * Settings before, so a brand-new account still shows sensible defaults rather than
     * blank or zeroed fields.
     */
    public boolean load(LocalProfileStore store, String username) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "SELECT * FROM preferences WHERE id = (SELECT id FROM users WHERE username = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                store.unitSystem = UnitSystem.parse(rs.getString("unit_system"));
                store.notifyWorkoutReminders = rs.getInt("notify_workout_reminders") != 0;
                store.notifyGoalAlerts = rs.getInt("notify_goal_alerts") != 0;
                store.notifyWeeklySummary = rs.getInt("notify_weekly_summary") != 0;
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Failed to load preferences: " + e.getMessage());
        }
        return false;
    }
}
