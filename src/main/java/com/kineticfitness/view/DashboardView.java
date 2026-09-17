package com.kineticfitness.view;

import com.kineticfitness.db.ScheduleDAO;
import com.kineticfitness.db.WorkoutDAO;
import com.kineticfitness.model.DailyLog;
import com.kineticfitness.model.Exercise;
import com.kineticfitness.model.Meal;
import com.kineticfitness.model.ScheduledWorkout;
import com.kineticfitness.model.Workout;
import com.kineticfitness.session.UserSession;
import com.kineticfitness.util.BmiCalculator;
import com.kineticfitness.util.WorkoutStats;
import com.kineticfitness.util.WorkoutStats.DatedVolume;
import com.kineticfitness.util.WorkoutStats.Sample;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The landing page after login, and a summary of every other screen in the app.
 *
 * <p>It stores nothing of its own — it reads what the other pages have already saved
 * (profile details, workouts, schedule, meals, milestones) and lays them out in one view.
 * {@link AppShell} calls {@link #getContent()} each time the nav item is clicked, so the
 * whole page rebuilds with current data on every visit; no refresh button needed.
 */
public class DashboardView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";
    private static final String CARD = "-fx-background-color: white; -fx-background-radius: 10;"
            + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;";

    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("d MMM");

    private static final int CHART_SESSIONS = 6;
    private static final int PREVIEW_ROWS = 3;

    private final WorkoutDAO workoutDAO = new WorkoutDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final LocalProfileStore store = LocalProfileStore.getInstance();

    @Override
    public String label() {
        return "Dashboard";
    }

    @Override
    public Node getContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        content.getChildren().addAll(heading(), subheading());

        if (!UserSession.isLoggedIn()) {
            content.getChildren().add(hintCard("Log in to see your activity."));
            return scroll(content);
        }

        String username = UserSession.getCurrentUser().getUsername();
        List<Workout> workouts = workoutDAO.findAllByUsername(username);
        List<ScheduledWorkout> scheduled = scheduleDAO.findForUser(username);
        DailyLog today = MealStore.getInstance().today();

        content.getChildren().addAll(
                statRow(workouts, scheduled, today),
                bodyMetricsCard(),
                volumeCard(workouts),
                twoColumns(recentWorkoutsCard(workouts), scheduleCard(scheduled)),
                twoColumns(mealsCard(today), goalProgressCard(workouts)));

        return scroll(content);
    }

    // ---------- header ----------

    private Label heading() {
        String text = store.firstName == null || store.firstName.isBlank()
                ? "Dashboard"
                : "Welcome back, " + store.firstName;
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        return label;
    }

    private Label subheading() {
        Label label = new Label("Your fitness activity at a glance — "
                + LocalDate.now().format(LONG_DATE) + ".");
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");
        return label;
    }

    // ---------- headline stats ----------

    private HBox statRow(List<Workout> workouts, List<ScheduledWorkout> scheduled, DailyLog today) {
        List<LocalDate> dates = new ArrayList<>();
        for (Workout workout : workouts) {
            dates.add(workout.getDate());
        }
        int thisWeek = WorkoutStats.sessionsThisWeek(dates, LocalDate.now());

        HBox row = new HBox(16,
                statCard("Workouts Logged", String.valueOf(workouts.size())),
                statCard("This Week", thisWeek + " of " + store.weeklyWorkoutGoal),
                statCard("Upcoming Sessions", String.valueOf(scheduled.size())),
                statCard("Calories Today", today.getTotalCalories() + " kcal"));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox statCard(String caption, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + ORANGE + ";");

        Label captionLabel = new Label(caption);
        captionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        VBox card = new VBox(6, valueLabel, captionLabel);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        card.setPrefWidth(220);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // ---------- body metrics ----------

    private VBox bodyMetricsCard() {
        double bmi = BmiCalculator.bmi(store.heightCm, store.weightKg);

        Label bmiValue = new Label(bmi > 0 ? String.format("%.1f", bmi) : "—");
        bmiValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: "
                + BmiCalculator.categoryColour(bmi) + ";");

        Label bmiCaption = new Label(bmi > 0 ? BmiCalculator.category(bmi) : "BMI");
        bmiCaption.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");
        VBox bmiColumn = new VBox(4, bmiValue, bmiCaption);

        HBox metrics = new HBox(0,
                metricColumn("Height", store.heightCm > 0 ? String.format("%.0f cm", store.heightCm) : "—"),
                metricColumn("Weight", store.weightKg > 0 ? String.format("%.0f kg", store.weightKg) : "—"),
                bmiColumn,
                metricColumn("Age", age() > 0 ? age() + " yrs" : "—"),
                metricColumn("Goal", store.primaryGoal != null ? store.primaryGoal.display : "Not set"),
                metricColumn("Target Weight",
                        store.targetWeightKg > 0 ? String.format("%.0f kg", store.targetWeightKg) : "—"));
        metrics.setAlignment(Pos.CENTER_LEFT);
        for (Node column : metrics.getChildren()) {
            HBox.setHgrow(column, Priority.ALWAYS);
        }

        VBox card = sectionCard("Your Details", metrics);
        if (!store.hasPersonalDetails()) {
            card.getChildren().add(hint("Fill in the Profile page to see your BMI and metrics here."));
        }
        return card;
    }

    private VBox metricColumn(String caption, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label captionLabel = new Label(caption);
        captionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        return new VBox(4, valueLabel, captionLabel);
    }

    private int age() {
        if (store.dateOfBirth == null) {
            return 0;
        }
        return Period.between(store.dateOfBirth, LocalDate.now()).getYears();
    }

    // ---------- training volume chart ----------

    private VBox volumeCard(List<Workout> workouts) {
        List<DatedVolume> volumes = WorkoutStats.volumeByDate(toSamples(workouts), CHART_SESSIONS);

        VBox body = new VBox(10);
        body.getChildren().add(helper("Total reps per session (sets × reps), most recent "
                + CHART_SESSIONS + " sessions."));

        if (volumes.isEmpty()) {
            body.getChildren().add(hint("No workouts logged yet — log one to see your volume here."));
        } else {
            BarChart<String, Number> chart = VolumeChart.create(220, "Total reps");
            VolumeChart.plot(chart, volumes);
            body.getChildren().add(chart);
        }
        return sectionCard("Training Volume", body);
    }

    private List<Sample> toSamples(List<Workout> workouts) {
        List<Sample> samples = new ArrayList<>();
        for (Workout workout : workouts) {
            if (workout.getDate() == null) {
                continue;
            }
            String date = workout.getDate().format(SHORT_DATE);
            for (Exercise exercise : workout.getExercises()) {
                samples.add(new Sample(date, exercise.totalReps()));
            }
        }
        return samples;
    }

    // ---------- recent workouts ----------

    private VBox recentWorkoutsCard(List<Workout> workouts) {
        VBox rows = new VBox(8);
        if (workouts.isEmpty()) {
            rows.getChildren().add(hint("No workouts logged yet."));
        } else {
            for (Workout workout : workouts.subList(0, Math.min(PREVIEW_ROWS, workouts.size()))) {
                String date = workout.getDate() == null ? "—" : workout.getDate().format(LONG_DATE);
                int count = workout.getExercises().size();
                rows.getChildren().add(row(date,
                        count + (count == 1 ? " exercise" : " exercises") + " · " + workout.totalReps() + " reps"));
            }
        }
        return sectionCard("Recent Workouts", rows);
    }

    // ---------- schedule ----------

    private VBox scheduleCard(List<ScheduledWorkout> scheduled) {
        VBox rows = new VBox(8);
        if (scheduled.isEmpty()) {
            rows.getChildren().add(hint("Nothing scheduled — add a session on the Schedule page."));
        } else {
            for (ScheduledWorkout item : scheduled.subList(0, Math.min(PREVIEW_ROWS, scheduled.size()))) {
                rows.getChildren().add(row(item.getName(),
                        item.getDate().format(SHORT_DATE) + " · " + item.getStartTime() + " · " + item.durationLabel()));
            }
        }
        return sectionCard("Upcoming Schedule", rows);
    }

    // ---------- meals ----------

    private VBox mealsCard(DailyLog today) {
        VBox rows = new VBox(8);
        List<Meal> meals = today.getMeals();

        if (meals.isEmpty()) {
            rows.getChildren().add(hint("No meals logged today."));
        } else {
            for (Meal meal : meals.subList(0, Math.min(PREVIEW_ROWS, meals.size()))) {
                rows.getChildren().add(row(meal.getName(),
                        meal.getCalories() + " kcal · " + Math.round(meal.getProtein()) + "g protein"));
            }
            Label totals = new Label("Today's total: " + today.getTotalCalories() + " kcal · "
                    + Math.round(today.getTotalProtein()) + "g protein");
            totals.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + ORANGE + ";");
            rows.getChildren().add(totals);
        }
        return sectionCard("Today's Meals", rows);
    }

    // ---------- goal progress ----------

    private VBox goalProgressCard(List<Workout> workouts) {
        VBox rows = new VBox(14);

        List<LocalDate> dates = new ArrayList<>();
        for (Workout workout : workouts) {
            dates.add(workout.getDate());
        }
        int thisWeek = WorkoutStats.sessionsThisWeek(dates, LocalDate.now());
        rows.getChildren().add(progressRow("Weekly workout goal",
                thisWeek + " of " + store.weeklyWorkoutGoal + " sessions",
                WorkoutStats.percentOf(thisWeek, store.weeklyWorkoutGoal)));

        if (store.milestones.isEmpty()) {
            rows.getChildren().add(hint("No milestones yet — create one on the Goals page."));
        } else {
            for (LocalProfileStore.Milestone milestone : store.milestones) {
                rows.getChildren().add(progressRow(milestone.description,
                        trim(milestone.currentValue) + " / " + trim(milestone.targetValue)
                                + " " + milestone.unit,
                        milestone.progressPercent()));
            }
        }
        return sectionCard("Goal Progress", rows);
    }

    private VBox progressRow(String name, String detail, int percent) {
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        ProgressBar bar = new ProgressBar(percent / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + ORANGE + ";");

        Label detailLabel = new Label(detail + "  (" + percent + "%)");
        detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        return new VBox(4, nameLabel, bar, detailLabel);
    }

    /** Drops a trailing ".0" so "5.0 km" reads as "5 km". */
    private static String trim(double value) {
        return value == Math.rint(value)
                ? String.valueOf((long) value)
                : String.valueOf(value);
    }

    // ---------- shared building blocks ----------

    private HBox twoColumns(VBox left, VBox right) {
        HBox row = new HBox(16, left, right);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        return row;
    }

    private VBox sectionCard(String heading, Region body) {
        Label header = new Label(heading);
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        VBox card = new VBox(12, header, body);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private HBox row(String primary, String detail) {
        Label primaryLabel = new Label(primary);
        primaryLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");
        primaryLabel.setMinWidth(130);

        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        HBox row = new HBox(12, primaryLabel, detailLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label helper(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");
        return label;
    }

    private Label hint(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + "; -fx-font-style: italic;");
        return label;
    }

    private VBox hintCard(String text) {
        VBox card = new VBox(hint(text));
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        return card;
    }

    private ScrollPane scroll(VBox content) {
        ScrollPane pane = new ScrollPane(content);
        pane.setFitToWidth(true);
        pane.setStyle("-fx-background-color: " + CONTENT_BG + "; -fx-background: " + CONTENT_BG + ";");
        return pane;
    }
}