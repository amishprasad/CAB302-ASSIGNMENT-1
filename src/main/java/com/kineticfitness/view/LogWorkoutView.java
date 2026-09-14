package com.kineticfitness.view;

import com.kineticfitness.db.WorkoutDAO;
import com.kineticfitness.model.BodyPart;
import com.kineticfitness.model.Exercise;
import com.kineticfitness.model.User;
import com.kineticfitness.model.Workout;
import com.kineticfitness.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Screen for logging a single workout: add exercises (name / sets / reps),
 * see them listed, watch the running total of reps update live, and save to User history.
 */

public class LogWorkoutView implements Page {

    private final WorkoutDAO workoutDAO = new WorkoutDAO();

    @Override
    public String label() {
        return "Log Workout";
    }

    @Override
    public Node getContent() {
        Workout workout = new Workout(LocalDate.now());
        ObservableList<Exercise> exerciseData = FXCollections.observableArrayList();
        TableView<Exercise> exerciseTable = new TableView<>();
        TextField nameField = new TextField();
        TextField setsField = new TextField();
        TextField repsField = new TextField();
        ComboBox<BodyPart> bodyPartCombo = new ComboBox<>();
        Label statusLabel = new Label();
        boolean[] isSaved = { false }; // one-element array so the save handler lambda can mutate it

        HBox content = new HBox(20);
        content.getStyleClass().add("content-area");
        content.setPadding(new Insets(24));

        content.getChildren().addAll(
                buildExercisePanel(exerciseTable, exerciseData, statusLabel, workout, isSaved),
                buildAddExercisePanel(nameField, setsField, repsField, bodyPartCombo, exerciseData, statusLabel));

        return content;
    }

    // ---------- Content ----------

    private VBox buildExercisePanel(TableView<Exercise> exerciseTable, ObservableList<Exercise> exerciseData,
                                    Label statusLabel, Workout workout, boolean[] isSaved) {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("card");
        HBox.setHgrow(panel, Priority.ALWAYS);

        Label title = new Label("Log Active Workout");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Record each exercise, its sets/reps, and the body part it targets.");
        subtitle.getStyleClass().add("page-subtitle");

        setupTableColumns(exerciseTable);
        exerciseTable.setItems(exerciseData);
        VBox.setVgrow(exerciseTable, Priority.ALWAYS);

        statusLabel.getStyleClass().add("status-label");
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        Button deleteButton = new Button("Delete Selected");
        deleteButton.getStyleClass().add("btn-secondary");
        deleteButton.setOnAction(e -> handleDeleteExercise(exerciseTable, exerciseData, statusLabel));

        Button saveButton = new Button("Save Workout Session");
        saveButton.getStyleClass().add("btn-primary");
        saveButton.setOnAction(e -> handleSaveWorkout(exerciseData, statusLabel, workout, isSaved));

        HBox actionRow = new HBox(10, statusLabel, deleteButton, saveButton);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        panel.getChildren().addAll(title, subtitle, exerciseTable, actionRow);
        return panel;
    }

    private void setupTableColumns(TableView<Exercise> exerciseTable) {
        TableColumn<Exercise, BodyPart> colBodyPart = new TableColumn<>("Body Part");
        colBodyPart.setCellValueFactory(new PropertyValueFactory<>("bodyPart"));
        colBodyPart.setPrefWidth(110);

        TableColumn<Exercise, String> colName = new TableColumn<>("Exercise");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(220);

        TableColumn<Exercise, Number> colSets = new TableColumn<>("Sets");
        colSets.setCellValueFactory(new PropertyValueFactory<>("sets"));
        colSets.setPrefWidth(70);

        TableColumn<Exercise, Number> colReps = new TableColumn<>("Reps");
        colReps.setCellValueFactory(new PropertyValueFactory<>("reps"));
        colReps.setPrefWidth(70);

        exerciseTable.getColumns().addAll(colBodyPart, colName, colSets, colReps);
    }

