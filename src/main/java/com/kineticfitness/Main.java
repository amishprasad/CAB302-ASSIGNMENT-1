package com.kineticfitness;

import com.kineticfitness.view.*;
import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage stage) {
        com.kineticfitness.db.DatabaseConnection.getInstance();
        // The AppShell owns the window + sidebar and swaps the centre content per page.
        // Each screen implements Page and is registered here. Real pages are plugged in as
        // they're built; the rest show a labelled placeholder so navigation works end to end.
        new AppShell(stage)
                .add(new DashboardView())
                .add(new PlaceholderPage("Profile", "Adriel"))
                .add(new PlaceholderPage("Log Workout", "Junxi"))
                .add(new WorkoutHistoryView())
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
