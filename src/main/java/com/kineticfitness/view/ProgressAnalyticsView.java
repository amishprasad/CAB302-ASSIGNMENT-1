package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Progress & Analytics page: summarises training volume, body-metric trends, and goal
 * completion so a user can see how their training is tracking over time. Implements
 * {@link Page} so it plugs into the {@link AppShell} — the shell provides the sidebar,
 * this class provides only the centre content.
 */
public class ProgressAnalyticsView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String BORDER = "#E2E8F0";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: " + BORDER + "; -fx-border-radius: 10;";

    private record WeeklyVolume(String week, int totalReps) {}
    private record WeightPoint(String weekLabel, double weightKg) {}
    private record GoalSummary(String description, int current, int target) {}

    private final List<WeeklyVolume> weeklyVolume = List.of(
            new WeeklyVolume("Wk 1", 980),
            new WeeklyVolume("Wk 2", 1120),
            new WeeklyVolume("Wk 3", 1050),
            new WeeklyVolume("Wk 4", 1340),
            new WeeklyVolume("Wk 5", 1290),
            new WeeklyVolume("Wk 6", 1480));

    private final List<WeightPoint> weightTrend = List.of(
            new WeightPoint("Wk 1", 81.5),
            new WeightPoint("Wk 2", 80.9),
            new WeightPoint("Wk 3", 80.4),
            new WeightPoint("Wk 4", 79.8),
            new WeightPoint("Wk 5", 79.3),
            new WeightPoint("Wk 6", 79.0));

    private final List<GoalSummary> goalSummaries = List.of(
            new GoalSummary("Run 100 km this quarter", 62, 100),
            new GoalSummary("Bench press bodyweight", 75, 90),
            new GoalSummary("Log 40 workouts", 28, 40));

    @Override
    public String label() {
        return "Progress";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Progress & Analytics");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label("Track training volume, body metrics, and goal progress over time.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        VBox content = new VBox(16, title, subtitle,
                buildStatRow(), buildVolumeChartCard(), buildWeightChartCard(), buildGoalsCard());
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        return content;
    }

    private HBox buildStatRow() {
        int totalWorkouts = weeklyVolume.size() * 4;
        int totalReps = weeklyVolume.stream().mapToInt(WeeklyVolume::totalReps).sum();
        int goalsAchieved = (int) goalSummaries.stream().filter(g -> g.current() >= g.target()).count();

        HBox row = new HBox(16,
                statCard("Total Workouts", String.valueOf(totalWorkouts)),
                statCard("Total Reps", String.valueOf(totalReps)),
                statCard("Goals Achieved", goalsAchieved + " / " + goalSummaries.size()),
                statCard("Current Streak", "6 days"));
        for (Node node : row.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        return row;
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

    private VBox buildVolumeChartCard() {
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
        for (WeeklyVolume w : weeklyVolume) {
            series.getData().add(new XYChart.Data<>(w.week(), w.totalReps()));
        }
        chart.getData().add(series);

        VBox card = new VBox(10, header, helper, chart);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private VBox buildWeightChartCard() {
        Label header = new Label("Body Weight Trend");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label helper = new Label("Logged body weight over the last six weeks.");
        helper.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Weight (kg)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(240);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (WeightPoint p : weightTrend) {
            series.getData().add(new XYChart.Data<>(p.weekLabel(), p.weightKg()));
        }
        chart.getData().add(series);

        VBox card = new VBox(10, header, helper, chart);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private VBox buildGoalsCard() {
        Label header = new Label("Goal Completion");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        VBox list = new VBox(14);
        for (GoalSummary goal : goalSummaries) {
            list.getChildren().add(buildGoalRow(goal));
        }

        VBox card = new VBox(12, header, list);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private VBox buildGoalRow(GoalSummary goal) {
        double fraction = goal.target() == 0 ? 0 : Math.min(1.0, goal.current() / (double) goal.target());

        Label nameLabel = new Label(goal.description());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TITLE + "; -fx-font-weight: bold;");

        Label valueLabel = new Label(goal.current() + " / " + goal.target()
                + String.format("  (%.0f%%)", fraction * 100));
        valueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        ProgressBar bar = new ProgressBar(fraction);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + ORANGE + ";");

        return new VBox(4, nameLabel, bar, valueLabel);
    }
}
