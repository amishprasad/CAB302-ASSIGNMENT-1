package com.kineticfitness;

import com.kineticfitness.db.DatabaseConnection;
import com.kineticfitness.db.UserDAO;
import com.kineticfitness.model.User;
import com.kineticfitness.session.UserSession;
import com.kineticfitness.view.*;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseConnection.getInstance();

        // Reload the saved user so their data is there after a restart.
        User existing = new UserDAO().findFirst();
        if (existing != null) {
            UserSession.setCurrentUser(existing);
        }

        new AppShell(stage)
                .add(new DashboardView())
                .add(new ProfilePage())                        // ← was PlaceholderPage("Profile", "Adriel")
                .add(new PlaceholderPage("Log Workout", "Junxi"))
                .add(new WorkoutHistoryView())
                .add(new ExerciseSelectionView())
                .add(new PlaceholderPage("Goals", ""))
                .add(new PlaceholderPage("Progress", "amish"))
                .add(new ScheduleView())
                .add(new PlaceholderPage("Settings", "amish"))
                .show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}