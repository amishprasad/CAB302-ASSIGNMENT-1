package com.kineticfitness.view;

import com.kineticfitness.db.WorkoutDAO;
import com.kineticfitness.model.BodyPart;
import com.kineticfitness.model.Exercise;
import com.kineticfitness.model.User;
import com.kineticfitness.model.Workout;
import com.kineticfitness.session.UserSession;
import com.kineticfitness.util.WorkoutStats;
import com.kineticfitness.util.WorkoutStats.DatedVolume;
import com.kineticfitness.util.WorkoutStats.Sample;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WorkoutHistoryView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM");

    /** How many of the most recent sessions the chart shows, so the bars stay readable. */
    private static final int CHART_SESSIONS = 8;
    private static final String[] FILTERS = {
            "All", "Chest", "Back", "Legs", "Arms", "Shoulders", "Core"
    };

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

        List<Entry> allEntries = loadEntries();

        BarChart<String, Number> chart = buildChart();
        VBox chartCard = buildChartCard(chart);

        // Deleting a row changes the totals, so the chart is redrawn from what's left.
        TableView<Entry> table = buildTable(allEntries, remaining -> plotEntries(chart, remaining));

        // One filter click updates both the chart and the table, so they never disagree.
        HBox filterChips = buildFilterChips(filtered -> {
            table.setItems(FXCollections.observableArrayList(filtered));
            plotEntries(chart, filtered);
        }, allEntries);

        plotEntries(chart, allEntries);

        VBox content = new VBox(16, title, subtitle, filterChips, chartCard, table);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + CONTENT_BG + "; -fx-background: " + CONTENT_BG + ";");
        return scroll;
    }

    private List<Entry> loadEntries() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }

        List<Entry> entries = new ArrayList<>();
        List<Workout> workouts = new WorkoutDAO().findAllByUsername(user.getUsername());
        for (Workout workout : workouts) {
            String dateLabel = workout.getDate().format(DATE_FORMAT);
            for (Exercise exercise : workout.getExercises()) {
                entries.add(new Entry(
                        exercise.getId(),
                        dateLabel,
                        exercise.getName(),
                        String.valueOf(exercise.getSets()),
                        String.valueOf(exercise.getReps()),
                        bodyPartLabel(exercise.getBodyPart())));
            }
        }
        return entries;
    }

    private static String bodyPartLabel(BodyPart bodyPart) {
        if (bodyPart == null) {
            return "—";
        }
        String name = bodyPart.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
    // ---------- Chart ----------

    private BarChart<String, Number> buildChart() {
        return VolumeChart.create(260, "Total reps");
    }

    private VBox buildChartCard(BarChart<String, Number> chart) {
        Label header = new Label("Training Volume");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label helper = new Label("Total reps per session (sets × reps). Follows the filter above.");
        helper.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        VBox card = new VBox(10, header, helper, chart);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    /** Redraws the chart from the entries currently on screen. */
    private void plotEntries(BarChart<String, Number> chart, List<Entry> entries) {
        VolumeChart.plot(chart, volumeOf(entries));
    }

    /**
     * Groups the given entries into one bar per session. The grouping itself lives in
     * {@link WorkoutStats} so the Dashboard chart and this one can't drift apart.
     */
    static List<DatedVolume> volumeOf(List<Entry> entries) {
        List<Sample> samples = new ArrayList<>();
        for (Entry entry : entries) {
            samples.add(new Sample(entry.getDate(), entry.totalReps()));
        }
        return WorkoutStats.volumeByDate(samples, CHART_SESSIONS);
    }


    // ---------- Content ----------
    private HBox buildFilterChips(Consumer<List<Entry>> onFilter, List<Entry> allEntries) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        List<Button> chipButtons = new ArrayList<>();

        for (String filter : FILTERS) {
            Button chip = new Button(filter);
            chip.setPadding(new Insets(6, 16, 6, 16));
            applyChipStyle(chip, filter.equals("All"));
            chip.setOnAction(e -> {
                for (Button b : chipButtons) {
                    applyChipStyle(b, b.getText().equals(filter));
                }
                onFilter.accept(filterByPart(allEntries, filter));
            });
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

    public static List<Entry> filterByPart(List<Entry> entries, String part) {
        if ("All".equals(part)) {
            return new ArrayList<>(entries);
        }
        return entries.stream()
                .filter(e -> e.getBodyPart().equals(part))
                .collect(Collectors.toList());
    }
    private TableView<Entry> buildTable(List<Entry> allEntries, Consumer<List<Entry>> onDataChanged) {
        TableView<Entry> table = new TableView<>();

        TableColumn<Entry, String> dateCol = new TableColumn<>("DATE");
        dateCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getDate()));
        dateCol.setStyle("-fx-text-fill: " + SUBTITLE + ";");
        dateCol.setPrefWidth(110);

        TableColumn<Entry, String> exCol = new TableColumn<>("EXERCISE");
        exCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getExercise()));
        exCol.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        exCol.setPrefWidth(360);

        TableColumn<Entry, String> setsCol = new TableColumn<>("SETS");
        setsCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getSets()));
        setsCol.setStyle("-fx-alignment: CENTER;");
        setsCol.setPrefWidth(90);

        TableColumn<Entry, String> repsCol = new TableColumn<>("REPS");
        repsCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getReps()));
        repsCol.setStyle("-fx-alignment: CENTER;");
        repsCol.setPrefWidth(110);

        TableColumn<Entry, Void> deleteCol = new TableColumn<>("");
        deleteCol.setPrefWidth(100);
        deleteCol.setCellFactory(deleteButtonCellFactory(table, allEntries, onDataChanged));

        table.getColumns().addAll(dateCol, exCol, setsCol, repsCol, deleteCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(FXCollections.observableArrayList(allEntries));
        table.setPrefHeight(340);
        table.setMaxHeight(360);
        table.setPlaceholder(new Label("No workouts logged for this filter yet."));
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        return table;
    }

    private Callback<TableColumn<Entry, Void>, TableCell<Entry, Void>> deleteButtonCellFactory(
            TableView<Entry> table, List<Entry> allEntries, Consumer<List<Entry>> onDataChanged) {
        return column -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626;"
                        + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
                deleteButton.setOnAction(e -> {
                    Entry entry = getTableView().getItems().get(getIndex());
                    if (entry.getExerciseId() != null) {
                        new WorkoutDAO().deleteExercise(entry.getExerciseId());
                    }
                    table.getItems().remove(entry);
                    allEntries.remove(entry);
                    onDataChanged.accept(new ArrayList<>(table.getItems()));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        };
    }

    public static class Entry {
        private final Integer exerciseId;
        private final String date;
        private final String exercise;
        private final String sets;
        private final String reps;
        private final String bodyPart;

        public Entry(Integer exerciseId, String date, String exercise, String sets, String reps, String bodyPart) {
            this.exerciseId = exerciseId;
            this.date = date;
            this.exercise = exercise;
            this.sets = sets;
            this.reps = reps;
            this.bodyPart = bodyPart;
        }

        public Integer getExerciseId() { return exerciseId; }
        public String getDate() { return date; }
        public String getExercise() { return exercise; }
        public String getSets() { return sets; }
        public String getReps() { return reps; }
        public String getBodyPart() { return bodyPart; }


        public int totalReps() {
            try {
                return Integer.parseInt(sets.trim()) * Integer.parseInt(reps.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}