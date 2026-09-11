package com.kineticfitness.view;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Screen displaying the user's active profile, calculated health metrics (BMI),
 * and aggregated progress summary across workouts and goals.
 */
public class ProfileDetailsView {

    private final Stage stage;
    private final User user;
    private final Runnable onBack;
    private final Consumer<User> onProfileUpdated;

    public ProfileDetailsView(Stage stage, User user, Runnable onBack, Consumer<User> onProfileUpdated) {
        this.stage = stage;
        this.user = user;
        this.onBack = onBack;
        this.onProfileUpdated = onProfileUpdated;
    }

    public void show() {
        VBox root = new VBox(18);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(32));

        Label title = new Label("My Profile");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Personal details and fitness summary");
        subtitle.setStyle("-fx-text-fill: gray;");

        // Personal details card
        VBox detailsCard = buildDetailsCard();

        // Fitness metrics & summary card
        VBox statsCard = buildStatsCard();

        // Actions
        Button editButton = new Button("Edit profile");
        editButton.setMaxWidth(220);
        editButton.setOnAction(e -> {
            new CreateProfileView(stage, user, onBack, onProfileUpdated).show();
        });

        Button backButton = new Button("Back to menu");
        backButton.setMaxWidth(220);
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        HBox buttonBox = new HBox(12, editButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, subtitle, detailsCard, statsCard, buttonBox);

        Scene scene = new Scene(root, 460, 560);
        stage.setTitle("Kinetic Fitness - My Profile");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildDetailsCard() {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f4f4f6; -fx-background-radius: 8px; -fx-padding: 16px;");

        Label cardTitle = new Label("Personal Details");
        cardTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(8);

        grid.add(new Label("Username:"), 0, 0);
        Label nameVal = new Label(user.getUsername());
        nameVal.setStyle("-fx-font-weight: bold;");
        grid.add(nameVal, 1, 0);

        grid.add(new Label("Fitness Level:"), 0, 1);
        Label levelVal = new Label(formatFitnessLevel(user.getFitnessLevel()));
        levelVal.setStyle("-fx-font-weight: bold;");
        grid.add(levelVal, 1, 1);

        grid.add(new Label("Age:"), 0, 2);
        grid.add(new Label(user.getAge() > 0 ? user.getAge() + " years" : "Not set"), 1, 2);

        grid.add(new Label("Height & Weight:"), 0, 3);
        String hwText = String.format("%.0f cm  /  %.1f kg", user.getHeightCm(), user.getWeightKg());
        grid.add(new Label(hwText), 1, 3);

        grid.add(new Label("Body Mass Index (BMI):"), 0, 4);
        Label bmiVal = new Label(String.format("%.1f  (%s)", user.getBmi(), user.getBmiCategory()));
        bmiVal.setStyle("-fx-font-weight: bold;");
        grid.add(bmiVal, 1, 4);

        card.getChildren().addAll(cardTitle, grid);
        return card;
    }

    private VBox buildStatsCard() {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f4f4f6; -fx-background-radius: 8px; -fx-padding: 16px;");

        Label cardTitle = new Label("Activity & Goals Summary");
        cardTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(8);

        grid.add(new Label("Workouts Logged:"), 0, 0);
        Label workoutsCount = new Label(String.valueOf(user.getWorkouts().size()));
        workoutsCount.setStyle("-fx-font-weight: bold;");
        grid.add(workoutsCount, 1, 0);

        grid.add(new Label("Total Reps Completed:"), 0, 1);
        Label repsCount = new Label(String.valueOf(user.getTotalRepsAllWorkouts()));
        repsCount.setStyle("-fx-font-weight: bold;");
        grid.add(repsCount, 1, 1);

        grid.add(new Label("Goals Achieved:"), 0, 2);
        String goalsText = String.format("%d / %d achieved",
                user.getAchievedGoalsCount(), user.getGoals().size());
        Label goalsLabel = new Label(goalsText);
        goalsLabel.setStyle("-fx-font-weight: bold;");
        grid.add(goalsLabel, 1, 2);

        card.getChildren().addAll(cardTitle, grid);
        return card;
    }

    private String formatFitnessLevel(FitnessLevel level) {
        if (level == null) return "Beginner";
        String name = level.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
