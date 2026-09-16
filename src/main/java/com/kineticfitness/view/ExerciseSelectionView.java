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
            // ---- Chest ----
            new ExerciseInfo("Bench Press", "Chest", "Barbell flat bench press targeting the chest.",
                    "Pectoralis major · Triceps · Anterior deltoids"),
            new ExerciseInfo("Incline Bench Press", "Chest", "Barbell press on an incline, emphasising upper chest.",
                    "Upper pectoralis · Anterior deltoids · Triceps"),
            new ExerciseInfo("Push-Up", "Chest", "Bodyweight chest exercise, no equipment needed.",
                    "Pectoralis major · Triceps · Core"),
            new ExerciseInfo("Dumbbell Fly", "Chest", "Wide arc with dumbbells to stretch and isolate the chest.",
                    "Pectoralis major · Anterior deltoids"),
            new ExerciseInfo("Cable Crossover", "Chest", "Cable exercise bringing both arms across the body.",
                    "Pectoralis major · Anterior deltoids"),
            new ExerciseInfo("Chest Dip", "Chest", "Bodyweight dip with a forward lean to load the lower chest.",
                    "Lower pectoralis · Triceps · Anterior deltoids"),

            // ---- Back ----
            new ExerciseInfo("Deadlift", "Back", "Full posterior chain barbell lift.",
                    "Erector spinae · Glutes · Hamstrings · Trapezius"),
            new ExerciseInfo("Lat Pulldown", "Back", "Cable exercise targeting the lats.",
                    "Latissimus dorsi · Biceps · Rhomboids"),
            new ExerciseInfo("Pull-Up", "Back", "Bodyweight vertical pull from a bar.",
                    "Latissimus dorsi · Biceps · Rhomboids"),
            new ExerciseInfo("Barbell Row", "Back", "Bent-over row building mid-back thickness.",
                    "Latissimus dorsi · Rhomboids · Trapezius · Biceps"),
            new ExerciseInfo("Seated Cable Row", "Back", "Horizontal cable pull for the mid-back.",
                    "Rhomboids · Latissimus dorsi · Biceps"),
            new ExerciseInfo("Face Pull", "Back", "Rope pull to the face for rear delts and upper back.",
                    "Rear deltoids · Rhomboids · Trapezius"),

            // ---- Legs ----
            new ExerciseInfo("Squat", "Legs", "Barbell squat for quads, glutes and hamstrings.",
                    "Quadriceps · Glutes · Hamstrings · Core"),
            new ExerciseInfo("Lunges", "Legs", "Bodyweight or weighted lunge, per leg.",
                    "Quadriceps · Glutes · Hamstrings"),
            new ExerciseInfo("Leg Press", "Legs", "Machine press for quads and glutes.",
                    "Quadriceps · Glutes · Hamstrings"),
            new ExerciseInfo("Romanian Deadlift", "Legs", "Hip hinge targeting hamstrings and glutes.",
                    "Hamstrings · Glutes · Erector spinae"),
            new ExerciseInfo("Leg Extension", "Legs", "Machine isolation for the quadriceps.",
                    "Quadriceps"),
            new ExerciseInfo("Calf Raise", "Legs", "Standing or seated raise for the calves.",
                    "Gastrocnemius · Soleus"),

            // ---- Arms ----
            new ExerciseInfo("Bicep Curl", "Arms", "Dumbbell curl targeting the biceps.",
                    "Biceps brachii · Brachialis"),
            new ExerciseInfo("Hammer Curl", "Arms", "Neutral-grip curl for biceps and forearms.",
                    "Brachialis · Brachioradialis · Biceps brachii"),
            new ExerciseInfo("Preacher Curl", "Arms", "Curl braced on a pad to isolate the biceps.",
                    "Biceps brachii · Brachialis"),
            new ExerciseInfo("Tricep Dip", "Arms", "Bodyweight tricep exercise.",
                    "Triceps brachii · Anterior deltoids · Pectoralis major"),
            new ExerciseInfo("Tricep Pushdown", "Arms", "Cable pushdown isolating the triceps.",
                    "Triceps brachii"),
            new ExerciseInfo("Skull Crusher", "Arms", "Lying barbell extension for the triceps.",
                    "Triceps brachii"),

            // ---- Shoulders ----
            new ExerciseInfo("Shoulder Press", "Shoulders", "Standing or seated overhead press.",
                    "Anterior deltoids · Lateral deltoids · Triceps"),
            new ExerciseInfo("Lateral Raise", "Shoulders", "Dumbbell side raise for the delts.",
                    "Lateral deltoids"),
            new ExerciseInfo("Front Raise", "Shoulders", "Dumbbell raise to the front for anterior delts.",
                    "Anterior deltoids"),
            new ExerciseInfo("Arnold Press", "Shoulders", "Overhead press with a rotating grip.",
                    "Anterior deltoids · Lateral deltoids · Triceps"),
            new ExerciseInfo("Upright Row", "Shoulders", "Vertical pull to chest height for delts and traps.",
                    "Lateral deltoids · Trapezius"),
            new ExerciseInfo("Rear Delt Fly", "Shoulders", "Bent-over fly targeting the rear deltoids.",
                    "Rear deltoids · Rhomboids"),

            // ---- Core ----
            new ExerciseInfo("Plank", "Core", "Isometric core hold.",
                    "Transverse abdominis · Rectus abdominis · Obliques"),
            new ExerciseInfo("Sit-Up", "Core", "Basic core exercise.",
                    "Rectus abdominis · Hip flexors"),
            new ExerciseInfo("Russian Twist", "Core", "Seated rotation for the obliques.",
                    "Obliques · Rectus abdominis"),
            new ExerciseInfo("Hanging Leg Raise", "Core", "Leg raise from a bar for the lower abs.",
                    "Lower rectus abdominis · Hip flexors"),
            new ExerciseInfo("Mountain Climber", "Core", "Dynamic plank driving knees to chest.",
                    "Rectus abdominis · Obliques · Hip flexors"),
            new ExerciseInfo("Cable Crunch", "Core", "Kneeling cable crunch for the upper abs.",
                    "Rectus abdominis · Obliques")
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

    /**
     * Pure filtering logic (unit-testable): "All" returns everything, otherwise matches body part.
     */
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

        Label musclesLabel = new Label("Muscles worked: " + exercise.getMuscles());
        musclesLabel.setWrapText(true);
        musclesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569; -fx-font-style: italic;");

        VBox card = new VBox(6, nameLabel, bodyPartTag, descriptionLabel, musclesLabel);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        return card;
    }

    /**
     * Display-only exercise info for browsing (name, body part, description).
     */
    public static class ExerciseInfo {
        private final String name;
        private final String bodyPart;
        private final String description;
        private final String muscles;

        public ExerciseInfo(String name, String bodyPart, String description, String muscles) {
            this.name = name;
            this.bodyPart = bodyPart;
            this.description = description;
            this.muscles = muscles;
        }

        public String getName() {
            return name;
        }

        public String getBodyPart() {
            return bodyPart;
        }

        public String getDescription() {
            return description;
        }

        public String getMuscles() {
            return muscles;
        }
    }
}