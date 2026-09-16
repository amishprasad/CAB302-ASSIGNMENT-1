package com.kineticfitness.view;

import com.kineticfitness.model.ScheduledWorkout;
import com.kineticfitness.service.ReminderService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The strip across the top of the app that shows workouts whose reminder is due.
 *
 * <p>Sits above the sidebar so it is visible from every screen, not just the
 * schedule. Dismissing a reminder hides it for the rest of the session &mdash;
 * the poller runs every 30 seconds, so without that it would reappear
 * immediately.</p>
 *
 * <p>US-23 &mdash; See due reminders.</p>
 */
public class ReminderBanner {

    private static final String AMBER_BG = "#FEF3C7";
    private static final String AMBER_BORDER = "#FCD34D";
    private static final String AMBER_TEXT = "#78350F";

    private final VBox container = new VBox(0);
    private final Set<Integer> dismissed = new HashSet<>();
    private final Runnable onViewSchedule;

    /**
     * @param onViewSchedule what to run when the user clicks "View schedule";
     *                       may be null if there is nowhere to go
     */
    public ReminderBanner(Runnable onViewSchedule) {
        this.onViewSchedule = onViewSchedule;
        container.setManaged(false);
        container.setVisible(false);
    }

    /** The node to place at the top of the window. */
    public javafx.scene.Node getNode() {
        return container;
    }

    /**
     * Replaces what the banner is showing.
     *
     * <p>Must be called on the JavaFX application thread.</p>
     *
     * @param dueWorkouts the reminders currently due, from {@link ReminderService#due}
     * @param now         the instant to word the messages against
     */
    public void show(List<ScheduledWorkout> dueWorkouts, LocalDateTime now) {
        container.getChildren().clear();

        List<ScheduledWorkout> visible = dueWorkouts.stream()
                .filter(workout -> !dismissed.contains(workout.getId()))
                .toList();

        if (visible.isEmpty()) {
            container.setVisible(false);
            container.setManaged(false);
            return;
        }

        for (ScheduledWorkout workout : visible) {
            container.getChildren().add(buildRow(workout, now));
        }
        container.setVisible(true);
        container.setManaged(true);
    }

    /** Lets a reminder appear again, e.g. after the user reschedules that workout. */
    public void clearDismissals() {
        dismissed.clear();
    }

    private HBox buildRow(ScheduledWorkout workout, LocalDateTime now) {
        Label bell = new Label("⏰");
        bell.setStyle("-fx-font-size: 15px;");

        Label message = new Label(ReminderService.describe(workout, now));
        message.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + AMBER_TEXT + ";");

        Label detail = new Label("(" + workout.durationLabel() + ")");
        detail.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AMBER_TEXT + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, bell, message, detail, spacer);

        if (onViewSchedule != null) {
            Button view = new Button("View schedule");
            view.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AMBER_TEXT + ";"
                    + " -fx-font-size: 12px; -fx-underline: true;");
            view.setOnAction(e -> onViewSchedule.run());
            row.getChildren().add(view);
        }

        Button dismiss = new Button("✕");
        dismiss.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AMBER_TEXT + "; -fx-font-size: 12px;");
        dismiss.setOnAction(e -> {
            dismissed.add(workout.getId());
            container.getChildren().remove(row);
            if (container.getChildren().isEmpty()) {
                container.setVisible(false);
                container.setManaged(false);
            }
        });
        row.getChildren().add(dismiss);

        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: " + AMBER_BG + ";"
                + " -fx-border-color: transparent transparent " + AMBER_BORDER + " transparent;"
                + " -fx-border-width: 0 0 1 0;");
        return row;
    }
}
