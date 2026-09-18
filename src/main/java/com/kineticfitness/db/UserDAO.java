package com.kineticfitness.db;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import com.kineticfitness.util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    private static final String SELECT_COLUMNS =
            "username, fitness_level, age, height_cm, weight_kg, password, email, phone_number";


    public void save(User user) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            INSERT INTO users (username, fitness_level, age, height_cm, weight_kg, password, email, phone_number)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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
            statement.setString(6, user.getPasswordHash());
            statement.setString(7, user.getEmail());
            statement.setString(8, user.getPhoneNumber());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save user: " + e.getMessage());
        }
    }

    /** Used by the Settings page to change a signed-in user's password. */
    public void updatePassword(String username, String newHashedPassword) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newHashedPassword);
            statement.setString(2, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update password: " + e.getMessage());
        }
    }


    /**
     * Creates a brand-new account for the register page. The raw password is hashed
     * (see {@link PasswordUtil}) before it ever reaches the database.
     *
     * @return the newly created user, or {@code null} if the username, email or phone
     *         number is already taken, or the insert otherwise fails.
     */
    public User register(String username, String rawPassword, String email, String phoneNumber) {
        if (usernameExists(username) || emailExists(email) || phoneExists(phoneNumber)) {
            return null;
        }

        Connection connection = DatabaseConnection.getInstance();
        String sql = """
            INSERT INTO users (username, fitness_level, password, email, phone_number)
            VALUES (?, ?, ?, ?, ?)
        """;
        String hashedPassword = PasswordUtil.hash(rawPassword);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, FitnessLevel.BEGINNER.name());
            statement.setString(3, hashedPassword);
            statement.setString(4, email);
            statement.setString(5, phoneNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to register user: " + e.getMessage());
            return null;
        }
        return new User(username, hashedPassword, email, phoneNumber);
    }


    public User authenticate(String usernameOrEmail, String rawPassword) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usernameOrEmail);
            statement.setString(2, usernameOrEmail);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (PasswordUtil.verify(rawPassword, storedHash)) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to authenticate user: " + e.getMessage());
        }
        return null;
    }

    public boolean usernameExists(String username) {
        return exists("SELECT 1 FROM users WHERE username = ?", username);
    }

    public boolean emailExists(String email) {
        return exists("SELECT 1 FROM users WHERE email = ?", email);
    }

    public boolean phoneExists(String phoneNumber) {
        return exists("SELECT 1 FROM users WHERE phone_number = ?", phoneNumber);
    }

    private boolean exists(String sql, String value) {
        Connection connection = DatabaseConnection.getInstance();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Failed to check existing user: " + e.getMessage());
            return false;
        }
    }

    public User findByUsername(String username) {
        Connection connection = DatabaseConnection.getInstance();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users WHERE username = ?";
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
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users ORDER BY id LIMIT 1";
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

    /** The email stored on a user's account, or null if not found. */
    public String getEmail(String username) {
        Connection connection = DatabaseConnection.getInstance();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT email FROM users WHERE username = ?")) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("Failed to load email: " + e.getMessage());
        }
        return null;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("username"),
                FitnessLevel.valueOf(rs.getString("fitness_level")),
                rs.getInt("age"),
                rs.getDouble("height_cm"),
                rs.getDouble("weight_kg"));
        user.setPasswordHash(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        return user;
    }
}
