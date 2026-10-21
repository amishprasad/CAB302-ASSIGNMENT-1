package com.kineticfitness.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Persists the free-text note a user attaches to their fitness goal.
 *
 * <p>The note lives in its own table rather than as another column on
 * {@code profiles}. SQLite's {@code CREATE TABLE IF NOT EXISTS} does nothing to
 * a table that already exists, so widening {@code profiles} would leave every
 * database already on disk missing the column, and every save and load would
 * fail until its owner deleted the file and lost their data. A new table is
 * created on first use instead, which costs nobody anything.</p>
 *
 * <p>Keyed by {@code profile_id} the same way {@link ProfileDAO} is, so the note
 * belongs to one account rather than to whoever signed in last.</p>
 *
 * <p>US-12 &mdash; Set a fitness goal.</p>
 */
public class GoalNotesDAO {

    public GoalNotesDAO() {
        createTableIfMissing();
    }

    private void createTableIfMissing() {
        try (Statement st = DatabaseConnection.getInstance().createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS goal_notes (
                    profile_id INTEGER PRIMARY KEY,
                    notes TEXT
                )
            """);
        } catch (SQLException e) {
            System.err.println("Failed to prepare goal_notes table: " + e.getMessage());
        }
    }

    /** Stores the note against the given account, replacing any previous one. */
    public void save(String username, String notes) {
        String sql = """
            INSERT INTO goal_notes (profile_id, notes)
            VALUES ((SELECT id FROM users WHERE username = ?), ?)
            ON CONFLICT(profile_id) DO UPDATE SET notes = excluded.notes
        """;
        try (PreparedStatement st = DatabaseConnection.getInstance().prepareStatement(sql)) {
            st.setString(1, username);
            st.setString(2, notes == null ? "" : notes);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save goal note: " + e.getMessage());
        }
    }

    /**
     * Reads the note back.
     *
     * @return the stored note, or an empty string when the account has none
     */
    public String load(String username) {
        String sql = """
            SELECT notes FROM goal_notes
            WHERE profile_id = (SELECT id FROM users WHERE username = ?)
        """;
        try (PreparedStatement st = DatabaseConnection.getInstance().prepareStatement(sql)) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String notes = rs.getString("notes");
                return notes == null ? "" : notes;
            }
        } catch (SQLException e) {
            System.err.println("Failed to load goal note: " + e.getMessage());
        }
        return "";
    }

    /** Removes the note, used when the goal it belonged to is deleted. */
    public void delete(String username) {
        String sql = """
            DELETE FROM goal_notes
            WHERE profile_id = (SELECT id FROM users WHERE username = ?)
        """;
        try (PreparedStatement st = DatabaseConnection.getInstance().prepareStatement(sql)) {
            st.setString(1, username);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete goal note: " + e.getMessage());
        }
    }
}
