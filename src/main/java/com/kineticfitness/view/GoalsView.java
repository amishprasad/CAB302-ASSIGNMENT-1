package com.kineticfitness.view;

import com.kineticfitness.db.ProfileDAO;
import com.kineticfitness.model.FitnessGoal;
import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.GoalType;
import com.kineticfitness.service.GoalValidator;
import com.kineticfitness.service.ProfileGoalMapper;
import com.kineticfitness.service.ValidationResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;


public class GoalsView implements Page {

    private static final String ACCENT = "#f97316";
    private static final String ACCENT_BG = "#fff3e8";
    private static final String SELECTED_DARK_BG = "#0f172a";
    private static final String TITLE_COLOR = "#1E293B";
    private static final String MUTED_COLOR = "#64748B";
    private static final String PAGE_BG = "#F1F5F9";
    private static final String DANGER = "#dc2626";
    private static final String DANGER_BG = "#fff1f0";
    private static final String SUCCESS = "#16a34a";
    private static final String SUCCESS_BG = "#dcfce7";

    private static final String[] WORKOUT_TYPES = {"Strength training", "Cardio", "Walking", "Yoga or mobility"};
    private static final String[] WORKOUT_DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private final StackPane container = new StackPane();
    private final LocalProfileStore store = LocalProfileStore.getInstance();
    private final ProfileDAO profileDAO = new ProfileDAO();

    /** Persists the shared store against the signed-in account. */
    private void saveProfile() {
        com.kineticfitness.model.User user = com.kineticfitness.session.UserSession.getCurrentUser();
        if (user == null) return;
        profileDAO.save(store, user.getUsername());
    }

    @Override
    public String label() {
        return "Goals";
    }

    @Override
    public Node getContent() {
        refresh();
        return container;
    }

    private void refresh() {
        if (!store.hasPersonalDetails()) {
            container.getChildren().setAll(buildNoProfilePrompt());
            return;
        }
        if (!store.hasGoals()) {
            container.getChildren().setAll(buildFormView(false));
            return;
        }
        checkAchievement();
        container.getChildren().setAll(buildSummaryView(store.isGoalAchieved()));
    }

