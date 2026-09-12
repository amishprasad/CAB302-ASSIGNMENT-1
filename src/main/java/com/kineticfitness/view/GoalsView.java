package com.kineticfitness.view;

import com.kineticfitness.model.Goal;
import com.kineticfitness.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Screen for setting fitness goals and tracking progress against them.
 * Add a goal with a target, select it, and log progress to see the bar fill.
 */
public class GoalsView {

    private final Stage stage;
    private final User user;
    private final Runnable onBack;

    private final TextField descriptionField = new TextField();
    private final TextField targetField = new TextField();
    private final ListView<Goal> goalList = new ListView<>();
    private final Label errorLabel = new Label();

    // Progress panel (for the selected goal)
    private final Label selectedLabel = new Label("Select a goal to log progress.");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label progressLabel = new Label();
    private final TextField progressField = new TextField();
    private final Button logProgressButton = new Button("Log progress");

    public GoalsView(Stage stage) {
        this(stage, null, null);
    }

    public GoalsView(Stage stage, Runnable onBack) {
        this(stage, null, onBack);
    }

    public GoalsView(Stage stage, User user, Runnable onBack) {
        this.stage = stage;
        this.user = user;
        this.onBack = onBack;
    }

    public void show() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(32));

        Label title = new Label("Your goals");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label(user != null ? "Tracking goals for " + user.getUsername() : "Set targets and track your milestones.");
        subtitle.setStyle("-fx-text-fill: gray;");

        GridPane form = buildForm();

        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button addButton = new Button("Add goal");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> handleAddGoal());

        configureGoalList();
        VBox progressPanel = buildProgressPanel();

        root.getChildren().addAll(title, subtitle, form, addButton, errorLabel, goalList, progressPanel);

        if (onBack != null) {
            Button backButton = new Button("Back to menu");
            backButton.setOnAction(e -> onBack.run());
            root.getChildren().add(backButton);
        }

        Scene scene = new Scene(root, 460, 640);
        stage.setTitle("Kinetic Fitness - Goals");
        stage.setScene(scene);
        stage.show();
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        descriptionField.setPromptText("Run 100 km");
        targetField.setPromptText("100");
        targetField.setPrefWidth(90);

        grid.add(new Label("Goal"), 0, 0);
        grid.add(new Label("Target"), 1, 0);
        grid.add(descriptionField, 0, 1);
        grid.add(targetField, 1, 1);

        return grid;
    }

    private void configureGoalList() {
        goalList.setPrefHeight(160);
        if (user != null) {
            goalList.getItems().setAll(user.getGoals());
        }
        goalList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Goal goal, boolean empty) {
                super.updateItem(goal, empty);
                if (empty || goal == null) {
                    setText(null);
                } else {
                    String tick = goal.isAchieved() ? "  ✓" : "";
                    setText(String.format("%s  -  %d/%d (%.0f%%)%s",
                            goal.getDescription(), goal.getCurrentValue(),
                            goal.getTargetValue(), goal.progressPercent(), tick));
                }
            }
        });
        goalList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> refreshProgressPanel(selected));
    }

    private VBox buildProgressPanel() {
        selectedLabel.setStyle("-fx-font-weight: bold;");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        GridPane row = new GridPane();
        row.setHgap(10);
        progressField.setPromptText("Amount to add");
        progressField.setPrefWidth(140);
        logProgressButton.setDisable(true);
        logProgressButton.setOnAction(e -> handleLogProgress());
        row.add(progressField, 0, 0);
        row.add(logProgressButton, 1, 0);

        VBox panel = new VBox(8, selectedLabel, progressBar, progressLabel, row);
        panel.setPadding(new Insets(12, 0, 0, 0));
        return panel;
    }

    private void handleAddGoal() {
        String description = descriptionField.getText().trim();
        String targetText = targetField.getText().trim();

        if (description.isEmpty() || targetText.isEmpty()) {
            showError("Please enter a goal and a target value.");
            return;
        }

        try {
            int target = Integer.parseInt(targetText);
            if (target <= 0) {
                showError("Target must be a positive whole number.");
                return;
            }

            Goal newGoal = new Goal(description, target);
            if (user != null) {
                user.addGoal(newGoal);
            }
            goalList.getItems().add(newGoal);
            errorLabel.setVisible(false);
            descriptionField.clear();
            targetField.clear();

        } catch (NumberFormatException ex) {
            showError("Target must be a valid whole number.");
        }
    }

    private void handleLogProgress() {
        Goal selected = goalList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        String amountText = progressField.getText().trim();
        if (amountText.isEmpty()) {
            showError("Enter an amount of progress to add.");
            return;
        }

        try {
            int amount = Integer.parseInt(amountText);
            if (amount <= 0) {
                showError("Progress must be a positive whole number.");
                return;
            }

            selected.addProgress(amount);
            errorLabel.setVisible(false);
            progressField.clear();
            refreshProgressPanel(selected);
            goalList.refresh();

        } catch (NumberFormatException ex) {
            showError("Progress must be a valid whole number.");
        }
    }

    private void refreshProgressPanel(Goal goal) {
        boolean hasGoal = goal != null;
        logProgressButton.setDisable(!hasGoal);

        if (!hasGoal) {
            selectedLabel.setText("Select a goal to log progress.");
            progressBar.setProgress(0);
            progressLabel.setText("");
            return;
        }

        selectedLabel.setText(goal.getDescription()
                + (goal.isAchieved() ? "  -  achieved!" : ""));
        progressBar.setProgress(Math.min(1.0, goal.progressPercent() / 100.0));
        progressLabel.setText(String.format("%d / %d  (%.0f%%)",
                goal.getCurrentValue(), goal.getTargetValue(), goal.progressPercent()));
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
