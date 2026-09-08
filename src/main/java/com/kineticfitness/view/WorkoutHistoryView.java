package com.kineticfitness.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Workout History screen: a dark sidebar of navigation, plus a main panel that
 * lists past training logs in a table and lets the user filter them by muscle group.
 * Built to match the Kinetic Fitness Figma design (1280x800 desktop layout).
 */
public class WorkoutHistoryView {

    // Palette (from the design)
    private static final String NAVY = "#0F172A";
    private static final String NAV_TEXT = "#CBD5E1";
    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";

    private static final String[] NAV_ITEMS = {
            "Dashboard", "Profile", "Log Workout", "Workout History",
            "Schedule", "Goals", "Progress", "Settings"
    };
    private static final String ACTIVE_NAV = "Workout History";

    private static final String[] FILTERS = {
            "All", "Chest", "Back", "Legs", "Arms", "Shoulders", "Core"
    };

    private final Stage stage;
    private final Runnable onBack;

    private final List<Entry> allEntries = List.of(
            new Entry("30 Aug", "Bench Press", "3", "10", "60 kg", "Chest"),
            new Entry("29 Aug", "Squat", "4", "8", "80 kg", "Legs"),
            new Entry("27 Aug", "Lat Pulldown", "3", "12", "50 kg", "Back"),
            new Entry("25 Aug", "Shoulder Press", "3", "10", "40 kg", "Shoulders"),
            new Entry("23 Aug", "Lunges", "3", "15 (per leg)", "20 kg", "Legs"));

    private final TableView<Entry> table = new TableView<>();
    private final List<Button> chipButtons = new ArrayList<>();

    public WorkoutHistoryView(Stage stage) {
        this(stage, null);
    }

    public WorkoutHistoryView(Stage stage, Runnable onBack) {
        this.stage = stage;
        this.onBack = onBack;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(buildContent());

        Scene scene = new Scene(root, 1280, 800);
        stage.setTitle("Kinetic Fitness - Workout History");
        stage.setScene(scene);
        stage.show();
    }

    // ---- Sidebar ----------------------------------------------------------

    private VBox buildSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setPadding(new Insets(20, 16, 20, 16));
        sidebar.setStyle("-fx-background-color: " + NAVY + ";");

        // Logo row
        Region mark = new Region();
        mark.setMinSize(26, 26);
        mark.setPrefSize(26, 26);
        mark.setMaxSize(26, 26);
        mark.setStyle("-fx-background-color: " + ORANGE + "; -fx-background-radius: 7;");

        Label brand = new Label("Kinetic Fitness");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox logo = new HBox(10, mark, brand);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setPadding(new Insets(4, 0, 20, 4));

        VBox nav = new VBox(4);
        for (String item : NAV_ITEMS) {
            nav.getChildren().add(buildNavButton(item, item.equals(ACTIVE_NAV)));
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = buildNavButton("Logout", false);
        if (onBack != null) {
            logout.setOnAction(e -> onBack.run());
        }

        sidebar.getChildren().addAll(logo, nav, spacer, logout);
        return sidebar;
    }

    private Button buildNavButton(String text, boolean active) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(10, 14, 10, 14));
        if (active) {
            b.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                    + " -fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 13px;");
        } else {
            b.setStyle("-fx-background-color: transparent; -fx-text-fill: " + NAV_TEXT + ";"
                    + " -fx-background-radius: 8; -fx-font-size: 13px;");
        }
        return b;
    }

    // ---- Main content -----------------------------------------------------

    private VBox buildContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");

        Label title = new Label("Workout History");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label(
                "View and filter past training logs to monitor progression over cycles.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        HBox filters = buildFilterChips();
        buildTable();

        content.getChildren().addAll(title, subtitle, filters, table);
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

    // ---- Row model --------------------------------------------------------

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
