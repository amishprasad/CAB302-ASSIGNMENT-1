package com.kineticfitness.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static String url = "jdbc:sqlite:kineticfitness.db";
    private static Connection instance = null;

    /** For tests: point at a different database (e.g. "jdbc:sqlite::memory:"). Call before getInstance(). */
    public static void configure(String customUrl) {
        reset();
        url = customUrl;
    }

    /** Closes the current connection so the next getInstance() opens a fresh one. */
    public static void reset() {
        if (instance != null) {
            try { instance.close(); } catch (SQLException ignored) {}
            instance = null;
        }
    }

    public static Connection getInstance() {
        if (instance == null) {
            try {
                instance = DriverManager.getConnection(url);   // note: url, not URL
                createTables(instance);
            } catch (SQLException e) {
                System.err.println("Failed to connect to database: " + e.getMessage());
            }
        }
        return instance;
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    fitness_level TEXT NOT NULL,
                    age INTEGER,
                    height_cm REAL,
                    weight_kg REAL
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS workouts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    workout_date TEXT NOT NULL,
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
                    duration TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """);
        }
    }
}
