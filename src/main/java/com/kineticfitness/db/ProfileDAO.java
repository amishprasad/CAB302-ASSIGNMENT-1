package com.kineticfitness.db;

import com.kineticfitness.model.User;
import com.kineticfitness.session.UserSession;
import com.kineticfitness.view.LocalProfileStore;

import java.sql.*;
import java.time.LocalDate;
import java.time.Period;

/**
 * Persists the single active profile (LocalProfileStore) to the database.
 */
public class ProfileDAO {

    private final UserDAO userDAO = new UserDAO();

    public void save(LocalProfileStore s) {
        String username = currentUsername();
        if (username == null) {
            System.err.println("No signed-in user, profile not saved.");
            return;
        }
        Connection conn = DatabaseConnection.getInstance();
        String sql = """
            INSERT INTO profiles (username, first_name, email, gender, photo_path, date_of_birth,
                                  height_cm, weight_kg, fitness_level, primary_goal, target_weight_kg,
                                  weekly_workout_goal, weekly_duration_minutes, experience_level,
                                  preferred_types, preferred_days,
                                  goal_start_date, goal_target_date, goal_achieved_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(username) DO UPDATE SET
                first_name = excluded.first_name, email = excluded.email, gender = excluded.gender,
                photo_path = excluded.photo_path, date_of_birth = excluded.date_of_birth,
                height_cm = excluded.height_cm, weight_kg = excluded.weight_kg,
                fitness_level = excluded.fitness_level, primary_goal = excluded.primary_goal,
                target_weight_kg = excluded.target_weight_kg, weekly_workout_goal = excluded.weekly_workout_goal,
                weekly_duration_minutes = excluded.weekly_duration_minutes,
                experience_level = excluded.experience_level,
                preferred_types = excluded.preferred_types, preferred_days = excluded.preferred_days,
                goal_start_date = excluded.goal_start_date, goal_target_date = excluded.goal_target_date,
                goal_achieved_date = excluded.goal_achieved_date
        """;
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, username);
            st.setString(2, s.firstName);
            st.setString(3, s.email);
            st.setString(4, s.gender == null ? null : s.gender.name());
            st.setString(5, s.photoPath);
            st.setString(6, s.dateOfBirth == null ? null : s.dateOfBirth.toString());
            st.setDouble(7, s.heightCm);
            st.setDouble(8, s.weightKg);
            st.setString(9, s.fitnessLevel == null ? null : s.fitnessLevel.name());
            st.setString(10, s.primaryGoal == null ? null : s.primaryGoal.name());
            st.setDouble(11, s.targetWeightKg);
            st.setInt(12, s.weeklyWorkoutGoal);
            st.setInt(13, s.weeklyExerciseDurationMinutes);
            st.setString(14, s.experienceLevel == null ? null : s.experienceLevel.name());
            st.setString(15, String.join(",", s.preferredWorkoutTypes));
            st.setString(16, String.join(",", s.preferredWorkoutDays));
            st.setString(17, s.goalStartDate == null ? null : s.goalStartDate.toString());
            st.setString(18, s.goalTargetDate == null ? null : s.goalTargetDate.toString());
            st.setString(19, s.goalAchievedDate == null ? null : s.goalAchievedDate.toString());
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save profile: " + e.getMessage());
        }
    }

    public boolean load(LocalProfileStore s) {
        // Always start from a clean store: a previous account's data must never survive
        // into this one, and a user with no saved profile must load as empty.
        s.reset();
        String username = currentUsername();
        if (username == null) return false;

        Connection conn = DatabaseConnection.getInstance();
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM profiles WHERE username = ?")) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                s.firstName = orEmpty(rs.getString("first_name"));
                s.email = orEmpty(rs.getString("email"));
                String g = rs.getString("gender");
                s.gender = g == null ? null : LocalProfileStore.Gender.valueOf(g);
                s.photoPath = rs.getString("photo_path");
                String dob = rs.getString("date_of_birth");
                s.dateOfBirth = (dob == null || dob.isEmpty()) ? null : LocalDate.parse(dob);
                s.heightCm = rs.getDouble("height_cm");
                s.weightKg = rs.getDouble("weight_kg");
                String fl = rs.getString("fitness_level");
                if (fl != null) s.fitnessLevel = LocalProfileStore.FitnessLevel.valueOf(fl);
                String pg = rs.getString("primary_goal");
                s.primaryGoal = pg == null ? null : LocalProfileStore.PrimaryGoal.valueOf(pg);
                s.targetWeightKg = rs.getDouble("target_weight_kg");
                s.weeklyWorkoutGoal = rs.getInt("weekly_workout_goal");
                s.weeklyExerciseDurationMinutes = rs.getInt("weekly_duration_minutes");
                String el = rs.getString("experience_level");
                if (el != null) s.experienceLevel = LocalProfileStore.FitnessLevel.valueOf(el);
                fillSet(s.preferredWorkoutTypes, rs.getString("preferred_types"));
                fillSet(s.preferredWorkoutDays, rs.getString("preferred_days"));
                s.goalStartDate = parseDate(rs.getString("goal_start_date"));
                s.goalTargetDate = parseDate(rs.getString("goal_target_date"));
                s.goalAchievedDate = parseDate(rs.getString("goal_achieved_date"));

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Failed to load profile: " + e.getMessage());
        }
        return false;
    }

    private User toUser(LocalProfileStore s) {
        String username = !s.firstName.isEmpty() ? s.firstName
                : (!s.email.isEmpty() ? s.email : null);
        if (username == null) return null;
        int age = s.dateOfBirth == null ? 0 : Period.between(s.dateOfBirth, LocalDate.now()).getYears();
        com.kineticfitness.model.FitnessLevel level =
                com.kineticfitness.model.FitnessLevel.valueOf(s.fitnessLevel.name());
        return new User(username, level, age, s.heightCm, s.weightKg);
    }

    private String currentUsername() {
        return UserSession.getCurrentUser() == null ? null : UserSession.getCurrentUser().getUsername();
    }

    private String orEmpty(String v) { return v == null ? "" : v; }

    private LocalDate parseDate(String v) {
        return (v == null || v.isEmpty()) ? null : LocalDate.parse(v);
    }

    private void fillSet(java.util.Set<String> set, String joined) {
        set.clear();
        if (joined == null || joined.isEmpty()) return;
        for (String part : joined.split(",")) {
            if (!part.isEmpty()) set.add(part);
        }
    }
}