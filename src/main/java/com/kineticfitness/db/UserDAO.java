package com.kineticfitness.db;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;

import java.sql.*;
import java.time.LocalDate;

public class UserDAO {

    public void save(User user) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            INSERT INTO users (username, first_name, last_name, gender, email, date_of_birth, fitness_level, height_cm, weight_kg)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(username) DO UPDATE SET
                first_name = excluded.first_name,
                last_name = excluded.last_name,
                gender = excluded.gender,
                email = excluded.email,
                date_of_birth = excluded.date_of_birth,
                fitness_level = excluded.fitness_level,
                height_cm = excluded.height_cm,
                weight_kg = excluded.weight_kg
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getGender());
            statement.setString(5, user.getEmail());
            statement.setString(6, user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
            statement.setString(7, user.getFitnessLevel().name());
            statement.setDouble(8, user.getHeightCm());
            statement.setDouble(9, user.getWeightKg());
            statement.executeUpdate();
        } catch (SQLException e) {
            // Surface the real cause to the caller instead of only logging
            // it, so the UI can show the actual problem (e.g. a schema
            // mismatch like "no such column: first_name") rather than
            // silently failing to save and leaving the user confused.
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    public User findByUsername(String username) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            SELECT username, first_name, last_name, gender, email, date_of_birth, fitness_level, height_cm, weight_kg
            FROM users WHERE username = ?
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user: " + e.getMessage());
        }
        return null;
    }

    public User findFirst() {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            SELECT username, first_name, last_name, gender, email, date_of_birth, fitness_level, height_cm, weight_kg
            FROM users ORDER BY id LIMIT 1
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user: " + e.getMessage());
        }
        return null;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        String dobStr = rs.getString("date_of_birth");
        LocalDate dob = dobStr != null ? LocalDate.parse(dobStr) : null;

        return new User(
                rs.getString("username"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("gender"),
                rs.getString("email"),
                dob,
                FitnessLevel.valueOf(rs.getString("fitness_level")),
                rs.getDouble("height_cm"),
                rs.getDouble("weight_kg"));
    }
}