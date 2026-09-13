package com.kineticfitness.db;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;

import java.sql.*;

public class UserDAO {

    public void save(User user) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            INSERT INTO users (username, fitness_level, age, height_cm, weight_kg)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(username) DO UPDATE SET
                fitness_level = excluded.fitness_level,
                age = excluded.age,
                height_cm = excluded.height_cm,
                weight_kg = excluded.weight_kg
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFitnessLevel().name());
            statement.setInt(3, user.getAge());
            statement.setDouble(4, user.getHeightCm());
            statement.setDouble(5, user.getWeightKg());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save user: " + e.getMessage());
        }
    }

    public User findByUsername(String username) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "SELECT username, fitness_level, age, height_cm, weight_kg FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        FitnessLevel.valueOf(rs.getString("fitness_level")),
                        rs.getInt("age"),
                        rs.getDouble("height_cm"),
                        rs.getDouble("weight_kg"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user: " + e.getMessage());
        }
        return null;
    }
    public User findFirst() {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "SELECT username, fitness_level, age, height_cm, weight_kg FROM users ORDER BY id LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        FitnessLevel.valueOf(rs.getString("fitness_level")),
                        rs.getInt("age"),
                        rs.getDouble("height_cm"),
                        rs.getDouble("weight_kg"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user: " + e.getMessage());
        }
        return null;
    }
}