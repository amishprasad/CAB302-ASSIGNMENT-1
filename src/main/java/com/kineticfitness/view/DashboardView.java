package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;";

    @Override
    public String label() {
        return "Dashboard";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label("Your fitness activity at a glance.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        HBox statCards = new HBox(16,
                statCard("Workouts Logged", "12"),
                statCard("Active Goals", "3"),
                statCard("Next Scheduled", "Leg Day - 04 Sep"));
        statCards.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(16, title, subtitle, statCards);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        return content;
    }

    private VBox statCard(String label, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + ORANGE + ";");

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        VBox card = new VBox(6, valueLabel, labelLabel);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        card.setPrefWidth(220);
        HBox.setHgrow(card, Priority.SOMETIMES);
        return card;
    }
}