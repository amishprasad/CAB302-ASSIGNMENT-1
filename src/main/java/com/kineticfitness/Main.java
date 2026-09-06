package com.kineticfitness;

import com.kineticfitness.model.User;
import com.kineticfitness.view.CreateProfileView;
import com.kineticfitness.view.GoalsView;
import com.kineticfitness.view.LogWorkoutView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {

    private Stage stage;
    private final Label status = new Label("Team skeleton is running.");

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Kinetic Fitness");
        showMainMenu();
        stage.show();
    }

    /** Builds (or rebuilds) the home menu. Views call this to return here. */
    public void showMainMenu() {
        Label title = new Label("Kinetic Fitness");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button createProfileButton = new Button("Create Profile");
        createProfileButton.setOnAction(e ->
                new CreateProfileView(stage, this::showMainMenu, this::onProfileCreated).show());

        Button logWorkoutButton = new Button("Log Workout");
        logWorkoutButton.setOnAction(e ->
                new LogWorkoutView(stage, this::showMainMenu).show());

        Button goalsButton = new Button("Goals");
        goalsButton.setOnAction(e ->
                new GoalsView(stage, this::showMainMenu).show());

        for (Button button : new Button[]{createProfileButton, logWorkoutButton, goalsButton}) {
            button.setMaxWidth(220);
        }

        VBox root = new VBox(12, title, status, createProfileButton, logWorkoutButton, goalsButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        stage.setScene(new Scene(root, 440, 320));
    }

    private void onProfileCreated(User user) {
        status.setText("Welcome, " + user.getUsername() + "!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
