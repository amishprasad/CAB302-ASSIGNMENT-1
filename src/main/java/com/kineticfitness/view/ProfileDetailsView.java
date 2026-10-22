package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import com.kineticfitness.db.ProfileDAO;
import com.kineticfitness.db.WorkoutDAO;
import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.Gender;
import com.kineticfitness.model.Workout;
import com.kineticfitness.model.WorkoutMetric;
import com.kineticfitness.model.PersonalDetails;
import com.kineticfitness.service.PersonalDetailsMapper;
import com.kineticfitness.service.ProfileValidator;
import com.kineticfitness.service.ValidationResult;
import com.kineticfitness.util.BmiCalculator;
import com.kineticfitness.db.UserDAO;
import com.kineticfitness.session.UserSession;
import com.kineticfitness.util.WorkoutStats;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProfileDetailsView implements Page {

    private static final String ACCENT = "#f97316";
    private static final String ACCENT_BG = "rgba(249,115,22,0.08)";
    private static final String TITLE_COLOR = "#1E293B";
    private static final String MUTED_COLOR = "#64748B";
    private static final String PAGE_BG = "#F1F5F9";

    private static final java.time.format.DateTimeFormatter DATE_FORMAT =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** The week labels under the bar chart, e.g. "6 Oct". */
    private static final DateTimeFormatter WEEK_LABEL = DateTimeFormatter.ofPattern("d MMM");

    private final WorkoutDAO workoutDAO = new WorkoutDAO();


    private final StackPane container = new StackPane();
    private final LocalProfileStore store = LocalProfileStore.getInstance();
    private YearMonth currentCalendarMonth = YearMonth.now();

    @Override
    public String label() {
        return "Profile";
    }

    @Override
    public Node getContent() {
        refresh();
        return container;
    }

    private int calculatedAge() {
        return PersonalDetailsMapper.toDetails(store)
                .map(details -> details.age(LocalDate.now()))
                .orElse(0);
    }

    /** The form's current values as a validated domain object, or null if invalid. */
    private PersonalDetails readDetails(TextField nameField, TextField emailField,
                                        ComboBox<LocalProfileStore.Gender> genderBox,
                                        DatePicker dobPicker, TextField heightField,
                                        TextField weightField,
                                        ComboBox<FitnessLevel> fitnessBox,
                                        String photoPath, Label errorLabel) {

        Gender gender = genderBox.getValue() == null ? null
                : Gender.valueOf(genderBox.getValue().name());
        FitnessLevel level = fitnessBox.getValue();

        ValidationResult result = ProfileValidator.validate(
                nameField.getText(), emailField.getText(), gender, dobPicker.getValue(),
                heightField.getText(), weightField.getText(), level, LocalDate.now());

        if (result.isInvalid()) {
            errorLabel.setText(result.message());
            errorLabel.setVisible(true);
            return null;
        }

        return new PersonalDetails(
                nameField.getText(), emailField.getText(), gender, photoPath,
                dobPicker.getValue(),
                ProfileValidator.parseMeasurement(heightField.getText()).getAsDouble(),
                ProfileValidator.parseMeasurement(weightField.getText()).getAsDouble(),
                level);
    }

    private void refresh() {
        if (store.hasPersonalDetails()) {
            container.getChildren().setAll(buildDetailsView());
        } else {
            container.getChildren().setAll(buildStep1());
        }
    }

    // ---------- Read-only details view (personal details only) ----------

    private Node buildDetailsView() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        VBox headerCard = buildHeaderCard();

        VBox statsCard = buildWorkoutStatisticsCard();
        VBox calendarCard = buildCalendarCard();
        HBox bottomRow = new HBox(20, statsCard, calendarCard);
        HBox.setHgrow(statsCard, Priority.ALWAYS);
        statsCard.setMaxWidth(Double.MAX_VALUE);
        calendarCard.setPrefWidth(360);
        calendarCard.setMinWidth(360);

        page.getChildren().addAll(headerCard, bottomRow);

        // The page is taller than the window once the stats card has a chart in
        // it, so it scrolls rather than hiding whatever falls below the fold.
        ScrollPane scroller = new ScrollPane(page);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background-color: transparent; -fx-background: " + PAGE_BG + ";");
        return scroller;
    }

    /** Accent colour per metric, so the two tabs are told apart at a glance. */
    private static final java.util.Map<WorkoutMetric, String> METRIC_ACCENT = java.util.Map.of(
            WorkoutMetric.REPS, "#3b82f6",
            WorkoutMetric.SETS, "#ef4444");

    /**
     * Workout statistics for the signed-in user, read from {@link WorkoutDAO}.
     *
     * <p>The tabs are {@link WorkoutMetric}'s constants, and each one knows how to
     * read itself off a workout and how to aggregate across many. This method
     * never asks which metric is selected; it just asks the metric.</p>
     *
     * <p>There is no duration tab: a workout records its date and its exercises'
     * sets and reps, and nothing in the app stores how long it took.</p>
     */
    private VBox buildWorkoutStatisticsCard() {
        VBox card = new VBox(4);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label title = new Label("Workout Statistics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        VBox body = new VBox(16);

        ComboBox<Integer> rangeBox = new ComboBox<>();
        rangeBox.getItems().addAll(4, 12, 26);
        rangeBox.setValue(12);
        rangeBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer weeks) {
                return weeks == null ? "" : "Last " + weeks + " weeks";
            }
            @Override public Integer fromString(String text) {
                return rangeBox.getValue();
            }
        });

        ToggleGroup tabs = new ToggleGroup();
        HBox tabRow = new HBox(8);
        for (WorkoutMetric metric : WorkoutMetric.values()) {
            ToggleButton tab = new ToggleButton(metric.tabLabel());
            tab.setUserData(metric);
            tab.setToggleGroup(tabs);
            styleMetricTab(tab, metric == WorkoutMetric.values()[0]);
            tab.selectedProperty().addListener((o, was, isNow) -> styleMetricTab(tab, isNow));
            if (metric == WorkoutMetric.values()[0]) {
                tab.setSelected(true);
            }
            tabRow.getChildren().add(tab);
        }

        Runnable render = () -> {
            Toggle picked = tabs.getSelectedToggle();
            WorkoutMetric metric = picked == null
                    ? WorkoutMetric.values()[0]
                    : (WorkoutMetric) picked.getUserData();
            renderWorkoutStats(body, metric, rangeBox.getValue());
        };

        tabs.selectedToggleProperty().addListener((o, was, isNow) -> {
            if (isNow == null) {
                was.setSelected(true);
            } else {
                render.run();
            }
        });
        rangeBox.valueProperty().addListener((o, was, isNow) -> render.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox headerRow = new HBox(8, tabRow, spacer, rangeBox);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(headerRow, new Insets(10, 0, 18, 0));

        render.run();

        card.getChildren().addAll(title, headerRow, body);
        return card;
    }

    /** Fills the card's body for one metric and window. Re-run whenever either changes. */
    private void renderWorkoutStats(VBox body, WorkoutMetric metric, int weeks) {
        List<Workout> workouts = workoutsInWindow(weeks);

        if (workouts.isEmpty()) {
            body.getChildren().setAll(emptyWorkoutState());
            return;
        }

        String accent = METRIC_ACCENT.get(metric);
        String label = metric.tabLabel();

        HBox tiles = new HBox(16,
                workoutTile(String.valueOf(metric.totalAcross(workouts)),
                        "Total " + label, accent),
                workoutTile(String.valueOf(metric.averageAcross(workouts)),
                        "Average " + label + " per Workout", accent),
                workoutTile(String.valueOf(metric.highestIn(workouts)),
                        "Most " + label + " in One Workout", accent));
        for (Node tile : tiles.getChildren()) {
            HBox.setHgrow(tile, Priority.ALWAYS);
        }

        body.getChildren().setAll(
                tiles,
                weeklyWorkoutGoalBar(),
                metricPerWeekChart(workouts, metric, weeks, accent));
    }

    /** The signed-in user's workouts inside the chosen window. */
    private List<Workout> workoutsInWindow(int weeks) {
        com.kineticfitness.model.User user = UserSession.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        LocalDate firstMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(weeks - 1L);
        return workoutDAO.findAllByUsername(user.getUsername()).stream()
                .filter(w -> w.getDate() != null && !w.getDate().isBefore(firstMonday))
                .toList();
    }

    /**
     * This week's sessions against the weekly workout goal set on the Goals page.
     * Both figures are real: the count from {@link WorkoutStats#sessionsThisWeek},
     * the target from the saved goal.
     */
    private VBox weeklyWorkoutGoalBar() {
        List<LocalDate> dates = workoutsInWindow(1).stream().map(Workout::getDate).toList();
        int done = WorkoutStats.sessionsThisWeek(dates, LocalDate.now());
        int target = Math.max(1, store.weeklyWorkoutGoal);
        int percent = WorkoutStats.percentOf(done, target);

        Label heading = new Label("Weekly Workout Goal");
        heading.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label percentLabel = new Label(percent + "%");
        percentLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(heading, spacer, percentLabel);
        top.setAlignment(Pos.CENTER_LEFT);

        ProgressBar bar = new ProgressBar(Math.min(1.0, percent / 100.0));
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(12);
        bar.setStyle("-fx-accent: " + ACCENT + ";");

        int left = Math.max(0, target - done);
        Label detail = new Label(done + " of " + target + " workouts   ·   "
                + (left == 0 ? "goal met" : left + " to go"));
        detail.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        VBox box = new VBox(8, top, bar, detail);
        box.setPadding(new Insets(16, 18, 16, 18));
        box.setStyle("-fx-background-color: " + ACCENT_BG + "; -fx-background-radius: 8;");
        return box;
    }

    /** One bar per calendar week, bucketed by the team's {@link WorkoutStats}. */
    private VBox metricPerWeekChart(List<Workout> workouts, WorkoutMetric metric,
                                    int weeks, String accent) {
        List<WorkoutStats.DayVolume> days = workouts.stream()
                .map(w -> new WorkoutStats.DayVolume(w.getDate(), metric.of(w)))
                .toList();
        List<WorkoutStats.WeekBucket> buckets =
                WorkoutStats.weeklyBuckets(days, LocalDate.now(), weeks);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(metric.tabLabel());

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(260);
        chart.setCategoryGap(10);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (WorkoutStats.WeekBucket bucket : buckets) {
            series.getData().add(new XYChart.Data<>(
                    bucket.weekStart().format(WEEK_LABEL), bucket.totalReps()));
        }
        chart.getData().add(series);
        for (XYChart.Data<String, Number> point : series.getData()) {
            if (point.getNode() != null) {
                point.getNode().setStyle("-fx-bar-fill: " + accent + ";");
            }
        }

        Label heading = new Label(metric.tabLabel() + " per Week");
        heading.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        VBox box = new VBox(8, heading, chart);
        box.setPadding(new Insets(16, 12, 8, 12));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        return box;
    }

    /** One of the three figures across the top of the card. */
    private VBox workoutTile(String value, String caption, String accent) {
        Label dot = new Label("●");
        dot.setStyle("-fx-font-size: 14px; -fx-text-fill: " + accent + ";");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label captionLabel = new Label(caption);
        captionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        captionLabel.setWrapText(true);
        captionLabel.setAlignment(Pos.CENTER);

        VBox tile = new VBox(4, dot, valueLabel, captionLabel);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(18, 12, 18, 12));
        tile.setMaxWidth(Double.MAX_VALUE);
        tile.setStyle("-fx-background-color: " + PAGE_BG + "; -fx-background-radius: 10;");
        return tile;
    }

    private void styleMetricTab(ToggleButton tab, boolean selected) {
        if (selected) {
            tab.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT
                    + "; -fx-font-weight: bold; -fx-border-color: transparent transparent " + ACCENT
                    + " transparent; -fx-border-width: 0 0 2 0; -fx-background-radius: 0;");
        } else {
            tab.setStyle("-fx-background-color: transparent; -fx-text-fill: " + MUTED_COLOR
                    + "; -fx-border-color: transparent; -fx-background-radius: 0;");
        }
    }

    private VBox emptyWorkoutState() {
        VBox emptyState = new VBox(6);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPrefHeight(220);
        emptyState.setStyle("-fx-background-color: " + PAGE_BG + "; -fx-background-radius: 8; "
                + "-fx-border-color: #e2e8f0; -fx-border-style: dashed; -fx-border-radius: 8;");
        Label emptyLabel = new Label("No workouts logged in this period");
        emptyLabel.setStyle("-fx-text-fill: " + MUTED_COLOR + "; -fx-font-size: 13px;");
        Label hint = new Label("Log one on the Log Workout page and it will appear here.");
        hint.setStyle("-fx-text-fill: " + MUTED_COLOR + "; -fx-font-size: 11px;");
        emptyState.getChildren().addAll(emptyLabel, hint);
        return emptyState;
    }

    private VBox buildCalendarCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24, 24, 24, 24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label title = new Label("Calendar");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        VBox calendarBody = new VBox();
        card.getChildren().addAll(title, calendarBody);

        renderCalendarBody(calendarBody);

        return card;
    }

    private void renderCalendarBody(VBox calendarBody) {
        calendarBody.getChildren().clear();

        Button prevButton = new Button("<");
        prevButton.setStyle("-fx-background-color: " + PAGE_BG + "; -fx-background-radius: 50%; -fx-min-width: 28; -fx-min-height: 28;");
        Button nextButton = new Button(">");
        nextButton.setStyle("-fx-background-color: " + PAGE_BG + "; -fx-background-radius: 50%; -fx-min-width: 28; -fx-min-height: 28;");

        Label monthLabel = new Label(currentCalendarMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + currentCalendarMonth.getYear());
        monthLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        HBox navRow = new HBox(prevButton, leftSpacer, monthLabel, rightSpacer, nextButton);
        navRow.setAlignment(Pos.CENTER);
        VBox.setMargin(navRow, new Insets(8, 0, 12, 0));

        prevButton.setOnAction(e -> {
            currentCalendarMonth = currentCalendarMonth.minusMonths(1);
            renderCalendarBody(calendarBody);
        });
        nextButton.setOnAction(e -> {
            currentCalendarMonth = currentCalendarMonth.plusMonths(1);
            renderCalendarBody(calendarBody);
        });

        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(8);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHalignment(javafx.geometry.HPos.CENTER);
            grid.getColumnConstraints().add(cc);
        }

        String[] dayHeaders = {"S", "M", "T", "W", "T", "F", "S"};
        for (int i = 0; i < 7; i++) {
            Label h = new Label(dayHeaders[i]);
            h.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + "; -fx-font-weight: bold;");
            grid.add(h, i, 0);
            GridPane.setHalignment(h, javafx.geometry.HPos.CENTER);
        }

        LocalDate firstOfMonth = currentCalendarMonth.atDay(1);
        int firstDayColumn = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        LocalDate cursor = firstOfMonth.minusDays(firstDayColumn);
        LocalDate today = LocalDate.now();

        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                LocalDate cellDate = cursor;
                boolean inMonth = YearMonth.from(cellDate).equals(currentCalendarMonth);
                boolean isToday = cellDate.equals(today);

                Label dayLabel = new Label(String.valueOf(cellDate.getDayOfMonth()));
                dayLabel.setMinSize(32, 32);
                dayLabel.setAlignment(Pos.CENTER);
                if (isToday) {
                    dayLabel.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; "
                            + "-fx-background-radius: 50%; -fx-font-weight: bold; -fx-font-size: 13px;");
                } else if (inMonth) {
                    dayLabel.setStyle("-fx-text-fill: " + TITLE_COLOR + "; -fx-font-size: 13px;");
                } else {
                    dayLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
                }
                grid.add(dayLabel, col, row);
                GridPane.setHalignment(dayLabel, javafx.geometry.HPos.CENTER);

                cursor = cursor.plusDays(1);
            }
        }

        calendarBody.getChildren().addAll(navRow, grid);
    }

    private VBox buildHeaderCard() {
        VBox card = new VBox();
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(28, 32, 28, 32));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        // Left side: avatar, name, subtitle, edit button
        StackPane avatar = buildAvatar(84);

        Label nameLabel = new Label(store.firstName.isEmpty() ? "Guest" : store.firstName);
        nameLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label myProfileLabel = new Label("My Profile");
        myProfileLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MUTED_COLOR + ";");

        Button editButton = new Button("Edit Profile");
        editButton.setPrefHeight(36);
        editButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        editButton.setOnAction(e -> container.getChildren().setAll(buildEditForm()));

        VBox nameColumn = new VBox(4, nameLabel, myProfileLabel);
        VBox leftColumn = new VBox(14, nameColumn, editButton);
        leftColumn.setAlignment(Pos.CENTER_LEFT);

        HBox leftSide = new HBox(20, avatar, leftColumn);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        // Right side: personal + activity stats
        HBox personalStatsRow = new HBox(32,
                statColumn("Email", store.email.isEmpty() ? "-" : store.email),
                statColumn("Gender", store.gender != null ? store.gender.display : "-"),
                statColumn("Date of Birth", dateOfBirthText()),
                statColumn("Height", String.format("%.0f cm", store.heightCm)),
                statColumn("Weight", String.format("%.0f kg", store.weightKg)),
                statColumn("Fitness Level", levelLabel())
        );

        Separator divider = new Separator();
        VBox.setMargin(divider, new Insets(16, 0, 16, 0));

        // TODO: Workouts / This week come from workout-logging data owned
        // by another page (Log Workout / Workout History). Wired to 0 for
        // now until that data is available to read from here.
        // Only figures this page can actually work out. Workout counts live on the
        // Log Workout and Workout History pages and are not readable from here, and
        // a hardcoded zero reads as a real number rather than an absent one.
        HBox activityStatsRow = new HBox(40,
                bmiColumn(),
                statColumn("Age", String.valueOf(calculatedAge()))
        );
        activityStatsRow.setAlignment(Pos.CENTER_RIGHT);

        VBox rightSide = new VBox(personalStatsRow, divider, activityStatsRow);
        rightSide.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        HBox headerRow = new HBox(leftSide, rightSide);
        HBox.setHgrow(rightSide, Priority.ALWAYS);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerRow.getChildren().add(1, spacer);

        card.getChildren().add(headerRow);
        return card;
    }

    /** The date of birth as the page's placeholder promises it: dd/MM/yyyy. */
    private String dateOfBirthText() {
        return store.dateOfBirth == null ? "DD/MM/YYYY" : store.dateOfBirth.format(DATE_FORMAT);
    }

    /**
     * BMI and its category, both asked of {@link PersonalDetails} rather than
     * worked out here. An incomplete profile reports zero, which
     * {@link BmiCalculator} already renders as "Unknown" in grey, so there is no
     * special case to write.
     */
    private VBox bmiColumn() {
        double bmi = PersonalDetailsMapper.toDetails(store).map(PersonalDetails::bmi).orElse(0.0);
        String category = BmiCalculator.category(bmi);

        Label labelNode = new Label("BMI");
        labelNode.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");

        Label valueNode = new Label(bmi <= 0 ? "\u2014" : String.format(Locale.ROOT, "%.1f", bmi));
        valueNode.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: "
                + BmiCalculator.categoryColour(bmi) + ";");

        Label categoryNode = new Label(category);
        categoryNode.setStyle("-fx-font-size: 11px; -fx-text-fill: "
                + BmiCalculator.categoryColour(bmi) + ";");

        VBox col = new VBox(2, labelNode, valueNode, categoryNode);
        col.setAlignment(Pos.CENTER_RIGHT);
        return col;
    }

    private VBox statColumn(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        VBox col = new VBox(4, labelNode, valueNode);
        col.setAlignment(Pos.CENTER_RIGHT);
        return col;
    }

    private HBox detailRow(String label, String value) {
        Label labelNode = mutedLabel(label);
        labelNode.setMinWidth(170);
        Label valueNode = boldLabel(value);
        HBox row = new HBox(12, labelNode, valueNode);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setStyle("-fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 0 0 1 0;");
        return row;
    }

    private Label mutedLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + MUTED_COLOR + ";");
        return l;
    }

    private Label boldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        return l;
    }

    /** The email on the logged-in account (from the users table); the profile doesn't re-ask for it. */
    private String accountEmail() {
        if (!UserSession.isLoggedIn()) return "";
        String email = new UserDAO().getEmail(UserSession.getCurrentUser().getUsername());
        return email == null ? "" : email;
    }

    /** The level the profile already holds, or null when none has been chosen yet. */
    private FitnessLevel storedLevel() {
        return store.fitnessLevel == null ? null
                : FitnessLevel.valueOf(store.fitnessLevel.name());
    }

    /** The stored level's label, or a dash when the profile has none. */
    private String levelLabel() {
        FitnessLevel level = storedLevel();
        return level == null ? "\u2014" : level.display();
    }

    /** Lets each level supply its own label, so ADVANCED reads as "Pro". */
    private StringConverter<FitnessLevel> fitnessConverter(ComboBox<FitnessLevel> box) {
        return new StringConverter<>() {
            @Override public String toString(FitnessLevel fl) {
                return fl == null ? "" : fl.display();
            }
            @Override public FitnessLevel fromString(String s) {
                return box.getValue();
            }
        };
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        return l;
    }

    /**
     * Builds a circular avatar. Shows the chosen photo if photoPath is
     * set and the file still exists, otherwise falls back to a plain
     * placeholder icon.
     */
    private StackPane buildAvatar(double size) {
        StackPane avatar = new StackPane();
        avatar.setPrefSize(size, size);
        avatar.setMaxSize(size, size);
        avatar.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 50%;");

        if (store.photoPath != null && new File(store.photoPath).exists()) {
            ImageView imageView = new ImageView(new Image(new File(store.photoPath).toURI().toString()));
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(false);
            Circle clip = new Circle(size / 2, size / 2, size / 2);
            imageView.setClip(clip);
            avatar.getChildren().add(imageView);
        } else {
            Label avatarIcon = new Label("\uD83D\uDC64");
            avatarIcon.setStyle("-fx-font-size: " + (size * 0.45) + "px; -fx-text-fill: #9ca3af;");
            avatar.getChildren().add(avatarIcon);
        }
        return avatar;
    }

    /**
     * Opens a file chooser restricted to image files, updates the given
     * avatar preview immediately, and stores the chosen path into
     * pathHolder[0] (a 1-element array used as a mutable out-param since
     * this is called from a lambda).
     */
    private void pickPhoto(Node ownerNode, StackPane previewAvatar, String[] pathHolder) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a profile photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Window owner = ownerNode.getScene() != null ? ownerNode.getScene().getWindow() : null;
        File selected = chooser.showOpenDialog(owner);
        if (selected != null) {
            pathHolder[0] = selected.getAbsolutePath();
            previewAvatar.getChildren().clear();
            double size = previewAvatar.getPrefWidth();
            ImageView imageView = new ImageView(new Image(selected.toURI().toString()));
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(false);
            Circle clip = new Circle(size / 2, size / 2, size / 2);
            imageView.setClip(clip);
            previewAvatar.getChildren().add(imageView);
        }
    }

    private HBox buildAccountHeader() {
        Label avatar = new Label();
        avatar.setPrefSize(28, 28);
        avatar.setMaxSize(28, 28);
        avatar.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 50%;");

        Label nameLabel = new Label(store.firstName.isEmpty() ? "Guest" : store.firstName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        HBox header = new HBox(8, avatar, nameLabel);
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));
        return header;
    }

    // ---------- Shared wizard chrome ----------

    private VBox wizardShell(String title, String stepText, double progressFraction, String subtitleText, Node card) {
        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        HBox titleRow = new HBox();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label stepLabel = new Label(stepText);
        stepLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + MUTED_COLOR + ";");
        titleRow.getChildren().addAll(titleLabel, spacer, stepLabel);
        titleRow.setAlignment(Pos.BOTTOM_LEFT);

        StackPane progressTrack = new StackPane();
        progressTrack.setMaxWidth(Double.MAX_VALUE);
        progressTrack.setPrefHeight(4);
        progressTrack.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 2;");
        Region progressFill = new Region();
        progressFill.setStyle("-fx-background-color: " + ACCENT + "; -fx-background-radius: 2;");
        progressFill.setPrefHeight(4);
        StackPane.setAlignment(progressFill, Pos.CENTER_LEFT);
        progressFill.prefWidthProperty().bind(progressTrack.widthProperty().multiply(progressFraction));
        progressTrack.getChildren().add(progressFill);
        VBox.setMargin(progressTrack, new Insets(4, 0, 20, 0));

        Label subtitle = new Label(subtitleText);
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: " + MUTED_COLOR + ";");
        VBox.setMargin(subtitle, new Insets(0, 0, 24, 0));

        page.getChildren().addAll(titleRow, progressTrack, subtitle, card);
        return page;
    }

    // ---------- Step 1: personal details ----------

    private Node buildStep1() {
        TextField firstNameField = new TextField(store.firstName);
        firstNameField.setPromptText("Alex Taylor");
        firstNameField.setMaxWidth(Double.MAX_VALUE);
        firstNameField.setPrefHeight(38);

        TextField emailField = new TextField(accountEmail());
        emailField.setPromptText("alex@example.com");
        emailField.setPrefHeight(38);
        emailField.setEditable(false);   // taken from your account — not re-entered here

        ComboBox<LocalProfileStore.Gender> genderBox = new ComboBox<>();
        genderBox.getItems().addAll(LocalProfileStore.Gender.values());
        genderBox.setValue(store.gender);
        genderBox.setPromptText("Select gender");
        genderBox.setMaxWidth(Double.MAX_VALUE);
        genderBox.setPrefHeight(38);

        DatePicker dobPicker = new DatePicker(store.dateOfBirth);
        dobPicker.setPromptText("DD/MM/YYYY");
        dobPicker.setMaxWidth(Double.MAX_VALUE);
        dobPicker.setPrefHeight(38);

        TextField heightField = new TextField(store.heightCm > 0 ? String.valueOf(store.heightCm) : "");
        heightField.setPromptText("175");
        heightField.setPrefHeight(38);

        TextField weightField = new TextField(store.weightKg > 0 ? String.valueOf(store.weightKg) : "");
        weightField.setPromptText("70");
        weightField.setPrefHeight(38);

        // Left deliberately empty. The store defaults to BEGINNER, so pre-selecting
        // it would let someone create a profile without ever making the choice;
        // ProfileValidator refuses the form while this is null.
        ComboBox<FitnessLevel> fitnessBox = new ComboBox<>();
        fitnessBox.getItems().addAll(FitnessLevel.values());
        fitnessBox.setValue(null);
        fitnessBox.setPromptText("Choose an option");
        fitnessBox.setMaxWidth(Double.MAX_VALUE);
        fitnessBox.setPrefHeight(38);
        fitnessBox.setConverter(fitnessConverter(fitnessBox));

        StackPane photoPreview = buildAvatar(72);
        String[] photoPathHolder = {store.photoPath};
        Button choosePhotoButton = new Button(photoPathHolder[0] != null ? "Change photo" : "Add photo");
        choosePhotoButton.setOnAction(e -> {
            pickPhoto(choosePhotoButton, photoPreview, photoPathHolder);
            choosePhotoButton.setText("Change photo");
        });
        HBox photoRow = new HBox(16, photoPreview, choosePhotoButton);
        photoRow.setAlignment(Pos.CENTER_LEFT);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        VBox card = new VBox(16);
        card.setMaxWidth(760);
        card.setPadding(new Insets(28, 32, 28, 32));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(16);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            grid.getColumnConstraints().add(cc);
        }

        grid.add(fieldLabel("Profile photo"), 0, 0, 2, 1);
        grid.add(photoRow, 0, 1, 2, 1);

        grid.add(fieldLabel("Full name"), 0, 2);
        grid.add(fieldLabel("Email"), 1, 2);
        grid.add(firstNameField, 0, 3);
        grid.add(emailField, 1, 3);

        grid.add(fieldLabel("Date of birth"), 0, 4);
        grid.add(fieldLabel("Gender"), 1, 4);
        grid.add(dobPicker, 0, 5);
        grid.add(genderBox, 1, 5);

        grid.add(fieldLabel("Height (cm)"), 0, 6);
        grid.add(fieldLabel("Weight (kg)"), 1, 6);
        grid.add(heightField, 0, 7);
        grid.add(weightField, 1, 7);

        grid.add(fieldLabel("Fitness level"), 0, 8);
        grid.add(fitnessBox, 0, 9, 2, 1);

        Button continueButton = new Button("Create profile");
        continueButton.setMaxWidth(Double.MAX_VALUE);
        continueButton.setPrefHeight(42);
        continueButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        continueButton.setOnAction(e -> {
            PersonalDetails details = readDetails(firstNameField, emailField, genderBox,
                    dobPicker, heightField, weightField, fitnessBox,
                    photoPathHolder[0], errorLabel);
            if (details == null) {
                return;
            }
            // Seed the goals screen's experience level from the fitness level
            // chosen here, so a new user never starts with it unset.
            PersonalDetailsMapper.apply(details, store, true);
            new ProfileDAO().save(store, UserSession.getCurrentUser().getUsername());
            refresh();
        });

        card.getChildren().addAll(grid, errorLabel, continueButton);

        return wizardShell("Create your profile", "", 1.0,
                "Tell us about yourself so we can personalise your plan.", card);
    }

    // ---------- Step 2: fitness goals ----------

    private GridPane twoColumnGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(6);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            grid.getColumnConstraints().add(cc);
        }
        return grid;
    }

    private void styleGoalCard(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: " + ACCENT_BG + "; -fx-text-fill: #c2410c; "
                    + "-fx-border-color: " + ACCENT + "; -fx-border-width: 2; -fx-border-radius: 6; "
                    + "-fx-background-radius: 6; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE_COLOR + "; "
                    + "-fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        }
    }

    private void styleChip(ToggleButton chip, boolean selected) {
        if (selected) {
            chip.setStyle("-fx-background-color: " + ACCENT_BG + "; -fx-text-fill: #c2410c; "
                    + "-fx-border-color: " + ACCENT + "; -fx-border-radius: 6; -fx-background-radius: 6;");
        } else {
            chip.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE_COLOR + "; "
                    + "-fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        }
    }

    // ---------- Single-step edit form (personal details only) ----------

    private Node buildEditForm() {
        TextField firstNameField = new TextField(store.firstName);
        firstNameField.setMaxWidth(Double.MAX_VALUE);
        firstNameField.setPrefHeight(38);

        TextField emailField = new TextField(accountEmail());
        emailField.setPrefHeight(38);
        emailField.setEditable(false);   // taken from your account

        ComboBox<LocalProfileStore.Gender> genderBox = new ComboBox<>();
        genderBox.getItems().addAll(LocalProfileStore.Gender.values());
        genderBox.setValue(store.gender);
        genderBox.setPromptText("Select gender");
        genderBox.setMaxWidth(Double.MAX_VALUE);
        genderBox.setPrefHeight(38);

        DatePicker dobPicker = new DatePicker(store.dateOfBirth);
        dobPicker.setMaxWidth(Double.MAX_VALUE);
        dobPicker.setPrefHeight(38);

        TextField heightField = new TextField(String.valueOf(store.heightCm));
        heightField.setPrefHeight(38);

        TextField weightField = new TextField(String.valueOf(store.weightKg));
        weightField.setPrefHeight(38);

        ComboBox<FitnessLevel> fitnessBox = new ComboBox<>();
        fitnessBox.getItems().addAll(FitnessLevel.values());
        fitnessBox.setValue(storedLevel());
        fitnessBox.setPromptText("Choose an option");
        fitnessBox.setMaxWidth(Double.MAX_VALUE);
        fitnessBox.setPrefHeight(38);
        fitnessBox.setConverter(fitnessConverter(fitnessBox));

        StackPane photoPreview = buildAvatar(72);
        String[] photoPathHolder = {store.photoPath};
        Button choosePhotoButton = new Button(photoPathHolder[0] != null ? "Change photo" : "Add photo");
        choosePhotoButton.setOnAction(e -> {
            pickPhoto(choosePhotoButton, photoPreview, photoPathHolder);
            choosePhotoButton.setText("Change photo");
        });
        HBox photoRow = new HBox(16, photoPreview, choosePhotoButton);
        photoRow.setAlignment(Pos.CENTER_LEFT);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        Label title = new Label("Edit your profile");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label subtitle = new Label("Update your details to keep your plan accurate.");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: " + MUTED_COLOR + ";");
        VBox.setMargin(subtitle, new Insets(4, 0, 24, 0));

        VBox card = new VBox(16);
        card.setMaxWidth(760);
        card.setPadding(new Insets(28, 32, 28, 32));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(16);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            grid.getColumnConstraints().add(cc);
        }

        grid.add(fieldLabel("Profile photo"), 0, 0, 2, 1);
        grid.add(photoRow, 0, 1, 2, 1);

        grid.add(fieldLabel("Full name"), 0, 2);
        grid.add(fieldLabel("Email"), 1, 2);
        grid.add(firstNameField, 0, 3);
        grid.add(emailField, 1, 3);

        grid.add(fieldLabel("Date of birth"), 0, 4);
        grid.add(fieldLabel("Gender"), 1, 4);
        grid.add(dobPicker, 0, 5);
        grid.add(genderBox, 1, 5);

        grid.add(fieldLabel("Height (cm)"), 0, 6);
        grid.add(fieldLabel("Weight (kg)"), 1, 6);
        grid.add(heightField, 0, 7);
        grid.add(weightField, 1, 7);

        grid.add(fieldLabel("Fitness level"), 0, 8);
        grid.add(fitnessBox, 0, 9, 2, 1);

        Button saveButton = new Button("Save changes");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setPrefHeight(42);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        saveButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        saveButton.setOnAction(e -> {
            PersonalDetails details = readDetails(firstNameField, emailField, genderBox,
                    dobPicker, heightField, weightField, fitnessBox,
                    photoPathHolder[0], errorLabel);
            if (details == null) {
                return;
            }
            // An edit must not reset the experience level the goals screen owns.
            PersonalDetailsMapper.apply(details, store, false);
            new ProfileDAO().save(store, UserSession.getCurrentUser().getUsername());
            container.getChildren().setAll(buildDetailsView());
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setPrefHeight(42);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT
                + "; -fx-border-color: " + ACCENT + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 14px;");
        cancelButton.setOnAction(e -> container.getChildren().setAll(buildDetailsView()));

        HBox buttonRow = new HBox(12, saveButton, cancelButton);

        Label reassurance = new Label("Your updated information will be saved securely.");
        reassurance.setStyle("-fx-font-size: 11px; -fx-text-fill: " + MUTED_COLOR + ";");
        HBox reassuranceRow = new HBox(reassurance);
        reassuranceRow.setAlignment(Pos.CENTER);

        card.getChildren().addAll(grid, errorLabel, buttonRow, reassuranceRow);
        page.getChildren().addAll(buildAccountHeader(), title, subtitle, card);
        return page;
    }
}