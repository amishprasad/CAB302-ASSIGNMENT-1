package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Settings page: app-wide preferences (units, notifications) and account/data management.
 * Distinct from the Profile page, which holds personal fitness metrics. Implements
 * {@link Page} so it plugs into the {@link AppShell} — the shell provides the sidebar,
 * this class provides only the centre content.
 */
public class SettingsView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String BORDER = "#E2E8F0";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: " + BORDER + "; -fx-border-radius: 10;";

    private String selectedUnit = "Metric";
    private final HBox unitToggle = new HBox(4);

    private final CheckBox workoutReminders = new CheckBox("Workout reminders");
    private final CheckBox goalAlerts = new CheckBox("Goal progress alerts");
    private final CheckBox weeklySummary = new CheckBox("Weekly summary email");

    private final TextField usernameField = new TextField("alex.rivera");
    private final PasswordField newPasswordField = new PasswordField();
    private final PasswordField confirmPasswordField = new PasswordField();

    private final Label statusLabel = new Label();

    @Override
    public String label() {
        return "Settings";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label("Manage your preferences, notifications, and account.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        statusLabel.setStyle("-fx-text-fill: " + ORANGE + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        statusLabel.setVisible(false);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(16, title, subtitle,
                buildUnitsCard(), buildNotificationsCard(), buildAccountCard(), buildSaveRow());
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        content.setMaxWidth(640);
        return content;
    }

    private VBox buildUnitsCard() {
        Label header = new Label("Units & Measurement");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label helper = new Label("Choose how weights, heights, and distances are displayed.");
        helper.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        unitToggle.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 8; -fx-padding: 4;");
        unitToggle.getChildren().setAll(unitOption("Metric"), unitOption("Imperial"));

        VBox card = new VBox(10, header, helper, unitToggle);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private Label unitOption(String unitName) {
        Label option = new Label(unitName);
        option.setMaxWidth(Double.MAX_VALUE);
        option.setAlignment(Pos.CENTER);
        HBox.setHgrow(option, Priority.ALWAYS);
        option.setPadding(new Insets(8, 0, 8, 0));
        applyUnitStyle(option, unitName.equals(selectedUnit));
        option.setOnMouseClicked(e -> selectUnit(unitName));
        return option;
    }

    private void selectUnit(String unitName) {
        selectedUnit = unitName;
        for (Node node : unitToggle.getChildren()) {
            Label option = (Label) node;
            applyUnitStyle(option, option.getText().equals(unitName));
        }
    }

    private void applyUnitStyle(Label option, boolean active) {
        if (active) {
            option.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                    + " -fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 13px;");
        } else {
            option.setStyle("-fx-background-color: transparent; -fx-text-fill: " + SUBTITLE + ";"
                    + " -fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 13px;");
        }
    }

    private VBox buildNotificationsCard() {
        Label header = new Label("Notifications");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        workoutReminders.setSelected(true);
        goalAlerts.setSelected(true);
        weeklySummary.setSelected(false);
        for (CheckBox box : new CheckBox[]{workoutReminders, goalAlerts, weeklySummary}) {
            box.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TITLE + ";");
        }

        VBox card = new VBox(10, header, workoutReminders, goalAlerts, weeklySummary);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private VBox buildAccountCard() {
        Label header = new Label("Account & Data");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);

        grid.add(fieldLabel("Username"), 0, 0);
        grid.add(fieldLabel("New Password"), 1, 0);
        grid.add(fieldLabel("Confirm Password"), 2, 0);
        usernameField.setPrefWidth(160);
        newPasswordField.setPrefWidth(160);
        confirmPasswordField.setPrefWidth(160);
        grid.add(usernameField, 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(confirmPasswordField, 2, 1);

        Button exportButton = new Button("Export My Data");
        exportButton.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE + ";"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        Button clearButton = new Button("Clear All Data");
        clearButton.setStyle("-fx-background-color: white; -fx-text-fill: #DC2626;"
                + " -fx-border-color: #FCA5A5; -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox dataRow = new HBox(10, exportButton, clearButton);

        VBox card = new VBox(14, header, grid, dataRow);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private HBox buildSaveRow() {
        Button save = new Button("Save Settings");
        save.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 22 8 22;");
        save.setOnAction(e -> handleSave());

        HBox row = new HBox(12, statusLabel, save);
        row.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        return row;
    }

    private void handleSave() {
        statusLabel.setText("Settings saved.");
        statusLabel.setVisible(true);
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");
        return l;
    }
}
