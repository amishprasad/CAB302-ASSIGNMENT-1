package com.kineticfitness.view;

import com.kineticfitness.model.Exercise;
import com.kineticfitness.model.Workout;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Screen for logging a single workout: add exercises (name / sets / reps),
 * see them listed, and watch the running total of reps update live.
 */
public class LogWorkoutView {

    private final Stage stage;
    private final Runnable onBack;

    private final Workout workout = new Workout(LocalDate.now());

    private final TextField nameField = new TextField();
    private final TextField setsField = new TextField();
    private final TextField repsField = new TextField();
    private final ListView<String> exerciseList = new ListView<>();
    private final Label totalRepsLabel = new Label("Total reps: 0");
    private final Label errorLabel = new Label();

    public LogWorkoutView(Stage stage) {
        this(stage, null);
    }

    public LogWorkoutView(Stage stage, Runnable onBack) {
        this.stage = stage;
        this.onBack = onBack;
    }

    public void show() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(32));

        Label title = new Label("Log a workout");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Add each exercise you did today.");
        subtitle.setStyle("-fx-text-fill: gray;");

        GridPane form = buildForm();

        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button addButton = new Button("Add exercise");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> handleAddExercise());

        totalRepsLabel.setStyle("-fx-font-weight: bold;");
        exerciseList.setPrefHeight(160);

        root.getChildren().addAll(title, subtitle, form, addButton, errorLabel,
                exerciseList, totalRepsLabel);

        if (onBack != null) {
            Button backButton = new Button("Back to menu");
            backButton.setOnAction(e -> onBack.run());
            root.getChildren().add(backButton);
        }

        Scene scene = new Scene(root, 440, 560);
        stage.setTitle("Kinetic Fitness - Log Workout");
        stage.setScene(scene);
        stage.show();
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        nameField.setPromptText("Push-ups");
        setsField.setPromptText("3");
        repsField.setPromptText("10");

        HBox.setHgrow(nameField, Priority.ALWAYS);

        grid.add(new Label("Exercise"), 0, 0);
        grid.add(new Label("Sets"), 1, 0);
        grid.add(new Label("Reps"), 2, 0);
        grid.add(nameField, 0, 1);
        grid.add(setsField, 1, 1);
        grid.add(repsField, 2, 1);

        setsField.setPrefWidth(70);
        repsField.setPrefWidth(70);

        return grid;
    }

    private void handleAddExercise() {
        String name = nameField.getText().trim();
        String setsText = setsField.getText().trim();
        String repsText = repsField.getText().trim();

        if (name.isEmpty() || setsText.isEmpty() || repsText.isEmpty()) {
            showError("Please enter an exercise name, sets and reps.");
            return;
        }

        try {
            int sets = Integer.parseInt(setsText);
            int reps = Integer.parseInt(repsText);

            if (sets <= 0 || reps <= 0) {
                showError("Sets and reps must be positive whole numbers.");
                return;
            }

            Exercise exercise = new Exercise(name, sets, reps);
            workout.addExercise(exercise);

            exerciseList.getItems().add(
                    String.format("%s  -  %d x %d  (%d reps)",
                            name, sets, reps, exercise.totalReps()));
            totalRepsLabel.setText("Total reps: " + workout.totalReps());

            errorLabel.setVisible(false);
            clearForm();

        } catch (NumberFormatException ex) {
            showError("Sets and reps must be valid whole numbers.");
        }
    }

    private void clearForm() {
        nameField.clear();
        setsField.clear();
        repsField.clear();
        nameField.requestFocus();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