    private Node buildNoProfilePrompt() {
        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");
        Label title = new Label("Goals");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Label prompt = new Label("Create your profile first, then come back here to set a goal.");
        prompt.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MUTED_COLOR + ";");
        page.getChildren().addAll(title, prompt);
        return page;
    }

    // ---------- Form (create or edit the current goals) ----------

    private Node buildFormView(boolean hasGoals) {
        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        HBox headerRow = new HBox();
        VBox titleBlock = new VBox(2);
        Label title = new Label("Set a New Goal");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Label subtitle = new Label(hasGoals
                ? "Change anything below and save to replace your current goal."
                : "Tell us what you want to achieve. We'll create a plan to help you get there.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MUTED_COLOR + ";");
        titleBlock.getChildren().addAll(title, subtitle);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        headerRow.getChildren().addAll(titleBlock, headerSpacer);
        headerRow.setAlignment(Pos.TOP_LEFT);
        VBox.setMargin(headerRow, new Insets(0, 0, 20, 0));

        VBox card = new VBox(22);
        card.setMaxWidth(1000);
        card.setPadding(new Insets(26, 30, 26, 30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setStyle("-fx-text-fill: " + DANGER + "; -fx-font-size: 13px; -fx-font-weight: bold; "
                + "-fx-background-color: " + DANGER_BG + "; -fx-border-color: " + DANGER + "; "
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10 14;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Primary goal cards
        ToggleGroup goalGroup = new ToggleGroup();
        HBox goalRow = new HBox(12);
        for (LocalProfileStore.PrimaryGoal type : LocalProfileStore.PrimaryGoal.values()) {
            ToggleButton btn = new ToggleButton(type.display);
            btn.setUserData(type);
            btn.setToggleGroup(goalGroup);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(64);
            HBox.setHgrow(btn, Priority.ALWAYS);
            boolean selected = hasGoals && type == store.primaryGoal;
            styleGoalTypeCard(btn, selected);
            btn.selectedProperty().addListener((obs, was, isNow) -> styleGoalTypeCard(btn, isNow));
            if (selected) btn.setSelected(true);
            goalRow.getChildren().add(btn);
        }
        VBox goalTypeBlock = sectionBlock("Goal Type", "What is your primary goal?", goalRow);

        // Target weight + weekly exercise duration
        TextField targetWeightField = new TextField(
                store.targetWeightKg > 0 ? String.valueOf(store.targetWeightKg) : "");
        targetWeightField.setPromptText("e.g. 70");
        targetWeightField.setPrefHeight(38);
        VBox targetWeightBlock = sectionBlock("Target Weight", "Your goal weight (kg)", targetWeightField);

        ComboBox<Integer> durationBox = new ComboBox<>();
        for (int mins : new int[]{60, 90, 120, 150, 180, 240, 300}) durationBox.getItems().add(mins);
        int storedDuration = store.weeklyExerciseDurationMinutes;
        boolean hasStoredDuration = hasGoals && storedDuration > 0;
        if (hasStoredDuration && !durationBox.getItems().contains(storedDuration)) {
            durationBox.getItems().add(storedDuration);
        }
        durationBox.setValue(hasStoredDuration ? storedDuration : null);
        durationBox.setPromptText("Choose an option");
        durationBox.setMaxWidth(Double.MAX_VALUE);
        durationBox.setPrefHeight(38);
        durationBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer n) { return n == null ? "" : n + " minutes"; }
            @Override public Integer fromString(String s) { return durationBox.getValue(); }
        });
        VBox durationBlock = sectionBlock("Weekly Exercise Duration", "Total time per week", durationBox);

        HBox row1 = new HBox(24, targetWeightBlock, durationBlock);
        HBox.setHgrow(targetWeightBlock, Priority.ALWAYS);
        HBox.setHgrow(durationBlock, Priority.ALWAYS);

        // Weekly workout goal
        ComboBox<Integer> weeklyWorkoutBox = new ComboBox<>();
        for (int i = 1; i <= 7; i++) weeklyWorkoutBox.getItems().add(i);
        // Nothing is pre-selected for a new goal. A number the user never chose
        // is a number they will not notice is wrong, so the form asks instead of
        // guessing and GoalValidator refuses to save until they answer.
        weeklyWorkoutBox.setValue(hasGoals && store.weeklyWorkoutGoal > 0
                ? Math.max(1, Math.min(7, store.weeklyWorkoutGoal))
                : null);
        weeklyWorkoutBox.setPromptText("Choose an option");
        weeklyWorkoutBox.setMaxWidth(Double.MAX_VALUE);
        weeklyWorkoutBox.setPrefHeight(38);
        weeklyWorkoutBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer n) { return n == null ? "" : n + " workouts per week"; }
            @Override public Integer fromString(String s) { return weeklyWorkoutBox.getValue(); }
        });
        VBox weeklyWorkoutBlock = sectionBlock("Weekly Workout Goal", "How many workouts per week?", weeklyWorkoutBox);

        DatePicker targetDatePicker = new DatePicker();
        targetDatePicker.setValue(hasGoals ? store.goalTargetDate : null);
        targetDatePicker.setPromptText("Choose a date");
        targetDatePicker.setMaxWidth(Double.MAX_VALUE);
        targetDatePicker.setPrefHeight(38);
        VBox targetDateBlock = sectionBlock("Target Date", "When do you want to reach this goal?", targetDatePicker);

        HBox row2 = new HBox(24, weeklyWorkoutBlock, targetDateBlock);
        HBox.setHgrow(weeklyWorkoutBlock, Priority.ALWAYS);
        HBox.setHgrow(targetDateBlock, Priority.ALWAYS);

        // Preferred workout types
        HBox typesRow = new HBox(10);
        for (String option : WORKOUT_TYPES) {
            CheckBox cb = new CheckBox(option);
            cb.setSelected(store.preferredWorkoutTypes.contains(option));
            styleCheckBox(cb);
            typesRow.getChildren().add(cb);
        }
        VBox typesBlock = sectionBlock("Preferred Workout Types", "Select all that apply", typesRow);

        // Preferred workout days
        HBox daysRow = new HBox(10);
        for (String day : WORKOUT_DAYS) {
            CheckBox cb = new CheckBox(day);
            cb.setSelected(store.preferredWorkoutDays.contains(day));
            styleCheckBox(cb);
            daysRow.getChildren().add(cb);
        }
        VBox daysBlock = sectionBlock("Preferred Workout Days", "Select your preferred days", daysRow);

        // Experience level: chosen once when the profile is created and shown here
        // read-only, so a goal can never disagree with the profile it belongs to.
        Label experienceValue = new Label(profileLevel().display());
        experienceValue.setPrefHeight(44);
        experienceValue.setMaxWidth(Double.MAX_VALUE);
        experienceValue.setAlignment(Pos.CENTER_LEFT);
        experienceValue.setPadding(new Insets(0, 16, 0, 16));
        experienceValue.setStyle("-fx-background-color: " + ACCENT_BG + "; -fx-text-fill: " + ACCENT + "; "
                + "-fx-border-color: " + ACCENT + "; -fx-border-width: 2; -fx-border-radius: 6; "
                + "-fx-background-radius: 6; -fx-font-size: 14px; -fx-font-weight: bold;");
        HBox experienceRow = new HBox(experienceValue);
        HBox.setHgrow(experienceValue, Priority.ALWAYS);
        VBox experienceBlock = sectionBlock("Experience Level",
                "Set when you created your profile \u2014 change it from the Profile page",
                experienceRow);

        Separator divider = new Separator();
        VBox.setMargin(divider, new Insets(6, 0, 0, 0));

        Button cancelButton = new Button("← Cancel");
        cancelButton.setPrefHeight(40);
        cancelButton.setPrefWidth(120);
        cancelButton.setStyle("-fx-background-color: " + PAGE_BG + "; -fx-text-fill: " + TITLE_COLOR + ";");
        cancelButton.setOnAction(e -> refresh());

        Button saveButton = new Button("Save Goal");
        saveButton.setPrefHeight(40);
        saveButton.setPrefWidth(140);
        saveButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> {
            // The view only gathers what the user picked. The rules live in
            // GoalValidator, and the goal itself is a FitnessGoal, not loose fields.
            GoalType type = selectedGoalType(goalGroup);
            FitnessLevel experience = profileLevel();
            LocalDate startDate = store.goalStartDate != null ? store.goalStartDate : LocalDate.now();

            ValidationResult result = GoalValidator.validate(
                    type,
                    targetWeightField.getText(),
                    durationBox.getValue(),
                    weeklyWorkoutBox.getValue(),
                    targetDatePicker.getValue(),
                    experience,
                    hasSelection(typesRow),
                    hasSelection(daysRow),
                    startDate);

            if (result.isInvalid()) {
                errorLabel.setText(result.message());
                errorLabel.setManaged(true);
                errorLabel.setVisible(true);
                return;
            }

            FitnessGoal goal = new FitnessGoal(
                    type,
                    GoalValidator.parseTargetWeight(targetWeightField.getText()).getAsDouble(),
                    weeklyWorkoutBox.getValue(),
                    durationBox.getValue(),
                    experience,
                    selectedLabels(typesRow),
                    selectedLabels(daysRow),
                    startDate,
                    targetDatePicker.getValue(),
                    null);

            ProfileGoalMapper.apply(goal, store);
            saveProfile();

            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            refresh();
        });

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        HBox buttonRow = new HBox(12, cancelButton, buttonSpacer, saveButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(goalTypeBlock, row1, row2, typesBlock, daysBlock,
                experienceBlock, divider, errorLabel, buttonRow);

        ScrollPane scroller = new ScrollPane(page);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background-color: transparent; -fx-background: " + PAGE_BG + ";");

        page.getChildren().addAll(headerRow, card);
        return scroller;
    }

    // ---------- Summary (shown once a goal is saved) ----------

    private Node buildSummaryView(boolean achieved) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        Label heading = new Label("My Goals");
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        page.getChildren().addAll(heading,
                achieved ? buildAchievedHero() : buildHeroCard(),
                buildColumns(achieved));
        if (achieved) {
            page.getChildren().add(buildQuoteBanner());
        }

        ScrollPane scroller = new ScrollPane(page);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background-color: transparent; -fx-background: " + PAGE_BG + ";");
        return scroller;
    }

    private HBox buildHeroCard() {
        Label icon = new Label("🎯");
        icon.setStyle("-fx-font-size: 26px; -fx-background-color: " + ACCENT_BG
                + "; -fx-background-radius: 12; -fx-padding: 12 14;");

        Label name = new Label(store.primaryGoal.display);
        name.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label blurb = new Label(goalBlurb());
        blurb.setWrapText(true);
        blurb.setStyle("-fx-font-size: 13px; -fx-text-fill: " + MUTED_COLOR + ";");

        VBox text = new VBox(4, name, blurb);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label active = new Label("● Active");
        active.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + SUCCESS
                + "; -fx-background-color: " + SUCCESS_BG + "; -fx-background-radius: 20; -fx-padding: 6 14;");

        HBox card = new HBox(18, icon, text, active);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(22, 26, 22, 26));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");
        return card;
    }

    private HBox buildAchievedHero() {
        Label tick = new Label("✔");
        tick.setStyle("-fx-font-size: 30px; -fx-text-fill: white; -fx-background-color: " + SUCCESS
                + "; -fx-background-radius: 40; -fx-padding: 14 20;");

        Label title = new Label("Goal Achieved!");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label when = new Label("You reached your goal on " + formatDate(store.goalAchievedDate));
        when.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + SUCCESS + ";");

        Label blurb = new Label("Well done! You have successfully achieved your goal. Keep up your healthy "
                + "habits and set a new goal to continue your fitness journey!");
        blurb.setWrapText(true);
        blurb.setStyle("-fx-font-size: 13px; -fx-text-fill: " + MUTED_COLOR + ";");

        VBox text = new VBox(6, title, when);
        if (store.goalTargetDate != null) {
            Label margin = new Label(achievedMarginText());
            margin.setWrapText(true);
            margin.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
            text.getChildren().add(margin);
        }
        text.getChildren().add(blurb);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label slogan = new Label("Stronger\nHealthier\nHappier You!");
        slogan.setStyle("-fx-font-size: 16px; -fx-font-style: italic; -fx-font-weight: bold; "
                + "-fx-text-fill: " + SUCCESS + ";");

        HBox card = new HBox(20, tick, text, slogan);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(26, 30, 26, 30));
        card.setStyle("-fx-background-color: " + SUCCESS_BG + "; -fx-background-radius: 12; "
                + "-fx-border-color: #bbf7d0; -fx-border-radius: 12;");
        return card;
    }

    private HBox buildColumns(boolean achieved) {
        VBox details = new VBox(0);
        details.setPadding(new Insets(22, 26, 22, 26));
        details.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label detailsTitle = new Label("Goal Details");
        detailsTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        HBox titleRow = new HBox(10, detailsTitle, titleSpacer);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        if (achieved) {
            Label completed = new Label("Completed");
            completed.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + SUCCESS
                    + "; -fx-background-color: " + SUCCESS_BG + "; -fx-background-radius: 20; -fx-padding: 5 14;");
            titleRow.getChildren().add(completed);
        }
        VBox.setMargin(titleRow, new Insets(0, 0, 14, 0));
        details.getChildren().add(titleRow);

        details.getChildren().addAll(
                detailRow("Goal Type", store.primaryGoal.display),
                detailRow("Target Weight", formatWeight(store.targetWeightKg)));

        if (store.goalStartDate != null && store.goalTargetDate != null) {
            details.getChildren().addAll(
                    detailRow("Start Date", formatDate(store.goalStartDate)),
                    detailRow("Target Date", formatDate(store.goalTargetDate)),
                    detailRow("Duration", durationText(store.goalStartDate, store.goalTargetDate)));
        }

        details.getChildren().addAll(
                detailRow("Weekly Exercise Duration", store.weeklyExerciseDurationMinutes + " minutes/week"),
                detailRow("Weekly Workout Goal", store.weeklyWorkoutGoal + " workouts per week"),
                detailRow("Preferred Workout Types", joinOrDash(store.preferredWorkoutTypes)),
                detailRow("Preferred Workout Days", joinOrDash(store.preferredWorkoutDays)),
                detailRow("Experience Level", profileLevel().display()));

        HBox.setHgrow(details, Priority.ALWAYS);

        VBox side = new VBox(20);
        side.getChildren().add(buildTimelineCard(achieved));
        side.getChildren().add(achieved ? buildAchievementSummary() : buildMotivationCard());
        side.getChildren().add(buildActionButtons(achieved));
        side.setPrefWidth(360);
        side.setMinWidth(320);
        side.setMaxWidth(400);

        HBox columns = new HBox(20, details, side);
        columns.setAlignment(Pos.TOP_LEFT);
        return columns;
    }

    private VBox buildAchievementSummary() {
        Label title = new Label("📊 Achievement Summary");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        HBox stats = new HBox(12,
                statTile("🏋", formatWeight(store.targetWeightKg), "Target Weight"),
                statTile("📅", String.valueOf(store.weeklyWorkoutGoal), "Workouts per Week"),
                statTile("⏱", store.weeklyExerciseDurationMinutes + " mins", "per Week"));
        stats.setAlignment(Pos.CENTER);

        Label done = new Label("🏆  You did it!");
        done.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + SUCCESS + ";");

        Label doneText = new Label("You reached the target you set and completed your goal. "
                + "Your consistency and hard work have paid off. Keep up the great work!");
        doneText.setWrapText(true);
        doneText.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");

        VBox note = new VBox(4, done, doneText);
        note.setMaxWidth(Double.MAX_VALUE);
        note.setPadding(new Insets(14, 16, 14, 16));
        note.setStyle("-fx-background-color: " + SUCCESS_BG + "; -fx-background-radius: 8;");

        VBox card = new VBox(16, title, stats, note);
        card.setPadding(new Insets(22, 26, 22, 26));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");
        return card;
    }

    private VBox statTile(String icon, String value, String caption) {
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Label captionLabel = new Label(caption);
        captionLabel.setWrapText(true);
        captionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + MUTED_COLOR + ";");

        VBox tile = new VBox(3, iconLabel, valueLabel, captionLabel);
        tile.setAlignment(Pos.CENTER);
        tile.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tile, Priority.ALWAYS);
        return tile;
    }

    private VBox buildMotivationCard() {
        Label title = new Label("💡 Motivation");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label quote = new Label(motivationQuote());
        quote.setWrapText(true);
        quote.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: " + TITLE_COLOR
                + "; -fx-background-color: " + ACCENT_BG + "; -fx-background-radius: 8; -fx-padding: 18;");
        quote.setMaxWidth(Double.MAX_VALUE);

        VBox card = new VBox(14, title, quote);
        card.setPadding(new Insets(22, 26, 22, 26));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");
        return card;
    }

    private VBox buildQuoteBanner() {
        Label quote = new Label("“Discipline today leads to a healthier tomorrow.”");
        quote.setWrapText(true);
        quote.setStyle("-fx-font-size: 17px; -fx-font-style: italic; -fx-text-fill: " + TITLE_COLOR + ";");

        Label attribution = new Label("– Kinetic Fitness");
        attribution.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");

        VBox banner = new VBox(6, quote, attribution);
        banner.setAlignment(Pos.CENTER);
        banner.setMaxWidth(Double.MAX_VALUE);
        banner.setPadding(new Insets(28, 30, 28, 30));
        banner.setStyle("-fx-background-color: #eef4f8; -fx-background-radius: 12;");
        return banner;
    }

    private Node buildActionButtons(boolean achieved) {
        Button edit = new Button("✎  Edit Goal");
        edit.setPrefHeight(44);
        edit.setMaxWidth(Double.MAX_VALUE);
        edit.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE_COLOR + "; -fx-font-weight: bold; "
                + "-fx-font-size: 14px; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8;");
        edit.setOnAction(e -> container.getChildren().setAll(buildFormView(true)));
        HBox.setHgrow(edit, Priority.ALWAYS);

        if (achieved) {
            Button newGoal = new Button("+  Set a New Goal");
            newGoal.setPrefHeight(44);
            newGoal.setMaxWidth(Double.MAX_VALUE);
            newGoal.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; "
                    + "-fx-font-size: 14px; -fx-background-radius: 8;");
            newGoal.setOnAction(e -> {
                clearGoal();
                refresh();
            });
            HBox.setHgrow(newGoal, Priority.ALWAYS);
            return new HBox(12, edit, newGoal);
        }

        Button delete = new Button("🗑  Delete Goal");
        delete.setPrefHeight(44);
        delete.setMaxWidth(Double.MAX_VALUE);
        delete.setStyle("-fx-background-color: white; -fx-text-fill: " + DANGER + "; -fx-font-weight: bold; "
                + "-fx-font-size: 14px; -fx-border-color: " + DANGER + "; -fx-border-radius: 8; "
                + "-fx-background-radius: 8;");
        delete.setOnAction(e -> {
            clearGoal();
            refresh();
        });
        HBox.setHgrow(delete, Priority.ALWAYS);

        Button markDone = new Button("✔  Mark as Achieved");
        markDone.setPrefHeight(40);
        markDone.setMaxWidth(Double.MAX_VALUE);
        markDone.setStyle("-fx-background-color: " + SUCCESS_BG + "; -fx-text-fill: " + SUCCESS
                + "; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8;");
        markDone.setOnAction(e -> {
            FitnessGoal goal = currentGoal();
            if (goal == null || goal.isAchieved()) {
                return;
            }
            ProfileGoalMapper.apply(goal.achievedOn(LocalDate.now()), store);
            saveProfile();
            refresh();
        });

        return new VBox(10, new HBox(12, edit, delete), markDone);
    }


    private void clearGoal() {
        ProfileGoalMapper.clearGoal(store);
        saveProfile();
    }

    /** The saved goal as a domain object, or null when none is set. */
    private FitnessGoal currentGoal() {
        return ProfileGoalMapper.toGoal(store).orElse(null);
    }

    private GoalType selectedGoalType(ToggleGroup group) {
        Toggle picked = group.getSelectedToggle();
        return picked == null ? null
                : GoalType.valueOf(((LocalProfileStore.PrimaryGoal) picked.getUserData()).name());
    }

    /**
     * The fitness level the user chose on their profile. The goals screen displays
     * it but never changes it, so there is only ever one answer to "how experienced
     * am I" and the profile owns it.
     */
    private FitnessLevel profileLevel() {
        return store.fitnessLevel == null
                ? FitnessLevel.BEGINNER
                : FitnessLevel.valueOf(store.fitnessLevel.name());
    }

    private boolean hasSelection(HBox row) {
        return row.getChildren().stream().anyMatch(n -> n instanceof CheckBox cb && cb.isSelected());
    }

    private java.util.Set<String> selectedLabels(HBox row) {
        java.util.Set<String> picked = new java.util.LinkedHashSet<>();
        for (Node n : row.getChildren()) {
            if (n instanceof CheckBox cb && cb.isSelected()) {
                picked.add(cb.getText());
            }
        }
        return picked;
    }

    /**
     * Completes the goal if the profile weight has reached its target. The rule
     * for "reached" belongs to {@link GoalType}, so this only persists the result.
     */
    private void checkAchievement() {
        if (ProfileGoalMapper.completeIfReached(store, LocalDate.now())) {
            saveProfile();
        }
    }

    private VBox buildTimelineCard(boolean achieved) {
        Label title = new Label("📅 Goal Timeline");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        VBox card = new VBox(16, title);
        card.setPadding(new Insets(22, 26, 22, 26));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        if (store.goalStartDate == null || store.goalTargetDate == null) {
            Label none = new Label("No timeline set. Use Edit Goal to add a target date.");
            none.setWrapText(true);
            none.setStyle("-fx-font-size: 13px; -fx-text-fill: " + MUTED_COLOR + ";");
            card.getChildren().add(none);
            return card;
        }

        String barColor = achieved ? SUCCESS : ACCENT;
        String barBg = achieved ? SUCCESS_BG : ACCENT_BG;

        Region track = new Region();
        track.setPrefHeight(6);
        track.setMinHeight(6);
        track.setMaxWidth(Double.MAX_VALUE);
        track.setStyle("-fx-background-color: " + barBg + "; -fx-background-radius: 3;");

        Label pill = new Label(achieved ? "Completed" : durationText(store.goalStartDate, store.goalTargetDate));
        pill.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + barColor
                + "; -fx-background-color: " + barBg + "; -fx-background-radius: 20; -fx-padding: 5 14;");

        StackPane bar = new StackPane(track, pill);
        bar.setPrefHeight(30);

        VBox startBox = new VBox(2, boldDate(store.goalStartDate), mutedCaption("Start Date"));
        VBox endBox = new VBox(2, boldDate(store.goalTargetDate), mutedCaption("Target Date"));
        endBox.setAlignment(Pos.CENTER_RIGHT);

        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox dates = new HBox(12, startBox, gap, endBox);
        dates.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(bar, dates);

        if (!achieved) {
            Label remaining = new Label(remainingText());
            remaining.setWrapText(true);
            remaining.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

            Label encouragement = new Label(
                    java.time.LocalDate.now().isAfter(store.goalTargetDate)
                            ? "Update your weight, or edit the goal to set a new date."
                            : "Stay consistent and keep going!");
            encouragement.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");

            VBox note = new VBox(2, remaining, encouragement);
            note.setMaxWidth(Double.MAX_VALUE);
            note.setPadding(new Insets(14, 16, 14, 16));
            note.setStyle("-fx-background-color: " + ACCENT_BG + "; -fx-background-radius: 8;");
            card.getChildren().add(note);
        }
        return card;
    }

    private Label boldDate(java.time.LocalDate date) {
        Label label = new Label(formatDate(date));
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        return label;
    }

    private Label mutedCaption(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        return label;
    }

    private HBox detailRow(String label, String value) {
        Label key = new Label(label);
        key.setMinWidth(210);
        key.setPrefWidth(210);
        key.setStyle("-fx-font-size: 13px; -fx-text-fill: " + MUTED_COLOR + ";");

        Label val = new Label(value);
        val.setWrapText(true);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TITLE_COLOR + ";");
        HBox.setHgrow(val, Priority.ALWAYS);

        HBox row = new HBox(12, key, val);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 0, 13, 0));
        row.setStyle("-fx-border-color: #f1f5f9 transparent transparent transparent; -fx-border-width: 1 0 0 0;");
        return row;
    }

    private String joinOrDash(java.util.Set<String> values) {
        return values.isEmpty() ? "—" : String.join(", ", values);
    }

    private String formatWeight(double kg) {
        if (kg <= 0) return "—";
        return (kg == Math.rint(kg) ? String.valueOf((long) kg) : String.format("%.1f", kg)) + " kg";
    }

    private String formatDate(java.time.LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }

    private String durationText(java.time.LocalDate from, java.time.LocalDate to) {
        Period p = Period.between(from, to);
        int years = p.getYears();
        int months = p.getMonths();
        if (years > 0) {
            String text = plural(years, "year");
            return months > 0 ? text + " " + plural(months, "month") : text;
        }
        if (months > 0) return plural(months, "month");
        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to);
        if (days >= 14) return plural((int) (days / 7), "week");
        return plural((int) days, "day");
    }

    private String remainingText() {
        FitnessGoal goal = currentGoal();
        if (goal == null) return "";
        long days = goal.daysRemaining(LocalDate.now());
        if (days == 0) {
            return "Today is your target date \u2014 " + formatDate(store.goalTargetDate) + ".";
        }
        if (days < 0) {
            return "Your target date passed " + plural((int) -days, "day") + " ago.";
        }
        return "You have " + plural((int) days, "day") + " left, until "
                + formatDate(store.goalTargetDate) + ".";
    }

    /** How the achieved date landed against the target date. */
    private String achievedMarginText() {
        FitnessGoal goal = currentGoal();
        if (goal == null || !goal.isAchieved()) return "";
        long days = goal.daysAheadOfTarget();
        String target = formatDate(goal.getTargetDate());
        if (days > 0) {
            return plural((int) days, "day") + " ahead of your target date of " + target + ".";
        }
        if (days == 0) {
            return "Right on your target date of " + target + ".";
        }
        return plural((int) -days, "day") + " after your target date of " + target + ".";
    }

    private String plural(int n, String unit) {
        return n + " " + unit + (n == 1 ? "" : "s");
    }

    /** The goal type currently stored, or null when no goal is set. */
    private GoalType goalType() {
        return store.primaryGoal == null ? null : GoalType.valueOf(store.primaryGoal.name());
    }

    private String goalBlurb() {
        GoalType type = goalType();
        return type == null ? "" : type.description();
    }

    private String motivationQuote() {
        GoalType type = goalType();
        return type == null ? "" : type.motivation();
    }

    private VBox sectionBlock(String title, String helpText, Node content) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Label helpLabel = new Label(helpText);
        helpLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        VBox box = new VBox(2, titleLabel, helpLabel, content);
        VBox.setMargin(content, new Insets(8, 0, 0, 0));
        return box;
    }

    private void styleGoalTypeCard(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: " + SELECTED_DARK_BG + "; -fx-text-fill: white; "
                    + "-fx-border-color: " + ACCENT + "; -fx-border-width: 2; -fx-border-radius: 6; "
                    + "-fx-background-radius: 6; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE_COLOR + "; "
                    + "-fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        }
    }

    private void styleCheckBox(CheckBox cb) {
        cb.setStyle(checkBoxStyle(cb.isSelected()));
        cb.selectedProperty().addListener((o, was, isNow) -> cb.setStyle(checkBoxStyle(isNow)));
    }

    private String checkBoxStyle(boolean selected) {
        if (selected) {
            return "-fx-padding: 8 14; -fx-border-color: " + ACCENT + "; -fx-border-radius: 6; "
                    + "-fx-background-color: " + ACCENT_BG + "; -fx-background-radius: 6; -fx-text-fill: " + ACCENT + ";";
        }
        return "-fx-padding: 8 14; -fx-border-color: #e2e8f0; -fx-border-radius: 6; "
                + "-fx-background-color: white; -fx-background-radius: 6;";
    }
}
