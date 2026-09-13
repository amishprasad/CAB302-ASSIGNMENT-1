package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.util.Set;


public class GoalsView implements Page {

    private static final String ACCENT = "#f97316";
    private static final String ACCENT_BG = "rgba(249,115,22,0.08)";
    private static final String TITLE_COLOR = "#1E293B";
    private static final String MUTED_COLOR = "#64748B";
    private static final String PAGE_BG = "#F1F5F9";

    private static final String[] WORKOUT_TYPES = {"Strength training", "Cardio", "Walking", "Yoga & mobility"};
    private static final String[] WORKOUT_DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private final StackPane container = new StackPane();
    private final LocalProfileStore store = LocalProfileStore.getInstance();

    @Override
    public String label() {
        return "Goals";
    }

    @Override
    public Node getContent() {
        refresh();
        return container;
    }

    private void refresh() {
        if (store.hasGoals()) {
            container.getChildren().setAll(buildDetailsView());
        } else {
            container.getChildren().setAll(buildFormView());
        }
    }

    // ---------- Read-only details view ----------

    private Node buildDetailsView() {
        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        Label title = new Label("Goals");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label subtitle = new Label("Your fitness goals and workout preferences.");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: " + MUTED_COLOR + ";");
        VBox.setMargin(subtitle, new Insets(4, 0, 24, 0));

        VBox card = new VBox(14);
        card.setMaxWidth(760);
        card.setPadding(new Insets(28, 32, 28, 32));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(12);

        int r = 0;
        grid.add(mutedLabel("Primary goal:"), 0, r);
        grid.add(boldLabel(store.primaryGoal.display), 1, r++);

        grid.add(mutedLabel("Target weight:"), 0, r);
        grid.add(boldLabel(String.format("%.1f kg", store.targetWeightKg)), 1, r++);

        grid.add(mutedLabel("Weekly workout goal:"), 0, r);
        grid.add(boldLabel(store.weeklyWorkoutGoal + " workouts / week"), 1, r++);

        grid.add(mutedLabel("Weekly exercise duration:"), 0, r);
        grid.add(boldLabel(store.weeklyExerciseDurationMinutes + " minutes"), 1, r++);

        grid.add(mutedLabel("Experience level:"), 0, r);
        grid.add(boldLabel(formatEnum(store.experienceLevel.name())), 1, r++);

        grid.add(mutedLabel("Preferred workout types:"), 0, r);
        grid.add(boldLabel(String.join(", ", store.preferredWorkoutTypes)), 1, r++);

        grid.add(mutedLabel("Preferred workout days:"), 0, r);
        grid.add(boldLabel(String.join(", ", store.preferredWorkoutDays)), 1, r);

        Button editButton = new Button("Edit goals");
        editButton.setPrefHeight(38);
        editButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        editButton.setOnAction(e -> container.getChildren().setAll(buildFormView()));

        HBox buttonRow = new HBox(editButton);
        buttonRow.setPadding(new Insets(16, 0, 0, 0));

        card.getChildren().addAll(grid, buttonRow);
        page.getChildren().addAll(title, subtitle, card);
        return page;
    }

