package com.kineticfitness.view;

import com.kineticfitness.db.WorkoutDAO;
import com.kineticfitness.model.User;
import com.kineticfitness.model.Workout;
import com.kineticfitness.session.UserSession;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Progress & Analytics page: summarises real training volume, current body metrics, and
 * goal completion for the signed-in user. Data comes from {@link WorkoutDAO} (workouts
 * table) and the shared {@link LocalProfileStore} milestones list (the same store the
 * Goals page reads and writes, so both pages always agree). Implements {@link Page} so
 * it plugs into the {@link AppShell} — the shell provides the sidebar, this class
 * provides only the centre content.
 */
public class ProgressAnalyticsView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String BORDER = "#E2E8F0";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: " + BORDER + "; -fx-border-radius: 10;";
    private static final int WEEKS_SHOWN = 8;

    private final WorkoutDAO workoutDAO = new WorkoutDAO();

    @Override
    public String label() {
        return "Progress";
    }

    @Override
    public Node getContent() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return emptyState("Sign in to see your progress.");
        }

        List<Workout> workouts = workoutDAO.findAllByUsername(currentUser.getUsername());

        Label title = new Label("Progress & Analytics");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label("Track training volume, body metrics, and goal progress over time.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        VBox content = new VBox(16, title, subtitle,
                buildStatRow(workouts), buildVolumeChartCard(workouts),
                buildBodyMetricsCard(), buildGoalsCard());
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + CONTENT_BG + "; -fx-background: " + CONTENT_BG + ";");
        return scroll;
    }

    private Node emptyState(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: " + SUBTITLE + ";");
        VBox box = new VBox(label);
        box.setPadding(new Insets(32, 40, 32, 40));
        box.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        return box;
    }

    // ---- Stats --------------------------------------------------------

    private HBox buildStatRow(List<Workout> workouts) {
        int totalWorkouts = workouts.size();
        int totalReps = workouts.stream().mapToInt(Workout::totalReps).sum();
        List<LocalProfileStore.Milestone> milestones = LocalProfileStore.getInstance().milestones;
        long goalsAchieved = milestones.stream().filter(LocalProfileStore.Milestone::isAchieved).count();
        int streak = currentStreak(workouts);

        HBox row = new HBox(16,
                statCard("Total Workouts", String.valueOf(totalWorkouts)),
                statCard("Total Reps", String.valueOf(totalReps)),
                statCard("Goals Achieved", goalsAchieved + " / " + milestones.size()),
                statCard("Current Streak", streak + (streak == 1 ? " day" : " days")));
        for (Node node : row.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        return row;
    }

    /** Consecutive days with at least one workout, counting back from the most recent workout day. */
    private int currentStreak(List<Workout> workouts) {
        Set<LocalDate> days = new TreeSet<>();
        for (Workout w : workouts) {
            days.add(w.getDate());
        }
        if (days.isEmpty()) {
            return 0;
        }
        List<LocalDate> descending = new ArrayList<>(days);
        java.util.Collections.reverse(descending);

        int streak = 1;
        LocalDate cursor = descending.get(0);
        for (int i = 1; i < descending.size(); i++) {
            if (descending.get(i).equals(cursor.minusDays(1))) {
                streak++;
                cursor = descending.get(i);
            } else {
                break;
            }
        }
        return streak;
    }

    private VBox statCard(String label, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + ORANGE + ";");

        Label captionLabel = new Label(label);
        captionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        VBox card = new VBox(6, valueLabel, captionLabel);
        card.setPadding(new Insets(16));
        card.setStyle(CARD);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // ---- Weekly volume chart -------------------------------------------

    private VBox buildVolumeChartCard(List<Workout> workouts) {
        Label header = new Label("Weekly Training Volume");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label helper = new Label("Total reps logged per week, across all exercises.");
        helper.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total reps");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(240);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (WeekBucket bucket : weeklyBuckets(workouts)) {
            series.getData().add(new XYChart.Data<>(bucket.label, bucket.totalReps));
        }
        chart.getData().add(series);

        VBox card = new VBox(10, header, helper, chart);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private record WeekBucket(String label, int totalReps) {}

    /** Buckets real workout reps into the last {@value WEEKS_SHOWN} calendar weeks (Mon–Sun), oldest first. */
    private List<WeekBucket> weeklyBuckets(List<Workout> workouts) {
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM d");
        LocalDate thisWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<WeekBucket> buckets = new ArrayList<>();
        for (int i = WEEKS_SHOWN - 1; i >= 0; i--) {
            LocalDate weekStart = thisWeekStart.minusWeeks(i);
            LocalDate weekEnd = weekStart.plusDays(6);
            int reps = 0;
            for (Workout w : workouts) {
                LocalDate d = w.getDate();
                if (!d.isBefore(weekStart) && !d.isAfter(weekEnd)) {
                    reps += w.totalReps();
                }
            }
            buckets.add(new WeekBucket(weekStart.format(labelFormat), reps));
        }
        return buckets;
    }

    // ---- Body metrics (single current snapshot — no historical log exists yet) ------

    private VBox buildBodyMetricsCard() {
        Label header = new Label("Current Body Metrics");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        LocalProfileStore profile = LocalProfileStore.getInstance();
        double weight = profile.weightKg;
        double height = profile.heightCm;
        double bmi = (height > 0) ? weight / Math.pow(height / 100.0, 2) : 0;

        HBox row = new HBox(16,
                statCard("Weight", weight > 0 ? String.format("%.1f kg", weight) : "—"),
                statCard("Height", height > 0 ? String.format("%.0f cm", height) : "—"),
                statCard("BMI", bmi > 0 ? String.format("%.1f", bmi) : "—"));
        for (Node node : row.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        Label note = new Label("From your Profile page. Historical weight trends aren't tracked yet.");
        note.setStyle("-fx-font-size: 11px; -fx-text-fill: " + SUBTITLE + ";");

        VBox card = new VBox(10, header, row, note);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    // ---- Goals (shared live data with the Goals page) -------------------

    private VBox buildGoalsCard() {
        Label header = new Label("Goal Completion");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        List<LocalProfileStore.Milestone> milestones = LocalProfileStore.getInstance().milestones;

        VBox list = new VBox(14);
        if (milestones.isEmpty()) {
            Label empty = new Label("No goals yet — create one on the Goals page to see it here.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");
            list.getChildren().add(empty);
        } else {
            for (LocalProfileStore.Milestone milestone : milestones) {
                list.getChildren().add(buildGoalRow(milestone));
            }
        }

        VBox card = new VBox(12, header, list);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private VBox buildGoalRow(LocalProfileStore.Milestone milestone) {
        double fraction = milestone.progressPercent() / 100.0;

        Label nameLabel = new Label(milestone.description);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TITLE + "; -fx-font-weight: bold;");

        Label valueLabel = new Label(String.format("%s / %s %s  (%d%%)",
                trimNumber(milestone.currentValue), trimNumber(milestone.targetValue),
                milestone.unit, milestone.progressPercent()));
        valueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        ProgressBar bar = new ProgressBar(fraction);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + ORANGE + ";");

        return new VBox(4, nameLabel, bar, valueLabel);
    }

    private String trimNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
