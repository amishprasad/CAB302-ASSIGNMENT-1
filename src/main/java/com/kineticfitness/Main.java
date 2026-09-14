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
        showLogin(stage, null);
    }

    private void showLogin(Stage stage, String infoMessage) {
        new Login(
                stage,
                user -> {
                    UserSession.setCurrentUser(user);
                    new ProfileDAO().load(LocalProfileStore.getInstance());
                    boolean hasProfile = LocalProfileStore.getInstance().hasPersonalDetails();
                    launchApp(stage, hasProfile ? "Dashboard" : "Profile");
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

    private void launchApp(Stage stage, String startLabel) {
        new AppShell(stage)
                .add(new DashboardView())
                .add(new ProfileDetailsView())
                .add(new PlaceholderPage("Log Workout", "Junxi"))
                .add(new WorkoutHistoryView())
                .add(new ExerciseSelectionView())
                .add(new GoalsView())
                .add(new GoalSettingsView())
                .add(new PlaceholderPage("Progress", "amish"))
                .add(new ScheduleView())
                .add(new PlaceholderPage("Settings", "amish"))
                .onLogout(() -> {
                    UserSession.clear();
                    showLogin(stage, null);
                })
                .show(startLabel);
    }

    public static void main(String[] args) {
        launch(args);
    }
}