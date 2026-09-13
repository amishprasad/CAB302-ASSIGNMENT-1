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

/**
 * Full-window registration screen shown before the {@link AppShell}. Creates a new row in
 * the {@code users} table via {@link UserDAO#register}, hashing the password before it is
 * stored and checking username/email/phone number uniqueness first.
 */
public class Register {

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
    private final Runnable onRegisterSuccess;
    private final Runnable onNavigateToLogin;
    private final UserDAO userDAO = new UserDAO();

    public Register(Stage stage, Runnable onRegisterSuccess, Runnable onNavigateToLogin) {
        this.stage = stage;
        this.onRegisterSuccess = onRegisterSuccess;
        this.onNavigateToLogin = onNavigateToLogin;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setLeft(buildBrandPanel());
        root.setCenter(buildFormPanel());
        stage.setTitle("Kinetic Fitness - Register");
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

        Label tagline = new Label("Create an account to start\nlogging workouts and goals.");
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
        TextField usernameField = new TextField();
        usernameField.setPromptText("e.g. alexrivera");
        usernameField.setStyle(FIELD);

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. alex@email.com");
        emailField.setStyle(FIELD);

        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g. 0400 000 000");
        phoneField.setStyle(FIELD);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("At least 6 characters");
        passwordField.setStyle(FIELD);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Re-enter your password");
        confirmField.setStyle(FIELD);

        Label error = new Label();
        error.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        error.setWrapText(true);
        error.setVisible(false);
        error.setManaged(false);

        Button registerButton = new Button("Create Account");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10;");
        registerButton.setOnAction(e -> attemptRegister(
                usernameField, emailField, phoneField, passwordField, confirmField, error));
        confirmField.setOnAction(e -> attemptRegister(
                usernameField, emailField, phoneField, passwordField, confirmField, error));

        Button toLogin = new Button("Already have an account? Log in");
        toLogin.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ORANGE + ";"
                + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
        toLogin.setOnAction(e -> onNavigateToLogin.run());

        VBox card = new VBox(12,
                heading("Create your account"),
                sub("Fill in your details to start using Kinetic Fitness."),
                labeled("Username", usernameField),
                labeled("Email", emailField),
                labeled("Phone number", phoneField),
                labeled("Password", passwordField),
                labeled("Confirm password", confirmField),
                error,
                registerButton,
                toLogin);
        card.setPadding(new Insets(28));
        card.setMaxWidth(420);
        card.setStyle(CARD);

        StackPane wrap = new StackPane(card);
        wrap.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        return wrap;
    }

    private void attemptRegister(TextField usernameField, TextField emailField, TextField phoneField,
                                  PasswordField passwordField, PasswordField confirmField, Label error) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError(error, "Please fill in every field before continuing.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError(error, "Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            showError(error, "Password must be at least 6 characters long.");
            return;
        }
        if (!password.equals(confirm)) {
            showError(error, "Passwords do not match.");
            return;
        }
        if (userDAO.usernameExists(username)) {
            showError(error, "That username is already taken.");
            return;
        }
        if (userDAO.emailExists(email)) {
            showError(error, "An account with that email already exists.");
            return;
        }
        if (userDAO.phoneExists(phone)) {
            showError(error, "An account with that phone number already exists.");
            return;
        }

        User created = userDAO.register(username, password, email, phone);
        if (created == null) {
            showError(error, "Something went wrong creating your account. Please try again.");
            return;
        }

        error.setVisible(false);
        error.setManaged(false);
        onRegisterSuccess.run();
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
        l.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        return l;
    }

    private Label sub(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");
        l.setWrapText(true);
        return l;
    }
}
