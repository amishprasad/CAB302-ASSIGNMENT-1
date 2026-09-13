package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class GoalsView implements Page {

    private static final String ACCENT = "#f97316";
    private static final String ACHIEVED_COLOR = "#16a34a";
    private static final String ACHIEVED_BG = "#dcfce7";
    private static final String TITLE_COLOR = "#1E293B";
    private static final String MUTED_COLOR = "#64748B";
    private static final String PAGE_BG = "#F1F5F9";
    private static final String TRACK_COLOR = "#e2e8f0";

    private final VBox milestonesListBox = new VBox(12);
    private final LocalProfileStore store = LocalProfileStore.getInstance();

    @Override
    public String label() {
        return "Goals";
    }

    @Override
    public Node getContent() {
        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        Label title = new Label("Performance & Physical Goals");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label subtitle = new Label("Define targets, check percentages, and maintain consistent milestones over long-term cycles.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MUTED_COLOR + ";");
        VBox.setMargin(subtitle, new Insets(4, 0, 20, 0));

        VBox createCard = buildCreateCard();

        Label activeLabel = new Label("Active Milestones Progress");
        activeLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        VBox.setMargin(activeLabel, new Insets(24, 0, 12, 0));

        refreshMilestonesList();

        page.getChildren().addAll(title, subtitle, createCard, activeLabel, milestonesListBox);
        return page;
    }

    private VBox buildCreateCard() {
        VBox card = new VBox(12);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(22, 26, 22, 26));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label cardTitle = new Label("Create New Milestone Goal");
        cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("e.g. Overhead Press 60 kg");
        descriptionField.setPrefHeight(38);
        descriptionField.setMaxWidth(Double.MAX_VALUE);

        TextField targetField = new TextField();
        targetField.setPromptText("60");
        targetField.setPrefHeight(38);

        TextField unitField = new TextField("kg");
        unitField.setPrefHeight(38);
        unitField.setPrefWidth(60);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button saveButton = new Button("Save Goal Milestone");
        saveButton.setPrefHeight(38);
        saveButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> {
            String desc = descriptionField.getText().trim();
            String targetText = targetField.getText().trim();
            String unit = unitField.getText().trim();

            if (desc.isEmpty() || targetText.isEmpty() || unit.isEmpty()) {
                errorLabel.setText("Please fill in a goal description, target, and unit.");
                errorLabel.setVisible(true);
                return;
            }
            try {
                double target = Double.parseDouble(targetText);
                if (target <= 0) {
                    errorLabel.setText("Target must be a positive number.");
                    errorLabel.setVisible(true);
                    return;
                }

                store.milestones.add(new LocalProfileStore.Milestone(desc, target, unit, 0));

                descriptionField.clear();
                targetField.clear();
                unitField.setText("kg");
                errorLabel.setVisible(false);

                refreshMilestonesList();

            } catch (NumberFormatException ex) {
                errorLabel.setText("Target must be a valid number.");
                errorLabel.setVisible(true);
            }
        });

        HBox targetRow = new HBox(12, buildLabeledField("Numeric Target", targetField),
                buildLabeledField("Unit", unitField), saveButton);
        targetRow.setAlignment(Pos.BOTTOM_LEFT);

        card.getChildren().addAll(cardTitle, buildLabeledField("Goal Description", descriptionField), targetRow, errorLabel);
        return card;
    }

    private VBox buildLabeledField(String labelText, javafx.scene.control.Control field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        VBox box = new VBox(4, label, field);
        return box;
    }

    private void refreshMilestonesList() {
        milestonesListBox.getChildren().clear();

        if (store.milestones.isEmpty()) {
            Label empty = new Label("No milestones yet. Create one above to start tracking progress.");
            empty.setStyle("-fx-text-fill: " + MUTED_COLOR + "; -fx-font-size: 13px;");
            milestonesListBox.getChildren().add(empty);
            return;
        }

        for (LocalProfileStore.Milestone milestone : store.milestones) {
            milestonesListBox.getChildren().add(buildMilestoneCard(milestone));
        }
    }

    private HBox buildMilestoneCard(LocalProfileStore.Milestone milestone) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 10;");

        HBox nameRow = new HBox(8);
        Label nameLabel = new Label(milestone.description);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        nameRow.getChildren().add(nameLabel);
        if (milestone.isAchieved()) {
            Label badge = new Label("ACHIEVED");
            badge.setStyle("-fx-background-color: " + ACHIEVED_BG + "; -fx-text-fill: " + ACHIEVED_COLOR
                    + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;");
            nameRow.getChildren().add(badge);
        }

        Label detailLabel = new Label(String.format("Current: %s %s / Target: %s %s",
                trimNumber(milestone.currentValue), milestone.unit, trimNumber(milestone.targetValue), milestone.unit));
        detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");

        Button updateButton = new Button("Update progress");
        updateButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT + "; -fx-font-size: 11px;");
        updateButton.setOnAction(e -> showUpdateProgressDialog(milestone));
        VBox.setMargin(updateButton, new Insets(4, 0, 0, 0));

        VBox leftColumn = new VBox(4, nameRow, detailLabel, updateButton);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        StackPane progressTrack = new StackPane();
        progressTrack.setPrefWidth(220);
        progressTrack.setPrefHeight(8);
        progressTrack.setStyle("-fx-background-color: " + TRACK_COLOR + "; -fx-background-radius: 4;");
        Region progressFill = new Region();
        progressFill.setPrefHeight(8);
        progressFill.setStyle("-fx-background-color: " + (milestone.isAchieved() ? ACHIEVED_COLOR : ACCENT)
                + "; -fx-background-radius: 4;");
        StackPane.setAlignment(progressFill, Pos.CENTER_LEFT);
        progressFill.prefWidthProperty().bind(progressTrack.widthProperty().multiply(milestone.progressPercent() / 100.0));
        progressTrack.getChildren().add(progressFill);

        Label percentLabel = new Label(milestone.progressPercent() + "%");
        percentLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        percentLabel.setMinWidth(40);

        HBox progressSection = new HBox(12, progressTrack, percentLabel);
        progressSection.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(leftColumn, progressSection);
        return card;
    }

    private String trimNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private void showUpdateProgressDialog(LocalProfileStore.Milestone milestone) {
        TextInputDialog dialog = new TextInputDialog(trimNumber(milestone.currentValue));
        dialog.setTitle("Update progress");
        dialog.setHeaderText("Update current value for \"" + milestone.description + "\"");
        dialog.setContentText("Current (" + milestone.unit + "):");
        dialog.showAndWait().ifPresent(input -> {
            try {
                double newValue = Double.parseDouble(input.trim());
                if (newValue >= 0) {
                    milestone.currentValue = newValue;
                    refreshMilestonesList();
                }
            } catch (NumberFormatException ignored) {
                // Silently ignore invalid input; dialog can be reopened to retry.
            }
        });
    }
}