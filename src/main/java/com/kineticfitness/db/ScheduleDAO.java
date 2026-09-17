package com.kineticfitness.db;

import com.kineticfitness.model.ScheduleStatus;
import com.kineticfitness.model.ScheduledWorkout;
import com.kineticfitness.service.ScheduleValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads and writes the user's workout schedule.
 *
 * <p>Dates and times are stored in ISO form ({@code 2026-09-20}, {@code 18:00})
 * so they sort and compare correctly in SQL, and duration is stored as an
 * integer number of minutes rather than as the string "45 mins".</p>
 *
 * <p>US-10, US-22, US-32.</p>
 */
public class ScheduleDAO {

    private static final String COLUMNS =
            "id, workout_name, workout_date, start_time, duration_minutes, status, reminder_minutes";

    /**
     * Adds columns introduced after the first release, so an existing database
     * upgrades in place instead of having to be deleted.
     *
     * <p>Called once per connection from {@link DatabaseConnection}.</p>
     */
    static void migrate(Connection connection) throws SQLException {
        Set<String> existing = columnNames(connection, "scheduled_workouts");
        if (existing.isEmpty()) {
            return; // Table not created yet; the CREATE TABLE already has every column.
        }
        if (existing.contains("duration") && !existing.contains("duration_minutes")) {
            // The old table stored duration as the text "45 mins" in a NOT NULL column.
            // SQLite cannot drop a column, so the table is rebuilt the standard way:
            // create, copy, drop, rename.
            rebuildLegacyTable(connection);
            return;
        }
        if (!existing.contains("duration_minutes")) {
            addColumn(connection, "duration_minutes INTEGER NOT NULL DEFAULT 60");
        }
        if (!existing.contains("status")) {
            addColumn(connection, "status TEXT NOT NULL DEFAULT 'SCHEDULED'");
        }
        if (!existing.contains("reminder_minutes")) {
            addColumn(connection, "reminder_minutes INTEGER NOT NULL DEFAULT 0");
        }
    }

