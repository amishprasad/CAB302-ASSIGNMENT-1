package com.kineticfitness.view;

import com.kineticfitness.db.ScheduleDAO;
import com.kineticfitness.model.ScheduledWorkout;
import com.kineticfitness.session.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Workout Schedule page: plan a workout via a form and see upcoming workouts, each of which
 * can be Completed, Skipped, or Rescheduled. Reads/writes through {@link ScheduleDAO} so the
 * schedule persists across sessions for the logged-in user.
 */
public class ScheduleView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;";

    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    private final TextField nameField = new TextField();
    private final TextField dateField = new TextField();
    private final TextField timeField = new TextField();
    private final TextField durationField = new TextField();
    private final Label errorLabel = new Label();
    private final VBox rows = new VBox(0);

    @Override
    public String label() {
        return "Schedule";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Workout Schedule");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label(
                "Plan your physical activities, set reminders, and log recurring workout routines.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        VBox content = new VBox(16, title, subtitle, buildForm(), buildUpcoming());
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");

        refreshRows();
        return content;
    }

    private VBox buildForm() {
        Label header = new Label("Schedule New Workout");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        nameField.setPromptText("Leg Hypertrophy Focus");
        dateField.setPromptText("04 Sep 2026");
        timeField.setPromptText("06:30 PM");
        durationField.setPromptText("75");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(6);
        grid.add(fieldLabel("Workout Name / Type"), 0, 0);
        grid.add(fieldLabel("Date"), 1, 0);
        grid.add(fieldLabel("Start Time"), 2, 0);
        grid.add(fieldLabel("Duration (mins)"), 3, 0);
        grid.add(nameField, 0, 1);
        grid.add(dateField, 1, 1);
        grid.add(timeField, 2, 1);
        grid.add(durationField, 3, 1);
        nameField.setPrefWidth(280);

        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button save = new Button("Save to Schedule");
        save.setOnAction(e -> handleSave());
        save.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 8;");

        VBox card = new VBox(12, header, grid, errorLabel, save);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private VBox buildUpcoming() {
        Label header = new Label("Upcoming Workouts");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        HBox columnHeader = new HBox(12,
                colLabel("WORKOUT NAME", true),
                colLabel("DATE", false),
                colLabel("TIME", false),
                colLabel("DURATION", false),
                colLabel("STATUS CONTROL", false));
        columnHeader.setPadding(new Insets(0, 0, 6, 0));

        VBox card = new VBox(8, header, columnHeader, rows);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private void handleSave() {
        if (!UserSession.isLoggedIn()) {
            showError("Create a profile first before scheduling a workout.");
            return;
        }

        String name = nameField.getText().trim();
        String date = dateField.getText().trim();
        String time = timeField.getText().trim();
        String duration = durationField.getText().trim();

        if (!isComplete(name, date, time, duration)) {
            showError("Please fill in the name, date, time and duration.");
            return;
        }

        errorLabel.setVisible(false);
        scheduleDAO.save(
                UserSession.getCurrentUser().getUsername(),
                new ScheduledWorkout(name, date, time,
                        duration.endsWith("mins") ? duration : duration + " mins"));

        nameField.clear();
        dateField.clear();
        timeField.clear();
        durationField.clear();
        refreshRows();
    }

    /** True only when every field has content — the rule for a valid schedule entry. */
    public static boolean isComplete(String... fields) {
        for (String f : fields) {
            if (f == null || f.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void refreshRows() {
        rows.getChildren().clear();

        if (!UserSession.isLoggedIn()) {
            rows.getChildren().add(hint("Create a profile to start scheduling workouts."));
            return;
        }

        List<ScheduledWorkout> upcoming =
                scheduleDAO.findForUser(UserSession.getCurrentUser().getUsername());

        if (upcoming.isEmpty()) {
            rows.getChildren().add(hint("No upcoming workouts scheduled."));
            return;
        }
        for (ScheduledWorkout item : upcoming) {
            rows.getChildren().add(buildRow(item));
        }
    }

    private HBox buildRow(ScheduledWorkout item) {
        Label name = cell(item.getName(), true);
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Button complete = smallButton("Complete", ORANGE, "white");
        complete.setOnAction(e -> { scheduleDAO.delete(item.getId()); refreshRows(); });
        Button skip = smallButton("Skip", "white", TITLE);
        skip.setOnAction(e -> { scheduleDAO.delete(item.getId()); refreshRows(); });
        Button reschedule = smallButton("Reschedule", "white", TITLE);
        reschedule.setOnAction(e -> {
            nameField.setText(item.getName());
            dateField.setText(item.getDate());
            timeField.setText(item.getTime());
            durationField.setText(item.getDuration().replace(" mins", ""));
            scheduleDAO.delete(item.getId());
            refreshRows();
        });

        HBox actions = new HBox(6, complete, skip, reschedule);
        actions.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(12, name, cell(item.getDate(), false), cell(item.getTime(), false),
                cell(item.getDuration(), false), actions);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setStyle("-fx-border-color: transparent transparent #E2E8F0 transparent;");
        return row;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    // ---- small UI helpers -------------------------------------------------

    private Label hint(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + SUBTITLE + "; -fx-padding: 12 0 0 0;");
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");
        return l;
    }

    private Label colLabel(String text, boolean grow) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + SUBTITLE + ";");
        sizeCell(l, grow);
        return l;
    }

    private Label cell(String text, boolean grow) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + TITLE + ";");
        sizeCell(l, grow);
        return l;
    }

    private void sizeCell(Label l, boolean grow) {
        if (grow) {
            l.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(l, Priority.ALWAYS);
        } else {
            l.setMinWidth(110);
            l.setPrefWidth(110);
        }
    }

    private Button smallButton(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                + " -fx-font-size: 12px; -fx-background-radius: 6; -fx-border-color: #E2E8F0;"
                + " -fx-border-radius: 6;");
        return b;
    }
}