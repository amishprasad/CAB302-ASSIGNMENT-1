package com.kineticfitness;

import com.kineticfitness.view.CreateProfileView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("Kinetic Fitness");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label status = new Label("Team skeleton is running.");
        Button button = new Button("Test me");
        button.setOnAction(e -> status.setText("It works. Start building features on your branch."));

        Button createProfileButton = new Button("Open: Create Profile");
        createProfileButton.setOnAction(e -> new CreateProfileView(stage).show());

        VBox root = new VBox(12, title, status, button, createProfileButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        stage.setTitle("Kinetic Fitness");
        stage.setScene(new Scene(root, 440, 280));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}