    private Label mutedLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + MUTED_COLOR + ";");
        return l;
    }

    private Label boldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        return l;
    }

    private String formatEnum(String name) {
        String s = name.toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        return l;
    }

    // ---------- Create / edit form (fallback if reached before the Profile wizard) ----------

    private Node buildFormView() {
        boolean isNew = !store.hasGoals();

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        Label title = new Label(isNew ? "Set your fitness goals" : "Edit your fitness goals");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label subtitle = new Label("Choose your goals and preferences to personalise your fitness plan.");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: " + MUTED_COLOR + ";");
        VBox.setMargin(subtitle, new Insets(4, 0, 24, 0));

        VBox card = new VBox(18);
        card.setMaxWidth(760);
        card.setPadding(new Insets(28, 32, 28, 32));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        ToggleGroup goalGroup = new ToggleGroup();
        HBox goalRow = new HBox(10);
        for (LocalProfileStore.PrimaryGoal goal : LocalProfileStore.PrimaryGoal.values()) {
            ToggleButton btn = new ToggleButton(goal.display);
            btn.setUserData(goal);
            btn.setToggleGroup(goalGroup);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(44);
            HBox.setHgrow(btn, Priority.ALWAYS);
            boolean selected = goal == store.primaryGoal;
            styleGoalCard(btn, selected);
            btn.selectedProperty().addListener((obs, was, isNow2) -> styleGoalCard(btn, isNow2));
            if (selected) btn.setSelected(true);
            goalRow.getChildren().add(btn);
        }
        VBox goalSection = new VBox(8, fieldLabel("Primary goal"), goalRow);

        TextField targetWeightField = new TextField(store.targetWeightKg > 0 ? String.valueOf(store.targetWeightKg) : "");
        targetWeightField.setPromptText("75");
        targetWeightField.setPrefHeight(38);

        ComboBox<Integer> weeklyWorkoutBox = new ComboBox<>();
        for (int i = 1; i <= 7; i++) weeklyWorkoutBox.getItems().add(i);
        weeklyWorkoutBox.setValue(store.weeklyWorkoutGoal);
        weeklyWorkoutBox.setMaxWidth(Double.MAX_VALUE);
        weeklyWorkoutBox.setPrefHeight(38);
        weeklyWorkoutBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer n) { return n == null ? "" : n + " workouts per week"; }
            @Override public Integer fromString(String s) { return weeklyWorkoutBox.getValue(); }
        });

        GridPane row1 = twoColumnGrid();
        row1.add(fieldLabel("Target weight (kg)"), 0, 0);
        row1.add(fieldLabel("Weekly workout goal"), 1, 0);
        row1.add(targetWeightField, 0, 1);
        row1.add(weeklyWorkoutBox, 1, 1);

        ComboBox<Integer> durationBox = new ComboBox<>();
        for (int mins : new int[]{60, 120, 180, 240, 300, 360}) durationBox.getItems().add(mins);
        durationBox.setValue(store.weeklyExerciseDurationMinutes);
        durationBox.setMaxWidth(Double.MAX_VALUE);
        durationBox.setPrefHeight(38);
        durationBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer n) { return n == null ? "" : n + " minutes"; }
            @Override public Integer fromString(String s) { return durationBox.getValue(); }
        });

        ComboBox<LocalProfileStore.FitnessLevel> experienceBox = new ComboBox<>();
        experienceBox.getItems().addAll(LocalProfileStore.FitnessLevel.values());
        experienceBox.setValue(store.experienceLevel);
        experienceBox.setMaxWidth(Double.MAX_VALUE);
        experienceBox.setPrefHeight(38);

        GridPane row2 = twoColumnGrid();
        row2.add(fieldLabel("Weekly exercise duration"), 0, 0);
        row2.add(fieldLabel("Experience level"), 1, 0);
        row2.add(durationBox, 0, 1);
        row2.add(experienceBox, 1, 1);

        VBox typesSection = new VBox(8, fieldLabel("Preferred workout types"),
                buildChipRow(WORKOUT_TYPES, store.preferredWorkoutTypes));
        VBox daysSection = new VBox(8, fieldLabel("Preferred workout days"),
                buildChipRow(WORKOUT_DAYS, store.preferredWorkoutDays));

        Button saveButton = new Button(isNew ? "Save goals" : "Save changes");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setPrefHeight(42);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        saveButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        saveButton.setOnAction(e -> {
            LocalProfileStore.PrimaryGoal selectedGoal = (LocalProfileStore.PrimaryGoal) (goalGroup.getSelectedToggle() != null
                    ? goalGroup.getSelectedToggle().getUserData() : null);
            String targetWeightText = targetWeightField.getText().trim();

            if (selectedGoal == null || targetWeightText.isEmpty()) {
                errorLabel.setText("Please choose a primary goal and enter a target weight.");
                errorLabel.setVisible(true);
                return;
            }
            if (store.preferredWorkoutTypes.isEmpty() || store.preferredWorkoutDays.isEmpty()) {
                errorLabel.setText("Pick at least one workout type and one workout day.");
                errorLabel.setVisible(true);
                return;
            }
            try {
                double newTargetWeight = Double.parseDouble(targetWeightText);
                if (newTargetWeight <= 0) {
                    errorLabel.setText("Target weight must be a positive number.");
                    errorLabel.setVisible(true);
                    return;
                }

                store.primaryGoal = selectedGoal;
                store.targetWeightKg = newTargetWeight;
                store.weeklyWorkoutGoal = weeklyWorkoutBox.getValue();
                store.weeklyExerciseDurationMinutes = durationBox.getValue();
                store.experienceLevel = experienceBox.getValue();

                container.getChildren().setAll(buildDetailsView());

            } catch (NumberFormatException ex) {
                errorLabel.setText("Target weight must be a valid number.");
                errorLabel.setVisible(true);
            }
        });

        HBox buttonRow = new HBox(saveButton);
        if (!isNew) {
            Button cancelButton = new Button("Cancel");
            cancelButton.setMaxWidth(Double.MAX_VALUE);
            cancelButton.setPrefHeight(42);
            HBox.setHgrow(cancelButton, Priority.ALWAYS);
            cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT
                    + "; -fx-border-color: " + ACCENT + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 14px;");
            cancelButton.setOnAction(e -> container.getChildren().setAll(buildDetailsView()));
            buttonRow.getChildren().add(cancelButton);
        }
        buttonRow.setSpacing(12);

        card.getChildren().addAll(goalSection, row1, row2, typesSection, daysSection, errorLabel, buttonRow);
        page.getChildren().addAll(title, subtitle, card);
        return page;
    }

    private GridPane twoColumnGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(6);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            grid.getColumnConstraints().add(cc);
        }
        return grid;
    }

    private void styleGoalCard(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: " + ACCENT_BG + "; -fx-text-fill: #c2410c; "
                    + "-fx-border-color: " + ACCENT + "; -fx-border-width: 2; -fx-border-radius: 6; "
                    + "-fx-background-radius: 6; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE_COLOR + "; "
                    + "-fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        }
    }

    private Node buildChipRow(String[] options, Set<String> selectedSet) {
        HBox row = new HBox(8);
        for (String option : options) {
            ToggleButton chip = new ToggleButton(option);
            boolean isSelected = selectedSet.contains(option);
            chip.setSelected(isSelected);
            chip.setPrefHeight(34);
            styleChip(chip, isSelected);
            chip.selectedProperty().addListener((obs, was, isNow) -> {
                styleChip(chip, isNow);
                if (isNow) selectedSet.add(option);
                else selectedSet.remove(option);
            });
            row.getChildren().add(chip);
        }
        return row;
    }

    private void styleChip(ToggleButton chip, boolean selected) {
        if (selected) {
            chip.setStyle("-fx-background-color: " + ACCENT_BG + "; -fx-text-fill: #c2410c; "
                    + "-fx-border-color: " + ACCENT + "; -fx-border-radius: 6; -fx-background-radius: 6;");
        } else {
            chip.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE_COLOR + "; "
                    + "-fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        }
    }
}