package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exercise Selection page: browse exercises grouped/filterable by body part,
 * each shown as an info card (name, body part, description).
 *
 * Mirrors WorkoutHistoryView's style (same colours, same filter-chip pattern)
 * so the app looks consistent. Uses its own static ExerciseInfo list, since
 * the real Exercise model (name/sets/reps) doesn't carry body part or
 * description data — that's only needed here for browsing, not for logging.
 */
public class ExerciseSelectionView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";

    private static final String[] FILTERS = {
            "All", "Chest", "Back", "Legs", "Arms", "Shoulders", "Core"
    };

    private final List<ExerciseInfo> allExercises = List.of(
            new ExerciseInfo("Bench Press", "Chest", "Barbell flat bench press targeting the chest."),
            new ExerciseInfo("Push-Up", "Chest", "Bodyweight chest exercise, no equipment needed."),
            new ExerciseInfo("Deadlift", "Back", "Full posterior chain barbell lift."),
            new ExerciseInfo("Lat Pulldown", "Back", "Cable exercise targeting the lats."),
            new ExerciseInfo("Squat", "Legs", "Barbell squat for quads, glutes and hamstrings."),
            new ExerciseInfo("Lunges", "Legs", "Bodyweight or weighted lunge, per leg."),
            new ExerciseInfo("Bicep Curl", "Arms", "Dumbbell curl targeting the biceps."),
            new ExerciseInfo("Tricep Dip", "Arms", "Bodyweight tricep exercise."),
            new ExerciseInfo("Shoulder Press", "Shoulders", "Standing or seated overhead press."),
            new ExerciseInfo("Lateral Raise", "Shoulders", "Dumbbell side raise for the delts."),
            new ExerciseInfo("Plank", "Core", "Isometric core hold."),
            new ExerciseInfo("Sit-Up", "Core", "Basic core exercise.")
    );

    private final VBox exerciseListContainer = new VBox(10);
    private final List<Button> chipButtons = new ArrayList<>();

    @Override
    public String label() {
        return "Exercise Library";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Exercise Library");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label("Browse exercises by body part before logging a workout.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        renderExercises("All");

        VBox content = new VBox(16, title, subtitle, buildFilterChips(), exerciseListContainer);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: " + CONTENT_BG + "; -fx-background: " + CONTENT_BG + ";");
        return scrollPane;
    }

    private HBox buildFilterChips() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        for (String filter : FILTERS) {
            Button chip = new Button(filter);
            chip.setPadding(new Insets(6, 16, 6, 16));
            applyChipStyle(chip, filter.equals("All"));
            chip.setOnAction(e -> selectFilter(filter));
            chipButtons.add(chip);
            row.getChildren().add(chip);
        }
        return row;
    }

    private void applyChipStyle(Button chip, boolean active) {
        if (active) {
            chip.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                    + " -fx-font-weight: bold; -fx-background-radius: 16; -fx-font-size: 13px;");
        } else {
            chip.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE + ";"
                    + " -fx-border-color: #E2E8F0; -fx-border-radius: 16; -fx-background-radius: 16;"
                    + " -fx-font-size: 13px;");
        }
    }

    private void selectFilter(String filter) {
        for (Button chip : chipButtons) {
            applyChipStyle(chip, chip.getText().equals(filter));
        }
        renderExercises(filter);
    }

    /** Pure filtering logic (unit-testable): "All" returns everything, otherwise matches body part. */
    public static List<ExerciseInfo> filterByPart(List<ExerciseInfo> exercises, String part) {
        if ("All".equals(part)) {
            return new ArrayList<>(exercises);
        }
        return exercises.stream()
                .filter(ex -> ex.getBodyPart().equals(part))
                .collect(Collectors.toList());
    }

    private void renderExercises(String filter) {
        exerciseListContainer.getChildren().clear();
        for (ExerciseInfo exercise : filterByPart(allExercises, filter)) {
            exerciseListContainer.getChildren().add(buildExerciseCard(exercise));
        }
    }

    private VBox buildExerciseCard(ExerciseInfo exercise) {
        Label nameLabel = new Label(exercise.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label bodyPartTag = new Label(exercise.getBodyPart());
        bodyPartTag.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;"
                + " -fx-background-color: " + ORANGE + "; -fx-background-radius: 10; -fx-padding: 2 8 2 8;");

        Label descriptionLabel = new Label(exercise.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        VBox card = new VBox(6, nameLabel, bodyPartTag, descriptionLabel);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        return card;
    }

    /** Display-only exercise info for browsing (name, body part, description). */
    public static class ExerciseInfo {
        private final String name;
        private final String bodyPart;
        private final String description;

        public ExerciseInfo(String name, String bodyPart, String description) {
            this.name = name;
            this.bodyPart = bodyPart;
            this.description = description;
        }

        public String getName() { return name; }
        public String getBodyPart() { return bodyPart; }
        public String getDescription() { return description; }
    }
}