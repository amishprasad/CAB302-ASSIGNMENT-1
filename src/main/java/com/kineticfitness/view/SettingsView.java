package com.kineticfitness.view;

import com.kineticfitness.db.WorkoutDAO;
import com.kineticfitness.db.UserDAO;
import com.kineticfitness.model.User;
import com.kineticfitness.model.Workout;
import com.kineticfitness.session.UserSession;
import com.kineticfitness.util.PasswordUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Settings page: app-wide preferences (units, notifications) and account/data management
 * for the signed-in user. Distinct from the Profile page, which holds personal fitness
 * metrics. Implements {@link Page} so it plugs into the {@link AppShell} — the shell
 * provides the sidebar, this class provides only the centre content.
 */
public class SettingsView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String BORDER = "#E2E8F0";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: " + BORDER + "; -fx-border-radius: 10;";

    private final UserDAO userDAO = new UserDAO();
    private final WorkoutDAO workoutDAO = new WorkoutDAO();

    private String selectedUnit = "Metric";
    private final HBox unitToggle = new HBox(4);

    private final CheckBox workoutReminders = new CheckBox("Workout reminders");
    private final CheckBox goalAlerts = new CheckBox("Goal progress alerts");
    private final CheckBox weeklySummary = new CheckBox("Weekly summary email");

    private final TextField usernameField = new TextField();
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

        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        statusLabel.setVisible(false);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        User currentUser = UserSession.getCurrentUser();
        usernameField.setText(currentUser != null ? currentUser.getUsername() : "");
        usernameField.setEditable(false);
        usernameField.setStyle("-fx-opacity: 0.75;");

        VBox content = new VBox(16, title, subtitle,
                buildUnitsCard(), buildNotificationsCard(), buildAccountCard(), buildSaveRow());
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        content.setMaxWidth(640);
        return content;
    }

    // ---- Units & Measurement (session-only — no preferences table exists yet) ----

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

    // ---- Account & Data (real: password change, export, clear) ----

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
        exportButton.setOnAction(e -> handleExport(exportButton));

        Button clearButton = new Button("Clear All Data");
        clearButton.setStyle("-fx-background-color: white; -fx-text-fill: #DC2626;"
                + " -fx-border-color: #FCA5A5; -fx-border-radius: 8; -fx-background-radius: 8;");
        clearButton.setOnAction(e -> handleClearData());

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

    /** Saves a password change when one was entered. Units/notifications stay session-only for now. */
    private void handleSave() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showStatus("You need to be signed in to save settings.", true);
            return;
        }

        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() && confirmPassword.isEmpty()) {
            showStatus("Settings saved.", false);
            return;
        }
        if (newPassword.length() < 6) {
            showStatus("New password must be at least 6 characters.", true);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showStatus("Passwords don't match.", true);
            return;
        }

        String hashed = PasswordUtil.hash(newPassword);
        userDAO.updatePassword(currentUser.getUsername(), hashed);
        currentUser.setPasswordHash(hashed);

        newPasswordField.clear();
        confirmPasswordField.clear();
        showStatus("Password updated.", false);
    }

    private void handleExport(Node anchor) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showStatus("You need to be signed in to export data.", true);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export My Data");
        chooser.setInitialFileName("kinetic-fitness-export-" + currentUser.getUsername() + ".txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text file", "*.txt"));

        Window window = anchor.getScene() != null ? anchor.getScene().getWindow() : null;
        java.io.File file = chooser.showSaveDialog(window);
        if (file == null) {
            return;
        }

        try (Writer writer = Files.newBufferedWriter(Path.of(file.getPath()), StandardCharsets.UTF_8)) {
            writeExport(writer, currentUser);
            showStatus("Data exported to " + file.getName() + ".", false);
        } catch (IOException ex) {
            showStatus("Export failed: " + ex.getMessage(), true);
        }
    }

    private void writeExport(Writer writer, User user) throws IOException {
        DateTimeFormatter timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalProfileStore profile = LocalProfileStore.getInstance();

        writer.write("Kinetic Fitness — Data Export\n");
        writer.write("Generated: " + LocalDateTime.now().format(timestamp) + "\n");
        writer.write("=".repeat(40) + "\n\n");

        writer.write("Account\n");
        writer.write("  Username: " + user.getUsername() + "\n");
        writer.write("  Email: " + (user.getEmail() != null ? user.getEmail() : "—") + "\n\n");

        writer.write("Profile\n");
        writer.write("  Name: " + (profile.firstName.isEmpty() ? "—" : profile.firstName) + "\n");
        writer.write("  Height: " + (profile.heightCm > 0 ? profile.heightCm + " cm" : "—") + "\n");
        writer.write("  Weight: " + (profile.weightKg > 0 ? profile.weightKg + " kg" : "—") + "\n\n");

        List<Workout> workouts = workoutDAO.findAllByUsername(user.getUsername());
        writer.write("Workouts (" + workouts.size() + ")\n");
        for (Workout w : workouts) {
            writer.write("  " + w.getDate() + " — " + w.getExercises().size() + " exercise(s)\n");
        }
        writer.write("\n");

        writer.write("Goals (" + profile.milestones.size() + ")\n");
        for (LocalProfileStore.Milestone m : profile.milestones) {
            writer.write("  " + m.description + ": " + m.currentValue + " / " + m.targetValue
                    + " " + m.unit + " (" + m.progressPercent() + "%)\n");
        }
    }

    private void handleClearData() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showStatus("You need to be signed in to clear data.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This permanently deletes all your logged workouts and goals. This can't be undone.",
                ButtonType.CANCEL, ButtonType.OK);
        confirm.setHeaderText("Clear all your data?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        workoutDAO.deleteAllForUser(currentUser.getUsername());
        LocalProfileStore.getInstance().milestones.clear();
        showStatus("Your workouts and goals have been cleared.", false);
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");
        return l;
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (isError ? "#DC2626" : ORANGE) + ";");
        statusLabel.setVisible(true);
    }
}
