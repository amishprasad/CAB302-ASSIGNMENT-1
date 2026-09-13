package com.kineticfitness.view;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;

public class CreateProfileView {

    private final Stage stage;
    private final User user;
    private final Runnable onBack;
    private final Consumer<User> onProfileUpdated;

    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final ComboBox<String> genderBox = new ComboBox<>();
    private final TextField emailField = new TextField();
    private final DatePicker dobPicker = new DatePicker();
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

        boolean isNewProfile = user.getDateOfBirth() == null;

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

        Scene scene = new Scene(root, 460, 560);
        stage.setTitle("Kinetic Fitness - " + (isNewProfile ? "Create Profile" : "Edit Profile"));
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildFormCard() {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f4f4f6; -fx-background-radius: 8px; -fx-padding: 16px;");

        firstNameField.setText(user.getFirstName() != null ? user.getFirstName() : "");
        firstNameField.setPromptText("Alex");

        lastNameField.setText(user.getLastName() != null ? user.getLastName() : "");
        lastNameField.setPromptText("Rivera");

        genderBox.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        genderBox.setValue(user.getGender());
        genderBox.setPromptText("Select gender");
        genderBox.setMaxWidth(Double.MAX_VALUE);

        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        emailField.setPromptText("alex@example.com");

        dobPicker.setValue(user.getDateOfBirth());
        dobPicker.setPromptText("DD/MM/YYYY");
        dobPicker.setMaxWidth(Double.MAX_VALUE);

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

        grid.add(new Label("First name"), 0, 0);
        grid.add(new Label("Last name"), 1, 0);
        grid.add(firstNameField, 0, 1);
        grid.add(lastNameField, 1, 1);

        grid.add(new Label("Gender"), 0, 2);
        grid.add(new Label("Email"), 1, 2);
        grid.add(genderBox, 0, 3);
        grid.add(emailField, 1, 3);

        grid.add(new Label("Date of birth"), 0, 4);
        grid.add(new Label("Fitness level"), 1, 4);
        grid.add(dobPicker, 0, 5);
        grid.add(fitnessLevelBox, 1, 5);

        grid.add(new Label("Height (cm)"), 0, 6);
        grid.add(new Label("Weight (kg)"), 1, 6);
        grid.add(heightField, 0, 7);
        grid.add(weightField, 1, 7);

        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        card.getChildren().add(grid);
        return card;
    }

    private void handleSave() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String gender = genderBox.getValue();
        String email = emailField.getText().trim();
        LocalDate dob = dobPicker.getValue();
        String heightText = heightField.getText().trim();
        String weightText = weightField.getText().trim();
        FitnessLevel fitnessLevel = fitnessLevelBox.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || gender == null || email.isEmpty()
                || dob == null || heightText.isEmpty() || weightText.isEmpty() || fitnessLevel == null) {
            showError("Please fill in every field before continuing.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("Enter a valid email address.");
            return;
        }
        if (dob.isAfter(LocalDate.now())) {
            showError("Date of birth can't be in the future.");
            return;
        }

        try {
            double height = Double.parseDouble(heightText);
            double weight = Double.parseDouble(weightText);

            if (height <= 0 || weight <= 0) {
                showError("Height and weight must be positive numbers.");
                return;
            }

            errorLabel.setVisible(false);

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setGender(gender);
            user.setEmail(email);
            user.setDateOfBirth(dob);
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
            showError("Height and weight must be valid numbers.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}