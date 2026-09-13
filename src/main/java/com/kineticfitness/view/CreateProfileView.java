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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;


public class CreateProfileView {

    private final Stage stage;
    private final User user;
    private final Runnable onBack;
    private final Consumer<User> onProfileUpdated;

    private final TextField usernameField = new TextField();
    private final TextField ageField = new TextField();
    private final TextField heightField = new TextField();
    private final TextField weightField = new TextField();
    private final ComboBox<FitnessLevel> fitnessLevelBox = new ComboBox<>();
    private final Label errorLabel = new Label();

    public CreateProfileView(Stage stage, User user, Runnable onBack, Consumer<User> onProfileUpdated) {
        this.stage = stage;
        this.user = user;
        this.onBack = onBack;
        this.onProfileUpdated = onProfileUpdated;
    }

    public void show() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(32));

        boolean isNewProfile = user.getAge() <= 0;

        Label title = new Label(isNewProfile ? "Create your profile" : "Edit your profile");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label(isNewProfile
                ? "Tell us about yourself so we can personalise your plan."
                : "Update your details to keep your plan accurate.");
        subtitle.setStyle("-fx-text-fill: gray;");

        VBox formCard = buildFormCard();

        Button saveButton = new Button(isNewProfile ? "Create profile" : "Save changes");
        saveButton.setMaxWidth(220);
        saveButton.setOnAction(e -> handleSave());

        Button cancelButton = new Button("Cancel");
        cancelButton.setMaxWidth(220);
        cancelButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        HBox buttonBox = new HBox(12, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, subtitle, formCard, errorLabel, buttonBox);

        Scene scene = new Scene(root, 460, 480);
        stage.setTitle("Kinetic Fitness - " + (isNewProfile ? "Create Profile" : "Edit Profile"));
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildFormCard() {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f4f4f6; -fx-background-radius: 8px; -fx-padding: 16px;");

        usernameField.setText(user.getUsername() != null ? user.getUsername() : "");
        usernameField.setPromptText("Alex");

        ageField.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
        ageField.setPromptText("27");

        heightField.setText(user.getHeightCm() > 0 ? String.valueOf(user.getHeightCm()) : "");
        heightField.setPromptText("175");

        weightField.setText(user.getWeightKg() > 0 ? String.valueOf(user.getWeightKg()) : "");
        weightField.setPromptText("70");

        fitnessLevelBox.getItems().addAll(FitnessLevel.values());
        fitnessLevelBox.setValue(user.getFitnessLevel() != null ? user.getFitnessLevel() : FitnessLevel.BEGINNER);
        fitnessLevelBox.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);

        grid.add(new Label("Username"), 0, 0);
        grid.add(usernameField, 0, 1, 2, 1);

        grid.add(new Label("Age"), 0, 2);
        grid.add(new Label("Fitness level"), 1, 2);
        grid.add(ageField, 0, 3);
        grid.add(fitnessLevelBox, 1, 3);

        grid.add(new Label("Height (cm)"), 0, 4);
        grid.add(new Label("Weight (kg)"), 1, 4);
        grid.add(heightField, 0, 5);
        grid.add(weightField, 1, 5);

        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        card.getChildren().add(grid);
        return card;
    }

    private void handleSave() {
        String username = usernameField.getText().trim();
        String ageText = ageField.getText().trim();
        String heightText = heightField.getText().trim();
        String weightText = weightField.getText().trim();
        FitnessLevel fitnessLevel = fitnessLevelBox.getValue();

        if (username.isEmpty() || ageText.isEmpty() || heightText.isEmpty()
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

            user.setUsername(username);
            user.setAge(age);
            user.setHeightCm(height);
            user.setWeightKg(weight);
            user.setFitnessLevel(fitnessLevel);

            if (onProfileUpdated != null) {
                onProfileUpdated.accept(user);
            }
            if (onBack != null) {
                onBack.run();
            }

        } catch (NumberFormatException ex) {
            showError("Age, height and weight must be valid numbers.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}