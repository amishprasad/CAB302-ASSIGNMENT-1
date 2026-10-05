package com.kineticfitness.view;


import com.kineticfitness.db.PreferencesDAO;
import com.kineticfitness.db.ProfileDAO;
import com.kineticfitness.db.UserDAO;
import com.kineticfitness.db.WorkoutDAO;
import com.kineticfitness.db.ScheduleDAO;
import com.kineticfitness.model.User;
import com.kineticfitness.service.ExportService;
import com.kineticfitness.service.PasswordChangeValidator;
import com.kineticfitness.service.ValidationResult;
import com.kineticfitness.session.UserSession;
import com.kineticfitness.util.PasswordUtil;
import com.kineticfitness.util.UnitSystem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
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
    private final PreferencesDAO preferencesDAO = new PreferencesDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final ProfileDAO profileDAO = new ProfileDAO();

    private UnitSystem selectedUnit = UnitSystem.METRIC;
    private final HBox unitToggle = new HBox(4);

    private final CheckBox workoutReminders = new CheckBox("Workout reminders");
    private final CheckBox goalAlerts = new CheckBox("Goal progress alerts");
    private final CheckBox weeklySummary = new CheckBox("Weekly summary (shown on Dashboard)");

    private final TextField usernameField = new TextField();
    private final PasswordField newPasswordField = new PasswordField();
    private final PasswordField confirmPasswordField = new PasswordField();
    private final PasswordField currentPasswordField = new PasswordField();

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

        if (currentUser != null) {
            preferencesDAO.load(LocalProfileStore.getInstance(), currentUser.getUsername());
        }
        selectedUnit = LocalProfileStore.getInstance().unitSystem;

        VBox content = new VBox(16, title, subtitle,
                buildUnitsCard(), buildNotificationsCard(), buildAccountCard(), buildSaveRow());
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        content.setMaxWidth(640);
        return content;
    }

    // ---- Units & Measurement ----

    private VBox buildUnitsCard() {
        Label header = new Label("Units & Measurement");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label helper = new Label("Choose how weights, heights, and distances are displayed.");
        helper.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        unitToggle.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 8; -fx-padding: 4;");
        unitToggle.getChildren().setAll(unitOption(UnitSystem.METRIC), unitOption(UnitSystem.IMPERIAL));

        VBox card = new VBox(10, header, helper, unitToggle);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private Label unitOption(UnitSystem system) {
        Label option = new Label(system.displayName());
        option.setMaxWidth(Double.MAX_VALUE);
        option.setAlignment(Pos.CENTER);
        HBox.setHgrow(option, Priority.ALWAYS);
        option.setPadding(new Insets(8, 0, 8, 0));
        applyUnitStyle(option, system == selectedUnit);
        option.setOnMouseClicked(e -> selectUnit(system));
        return option;
    }

    private void selectUnit(UnitSystem system) {
        selectedUnit = system;
        for (Node node : unitToggle.getChildren()) {
            Label option = (Label) node;
            applyUnitStyle(option, option.getText().equals(system.displayName()));
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

        LocalProfileStore store = LocalProfileStore.getInstance();
        workoutReminders.setSelected(store.notifyWorkoutReminders);
        goalAlerts.setSelected(store.notifyGoalAlerts);
        weeklySummary.setSelected(store.notifyWeeklySummary);
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

        usernameField.setPrefWidth(160);
        currentPasswordField.setPrefWidth(160);
        newPasswordField.setPrefWidth(160);
        confirmPasswordField.setPrefWidth(160);

        grid.add(fieldLabel("Username"), 0, 0);
        grid.add(fieldLabel("Current Password"), 1, 0);
        grid.add(usernameField, 0, 1);
        grid.add(currentPasswordField, 1, 1);
        grid.add(fieldLabel("New Password"), 0, 2);
        grid.add(fieldLabel("Confirm Password"), 1, 2);
        grid.add(newPasswordField, 0, 3);
        grid.add(confirmPasswordField, 1, 3);

        Button exportButton = new Button("Export Workouts (CSV)");
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

    /**
     * Always persists units and notification choices. A password change only happens
     * when the password fields were actually filled in — leaving them blank just saves
     * the rest of the page, the same as before.
     */
    private void handleSave() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showStatus("You need to be signed in to save settings.", true);
            return;
        }

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        boolean changingPassword = !currentPassword.isEmpty()
                || !newPassword.isEmpty() || !confirmPassword.isEmpty();

        if (changingPassword) {
            ValidationResult check = PasswordChangeValidator.validate(
                    currentUser.getPasswordHash(), currentPassword, newPassword, confirmPassword);
            if (check.isInvalid()) {
                showStatus(check.message(), true);
                return;
            }
        }

        LocalProfileStore store = LocalProfileStore.getInstance();
        store.unitSystem = selectedUnit;
        store.notifyWorkoutReminders = workoutReminders.isSelected();
        store.notifyGoalAlerts = goalAlerts.isSelected();
        store.notifyWeeklySummary = weeklySummary.isSelected();
        preferencesDAO.save(store, currentUser.getUsername());

        if (!changingPassword) {
            showStatus("Settings saved.", false);
            return;
        }

        String hashed = PasswordUtil.hash(newPassword);
        userDAO.updatePassword(currentUser.getUsername(), hashed);
        currentUser.setPasswordHash(hashed);

        newPasswordField.clear();
        confirmPasswordField.clear();
        currentPasswordField.clear();
        showStatus("Settings and password updated.", false);
    }

    private void handleExport(Node anchor) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showStatus("You need to be signed in to export data.", true);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export My Workouts");
        chooser.setInitialFileName(ExportService.fileName(currentUser.getUsername(), LocalDate.now()));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file", "*.csv"));

        Window window = anchor.getScene() != null ? anchor.getScene().getWindow() : null;
        java.io.File file = chooser.showSaveDialog(window);
        if (file == null) {
            return;
        }

        String csv = ExportService.workoutsToCsv(workoutDAO.findAllByUsername(currentUser.getUsername()));
        try {
            Files.writeString(file.toPath(), csv, StandardCharsets.UTF_8);
            showStatus("Workouts exported to " + file.getName() + ".", false);
        } catch (IOException ex) {
            showStatus("Export failed: " + ex.getMessage(), true);
        }
    }

    private void handleClearData() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showStatus("You need to be signed in to clear data.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This permanently deletes your logged workouts, scheduled workouts and goals. "
                        + "Your account and profile details are kept. This can't be undone.",
                ButtonType.CANCEL, ButtonType.OK);
        confirm.setHeaderText("Clear all your data?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String username = currentUser.getUsername();
        workoutDAO.deleteAllForUser(username);
        scheduleDAO.deleteAllForUser(username);

        LocalProfileStore store = LocalProfileStore.getInstance();
        store.milestones.clear();
        store.primaryGoal = null;
        store.targetWeightKg = 0;
        store.goalStartDate = null;
        store.goalTargetDate = null;
        store.goalAchievedDate = null;
        profileDAO.save(store, username);

        showStatus("Your workouts, schedule and goals have been cleared.", false);
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
