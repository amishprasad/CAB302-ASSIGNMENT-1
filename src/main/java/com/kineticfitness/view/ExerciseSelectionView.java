package com.kineticfitness.view;

import com.kineticfitness.model.ExerciseCatalog;
import com.kineticfitness.model.ExerciseCatalog.ExerciseInfo;

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


public class ExerciseSelectionView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";

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

        renderExercises(ExerciseCatalog.ALL);

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
        for (String filter : ExerciseCatalog.filters()) {
            Button chip = new Button(filter);
            chip.setPadding(new Insets(6, 16, 6, 16));
            applyChipStyle(chip, ExerciseCatalog.ALL.equals(filter));
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

    private void renderExercises(String filter) {
        exerciseListContainer.getChildren().clear();
        for (ExerciseInfo exercise : ExerciseCatalog.filterByPart(filter)) {
            exerciseListContainer.getChildren().add(buildExerciseCard(exercise));
        }
    }

    private VBox buildExerciseCard(ExerciseInfo exercise) {
        Label nameLabel = new Label(exercise.name());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label bodyPartTag = new Label(exercise.bodyPart());
        bodyPartTag.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;"
                + " -fx-background-color: " + ORANGE + "; -fx-background-radius: 10; -fx-padding: 2 8 2 8;");

        Label descriptionLabel = new Label(exercise.description());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        Label musclesLabel = new Label("Muscles worked: " + exercise.muscles());
        musclesLabel.setWrapText(true);
        musclesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569; -fx-font-style: italic;");

        VBox card = new VBox(6, nameLabel, bodyPartTag, descriptionLabel, musclesLabel);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        return card;
    }
}