package com.kineticfitness.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Workout History page: lists past training logs in a table and filters them by muscle group.
 * Implements {@link Page} so it plugs into the {@link AppShell} — the shell provides the
 * sidebar, this class provides only the centre content.
 */
public class WorkoutHistoryView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";

    private static final String[] FILTERS = {
            "All", "Chest", "Back", "Legs", "Arms", "Shoulders", "Core"
    };

    private final List<Entry> allEntries = List.of(
            new Entry("30 Aug", "Bench Press", "3", "10", "60 kg", "Chest"),
            new Entry("29 Aug", "Squat", "4", "8", "80 kg", "Legs"),
            new Entry("27 Aug", "Lat Pulldown", "3", "12", "50 kg", "Back"),
            new Entry("25 Aug", "Shoulder Press", "3", "10", "40 kg", "Shoulders"),
            new Entry("23 Aug", "Lunges", "3", "15 (per leg)", "20 kg", "Legs"));

    private final TableView<Entry> table = new TableView<>();
    private final List<Button> chipButtons = new ArrayList<>();

    @Override
    public String label() {
        return "Workout History";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Workout History");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label(
                "View and filter past training logs to monitor progression over cycles.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        buildTable();

        VBox content = new VBox(16, title, subtitle, buildFilterChips(), table);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        return content;
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
        table.setItems(FXCollections.observableArrayList(filterByPart(allEntries, filter)));
    }

    /** Pure filtering logic (unit-testable): "All" returns everything, otherwise matches body part. */
    public static List<Entry> filterByPart(List<Entry> entries, String part) {
        if ("All".equals(part)) {
            return new ArrayList<>(entries);
        }
        return entries.stream()
                .filter(e -> e.getBodyPart().equals(part))
                .collect(Collectors.toList());
    }

    private void buildTable() {
        TableColumn<Entry, String> dateCol = new TableColumn<>("DATE");
        dateCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getDate()));
        dateCol.setStyle("-fx-text-fill: " + SUBTITLE + ";");
        dateCol.setPrefWidth(120);

        TableColumn<Entry, String> exCol = new TableColumn<>("EXERCISE");
        exCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getExercise()));
        exCol.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        exCol.setPrefWidth(420);

        TableColumn<Entry, String> setsCol = new TableColumn<>("SETS");
        setsCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getSets()));
        setsCol.setStyle("-fx-alignment: CENTER;");
        setsCol.setPrefWidth(90);

        TableColumn<Entry, String> repsCol = new TableColumn<>("REPS");
        repsCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getReps()));
        repsCol.setStyle("-fx-alignment: CENTER;");
        repsCol.setPrefWidth(120);

        TableColumn<Entry, String> weightCol = new TableColumn<>("WEIGHT");
        weightCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getWeight()));
        weightCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: " + ORANGE
                + "; -fx-font-weight: bold;");
        weightCol.setPrefWidth(120);

        table.getColumns().addAll(dateCol, exCol, setsCol, repsCol, weightCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(FXCollections.observableArrayList(allEntries));
        table.setPrefHeight(340);
        table.setMaxHeight(360);
        table.setPlaceholder(new Label("No workouts logged for this filter yet."));
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
    }

    /** One row of the workout-history table. */
    public static class Entry {
        private final String date;
        private final String exercise;
        private final String sets;
        private final String reps;
        private final String weight;
        private final String bodyPart;

        public Entry(String date, String exercise, String sets, String reps,
                     String weight, String bodyPart) {
            this.date = date;
            this.exercise = exercise;
            this.sets = sets;
            this.reps = reps;
            this.weight = weight;
            this.bodyPart = bodyPart;
        }

        public String getDate() { return date; }
        public String getExercise() { return exercise; }
        public String getSets() { return sets; }
        public String getReps() { return reps; }
        public String getWeight() { return weight; }
        public String getBodyPart() { return bodyPart; }
    }
}
