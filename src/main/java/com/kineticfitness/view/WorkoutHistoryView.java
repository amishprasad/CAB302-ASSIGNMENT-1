package com.kineticfitness.view;

import com.kineticfitness.db.WorkoutDAO;
import com.kineticfitness.model.BodyPart;
import com.kineticfitness.model.Exercise;
import com.kineticfitness.model.User;
import com.kineticfitness.model.Workout;
import com.kineticfitness.session.UserSession;
import com.kineticfitness.util.SessionNames;
import com.kineticfitness.util.WorkoutStats;
import com.kineticfitness.util.WorkoutStats.DatedVolume;
import com.kineticfitness.util.WorkoutStats.Sample;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
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
    private static final DateTimeFormatter SESSION_DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");

    /** How many of the most recent sessions the chart shows, so the bars stay readable. */
    private static final int CHART_SESSIONS = 8;
    private static final String[] FILTERS = {
            "All", "Chest", "Back", "Legs", "Arms", "Shoulders", "Core"
    };

    private final List<Session> sessions = new ArrayList<>();
    private String activeFilter = "All";
    private BarChart<String, Number> chart;
    private VBox sessionList;

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

        sessions.clear();
        sessions.addAll(loadSessions());
        activeFilter = "All";

        chart = buildChart();
        VBox chartCard = buildChartCard(chart);
        sessionList = new VBox(10);

        // One filter click updates both the chart and the session list, so they never disagree.
        HBox filterChips = buildFilterChips(filter -> {
            activeFilter = filter;
            refresh();
        });

        refresh();

        VBox content = new VBox(16, title, subtitle, filterChips, chartCard, sessionList);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + CONTENT_BG + "; -fx-background: " + CONTENT_BG + ";");
        return scroll;
    }

    private List<Session> loadSessions() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }

        List<Session> loaded = new ArrayList<>();
        List<Workout> workouts = new WorkoutDAO().findAllByUsername(user.getUsername());
        for (Workout workout : workouts) {
            String dateLabel = workout.getDate().format(DATE_FORMAT);
            List<Entry> entries = new ArrayList<>();
            for (Exercise exercise : workout.getExercises()) {
                entries.add(new Entry(
                        exercise.getId(),
                        dateLabel,
                        exercise.getName(),
                        String.valueOf(exercise.getSets()),
                        String.valueOf(exercise.getReps()),
                        bodyPartLabel(exercise.getBodyPart())));
            }
            if (!entries.isEmpty()) {
                loaded.add(new Session(workout.getId(), workout.getName(),
                        workout.getDate().format(SESSION_DATE_FORMAT), entries));
            }
        }
        return loaded;
    }

    private static String bodyPartLabel(BodyPart bodyPart) {
        if (bodyPart == null) {
            return "—";
        }
        String name = bodyPart.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private void refresh() {
        sessions.removeIf(s -> s.getEntries().isEmpty());

        List<Session> visible = new ArrayList<>();
        List<Entry> visibleEntries = new ArrayList<>();
        for (Session session : sessions) {
            List<Entry> matching = filterByPart(session.getEntries(), activeFilter);
            if (!matching.isEmpty()) {
                visible.add(session);
                visibleEntries.addAll(matching);
            }
        }

        plotEntries(chart, visibleEntries);
        renderSessions(visible);
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
    private HBox buildFilterChips(Consumer<String> onFilter) {
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
                onFilter.accept(filter);
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

    private void renderSessions(List<Session> visible) {
        sessionList.getChildren().clear();
        if (visible.isEmpty()) {
            Label empty = new Label("No workouts logged for this filter yet.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");
            sessionList.getChildren().add(empty);
            return;
        }
        for (Session session : visible) {
            sessionList.getChildren().add(buildSessionCard(session));
        }
    }

    private HBox buildSessionCard(Session session) {
        Label date = new Label(session.getDisplayTitle());
        date.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        int count = session.getEntries().size();
        String counts = count + (count == 1 ? " exercise" : " exercises")
                + "  ·  " + session.totalReps() + " total reps";
        Label summary = new Label(session.hasName() ? session.getDateTitle() + "  ·  " + counts : counts);
        summary.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        VBox text = new VBox(4, date, summary);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label view = new Label("View ›");
        view.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + ORANGE + ";");

        HBox card = new HBox(text, spacer, view);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(CARD + " -fx-cursor: hand;");
        card.setOnMouseClicked(e -> showSessionDialog(card, session));
        return card;
    }

    private void showSessionDialog(Node owner, Session session) {
        Stage stage = new Stage();
        stage.initOwner(owner.getScene().getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(session.getDisplayTitle());

        Map<Entry, String[]> original = new HashMap<>();
        for (Entry entry : session.getEntries()) {
            original.put(entry, new String[] { entry.getSets(), entry.getReps() });
        }
        String[] savedName = { session.getName() };

        TextField nameField = new TextField(session.getName() == null ? "" : session.getName());
        nameField.setPromptText(session.getDateTitle());
        nameField.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label dateLabel = new Label(session.getDateTitle());
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        Label hint = new Label("Double-click sets or reps to edit, then press Save Changes. "
                + "Select a row to delete it.");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #16a34a;");
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        Button saveButton = new Button("Save Changes");
        saveButton.setPadding(new Insets(8, 20, 8, 20));
        saveButton.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8;"
                + " -fx-cursor: hand;");
        saveButton.setDisable(true);

        BooleanSupplier hasChanges = () -> {
            if (!Objects.equals(SessionNames.normalize(nameField.getText()), savedName[0])) {
                return true;
            }
            for (Entry entry : session.getEntries()) {
                if (isEdited(entry, original.get(entry))) {
                    return true;
                }
            }
            return false;
        };

        Runnable updateSaveState = () -> {
            boolean dirty = hasChanges.getAsBoolean();
            saveButton.setDisable(!dirty);
            if (dirty) {
                statusLabel.setText("");
            }
        };
        nameField.textProperty().addListener((obs, oldValue, newValue) -> updateSaveState.run());

        Runnable onDeleted = () -> {
            refresh();
            if (session.getEntries().isEmpty()) {
                stage.close();
            } else {
                updateSaveState.run();
            }
        };

        TableView<Entry> table = buildTable(session.getEntries(), updateSaveState);
        Button deleteButton = buildDeleteButton(table, session.getEntries(), onDeleted);

        saveButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Save your changes to this session?", ButtonType.OK, ButtonType.CANCEL);
            confirm.initOwner(stage);
            confirm.setTitle("Save changes");
            confirm.setHeaderText("Confirm changes");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            for (Entry entry : session.getEntries()) {
                if (isEdited(entry, original.get(entry))) {
                    saveEdit(entry);
                    original.put(entry, new String[] { entry.getSets(), entry.getReps() });
                }
            }

            String newName = SessionNames.normalize(nameField.getText());
            if (!Objects.equals(newName, savedName[0])) {
                session.setName(newName);
                if (session.getWorkoutId() != null) {
                    new WorkoutDAO().updateWorkoutName(session.getWorkoutId(), newName);
                }
                savedName[0] = newName;
                stage.setTitle(session.getDisplayTitle());
            }
            nameField.setText(newName == null ? "" : newName);

            statusLabel.setText("Changes saved.");
            updateSaveState.run();
            refresh();
        });

        BooleanSupplier canClose = () -> {
            if (!hasChanges.getAsBoolean()) {
                return true;
            }
            Alert discard = new Alert(Alert.AlertType.CONFIRMATION,
                    "You have unsaved changes. Discard them?", ButtonType.OK, ButtonType.CANCEL);
            discard.initOwner(stage);
            discard.setTitle("Unsaved changes");
            discard.setHeaderText("Discard changes?");
            Optional<ButtonType> result = discard.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return false;
            }
            for (Entry entry : session.getEntries()) {
                String[] saved = original.get(entry);
                if (saved != null) {
                    entry.setSets(saved[0]);
                    entry.setReps(saved[1]);
                }
            }
            refresh();
            return true;
        };
        stage.setOnCloseRequest(ev -> {
            if (!canClose.getAsBoolean()) {
                ev.consume();
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setPadding(new Insets(8, 20, 8, 20));
        closeButton.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE + ";"
                + " -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;"
                + " -fx-font-size: 13px; -fx-cursor: hand;");
        closeButton.setOnAction(e -> {
            if (canClose.getAsBoolean()) {
                stage.close();
            }
        });

        HBox actionBar = new HBox(10, statusLabel, deleteButton, saveButton, closeButton);
        actionBar.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, nameField, dateLabel, hint, table, actionBar);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: " + CONTENT_BG + ";");

        stage.setScene(new Scene(root, 640, 590));
        stage.show();
    }

    private TableView<Entry> buildTable(List<Entry> sessionEntries, Runnable onEdited) {
        TableView<Entry> table = new TableView<>();
        table.setEditable(true);

        TableColumn<Entry, String> dateCol = new TableColumn<>("DATE");
        dateCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getDate()));
        dateCol.setStyle("-fx-text-fill: " + SUBTITLE + ";");
        dateCol.setPrefWidth(110);
        dateCol.setEditable(false);

        TableColumn<Entry, String> exCol = new TableColumn<>("EXERCISE");
        exCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getExercise()));
        exCol.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        exCol.setPrefWidth(360);
        exCol.setEditable(false);

        TableColumn<Entry, String> setsCol = new TableColumn<>("SETS");
        setsCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getSets()));
        setsCol.setStyle("-fx-alignment: CENTER;");
        setsCol.setPrefWidth(90);
        setsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        setsCol.setOnEditCommit(ev -> {
            String value = ev.getNewValue() == null ? "" : ev.getNewValue().trim();
            if (!isPositiveInt(value)) {
                table.refresh();
                return;
            }
            Entry entry = ev.getRowValue();
            entry.setSets(value);
            table.refresh();
            onEdited.run();
        });

        TableColumn<Entry, String> repsCol = new TableColumn<>("REPS");
        repsCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getReps()));
        repsCol.setStyle("-fx-alignment: CENTER;");
        repsCol.setPrefWidth(110);
        repsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        repsCol.setOnEditCommit(ev -> {
            String value = ev.getNewValue() == null ? "" : ev.getNewValue().trim();
            if (!isPositiveInt(value)) {
                table.refresh();
                return;
            }
            Entry entry = ev.getRowValue();
            entry.setReps(value);
            table.refresh();
            onEdited.run();
        });

        table.getColumns().addAll(dateCol, exCol, setsCol, repsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(FXCollections.observableArrayList(sessionEntries));
        table.setPrefHeight(340);
        table.setMaxHeight(360);
        table.setPlaceholder(new Label("No workouts logged for this filter yet."));
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;");

        table.setRowFactory(tv -> {
            TableRow<Entry> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (row.isEmpty()) {
                    table.getSelectionModel().clearSelection();
                }
            });
            return row;
        });
        table.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE && table.getEditingCell() == null) {
                table.getSelectionModel().clearSelection();
            }
        });
        return table;
    }

    private static boolean isPositiveInt(String text) {
        try {
            return Integer.parseInt(text) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isEdited(Entry entry, String[] original) {
        return original != null
                && (!original[0].equals(entry.getSets()) || !original[1].equals(entry.getReps()));
    }

    private void saveEdit(Entry entry) {
        if (entry.getExerciseId() == null) {
            return;
        }
        new WorkoutDAO().updateExercise(
                entry.getExerciseId(),
                Integer.parseInt(entry.getSets()),
                Integer.parseInt(entry.getReps()));
    }

    private Button buildDeleteButton(TableView<Entry> table, List<Entry> sessionEntries, Runnable onChanged) {
        Button button = new Button("Delete Selected");
        button.setPadding(new Insets(8, 20, 8, 20));
        button.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8;"
                + " -fx-cursor: hand;");

        button.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        button.setOnAction(e -> {
            Entry entry = table.getSelectionModel().getSelectedItem();
            if (entry == null) {
                return;
            }
            if (entry.getExerciseId() != null) {
                new WorkoutDAO().deleteExercise(entry.getExerciseId());
            }
            table.getItems().remove(entry);
            sessionEntries.remove(entry);
            table.getSelectionModel().clearSelection();
            onChanged.run();
        });
        return button;
    }

    public static class Session {
        private final Integer workoutId;
        private String name;
        private final String dateTitle;
        private final List<Entry> entries;

        public Session(Integer workoutId, String name, String dateTitle, List<Entry> entries) {
            this.workoutId = workoutId;
            this.name = name;
            this.dateTitle = dateTitle;
            this.entries = entries;
        }

        public Integer getWorkoutId() { return workoutId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDateTitle() { return dateTitle; }
        public boolean hasName() { return name != null && !name.isBlank(); }
        public String getDisplayTitle() { return hasName() ? name : dateTitle; }
        public List<Entry> getEntries() { return entries; }

        public int totalReps() {
            return entries.stream().mapToInt(Entry::totalReps).sum();
        }
    }

    public static class Entry {
        private final Integer exerciseId;
        private final String date;
        private final String exercise;
        private String sets;
        private String reps;
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

        public void setSets(String sets) { this.sets = sets; }
        public void setReps(String reps) { this.reps = reps; }


        public int totalReps() {
            try {
                return Integer.parseInt(sets.trim()) * Integer.parseInt(reps.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}