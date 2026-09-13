package com.kineticfitness.view;

import com.kineticfitness.db.GoalsDAO;
import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.Goal;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;


public class GoalsView implements Page {

    private static final String CURRENT_USERNAME = "guest";

    private static final String ACCENT = "#f97316";
    private static final String ACCENT_BG = "#fff3e8";
    private static final String SELECTED_DARK_BG = "#0f172a";
    private static final String TITLE_COLOR = "#1E293B";
    private static final String MUTED_COLOR = "#64748B";
    private static final String PAGE_BG = "#F1F5F9";
    private static final String DANGER = "#dc2626";
    private static final String DANGER_BG = "#fff1f0";

    private static final String[] WORKOUT_TYPES = {"Strength training", "Cardio", "Walking", "Yoga or mobility"};
    private static final String[] WORKOUT_DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private final StackPane container = new StackPane();
    private final GoalsDAO goalsDAO = new GoalsDAO();
    private final com.kineticfitness.db.UserDAO userDAO = new com.kineticfitness.db.UserDAO();

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
        if (userDAO.findByUsername(CURRENT_USERNAME) == null) {
            container.getChildren().setAll(buildNoProfilePrompt());
            return;
        }
        Goal existingGoal = goalsDAO.findForUser(CURRENT_USERNAME);
        container.getChildren().setAll(buildFormView(existingGoal));
    }

    private Node buildNoProfilePrompt() {
        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");
        Label title = new Label("Goals");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Label prompt = new Label("Create your profile first, then come back here to set a goal.");
        prompt.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MUTED_COLOR + ";");
        page.getChildren().addAll(title, prompt);
        return page;
    }

    // ---------- Form (create or edit the single current goal) ----------

    private Node buildFormView(Goal existingGoal) {
        boolean isNew = existingGoal == null;

        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        HBox headerRow = new HBox();
        VBox titleBlock = new VBox(2);
        Label title = new Label("Set a New Goal");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Label subtitle = new Label(isNew
                ? "Tell us what you want to achieve. We'll create a plan to help you get there."
                : "Remove your current goal and set a new one to continue your fitness journey.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MUTED_COLOR + ";");
        titleBlock.getChildren().addAll(title, subtitle);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        headerRow.getChildren().addAll(titleBlock, headerSpacer);

        if (!isNew) {
            headerRow.getChildren().add(buildDeleteGoalPanel());
        }
        headerRow.setAlignment(Pos.TOP_LEFT);
        VBox.setMargin(headerRow, new Insets(0, 0, 20, 0));

        VBox card = new VBox(22);
        card.setMaxWidth(1000);
        card.setPadding(new Insets(26, 30, 26, 30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + DANGER + "; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        // Goal type cards
        ToggleGroup goalGroup = new ToggleGroup();
        HBox goalRow = new HBox(12);
        for (Goal.GoalType type : Goal.GoalType.values()) {
            ToggleButton btn = new ToggleButton(type.display);
            btn.setUserData(type);
            btn.setToggleGroup(goalGroup);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(64);
            HBox.setHgrow(btn, Priority.ALWAYS);
            boolean selected = !isNew && type == existingGoal.getGoalType();
            styleGoalTypeCard(btn, selected);
            btn.selectedProperty().addListener((obs, was, isNow2) -> styleGoalTypeCard(btn, isNow2));
            if (selected) btn.setSelected(true);
            goalRow.getChildren().add(btn);
        }
        VBox goalTypeBlock = sectionBlock("Goal Type", "What is your primary goal?", goalRow);

        // Target weight + weekly exercise duration
        TextField targetWeightField = new TextField(isNew ? "" : String.valueOf(existingGoal.getTargetWeightKg()));
        targetWeightField.setPromptText("e.g. 70");
        targetWeightField.setPrefHeight(38);
        VBox targetWeightBlock = sectionBlock("Target Weight", "Your goal weight (kg)", targetWeightField);

        ComboBox<Integer> durationBox = new ComboBox<>();
        for (int mins : new int[]{60, 90, 120, 150, 180, 240, 300}) durationBox.getItems().add(mins);
        durationBox.setValue(isNew ? 150 : existingGoal.getWeeklyExerciseDurationMinutes());
        durationBox.setMaxWidth(Double.MAX_VALUE);
        durationBox.setPrefHeight(38);
        durationBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer n) { return n == null ? "" : n + " minutes"; }
            @Override public Integer fromString(String s) { return durationBox.getValue(); }
        });
        VBox durationBlock = sectionBlock("Weekly Exercise Duration", "Total time per week", durationBox);

        HBox row1 = new HBox(24, targetWeightBlock, durationBlock);
        HBox.setHgrow(targetWeightBlock, Priority.ALWAYS);
        HBox.setHgrow(durationBlock, Priority.ALWAYS);

        // Weekly workout goal
        ComboBox<Integer> weeklyWorkoutBox = new ComboBox<>();
        for (int i = 1; i <= 7; i++) weeklyWorkoutBox.getItems().add(i);
        weeklyWorkoutBox.setValue(isNew ? 3 : existingGoal.getWeeklyWorkoutGoal());
        weeklyWorkoutBox.setMaxWidth(Double.MAX_VALUE);
        weeklyWorkoutBox.setPrefHeight(38);
        weeklyWorkoutBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer n) { return n == null ? "" : n + " workouts per week"; }
            @Override public Integer fromString(String s) { return weeklyWorkoutBox.getValue(); }
        });
        VBox weeklyWorkoutBlock = sectionBlock("Weekly Workout Goal", "How many workouts per week?", weeklyWorkoutBox);
        weeklyWorkoutBlock.setMaxWidth(480);

        // Preferred workout types (checkboxes)
        HBox typesRow = new HBox(10);
        for (String option : WORKOUT_TYPES) {
            CheckBox cb = new CheckBox(option);
            cb.setSelected(!isNew && existingGoal.getPreferredWorkoutTypes().contains(option));
            styleCheckBox(cb);
            typesRow.getChildren().add(cb);
        }
        VBox typesBlock = sectionBlock("Preferred Workout Types", "Select all that apply", typesRow);

        // Preferred workout days (checkboxes)
        HBox daysRow = new HBox(10);
        for (String day : WORKOUT_DAYS) {
            CheckBox cb = new CheckBox(day);
            cb.setSelected(!isNew && existingGoal.getPreferredWorkoutDays().contains(day));
            styleCheckBox(cb);
            daysRow.getChildren().add(cb);
        }
        VBox daysBlock = sectionBlock("Preferred Workout Days", "Select your preferred days", daysRow);

        // Experience level
        ToggleGroup experienceGroup = new ToggleGroup();
        HBox experienceRow = new HBox(12);
        for (FitnessLevel level : FitnessLevel.values()) {
            ToggleButton btn = new ToggleButton(formatEnum(level.name()));
            btn.setUserData(level);
            btn.setToggleGroup(experienceGroup);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(44);
            HBox.setHgrow(btn, Priority.ALWAYS);
            boolean selected = !isNew && level == existingGoal.getExperienceLevel();
            styleExperienceCard(btn, selected);
            btn.selectedProperty().addListener((obs, was, isNow2) -> styleExperienceCard(btn, isNow2));
            if (selected) btn.setSelected(true);
            experienceRow.getChildren().add(btn);
        }
        VBox experienceBlock = sectionBlock("Experience Level", "Your current fitness level", experienceRow);

        Separator divider = new Separator();
        VBox.setMargin(divider, new Insets(6, 0, 0, 0));

        Button cancelButton = new Button("\u2190 Cancel");
        cancelButton.setPrefHeight(40);
        cancelButton.setPrefWidth(120);
        cancelButton.setStyle("-fx-background-color: " + PAGE_BG + "; -fx-text-fill: " + TITLE_COLOR + ";");
        cancelButton.setOnAction(e -> refresh());

        Button saveButton = new Button("Save Goal");
        saveButton.setPrefHeight(40);
        saveButton.setPrefWidth(140);
        saveButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> {
            Goal.GoalType type = (Goal.GoalType) (goalGroup.getSelectedToggle() != null
                    ? goalGroup.getSelectedToggle().getUserData() : null);
            String targetWeightText = targetWeightField.getText().trim();
            FitnessLevel experience = (FitnessLevel) (experienceGroup.getSelectedToggle() != null
                    ? experienceGroup.getSelectedToggle().getUserData() : null);

            if (type == null || targetWeightText.isEmpty() || experience == null) {
                errorLabel.setText("Please choose a goal type, target weight, and experience level.");
                errorLabel.setVisible(true);
                return;
            }

            boolean anyTypeChecked = typesRow.getChildren().stream()
                    .anyMatch(n -> n instanceof CheckBox cb && cb.isSelected());
            boolean anyDayChecked = daysRow.getChildren().stream()
                    .anyMatch(n -> n instanceof CheckBox cb && cb.isSelected());
            if (!anyTypeChecked || !anyDayChecked) {
                errorLabel.setText("Pick at least one workout type and one workout day.");
                errorLabel.setVisible(true);
                return;
            }

            try {
                double targetWeight = Double.parseDouble(targetWeightText);
                if (targetWeight <= 0) {
                    errorLabel.setText("Target weight must be a positive number.");
                    errorLabel.setVisible(true);
                    return;
                }

                Goal goal = new Goal(type, targetWeight, weeklyWorkoutBox.getValue(),
                        durationBox.getValue(), experience);
                for (Node n : typesRow.getChildren()) {
                    if (n instanceof CheckBox cb && cb.isSelected()) goal.getPreferredWorkoutTypes().add(cb.getText());
                }
                for (Node n : daysRow.getChildren()) {
                    if (n instanceof CheckBox cb && cb.isSelected()) goal.getPreferredWorkoutDays().add(cb.getText());
                }

                goalsDAO.save(CURRENT_USERNAME, goal);
                refresh();

            } catch (NumberFormatException ex) {
                errorLabel.setText("Target weight must be a valid number.");
                errorLabel.setVisible(true);
            }
        });

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        HBox buttonRow = new HBox(12, cancelButton, buttonSpacer, saveButton);

        card.getChildren().addAll(goalTypeBlock, row1, weeklyWorkoutBlock, typesBlock, daysBlock,
                experienceBlock, errorLabel, divider, buttonRow);

        page.getChildren().addAll(headerRow, card);
        return page;
    }

    private VBox buildDeleteGoalPanel() {
        VBox panel = new VBox(2);
        panel.setPadding(new Insets(12, 16, 12, 16));
        panel.setMaxWidth(300);
        panel.setStyle("-fx-background-color: " + DANGER_BG + "; -fx-border-color: " + DANGER
                + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        Button deleteButton = new Button("\uD83D\uDDD1 Delete Current Goal");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + DANGER + "; -fx-font-weight: bold; -fx-padding: 0;");
        deleteButton.setOnAction(e -> {
            goalsDAO.delete(CURRENT_USERNAME);
            refresh();
        });

        Label note = new Label("Your existing goal will be removed.");
        note.setStyle("-fx-font-size: 11px; -fx-text-fill: " + DANGER + ";");

        panel.getChildren().addAll(deleteButton, note);
        return panel;
    }

    private VBox sectionBlock(String title, String helpText, Node content) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Label helpLabel = new Label(helpText);
        helpLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        VBox box = new VBox(2, titleLabel, helpLabel, content);
        VBox.setMargin(content, new Insets(8, 0, 0, 0));
        return box;
    }

    private String formatEnum(String name) {
        String s = name.toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void styleGoalTypeCard(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: " + SELECTED_DARK_BG + "; -fx-text-fill: white; "
                    + "-fx-border-color: " + ACCENT + "; -fx-border-width: 2; -fx-border-radius: 6; "
                    + "-fx-background-radius: 6; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE_COLOR + "; "
                    + "-fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        }
    }

    private void styleExperienceCard(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: " + ACCENT_BG + "; -fx-text-fill: " + ACCENT + "; "
                    + "-fx-border-color: " + ACCENT + "; -fx-border-width: 2; -fx-border-radius: 6; "
                    + "-fx-background-radius: 6; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE_COLOR + "; "
                    + "-fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        }
    }

    private void styleCheckBox(CheckBox cb) {
        cb.setStyle(checkBoxStyle(cb.isSelected()));
        cb.selectedProperty().addListener((o, was, isNow) -> cb.setStyle(checkBoxStyle(isNow)));
    }

    private String checkBoxStyle(boolean selected) {
        if (selected) {
            return "-fx-padding: 8 14; -fx-border-color: " + ACCENT + "; -fx-border-radius: 6; "
                    + "-fx-background-color: " + ACCENT_BG + "; -fx-background-radius: 6; -fx-text-fill: " + ACCENT + ";";
        }
        return "-fx-padding: 8 14; -fx-border-color: #e2e8f0; -fx-border-radius: 6; "
                + "-fx-background-color: white; -fx-background-radius: 6;";
    }
}