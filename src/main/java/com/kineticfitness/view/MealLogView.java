package com.kineticfitness.view;

import com.kineticfitness.model.DailyLog;
import com.kineticfitness.model.Meal;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.time.LocalDate;

public class MealLogView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;";

    private final DailyLog dailyLog = new DailyLog(LocalDate.now());
    private final VBox mealListBox = new VBox(6);
    private final Label totalsLabel = new Label();

    @Override
    public String label() {
        return "Meal Log";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Log a Meal");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label("Track your meals, protein, and macros for today.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        TextField nameField = new TextField();
        nameField.setPromptText("Meal name");
        TextField caloriesField = new TextField();
        caloriesField.setPromptText("Calories");
        TextField proteinField = new TextField();
        proteinField.setPromptText("Protein (g)");
        TextField carbsField = new TextField();
        carbsField.setPromptText("Carbs (g)");
        TextField fatsField = new TextField();
        fatsField.setPromptText("Fats (g)");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");

        Button addButton = new Button("Add Meal");
        addButton.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-background-radius: 6; -fx-padding: 8 20;");

        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                int calories = Integer.parseInt(caloriesField.getText().trim());
                double protein = Double.parseDouble(proteinField.getText().trim());
                double carbs = Double.parseDouble(carbsField.getText().trim());
                double fats = Double.parseDouble(fatsField.getText().trim());

                if (name.isEmpty()) {
                    errorLabel.setText("Meal name can't be empty.");
                    return;
                }

                Meal meal = new Meal(name, calories, protein, carbs, fats);
                dailyLog.addMeal(meal);
                errorLabel.setText("");
                nameField.clear();
                caloriesField.clear();
                proteinField.clear();
                carbsField.clear();
                fatsField.clear();
                refreshMealList();
            } catch (NumberFormatException ex) {
                errorLabel.setText("Calories/Protein/Carbs/Fats must be numbers.");
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Meal name"), nameField);
        form.addRow(1, new Label("Calories"), caloriesField);
        form.addRow(2, new Label("Protein (g)"), proteinField);
        form.addRow(3, new Label("Carbs (g)"), carbsField);
        form.addRow(4, new Label("Fats (g)"), fatsField);

        VBox formCard = new VBox(10, form, addButton, errorLabel);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(CARD);

        mealListBox.setPadding(new Insets(10));
        VBox listCard = new VBox(8, new Label("Today's Meals"), mealListBox, totalsLabel);
        listCard.setPadding(new Insets(20));
        listCard.setStyle(CARD);

        refreshMealList();

        VBox content = new VBox(16, title, subtitle, formCard, listCard);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        return content;
    }

    private void refreshMealList() {
        mealListBox.getChildren().clear();
        for (Meal meal : dailyLog.getMeals()) {
            HBox row = new HBox(20,
                    new Label(meal.getName()),
                    new Label(meal.getCalories() + " kcal"));
            mealListBox.getChildren().add(row);
        }
        totalsLabel.setText("Total: " + dailyLog.getTotalCalories() + " kcal, "
                + dailyLog.getTotalProtein() + "g protein");
    }
}