package com.kineticfitness.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * Provides the single shared connection to the app's SQLite database.
 *
 * <p>Uses the Singleton pattern: the connection is created the first time
 * {@link #getInstance()} is called and reused after that, so every DAO talks to
 * the same database. On first connection it also creates any missing tables and
 * upgrades older tables in place.
 *
 * <p>Tests can call {@link #configure(String)} to use an in-memory database instead
 * of the real file.
 */
public class DatabaseConnection {


    private static String url = "jdbc:sqlite:kineticfitness.db";
    private static Connection instance = null;
    /**
     * Points the app at a different database, closing any current connection.
     * Used by tests, e.g. with {@code "jdbc:sqlite::memory:"}. Call before {@link #getInstance()}.
     *
     * @param customUrl the JDBC URL of the database to use
     */
    public static void configure(String customUrl) {
        reset();
        url = customUrl;
    }
    /**
     * Closes the current connection so the next {@link #getInstance()} call opens a fresh one.
     * Does nothing if no connection is open.
     */

    public static void reset() {
        if (instance != null) {
            try { instance.close(); } catch (SQLException ignored) {}
            instance = null;
        }
    }

    /**
     * Returns the shared database connection, opening it on first use.
     * The first call also creates any missing tables and migrates older ones.
     *
     * @return the shared connection, or {@code null} if the database could not be opened
     */

    public static Connection getInstance() {
        if (instance == null) {
            try {
                instance = DriverManager.getConnection(url);   // note: url, not URL
                createTables(instance);
                ScheduleDAO.migrate(instance);   // upgrades a pre-existing schedule table in place
                WorkoutDAO.migrate(instance);    // adds the name column to a pre-existing workouts table
            } catch (SQLException e) {
                System.err.println("Failed to connect to database: " + e.getMessage());
            }
        }
        return instance;
    }
    /**
     * Creates every table the app needs, if it does not already exist.
     *
     * @param connection the open database connection
     * @throws SQLException if a table cannot be created
     */

    private static void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    fitness_level TEXT NOT NULL,
                    age INTEGER,
                    height_cm REAL,
                    weight_kg REAL,
                    password TEXT,
                    email TEXT UNIQUE,
                    phone_number TEXT UNIQUE
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS workouts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    workout_date TEXT NOT NULL,
                    name TEXT,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS exercises (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    workout_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    sets INTEGER NOT NULL,
                    reps INTEGER NOT NULL,
                    body_part TEXT,
                    FOREIGN KEY (workout_id) REFERENCES workouts(id)
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS goals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    target_value INTEGER NOT NULL,
                    current_value INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS scheduled_workouts (
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
                CREATE TABLE IF NOT EXISTS preferences (
                    id INTEGER PRIMARY KEY,
                    unit_system TEXT NOT NULL DEFAULT 'METRIC',
                    notify_workout_reminders INTEGER NOT NULL DEFAULT 1,
                    notify_goal_alerts INTEGER NOT NULL DEFAULT 1,
                    notify_weekly_summary INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (id) REFERENCES users(id)
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS profiles (
                    id INTEGER PRIMARY KEY,
                    first_name TEXT, email TEXT, gender TEXT, photo_path TEXT,
                    date_of_birth TEXT, height_cm REAL, weight_kg REAL, fitness_level TEXT,
                    primary_goal TEXT, target_weight_kg REAL, weekly_workout_goal INTEGER,
                    weekly_duration_minutes INTEGER, experience_level TEXT,
                    preferred_types TEXT, preferred_days TEXT,
                    goal_start_date TEXT, goal_target_date TEXT, goal_achieved_date TEXT
                )
            """);
        }
    }
}