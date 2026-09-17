package com.kineticfitness.view;

import com.kineticfitness.db.ScheduleDAO;
import com.kineticfitness.model.ScheduleStatus;
import com.kineticfitness.model.ScheduledWorkout;
import com.kineticfitness.service.ReminderService;
import com.kineticfitness.service.ScheduleConflictDetector;
import com.kineticfitness.service.ScheduleValidator;
import com.kineticfitness.service.ValidationResult;
import com.kineticfitness.session.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Workout Schedule: plan a session, choose whether to be reminded, and mark each
 * one completed, skipped or moved.
 *
 * <p>Every rule about what the form accepts lives in {@link ScheduleValidator},
 * and everything about when a reminder is due lives in {@link ReminderService},
 * so this class only wires controls to those decisions. That separation is what
 * makes both of them unit testable without starting JavaFX.</p>
 *
 * <p>The name, time and duration inputs are <em>editable</em> combo boxes: the
 * dropdown covers the common cases in one click, and anything unusual can still
 * be typed. Because the typed text is still handed to {@link ScheduleValidator},
 * the parsing and validation rules &mdash; and their tests &mdash; are unchanged.</p>
 *
 * <p>US-10 &mdash; Schedule a workout. US-22 &mdash; Set an activity reminder.
 * US-32 &mdash; Manage scheduled workout.</p>
 */
