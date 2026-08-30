package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class CreateProfileView {

    private final Stage stage;

    // Form fields
    private final TextField nameField = new TextField();
    private final TextField ageField = new TextField();
    private final TextField heightField = new TextField();
    private final TextField weightField = new TextField();
    private final ComboBox<String> fitnessLevelBox = new ComboBox<>();
    private final Label errorLabel = new Label();

    public CreateProfileView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(32));

        Label title = new Label("Create your profile");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Tell us about yourself so we can personalise your plan.");
        subtitle.setStyle("-fx-text-fill: gray;");

        GridPane form = buildForm();

        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button createButton = new Button("Create profile");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setOnAction(e -> handleCreateProfile());

        root.getChildren().addAll(title, subtitle, form, errorLabel, createButton);

        Scene scene = new Scene(root, 420, 480);
        stage.setTitle("Kinetic Fitness - Create Profile");
        stage.setScene(scene);
        stage.show();
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        ageField.setPromptText("27");
        heightField.setPromptText("175");
        weightField.setPromptText("70");
        nameField.setPromptText("Alex");

        fitnessLevelBox.getItems().addAll("Beginner", "Intermediate", "Advanced");
        fitnessLevelBox.setPromptText("Select level");
        fitnessLevelBox.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("First name"), 0, 0);
        grid.add(nameField, 0, 1, 2, 1);

        grid.add(new Label("Age"), 0, 2);
        grid.add(new Label("Fitness level"), 1, 2);
        grid.add(ageField, 0, 3);
        grid.add(fitnessLevelBox, 1, 3);

        grid.add(new Label("Height (cm)"), 0, 4);
        grid.add(new Label("Weight (kg)"), 1, 4);
        grid.add(heightField, 0, 5);
        grid.add(weightField, 1, 5);

        return grid;
    }


    private void handleCreateProfile() {
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String heightText = heightField.getText().trim();
        String weightText = weightField.getText().trim();
        String fitnessLevel = fitnessLevelBox.getValue();

        if (name.isEmpty() || ageText.isEmpty() || heightText.isEmpty()
                || weightText.isEmpty() || fitnessLevel == null) {
            showError("Please fill in every field before continuing.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            double height = Double.parseDouble(heightText);
            double weight = Double.parseDouble(weightText);

            if (age <= 0 || height <= 0 || weight <= 0) {
                showError("Age, height and weight must be positive numbers.");
                return;
            }

            errorLabel.setVisible(false);

            // TODO: replace with real persistence, e.g.
            // UserProfile profile = new UserProfile(name, age, height, weight, fitnessLevel);
            // new UserProfileDAO().save(profile);
            System.out.printf(
                    "Profile created: name=%s, age=%d, height=%.1fcm, weight=%.1fkg, level=%s%n",
                    name, age, height, weight, fitnessLevel);

        } catch (NumberFormatException ex) {
            showError("Age, height and weight must be valid numbers.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}