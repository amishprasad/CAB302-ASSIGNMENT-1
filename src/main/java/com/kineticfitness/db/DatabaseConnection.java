package com.kineticfitness.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:kineticfitness.db";
    private static Connection instance = null;

    private DatabaseConnection() {
        // prevent instantiation
    }

    public static Connection getInstance() {
        if (instance == null) {
            try {
                System.out.println("SQLite DB location: " + new java.io.File("kineticfitness.db").getAbsolutePath());
                instance = DriverManager.getConnection(URL);
                createTables(instance);
            } catch (SQLException e) {
                System.err.println("Failed to connect to database: " + e.getMessage());
            }
        }
        return instance;
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Updated schema: first_name, last_name, gender, email, and
            // date_of_birth replace the old single "age" column. Age is
            // now computed in Java from date_of_birth instead of stored.
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    first_name TEXT,
                    last_name TEXT,
                    gender TEXT,
                    email TEXT,
                    date_of_birth TEXT,
                    fitness_level TEXT NOT NULL,
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
                    user_id INTEGER NOT NULL UNIQUE,
                    goal_type TEXT NOT NULL,
                    target_weight_kg REAL,
                    weekly_workout_goal INTEGER,
                    weekly_exercise_duration_minutes INTEGER,
                    experience_level TEXT,
                    preferred_workout_types TEXT,
                    preferred_workout_days TEXT,
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