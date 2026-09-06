package com.kineticfitness.view;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class CreateProfileView {

    private final Stage stage;
    private final Runnable onBack;
    private final Consumer<User> onProfileCreated;

    // Form fields
    private final TextField nameField = new TextField();
    private final TextField ageField = new TextField();
    private final TextField heightField = new TextField();
    private final TextField weightField = new TextField();
    private final ComboBox<String> fitnessLevelBox = new ComboBox<>();
    private final Label errorLabel = new Label();

    public CreateProfileView(Stage stage) {
        this(stage, null, null);
    }

    public CreateProfileView(Stage stage, Runnable onBack, Consumer<User> onProfileCreated) {
        this.stage = stage;
        this.onBack = onBack;
        this.onProfileCreated = onProfileCreated;
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

        if (onBack != null) {
            Button backButton = new Button("Back to menu");
            backButton.setOnAction(e -> onBack.run());
            root.getChildren().add(backButton);
        }

        Scene scene = new Scene(root, 420, 520);
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

            User user = new User(
                    name,
                    FitnessLevel.valueOf(fitnessLevel.toUpperCase()),
                    age, height, weight);

            if (onProfileCreated != null) {
                onProfileCreated.accept(user);
            }

            showConfirmation(user);

        } catch (NumberFormatException ex) {
            showError("Age, height and weight must be valid numbers.");
        }
    }

    private void showConfirmation(User user) {
        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        Label heading = new Label("Profile created ✓");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label details = new Label(String.format(
                "%s  -  %s%nAge %d  |  %.0f cm  |  %.1f kg",
                user.getUsername(),
                displayLevel(user.getFitnessLevel()),
                user.getAge(), user.getHeightCm(), user.getWeightKg()));
        details.setStyle("-fx-text-fill: gray;");

        Label bmi = new Label(String.format(
                "BMI: %.1f  (%s)", user.getBmi(), bmiCategory(user.getBmi())));
        bmi.setStyle("-fx-font-weight: bold;");

        root.getChildren().addAll(heading, details, bmi);

        if (onBack != null) {
            Button menuButton = new Button("Back to menu");
            menuButton.setOnAction(e -> onBack.run());
            root.getChildren().add(menuButton);
        } else {
            Button editButton = new Button("Edit profile");
            editButton.setOnAction(e -> show());
            root.getChildren().add(editButton);
        }

        Scene scene = new Scene(root, 420, 320);
        stage.setScene(scene);
        stage.show();
    }

    private String displayLevel(FitnessLevel level) {
        String name = level.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String bmiCategory(double bmi) {
        if (bmi < 18.5) return "underweight";
        if (bmi < 25) return "healthy";
        if (bmi < 30) return "overweight";
        return "obese";
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
