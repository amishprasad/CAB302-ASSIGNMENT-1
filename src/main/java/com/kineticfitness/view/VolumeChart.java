package com.kineticfitness.view;

import com.kineticfitness.util.WorkoutStats.DatedVolume;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.List;

/**
 * Builds the orange "training volume" bar chart used on both the Dashboard and the
 * Workout History page, so the two look identical and the styling lives in one place.
 */
final class VolumeChart {

    private static final String ORANGE = "#F97316";

    private VolumeChart() {
    }

    /**
     * An empty bar chart ready to be filled by {@link #plot}.
     *
     * @param height    preferred height in pixels — the Dashboard uses a shorter chart
     *                  than the full Workout History page
     * @param axisLabel text down the y-axis, or null for none
     */
    static BarChart<String, Number> create(double height, String axisLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        if (axisLabel != null) {
            yAxis.setLabel(axisLabel);
        }

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);       // animation replays on every page visit otherwise
        chart.setPrefHeight(height);
        chart.setCategoryGap(18);
        return chart;
    }

    /** Replaces whatever the chart is showing with one orange bar per session. */
    static void plot(BarChart<String, Number> chart, List<DatedVolume> volumes) {
        chart.getData().clear();
        if (volumes.isEmpty()) {
            return;     // axes remain, plot area is simply empty
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (DatedVolume volume : volumes) {
            XYChart.Data<String, Number> bar = new XYChart.Data<>(volume.date(), volume.totalReps());
            // A bar's node doesn't exist until it's added to the scene, so colour it on arrival.
            bar.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    node.setStyle("-fx-bar-fill: " + ORANGE + ";");
                }
            });
            series.getData().add(bar);
        }
        chart.getData().add(series);
    }
}