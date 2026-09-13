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
        new com.kineticfitness.db.ProfileDAO().load(com.kineticfitness.view.LocalProfileStore.getInstance());

        new AppShell(stage)
                .add(new DashboardView())
                .add(new ProfileDetailsView())
                .add(new PlaceholderPage("Log Workout", "Junxi"))
                .add(new WorkoutHistoryView())
                .add(new ExerciseSelectionView())
                .add(new GoalsView())
                .add(new PlaceholderPage("Progress", "amish"))
                .add(new ScheduleView())
                .add(new PlaceholderPage("Settings", "amish"))
                .show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}