    private static Set<String> columnNames(Connection connection, String table) throws SQLException {
        Set<String> names = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        }
        return names;
    }

    /**
     * Rebuilds a pre-release {@code scheduled_workouts} table, converting the old
     * text duration ("45 mins") into an integer number of minutes and defaulting
     * every existing row to SCHEDULED with no reminder.
     */
    private static void rebuildLegacyTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE scheduled_workouts_migrated (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    workout_name TEXT NOT NULL,
                    workout_date TEXT NOT NULL,
                    start_time TEXT NOT NULL,
                    duration_minutes INTEGER NOT NULL DEFAULT 60,
                    status TEXT NOT NULL DEFAULT 'SCHEDULED',
                    reminder_minutes INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);
            statement.execute("""
                INSERT INTO scheduled_workouts_migrated
                    (id, user_id, workout_name, workout_date, start_time,
                     duration_minutes, status, reminder_minutes)
                SELECT id, user_id, workout_name, workout_date, start_time,
                       MAX(CAST(COALESCE(NULLIF(TRIM(REPLACE(REPLACE(duration, 'mins', ''), 'min', '')), ''), '60')
                                AS INTEGER), 1),
                       'SCHEDULED',
                       0
                FROM scheduled_workouts
            """);
            statement.execute("DROP TABLE scheduled_workouts");
            statement.execute("ALTER TABLE scheduled_workouts_migrated RENAME TO scheduled_workouts");
        }
    }

    private static void addColumn(Connection connection, String definition) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE scheduled_workouts ADD COLUMN " + definition);
        }
    }

    /**
     * Saves a new workout for the given user.
     *
     * @return the same workout carrying the id the database assigned, or the
     *         original unsaved instance if the insert failed
     */
    public ScheduledWorkout save(String username, ScheduledWorkout workout) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            INSERT INTO scheduled_workouts
                (user_id, workout_name, workout_date, start_time, duration_minutes, status, reminder_minutes)
            VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, workout.getName());
            statement.setString(3, workout.getDate().toString());
            statement.setString(4, workout.getStartTime().toString());
            statement.setInt(5, workout.getDurationMinutes());
            statement.setString(6, workout.getStatus().name());
            statement.setInt(7, workout.getReminderLeadMinutes());
            statement.executeUpdate();
            return workout.withId(lastInsertRowId(connection));
        } catch (SQLException e) {
            System.err.println("Failed to save scheduled workout: " + e.getMessage());
            return workout;
        }
    }

    private int lastInsertRowId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT last_insert_rowid()")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Every scheduled workout belonging to this user, soonest first.
     *
     * <p>Rows written before dates were stored in ISO form are read with
     * {@link ScheduleValidator#parseDate(String)}; anything still unreadable is
     * skipped rather than crashing the screen.</p>
     */
    public List<ScheduledWorkout> findForUser(String username) {
        List<ScheduledWorkout> results = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            SELECT s.id, s.workout_name, s.workout_date, s.start_time,
                   s.duration_minutes, s.status, s.reminder_minutes
            FROM scheduled_workouts s
            JOIN users u ON s.user_id = u.id
            WHERE u.username = ?
            ORDER BY s.workout_date, s.start_time
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ScheduledWorkout workout = mapRow(rs);
                    if (workout != null) {
                        results.add(workout);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load scheduled workouts: " + e.getMessage());
        }
        return results;
    }

    /** Only the workouts still waiting to be done. */
    public List<ScheduledWorkout> findOpenForUser(String username) {
        List<ScheduledWorkout> open = new ArrayList<>();
        for (ScheduledWorkout workout : findForUser(username)) {
            if (workout.isOpen()) {
                open.add(workout);
            }
        }
        return open;
    }

    /** Marks a workout completed, skipped, or back to scheduled. */
    public void updateStatus(int id, ScheduleStatus status) {
        Connection connection = DatabaseConnection.getInstance();
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE scheduled_workouts SET status = ? WHERE id = ?")) {
            statement.setString(1, status.name());
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update workout status: " + e.getMessage());
        }
    }

    /** Moves a workout to a new date and time and reopens it. */
    public void reschedule(int id, LocalDate newDate, LocalTime newStartTime) {
        Connection connection = DatabaseConnection.getInstance();
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE scheduled_workouts SET workout_date = ?, start_time = ?, status = ? WHERE id = ?")) {
            statement.setString(1, newDate.toString());
            statement.setString(2, newStartTime.toString());
            statement.setString(3, ScheduleStatus.SCHEDULED.name());
            statement.setInt(4, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to reschedule workout: " + e.getMessage());
        }
    }

    /** Changes how far ahead the user is reminded, or switches the reminder off with 0. */
    public void updateReminderLead(int id, int reminderLeadMinutes) {
        Connection connection = DatabaseConnection.getInstance();
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE scheduled_workouts SET reminder_minutes = ? WHERE id = ?")) {
            statement.setInt(1, Math.max(reminderLeadMinutes, 0));
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update reminder: " + e.getMessage());
        }
    }

    /** Removes a workout from the schedule entirely. */
    public void delete(int id) {
        Connection connection = DatabaseConnection.getInstance();
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM scheduled_workouts WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete scheduled workout: " + e.getMessage());
        }
    }

    private ScheduledWorkout mapRow(ResultSet rs) throws SQLException {
        LocalDate date = ScheduleValidator.parseDate(rs.getString("workout_date")).orElse(null);
        if (date == null) {
            System.err.println("Skipping scheduled workout with an unreadable date: "
                    + rs.getString("workout_date"));
            return null;
        }
        LocalTime startTime = readTime(rs.getString("start_time"));
        if (startTime == null) {
            System.err.println("Skipping scheduled workout with an unreadable time: "
                    + rs.getString("start_time"));
            return null;
        }
        int durationMinutes = Math.max(rs.getInt("duration_minutes"), 1);
        return new ScheduledWorkout(
                rs.getInt("id"),
                rs.getString("workout_name"),
                date,
                startTime,
                durationMinutes,
                readStatus(rs.getString("status")),
                Math.max(rs.getInt("reminder_minutes"), 0));
    }

    private LocalTime readTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            return ScheduleValidator.parseTime(value).orElse(null);
        }
    }

    private ScheduleStatus readStatus(String value) {
        if (value == null) {
            return ScheduleStatus.SCHEDULED;
        }
        try {
            return ScheduleStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return ScheduleStatus.SCHEDULED;
        }
    }
}
