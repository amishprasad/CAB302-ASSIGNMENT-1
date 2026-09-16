package com.kineticfitness;

import com.kineticfitness.db.DatabaseConnection;
import com.kineticfitness.db.ProfileDAO;
import com.kineticfitness.db.ScheduleDAO;
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
                    loadProfileFor(user.getUsername());
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
                .add(new ScheduleView())
                .add(new ProgressAnalyticsView())
                .add(new SettingsView())
                .withReminders(Main::currentUserSchedule)
                .onLogout(() -> {
                    UserSession.clear();
                    LocalProfileStore.getInstance().clear();
                    showLogin(stage, null);
                })
                .show();
    }

    /**
     * Loads this account's saved profile into the shared store, replacing whatever
     * the previous user left behind. Without this the profile is written to the
     * database but never read back, so the app asks you to create it every login.
     */
    private static void loadProfileFor(String username) {
        LocalProfileStore store = LocalProfileStore.getInstance();
        store.clear();
        new ProfileDAO().load(store, username);
    }

    /**
     * The signed-in user's schedule, used by the reminder strip in the app shell.
     * Returns an empty list when nobody is signed in, so the strip simply stays hidden.
     */
    private static java.util.List<com.kineticfitness.model.ScheduledWorkout> currentUserSchedule() {
        if (!UserSession.isLoggedIn()) {
            return java.util.List.of();
        }
        return new ScheduleDAO().findForUser(UserSession.getCurrentUser().getUsername());
    }

    public static void main(String[] args) {
        launch(args);
    }
}