public class ScheduleView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;";

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("EEE d MMM");
    private static final DateTimeFormatter TIME_LABEL = DateTimeFormatter.ofPattern("h:mm a");

    /** Suggested workout names. The field stays editable, so anything else can be typed. */
    private static final String[] WORKOUT_TYPES = {
            "Push Day", "Pull Day", "Leg Day", "Upper Body", "Lower Body",
            "Full Body", "Cardio", "HIIT", "Yoga & Mobility", "Walk"
    };

    /** Suggested durations in minutes. */
    private static final String[] DURATION_CHOICES = {"15", "20", "30", "45", "60", "75", "90", "105", "120"};

    /** Reminder lead times offered in the dropdown, in minutes. 0 means no reminder. */
    private static final Integer[] REMINDER_CHOICES = {0, 5, 10, 15, 30, 60, 120};

    /** Start times from 05:00 to 22:00 in quarter-hour steps. */
    private static final List<String> TIME_SLOTS = buildTimeSlots();

    private final ScheduleDAO scheduleDAO;

    private final ComboBox<String> nameBox = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker();
    private final ComboBox<String> timeBox = new ComboBox<>();
    private final ComboBox<String> durationBox = new ComboBox<>();
    private final ComboBox<Integer> reminderBox = new ComboBox<>();
    private final Label errorLabel = new Label();
    private final VBox rows = new VBox(0);

    private Filter filter = Filter.UPCOMING;

    /** Which workouts the list is showing. */
    private enum Filter {
        UPCOMING("Upcoming"), COMPLETED("Completed"), SKIPPED("Skipped"), ALL("All");

        private final String label;

        Filter(String label) {
            this.label = label;
        }
    }

    public ScheduleView() {
        this(new ScheduleDAO());
    }

    /** Lets a test supply its own data source instead of the live database. */
    public ScheduleView(ScheduleDAO scheduleDAO) {
        this.scheduleDAO = scheduleDAO;
    }

    private static List<String> buildTimeSlots() {
        List<String> slots = new ArrayList<>();
        for (LocalTime time = LocalTime.of(5, 0);
             !time.isAfter(LocalTime.of(22, 0));
             time = time.plusMinutes(15)) {
            slots.add(time.format(TIME_LABEL));
            if (time.equals(LocalTime.of(22, 0))) {
                break;   // plusMinutes would wrap past midnight
            }
        }
        return List.copyOf(slots);
    }

    @Override
    public String label() {
        return "Schedule";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Workout Schedule");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label(
                "Plan your sessions, set reminders, and keep a record of what you actually did.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        VBox content = new VBox(16, title, subtitle, buildForm(), buildList());
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");

        refreshRows();
        return content;
    }

    // ---- Form (US-10, US-22) ----------------------------------------------

    private VBox buildForm() {
        Label header = new Label("Schedule New Workout");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        configureEditable(nameBox, "Leg Hypertrophy Focus", 260);
        nameBox.getItems().setAll(WORKOUT_TYPES);

        datePicker.setPromptText("Pick a date");
        datePicker.setPrefWidth(160);
        datePicker.setValue(LocalDate.now());

        configureEditable(timeBox, "18:30 or 6:30 PM", 150);
        timeBox.getItems().setAll(TIME_SLOTS);
        timeBox.setVisibleRowCount(10);

        configureEditable(durationBox, "75", 130);
        durationBox.getItems().setAll(DURATION_CHOICES);

        reminderBox.getItems().setAll(REMINDER_CHOICES);
        reminderBox.setValue(15);
        reminderBox.setPrefWidth(160);
        reminderBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer minutes) {
                if (minutes == null || minutes == ScheduledWorkout.NO_REMINDER) {
                    return "No reminder";
                }
                return ReminderService.humanise(minutes) + " before";
            }

            @Override
            public Integer fromString(String text) {
                return reminderBox.getValue();
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(6);
        grid.add(fieldLabel("Workout Name / Type"), 0, 0);
        grid.add(fieldLabel("Date"), 1, 0);
        grid.add(fieldLabel("Start Time"), 2, 0);
        grid.add(fieldLabel("Duration (mins)"), 3, 0);
        grid.add(fieldLabel("Remind me"), 4, 0);
        grid.add(nameBox, 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(timeBox, 2, 1);
        grid.add(durationBox, 3, 1);
        grid.add(reminderBox, 4, 1);

        errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button save = new Button("Save to Schedule");
        save.setOnAction(e -> handleSave());
        save.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 8;");

        VBox card = new VBox(12, header, grid, errorLabel, save);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    /** A dropdown of common choices that still accepts anything typed into it. */
    private void configureEditable(ComboBox<String> box, String promptText, double width) {
        box.setEditable(true);
        box.setPromptText(promptText);
        box.setPrefWidth(width);
    }

    /**
     * What the user can currently see in an editable combo box.
     *
     * <p>Read from the editor rather than {@code getValue()}: an editable combo
     * box only commits its value on Enter or focus loss, so a user who types and
     * clicks Save straight away would otherwise have their input ignored.</p>
     */
    private static String textOf(ComboBox<String> box) {
        String typed = box.getEditor().getText();
        return typed == null ? "" : typed;
    }

    private void handleSave() {
        if (!UserSession.isLoggedIn()) {
            showError("Log in before scheduling a workout.");
            return;
        }

        String name = textOf(nameBox);
        String timeText = textOf(timeBox);
        String durationText = textOf(durationBox);

        ValidationResult result = ScheduleValidator.validate(
                name, datePicker.getValue(), timeText, durationText, LocalDate.now());

        if (result.isInvalid()) {
            showError(result.message());
            return;
        }

        LocalTime startTime = ScheduleValidator.parseTime(timeText).orElseThrow();
        int minutes = ScheduleValidator.parseDurationMinutes(durationText).getAsInt();
        int reminderLead = reminderBox.getValue() == null ? ScheduledWorkout.NO_REMINDER : reminderBox.getValue();

        ScheduledWorkout candidate =
                new ScheduledWorkout(name, datePicker.getValue(), startTime, minutes, reminderLead);

        // An overlap is a warning, not a rule - a user may genuinely want two
        // sessions in the same slot, so they get the final say.
        if (ScheduleConflictDetector.clashes(candidate, scheduleDAO.findForUser(currentUsername()))
                && !confirmClash()) {
            return;
        }

        scheduleDAO.save(currentUsername(), candidate);

        clearForm();
        refreshRows();
    }

    /** Asks whether to go ahead with a workout that overlaps one already booked. */
    private boolean confirmClash() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "This overlaps a workout already on your schedule.\n\nSchedule it anyway?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Scheduling clash");
        alert.setHeaderText(null);
        return alert.showAndWait().filter(button -> button == ButtonType.YES).isPresent();
    }

    private void clearForm() {
        clearEditable(nameBox);
        clearEditable(timeBox);
        clearEditable(durationBox);
        datePicker.setValue(LocalDate.now());
        reminderBox.setValue(15);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void clearEditable(ComboBox<String> box) {
        box.setValue(null);
        box.getEditor().clear();
    }

    // ---- List and actions (US-32) -----------------------------------------

    private VBox buildList() {
        Label header = new Label("Your Schedule");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        HBox filters = new HBox(8);
        ToggleGroup group = new ToggleGroup();
        for (Filter option : Filter.values()) {
            ToggleButton button = new ToggleButton(option.label);
            button.setToggleGroup(group);
            button.setSelected(option == filter);
            styleFilter(button, option == filter);
            button.setOnAction(e -> {
                filter = option;
                for (javafx.scene.control.Toggle toggle : group.getToggles()) {
                    styleFilter((ToggleButton) toggle, toggle == button);
                }
                refreshRows();
            });
            filters.getChildren().add(button);
        }

        HBox columnHeader = new HBox(12,
                colLabel("WORKOUT", true),
                colLabel("WHEN", false),
                colLabel("DURATION", false),
                colLabel("REMINDER", false),
                colLabel("STATUS", false),
                colLabel("ACTIONS", false));
        columnHeader.setPadding(new Insets(6, 0, 6, 0));

        VBox card = new VBox(10, header, filters, columnHeader, rows);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private void refreshRows() {
        rows.getChildren().clear();

        if (!UserSession.isLoggedIn()) {
            rows.getChildren().add(hint("Log in to start scheduling workouts."));
            return;
        }

        List<ScheduledWorkout> all = scheduleDAO.findForUser(currentUsername());
        List<ScheduledWorkout> shown = all.stream().filter(this::matchesFilter).toList();

        if (shown.isEmpty()) {
            rows.getChildren().add(hint(emptyMessage()));
            return;
        }
        for (ScheduledWorkout workout : shown) {
            rows.getChildren().add(buildRow(workout));
        }
    }

    private boolean matchesFilter(ScheduledWorkout workout) {
        return switch (filter) {
            case UPCOMING -> workout.getStatus() == ScheduleStatus.SCHEDULED;
            case COMPLETED -> workout.getStatus() == ScheduleStatus.COMPLETED;
            case SKIPPED -> workout.getStatus() == ScheduleStatus.SKIPPED;
            case ALL -> true;
        };
    }

    private String emptyMessage() {
        return switch (filter) {
            case UPCOMING -> "Nothing scheduled yet. Add a workout above.";
            case COMPLETED -> "No completed workouts yet.";
            case SKIPPED -> "No skipped workouts.";
            case ALL -> "Your schedule is empty.";
        };
    }

    private HBox buildRow(ScheduledWorkout workout) {
        Label name = cell(workout.getName(), true);
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        String when = workout.getDate().format(DATE_LABEL) + ", " + workout.getStartTime().format(TIME_LABEL);
        String reminder = workout.hasReminder()
                ? ReminderService.humanise(workout.getReminderLeadMinutes()) + " before"
                : "Off";

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setMinWidth(230);

        if (workout.getStatus() == ScheduleStatus.SCHEDULED) {
            actions.getChildren().addAll(
                    action("Complete", ORANGE, "white", () -> setStatus(workout, ScheduleStatus.COMPLETED)),
                    action("Skip", "white", TITLE, () -> setStatus(workout, ScheduleStatus.SKIPPED)),
                    action("Move", "white", TITLE, () -> reschedule(workout)));
        } else {
            actions.getChildren().addAll(
                    action("Undo", "white", TITLE, () -> setStatus(workout, ScheduleStatus.SCHEDULED)),
                    action("Delete", "white", "#DC2626", () -> {
                        scheduleDAO.delete(workout.getId());
                        refreshRows();
                    }));
        }

        HBox row = new HBox(12, name, cell(when, false), cell(workout.durationLabel(), false),
                cell(reminder, false), statusChip(workout.getStatus()), actions);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setStyle("-fx-border-color: transparent transparent #E2E8F0 transparent;");
        return row;
    }

    /**
     * Moves a workout to a new status, letting the model reject the change if it
     * is not a legal transition rather than trusting the button that was clicked.
     */
    private void setStatus(ScheduledWorkout workout, ScheduleStatus target) {
        try {
            ScheduledWorkout updated = workout.withStatus(target);
            scheduleDAO.updateStatus(updated.getId(), updated.getStatus());
            refreshRows();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Asks for a new date and time and moves the workout, keeping its history
     * rather than deleting and re-creating it.
     */
    private void reschedule(ScheduledWorkout workout) {
        Optional<LocalDateTime> chosen = askForNewSlot(workout);
        if (chosen.isEmpty()) {
            return;
        }
        scheduleDAO.reschedule(workout.getId(), chosen.get().toLocalDate(), chosen.get().toLocalTime());
        refreshRows();
    }

    private Optional<LocalDateTime> askForNewSlot(ScheduledWorkout workout) {
        DatePicker newDate = new DatePicker(workout.getDate());
        newDate.setPrefWidth(180);

        ComboBox<String> newTime = new ComboBox<>();
        configureEditable(newTime, "18:30 or 6:30 PM", 180);
        newTime.getItems().setAll(TIME_SLOTS);
        newTime.setVisibleRowCount(10);
        newTime.getEditor().setText(workout.getStartTime().format(TIME_LABEL));

        Label dialogError = new Label();
        dialogError.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.add(fieldLabel("New date"), 0, 0);
        grid.add(newDate, 1, 0);
        grid.add(fieldLabel("New start time"), 0, 1);
        grid.add(newTime, 1, 1);
        grid.add(dialogError, 0, 2, 2, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Move workout");
        dialog.setHeaderText("Move \"" + workout.getName() + "\" to a new slot");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Keep the dialog open when the input is not usable, instead of silently
        // discarding what the user typed.
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            ValidationResult result = ScheduleValidator.validate(
                    workout.getName(), newDate.getValue(), textOf(newTime),
                    String.valueOf(workout.getDurationMinutes()), LocalDate.now());
            if (result.isInvalid()) {
                dialogError.setText(result.message());
                event.consume();
            }
        });

        if (dialog.showAndWait().filter(button -> button == ButtonType.OK).isEmpty()) {
            return Optional.empty();
        }
        return ScheduleValidator.parseTime(textOf(newTime))
                .map(time -> LocalDateTime.of(newDate.getValue(), time));
    }

    // ---- small helpers ----------------------------------------------------

    private String currentUsername() {
        return UserSession.getCurrentUser().getUsername();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private Button action(String text, String background, String foreground, Runnable onClick) {
        Button button = new Button(text);
        button.setOnAction(e -> onClick.run());
        button.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + foreground + ";"
                + " -fx-font-size: 12px; -fx-background-radius: 6; -fx-border-color: #E2E8F0;"
                + " -fx-border-radius: 6;");
        return button;
    }

    private Label statusChip(ScheduleStatus status) {
        String background;
        String foreground;
        switch (status) {
            case COMPLETED -> {
                background = "#DCFCE7";
                foreground = "#16A34A";
            }
            case SKIPPED -> {
                background = "#FEE2E2";
                foreground = "#DC2626";
            }
            default -> {
                background = "#E0F2FE";
                foreground = "#0369A1";
            }
        }
        Label chip = new Label(status.toString());
        chip.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + foreground + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10;"
                + " -fx-background-radius: 10;");
        chip.setMinWidth(90);
        chip.setAlignment(Pos.CENTER);
        return chip;
    }

    private void styleFilter(ToggleButton button, boolean active) {
        if (active) {
            button.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                    + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 14;"
                    + " -fx-padding: 5 14;");
        } else {
            button.setStyle("-fx-background-color: white; -fx-text-fill: " + SUBTITLE + ";"
                    + " -fx-font-size: 12px; -fx-background-radius: 14; -fx-padding: 5 14;"
                    + " -fx-border-color: #E2E8F0; -fx-border-radius: 14;");
        }
    }

    private Label hint(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + SUBTITLE + "; -fx-padding: 12 0 0 0;");
        return label;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");
        return label;
    }

    private Label colLabel(String text, boolean grow) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + SUBTITLE + ";");
        sizeCell(label, grow);
        return label;
    }

    private Label cell(String text, boolean grow) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TITLE + ";");
        sizeCell(label, grow);
        return label;
    }

    private void sizeCell(Label label, boolean grow) {
        if (grow) {
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);
        } else {
            label.setMinWidth(130);
            label.setPrefWidth(130);
        }
    }
}
