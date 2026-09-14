package com.kineticfitness;

import com.kineticfitness.db.DatabaseConnection;
import com.kineticfitness.db.ProfileDAO;
import com.kineticfitness.session.UserSession;
import com.kineticfitness.view.*;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseConnection.getInstance();

        // Every session now starts at the login screen — no more auto-loading the first user.
        showLogin(stage, null);
    }

    private void showLogin(Stage stage, String infoMessage) {
        new Login(
                stage,
                user -> {
                    UserSession.setCurrentUser(user);
                    launchApp(stage);
                },
                () -> showRegister(stage),
                infoMessage
        ).show();
    }

    private void showRegister(Stage stage) {
        new Register(
                stage,
                () -> showLogin(stage, "Account created — please log in."),
                () -> showLogin(stage, null)
        ).show();
    }

    private void launchApp(Stage stage) {
        new AppShell(stage)
                .add(new DashboardView())
                .add(new ProfileDetailsView())
                .add(new MealLogView())// ← was ProfilePage, renamed on main
                .add(new LogWorkoutView())
                .add(new WorkoutHistoryView())
                .add(new ExerciseSelectionView())
                .add(new GoalsView())
                .add(new PlaceholderPage("Progress", "amish"))
                .add(new ScheduleView())
                .add(new PlaceholderPage("Settings", "amish"))
                .onLogout(() -> {
                    UserSession.clear();
                    showLogin(stage, null);
                })
                .show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}