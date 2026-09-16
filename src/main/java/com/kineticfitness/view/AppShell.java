package com.kineticfitness.view;

import com.kineticfitness.model.ScheduledWorkout;
import com.kineticfitness.service.ReminderService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The application frame every screen plugs into. It owns the window and the sidebar, and
 * swaps only the centre content when a nav item is clicked, so pages never redraw the
 * sidebar themselves. Register pages with {@link #add(Page)} then call {@link #show()}.
 *
 * <p>How a teammate adds their screen: implement {@link Page} and add it in Main —
 * {@code new AppShell(stage).add(new MyView()) ... .show();}
 *
 * <p>It also hosts the reminder strip along the top, so a due reminder is visible
 * from any screen rather than only from the Schedule page. See
 * {@link #withReminders(Supplier)}.</p>
 */
public class AppShell {

    private static final String NAVY = "#0F172A";
    private static final String NAV_TEXT = "#CBD5E1";
    private static final String ORANGE = "#F97316";

    /** How often the reminder strip re-checks the schedule. */
    private static final int REMINDER_POLL_SECONDS = 30;

    private final Stage stage;
    private final BorderPane root = new BorderPane();
    private final List<Page> pages = new ArrayList<>();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final ReminderBanner reminderBanner = new ReminderBanner(() -> navigate("Schedule"));

    private Runnable onLogout = () -> {};
    private Supplier<List<ScheduledWorkout>> reminderSource;
    private Timeline reminderPoll;

    public AppShell(Stage stage) {
        this.stage = stage;
    }

    /** Register a page. Order controls sidebar order. Returns this for chaining. */
    public AppShell add(Page page) {
        pages.add(page);
        return this;
    }

    /**
     * Turns on the reminder strip, fed by {@code source} every
     * {@value #REMINDER_POLL_SECONDS} seconds.
     *
     * <p>The poll runs on the JavaFX application thread on purpose. The whole app
     * shares one SQLite {@code Connection}, and using a shared connection from a
     * background thread at the same time as a button handler is a data race. The
     * query is a single indexed read of one user's schedule, so it costs well
     * under a frame. Moving this onto a background {@code Task} would first need
     * a connection per thread, or a lock around every DAO call.</p>
     *
     * <p>US-23 &mdash; See due reminders.</p>
     *
     * @param source supplies the current user's schedule; return an empty list
     *               when nobody is signed in
     */
    public AppShell withReminders(Supplier<List<ScheduledWorkout>> source) {
        this.reminderSource = source;
        return this;
    }

    /**
     * What to do when the sidebar Logout button is clicked — e.g. clear the session and
     * take the user back to the login screen. Set by {@code Main} so this class doesn't
     * need to know about {@code UserSession} or {@code Login} itself. Returns this for chaining.
     */
    public AppShell onLogout(Runnable onLogout) {
        this.onLogout = onLogout;
        return this;
    }

    public void show() {
        show(null);   // null → defaults to the first page added
    }

    public void show(String startLabel) {
        root.setTop(reminderBanner.getNode());
        root.setLeft(buildSidebar());
        String target = (startLabel != null && navButtons.containsKey(startLabel))
                ? startLabel
                : (pages.isEmpty() ? null : pages.get(0).label());
        if (target != null) {
            navigate(target);
        }
        startReminderPolling();
        stage.setTitle("Kinetic Fitness");
        stage.setScene(new Scene(root, 1280, 800));
        stage.show();
    }

    // ---- Reminders (US-23) ------------------------------------------------

    private void startReminderPolling() {
        if (reminderSource == null) {
            return;
        }
        stopReminderPolling();
        reminderPoll = new Timeline(
                new KeyFrame(javafx.util.Duration.ZERO, e -> refreshReminders()),
                new KeyFrame(javafx.util.Duration.seconds(REMINDER_POLL_SECONDS)));
        reminderPoll.setCycleCount(Animation.INDEFINITE);
        reminderPoll.play();
    }

    private void stopReminderPolling() {
        if (reminderPoll != null) {
            reminderPoll.stop();
            reminderPoll = null;
        }
    }

    private void refreshReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            reminderBanner.show(ReminderService.due(reminderSource.get(), now), now);
        } catch (RuntimeException e) {
            // A reminder that cannot be loaded must never take the whole app down.
            System.err.println("Could not refresh reminders: " + e.getMessage());
        }
    }

    // ---- Chrome -----------------------------------------------------------

    private VBox buildSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setPadding(new Insets(20, 16, 20, 16));
        sidebar.setStyle("-fx-background-color: " + NAVY + ";");

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
        for (Page page : pages) {
            Button button = new Button(page.label());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setPadding(new Insets(10, 14, 10, 14));
            button.setOnAction(e -> navigate(page.label()));
            navButtons.put(page.label(), button);
            nav.getChildren().add(button);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setPadding(new Insets(10, 14, 10, 14));
        logout.setOnAction(e -> {
            stopReminderPolling();
            reminderBanner.clearDismissals();
            onLogout.run();
        });
        styleNav(logout, false);

        sidebar.getChildren().addAll(logo, nav, spacer, logout);
        return sidebar;
    }

    private void navigate(String label) {
        Node content = null;
        for (Page page : pages) {
            if (page.label().equals(label)) {
                content = safeContent(page);
                break;
            }
        }
        root.setCenter(content);
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            styleNav(entry.getValue(), entry.getKey().equals(label));
        }
        if (reminderSource != null) {
            refreshReminders();   // don't make the user wait up to 30s after navigating
        }
    }

    /**
     * Builds a page's content, turning a crash into a visible message instead of a
     * blank window. One screen failing must not take the whole shell down, and the
     * cause needs to reach the person looking at it.
     */
    private Node safeContent(Page page) {
        try {
            return page.getContent();
        } catch (RuntimeException e) {
            e.printStackTrace();
            Label heading = new Label(page.label() + " could not be loaded");
            heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #B91C1C;");
            Label detail = new Label(e.getClass().getSimpleName() + ": " + e.getMessage());
            detail.setStyle("-fx-font-size: 13px; -fx-text-fill: #7F1D1D;");
            detail.setWrapText(true);
            VBox box = new VBox(10, heading, detail);
            box.setPadding(new Insets(32, 40, 32, 40));
            box.setStyle("-fx-background-color: #FEF2F2;");
            return box;
        }
    }

    private void styleNav(Button button, boolean active) {
        if (active) {
            button.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                    + " -fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 13px;");
        } else {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + NAV_TEXT + ";"
                    + " -fx-background-radius: 8; -fx-font-size: 13px;");
        }
    }
}
