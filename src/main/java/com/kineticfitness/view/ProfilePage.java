package com.kineticfitness.view;

import com.kineticfitness.db.UserDAO;
import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import com.kineticfitness.session.UserSession;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Profile screen. Shows the create form when no user is logged in, and the saved
 * profile details once one is. Persists through {@link UserDAO} and records the
 * active user in {@link UserSession} so every other page can load their data.
 */
public class ProfilePage implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;";

    private final UserDAO userDAO = new UserDAO();
    private final StackPane container = new StackPane();

    @Override
    public String label() {
        return "Profile";
    }

    @Override
    public Node getContent() {
        container.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        render();
        return container;
    }

    private void render() {
        container.getChildren().setAll(
                UserSession.isLoggedIn() ? buildDetails(UserSession.getCurrentUser()) : buildForm(null));
    }

    private Node buildForm(User existing) {
        boolean isEdit = existing != null;

        TextField nameField = new TextField();
        TextField ageField = new TextField();
        TextField heightField = new TextField();
        TextField weightField = new TextField();
        ComboBox<String> levelBox = new ComboBox<>();
        levelBox.getItems().addAll("Beginner", "Intermediate", "Advanced");
        levelBox.setMaxWidth(Double.MAX_VALUE);
        nameField.setPromptText("Alex");
        ageField.setPromptText("27");
        heightField.setPromptText("175");
        weightField.setPromptText("70");

        if (isEdit) {
            nameField.setText(existing.getUsername());
            if (existing.getAge() > 0) ageField.setText(String.valueOf(existing.getAge()));
            if (existing.getHeightCm() > 0) heightField.setText(String.valueOf((int) existing.getHeightCm()));
            if (existing.getWeightKg() > 0) weightField.setText(String.valueOf(existing.getWeightKg()));
            if (existing.getFitnessLevel() != null) levelBox.setValue(display(existing.getFitnessLevel()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("First name"), 0, 0);
        grid.add(nameField, 0, 1, 2, 1);
        grid.add(new Label("Age"), 0, 2);
        grid.add(new Label("Fitness level"), 1, 2);
        grid.add(ageField, 0, 3);
        grid.add(levelBox, 1, 3);
        grid.add(new Label("Height (cm)"), 0, 4);
        grid.add(new Label("Weight (kg)"), 1, 4);
        grid.add(heightField, 0, 5);
        grid.add(weightField, 1, 5);

        Label error = new Label();
        error.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        error.setVisible(false);

        Button action = new Button(isEdit ? "Save changes" : "Create profile");
        action.setMaxWidth(Double.MAX_VALUE);
        action.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 8;");
        action.setOnAction(e -> {
            User saved = validateAndBuild(existing, nameField, ageField, heightField, weightField, levelBox, error);
            if (saved != null) {
                userDAO.save(saved);                 // persist
                UserSession.setCurrentUser(saved);   // set active user
                render();                            // swap to details
            }
        });

        VBox card = new VBox(14,
                heading(isEdit ? "Edit Profile" : "Create Your Profile"),
                sub(isEdit ? "Update your details below."
                        : "Tell us about yourself so we can personalise your plan."),
                grid, error, action);
        card.setPadding(new Insets(24));
        card.setMaxWidth(460);
        card.setStyle(CARD);

        VBox wrap = new VBox(card);
        wrap.setPadding(new Insets(32, 40, 32, 40));
        wrap.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        return wrap;
    }

    private User validateAndBuild(User existing, TextField name, TextField age, TextField height,
                                  TextField weight, ComboBox<String> level, Label error) {
        String n = name.getText().trim();
        String a = age.getText().trim();
        String h = height.getText().trim();
        String w = weight.getText().trim();
        String lvl = level.getValue();

        if (n.isEmpty() || a.isEmpty() || h.isEmpty() || w.isEmpty() || lvl == null) {
            return showError(error, "Please fill in every field before continuing.");
        }
        try {
            int ageV = Integer.parseInt(a);
            double hV = Double.parseDouble(h);
            double wV = Double.parseDouble(w);
            if (ageV <= 0 || hV <= 0 || wV <= 0) {
                return showError(error, "Age, height and weight must be positive numbers.");
            }
            error.setVisible(false);
            FitnessLevel fl = FitnessLevel.valueOf(lvl.toUpperCase());
            if (existing != null) {
                existing.setUsername(n);
                existing.setFitnessLevel(fl);
                existing.setAge(ageV);
                existing.setHeightCm(hV);
                existing.setWeightKg(wV);
                return existing;
            }
            return new User(n, fl, ageV, hV, wV);
        } catch (NumberFormatException ex) {
            return showError(error, "Age, height and weight must be valid numbers.");
        }
    }

    private Node buildDetails(User user) {
        VBox details = new VBox(8,
                row("Username", user.getUsername()),
                row("Fitness level", display(user.getFitnessLevel())),
                row("Age", user.getAge() + " years"),
                row("Height / Weight", String.format("%.0f cm  /  %.1f kg",
                        user.getHeightCm(), user.getWeightKg())),
                row("BMI", String.format("%.1f  (%s)", user.getBmi(), user.getBmiCategory())));
        details.setPadding(new Insets(16));
        details.setMaxWidth(460);
        details.setStyle(CARD);

        Button edit = new Button("Edit profile");
        edit.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 8;");
        edit.setOnAction(e -> container.getChildren().setAll(buildForm(user)));

        VBox content = new VBox(16, heading("My Profile"),
                sub("Personal details and fitness summary."), details, edit);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        return content;
    }

    // ---- helpers ----------------------------------------------------------

    private User showError(Label error, String message) {
        error.setText(message);
        error.setVisible(true);
        return null;
    }

    private Label heading(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        return l;
    }

    private Label sub(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");
        return l;
    }

    private HBox row(String key, String value) {
        Label k = new Label(key);
        k.setStyle("-fx-text-fill: " + SUBTITLE + ";");
        k.setMinWidth(150);
        Label v = new Label(value);
        v.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        return new HBox(12, k, v);
    }

    private String display(FitnessLevel level) {
        if (level == null) return "Beginner";
        String n = level.name().toLowerCase();
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }
}