package com.kineticfitness.view;

import com.kineticfitness.db.UserDAO;
import com.kineticfitness.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Full-window login screen shown before the {@link AppShell}. Verifies the entered
 * username/email and password against the {@code users} table via {@link UserDAO#authenticate}.
 * On success, hands the authenticated {@link User} back to whoever constructed this screen
 * (see {@code Main}) so it can set the session and launch the app shell.
 */
public class Login {

    private static final String NAVY = "#0F172A";
    private static final String NAV_TEXT = "#CBD5E1";
    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String FIELD = "-fx-background-color: white; -fx-background-radius: 8;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-padding: 10;";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 12;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 12;";

    private final Stage stage;
    private final Consumer<User> onLoginSuccess;
    private final Runnable onNavigateToRegister;
    private final String infoMessage;
    private final UserDAO userDAO = new UserDAO();

    public Login(Stage stage, Consumer<User> onLoginSuccess, Runnable onNavigateToRegister) {
        this(stage, onLoginSuccess, onNavigateToRegister, null);
    }

    /** @param infoMessage optional banner (e.g. "Account created — please log in.") shown above the form. */
    public Login(Stage stage, Consumer<User> onLoginSuccess, Runnable onNavigateToRegister, String infoMessage) {
        this.stage = stage;
        this.onLoginSuccess = onLoginSuccess;
        this.onNavigateToRegister = onNavigateToRegister;
        this.infoMessage = infoMessage;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setLeft(buildBrandPanel());
        root.setCenter(buildFormPanel());
        stage.setTitle("Kinetic Fitness - Log In");
        stage.setScene(new Scene(root, 1280, 800));
        stage.show();
    }

    private VBox buildBrandPanel() {
        Region mark = new Region();
        mark.setMinSize(40, 40);
        mark.setPrefSize(40, 40);
        mark.setMaxSize(40, 40);
        mark.setStyle("-fx-background-color: " + ORANGE + "; -fx-background-radius: 10;");

        Label brand = new Label("Kinetic Fitness");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        HBox logo = new HBox(12, mark, brand);
        logo.setAlignment(Pos.CENTER_LEFT);

        Label tagline = new Label("Track workouts, hit your goals,\nand build momentum every day.");
        tagline.setStyle("-fx-text-fill: " + NAV_TEXT + "; -fx-font-size: 15px;");
        tagline.setWrapText(true);

        VBox panel = new VBox(20, logo, tagline);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(40));
        panel.setPrefWidth(460);
        panel.setMinWidth(460);
        panel.setStyle("-fx-background-color: " + NAVY + ";");
        return panel;
    }

    private StackPane buildFormPanel() {
        TextField identifierField = new TextField();
        identifierField.setPromptText("e.g. alexrivera or alex@email.com");
        identifierField.setStyle(FIELD);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Your password");
        passwordField.setStyle(FIELD);

        Label error = new Label();
        error.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        error.setWrapText(true);
        error.setVisible(false);
        error.setManaged(false);

        Label info = new Label();
        info.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px; -fx-font-weight: bold;");
        info.setWrapText(true);
        if (infoMessage != null && !infoMessage.isBlank()) {
            info.setText(infoMessage);
        } else {
            info.setVisible(false);
            info.setManaged(false);
        }

        Button loginButton = new Button("Log In");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10;");
        loginButton.setOnAction(e -> attemptLogin(identifierField, passwordField, error));
        passwordField.setOnAction(e -> attemptLogin(identifierField, passwordField, error));

        Button toRegister = new Button("Don't have an account? Register");
        toRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ORANGE + ";"
                + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
        toRegister.setOnAction(e -> onNavigateToRegister.run());

        VBox card = new VBox(14,
                heading("Welcome back"),
                sub("Log in to continue tracking your progress."),
                info,
                labeled("Username or email", identifierField),
                labeled("Password", passwordField),
                error,
                loginButton,
                toRegister);
        card.setPadding(new Insets(32));
        card.setMaxWidth(400);
        card.setStyle(CARD);

        StackPane wrap = new StackPane(card);
        wrap.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        return wrap;
    }

    private void attemptLogin(TextField identifierField, PasswordField passwordField, Label error) {
        String identifier = identifierField.getText().trim();
        String password = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            showError(error, "Please enter your username/email and password.");
            return;
        }

        User user = userDAO.authenticate(identifier, password);
        if (user == null) {
            showError(error, "Incorrect username, email or password.");
            return;
        }

        error.setVisible(false);
        error.setManaged(false);
        onLoginSuccess.accept(user);
    }

    private void showError(Label error, String message) {
        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
    }

    private VBox labeled(String labelText, Control field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: " + SUBTITLE + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        field.setMaxWidth(Double.MAX_VALUE);
        return new VBox(6, label, field);
    }

    private Label heading(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        return l;
    }

    private Label sub(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");
        l.setWrapText(true);
        return l;
    }
}