    private VBox buildAddExercisePanel(TextField nameField, TextField setsField, TextField repsField,
                                       ComboBox<BodyPart> bodyPartCombo, ObservableList<Exercise> exerciseData,
                                       Label statusLabel) {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("card");
        panel.setPrefWidth(260);

        Label title = new Label("Add Exercise");
        title.getStyleClass().add("page-title-sm");

        Label nameLabel = new Label("Exercise Name");
        nameLabel.getStyleClass().add("field-label");
        nameField.setPromptText("e.g. Overhead Press");

        Label setsLabel = new Label("Sets");
        setsLabel.getStyleClass().add("field-label");
        setsField.setPromptText("3");

        Label repsLabel = new Label("Reps");
        repsLabel.getStyleClass().add("field-label");
        repsField.setPromptText("10");

        Label bodyPartLabel = new Label("Body Part");
        bodyPartLabel.getStyleClass().add("field-label");
        bodyPartCombo.setPromptText("Select body part");
        bodyPartCombo.setPrefWidth(1000);
        bodyPartCombo.getItems().addAll(BodyPart.values());

        Button appendButton = new Button("Add to Log");
        appendButton.getStyleClass().add("btn-outline");
        appendButton.setOnAction(e -> handleAddExercise(
                nameField, setsField, repsField, bodyPartCombo, exerciseData, statusLabel));

        panel.getChildren().addAll(title, nameLabel, nameField, setsLabel, setsField,
                repsLabel, repsField, bodyPartLabel, bodyPartCombo, appendButton);
        return panel;
    }

    // ---------- Actions ----------

    private void handleAddExercise(TextField nameField, TextField setsField, TextField repsField,
                                   ComboBox<BodyPart> bodyPartCombo, ObservableList<Exercise> exerciseData,
                                   Label statusLabel) {
        String name = nameField.getText().trim();
        String setsText = setsField.getText().trim();
        String repsText = repsField.getText().trim();
        BodyPart bodyPart = bodyPartCombo.getValue();

        if (name.isEmpty() || setsText.isEmpty() || repsText.isEmpty() || bodyPart == null) {
            showStatus(statusLabel, "Please fill in name, sets, reps, and body part.", true);
            return;
        }

        int sets;
        int reps;
        try {
            sets = Integer.parseInt(setsText);
            reps = Integer.parseInt(repsText);
        } catch (NumberFormatException ex) {
            showStatus(statusLabel, "Sets and reps must be whole numbers.", true);
            return;
        }

        if (sets <= 0 || reps <= 0) {
            showStatus(statusLabel, "Sets and reps must be positive whole numbers.", true);
            return;
        }

        exerciseData.add(new Exercise(name, sets, reps, bodyPart));

        nameField.clear();
        setsField.clear();
        repsField.clear();
        bodyPartCombo.setValue(null);
        nameField.requestFocus();
        showStatus(statusLabel, "Added " + name + ".", false);
    }

    private void handleDeleteExercise(TableView<Exercise> exerciseTable, ObservableList<Exercise> exerciseData,
                                      Label statusLabel) {
        Exercise selected = exerciseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus(statusLabel, "Select an exercise in the table to delete it.", true);
            return;
        }
        exerciseData.remove(selected);
        showStatus(statusLabel, "Removed " + selected.getName() + ".", false);
    }

    private void handleSaveWorkout(ObservableList<Exercise> exerciseData, Label statusLabel,
                                   Workout workout, boolean[] isSaved) {
        if (exerciseData.isEmpty()) {
            showStatus(statusLabel, "Add at least one exercise before saving.", true);
            return;
        }
        if (isSaved[0]) {
            showStatus(statusLabel, "This workout has already been saved.", true);
            return;
        }

        User user = UserSession.getCurrentUser();
        if (user == null) {
            showStatus(statusLabel, "No active user - log in before saving a workout.", true);
            return;
        }

        exerciseData.forEach(workout::addExercise);
        user.addWorkout(workout);          // keep in-memory state consistent for this session
        workoutDAO.save(user, workout);    // persist to SQLite

        isSaved[0] = true;
        showStatus(statusLabel, "Workout session saved.", false);
    }

    private void showStatus(Label statusLabel, String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #dc2626;" : "-fx-text-fill: #16a34a;");
    }
}