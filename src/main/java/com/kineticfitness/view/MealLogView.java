package com.kineticfitness.view;

import com.kineticfitness.model.DailyLog;
import com.kineticfitness.model.LookupNutritionEstimator;
import com.kineticfitness.model.Meal;
import com.kineticfitness.model.MealValidator;
import com.kineticfitness.model.NutritionEstimator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Optional;

public class MealLogView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;";

    private final NutritionEstimator estimator;
    private final DailyLog dailyLog = new DailyLog(LocalDate.now());
    private final VBox mealListBox = new VBox(6);
    private final Label totalsLabel = new Label();

    public MealLogView() {
        this(new LookupNutritionEstimator());
    }

    public MealLogView(NutritionEstimator estimator) {
        this.estimator = estimator;
    }

    @Override
    public String label() {
        return "Meal Log";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Log a Meal");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label("Type a meal name and the calories and macros are filled in for you. "
                + "Edit them if you like, then press Enter or Add Meal.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        TextField nameField = new TextField();
        nameField.setPromptText("Meal name");
        InputAssistance.suggest(nameField, () -> {
            java.util.List<com.kineticfitness.service.InputSuggestions.Suggestion> names = new java.util.ArrayList<>();
            for (String name : java.util.List.of("Breakfast", "Lunch", "Dinner", "Snack")) {
                names.add(new com.kineticfitness.service.InputSuggestions.Suggestion(name, ""));
            }
            dailyLog.getMeals().forEach(meal -> names.add(
                    new com.kineticfitness.service.InputSuggestions.Suggestion(meal.getName(), "")));
            return names;
        });
        TextField caloriesField = new TextField();
        caloriesField.setPromptText("Calories");
        TextField proteinField = new TextField();
        proteinField.setPromptText("Protein (g)");
        TextField carbsField = new TextField();
        carbsField.setPromptText("Carbs (g)");
        TextField fatsField = new TextField();
        fatsField.setPromptText("Fats (g)");
        TextField[] valueFields = {caloriesField, proteinField, carbsField, fatsField};

        Label hintLabel = new Label();
        hintLabel.setStyle("-fx-text-fill: " + SUBTITLE + "; -fx-font-size: 12px;");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");

        Button addButton = new Button("Add Meal");
        addButton.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-background-radius: 6; -fx-padding: 8 20;");
        addButton.setDisable(true);

        // filling = we are setting the fields from code; autoFilled = the values came from an estimate
        boolean[] filling = {false};
        boolean[] autoFilled = {false};

        Runnable clearValues = () -> {
            filling[0] = true;
            for (TextField field : valueFields) {
                field.clear();
            }
            filling[0] = false;
            autoFilled[0] = false;
        };

        // Enable "Add Meal" only when the input is valid; show a message once the user has started typing numbers.
        Runnable updateAddState = () -> {
            Optional<String> problem = MealValidator.validate(nameField.getText(), caloriesField.getText(),
                    proteinField.getText(), carbsField.getText(), fatsField.getText());
            addButton.setDisable(problem.isPresent());
            boolean startedValues = false;
            for (TextField field : valueFields) {
                startedValues |= !field.getText().isBlank();
            }
            errorLabel.setText(problem.isPresent() && startedValues ? problem.get() : "");
        };

        // Look up an estimate for the typed name and fill the fields automatically.
        Runnable autoEstimate = () -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                hintLabel.setText("");
                return;
            }
            Optional<Meal> estimate = estimator.estimate(name);
            if (estimate.isPresent()) {
                Meal est = estimate.get();
                filling[0] = true;
                caloriesField.setText(String.valueOf(est.getCalories()));
                proteinField.setText(String.valueOf(est.getProtein()));
                carbsField.setText(String.valueOf(est.getCarbs()));
                fatsField.setText(String.valueOf(est.getFats()));
                filling[0] = false;
                autoFilled[0] = true;
                hintLabel.setText("Estimated values filled in - edit them if needed.");
            } else {
                if (autoFilled[0]) {
                    clearValues.run(); // don't leave stale numbers from a shorter meal name
                }
                hintLabel.setText("No estimate found - enter the values yourself for a custom meal.");
            }
        };

        nameField.textProperty().addListener((obs, oldText, newText) -> {
            autoEstimate.run();
            updateAddState.run();
        });
        nameField.setOnAction(e -> caloriesField.requestFocus());

        for (TextField field : valueFields) {
            field.textProperty().addListener((obs, oldText, newText) -> {
                if (!filling[0]) {
                    autoFilled[0] = false; // the user edited a value, so keep it
                    if (hintLabel.getText().startsWith("Added")) {
                        hintLabel.setText("");
                    }
                }
                updateAddState.run();
            });
            field.setOnAction(e -> addButton.fire()); // Enter adds the meal (ignored while disabled)
        }

        addButton.setOnAction(e -> {
            if (MealValidator.validate(nameField.getText(), caloriesField.getText(), proteinField.getText(),
                    carbsField.getText(), fatsField.getText()).isPresent()) {
                return;
            }
            String name = nameField.getText().trim();
            dailyLog.addMeal(new Meal(name,
                    Integer.parseInt(caloriesField.getText().trim()),
                    Double.parseDouble(proteinField.getText().trim()),
                    Double.parseDouble(carbsField.getText().trim()),
                    Double.parseDouble(fatsField.getText().trim())));

            clearValues.run();
            nameField.clear();
            hintLabel.setText("Added \"" + name + "\".");
            nameField.requestFocus();
            refreshMealList();
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Meal name"), nameField);
        form.addRow(1, new Label("Calories"), caloriesField);
        form.addRow(2, new Label("Protein (g)"), proteinField);
        form.addRow(3, new Label("Carbs (g)"), carbsField);
        form.addRow(4, new Label("Fats (g)"), fatsField);

        VBox formCard = new VBox(10, form, hintLabel, addButton, errorLabel);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(CARD);

        mealListBox.setPadding(new Insets(10));
        Label listTitle = new Label("Today's Meals");
        listTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        totalsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        VBox listCard = new VBox(8, listTitle, mealListBox, totalsLabel);
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

        if (dailyLog.getMeals().isEmpty()) {
            Label empty = new Label("No meals logged yet.");
            empty.setStyle("-fx-text-fill: " + SUBTITLE + ";");
            mealListBox.getChildren().add(empty);
        }

        for (Meal meal : dailyLog.getMeals()) {
            Label name = new Label(meal.getName());
            name.setStyle("-fx-font-weight: bold;");
            Label details = new Label(meal.getCalories() + " kcal   P " + grams(meal.getProtein())
                    + "g   C " + grams(meal.getCarbs()) + "g   F " + grams(meal.getFats()) + "g");
            details.setStyle("-fx-text-fill: " + SUBTITLE + ";");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button remove = new Button("Remove");
            remove.setStyle("-fx-background-color: white; -fx-text-fill: #DC2626; -fx-border-color: #DC2626;"
                    + " -fx-border-radius: 6; -fx-background-radius: 6;");
            remove.setOnAction(e -> {
                dailyLog.removeMeal(meal);
                refreshMealList();
            });

            HBox row = new HBox(12, name, details, spacer, remove);
            row.setAlignment(Pos.CENTER_LEFT);
            mealListBox.getChildren().add(row);
        }

        totalsLabel.setText("Total: " + dailyLog.getTotalCalories() + " kcal, "
                + grams(dailyLog.getTotalProtein()) + "g protein");
    }

    private static String grams(double value) {
        return String.format("%.1f", value);
    }
}
