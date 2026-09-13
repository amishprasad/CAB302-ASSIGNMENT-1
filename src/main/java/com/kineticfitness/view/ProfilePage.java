package com.kineticfitness.view;

import com.kineticfitness.db.UserDAO;
import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class ProfilePage implements Page {

    private static final String CURRENT_USERNAME = "guest";

    private static final String ACCENT = "#f97316";
    private static final String TITLE_COLOR = "#1E293B";
    private static final String MUTED_COLOR = "#64748B";
    private static final String PAGE_BG = "#F1F5F9";

    private final StackPane container = new StackPane();
    private final UserDAO userDAO = new UserDAO();
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

    private void refresh() {
        User user = userDAO.findByUsername(CURRENT_USERNAME);
        if (user != null) {
            container.getChildren().setAll(buildDetailsView(user));
        } else {
            container.getChildren().setAll(buildFormView(null));
        }
    }

    private Node buildDetailsView(User user) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        VBox headerCard = buildHeaderCard(user);

        VBox statsCard = buildWorkoutStatisticsCard();
        VBox calendarCard = buildCalendarCard();
        HBox bottomRow = new HBox(20, statsCard, calendarCard);
        HBox.setHgrow(statsCard, Priority.ALWAYS);
        statsCard.setMaxWidth(Double.MAX_VALUE);
        calendarCard.setPrefWidth(360);
        calendarCard.setMinWidth(360);

        page.getChildren().addAll(headerCard, bottomRow);
        return page;
    }

    private VBox buildHeaderCard(User user) {
        VBox card = new VBox();
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(28, 32, 28, 32));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        StackPane avatar = new StackPane();
        avatar.setPrefSize(84, 84);
        avatar.setMaxSize(84, 84);
        avatar.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 50%;");
        Label avatarIcon = new Label("\uD83D\uDC64");
        avatarIcon.setStyle("-fx-font-size: 38px; -fx-text-fill: #9ca3af;");
        avatar.getChildren().add(avatarIcon);

        String displayName = (nullToDash(user.getFirstName()).equals("-") ? "" : user.getFirstName());
        Label nameLabel = new Label(displayName.isEmpty() ? "Guest" : displayName);
        nameLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label myProfileLabel = new Label("My Profile");
        myProfileLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MUTED_COLOR + ";");

        Button editButton = new Button("Edit Profile");
        editButton.setPrefHeight(36);
        editButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        editButton.setOnAction(e -> container.getChildren().setAll(buildFormView(user)));

        VBox nameColumn = new VBox(4, nameLabel, myProfileLabel);
        VBox leftColumn = new VBox(14, nameColumn, editButton);
        leftColumn.setAlignment(Pos.CENTER_LEFT);

        HBox leftSide = new HBox(20, avatar, leftColumn);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        HBox personalStatsRow = new HBox(32,
                statColumn("Email", user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : "-"),
                statColumn("Gender", user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "-"),
                statColumn("Date of Birth", user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "-"),
                statColumn("Height", String.format("%.0f cm", user.getHeightCm())),
                statColumn("Weight", String.format("%.0f kg", user.getWeightKg())),
                statColumn("Fitness Level", formatEnum(user.getFitnessLevel().name()))
        );

        Separator divider = new Separator();
        VBox.setMargin(divider, new Insets(16, 0, 16, 0));

        HBox activityStatsRow = new HBox(40,
                statColumn("Workouts", "0"),
                statColumn("This week", "0 min")
        );

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

    private VBox statColumn(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        VBox col = new VBox(4, labelNode, valueNode);
        col.setAlignment(Pos.CENTER_RIGHT);
        return col;
    }

    private VBox buildWorkoutStatisticsCard() {
        VBox card = new VBox(4);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label title = new Label("Workout Statistics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        ToggleGroup tabGroup = new ToggleGroup();
        ToggleButton durationTab = new ToggleButton("Duration");
        ToggleButton repsTab = new ToggleButton("Reps");
        durationTab.setToggleGroup(tabGroup);
        repsTab.setToggleGroup(tabGroup);
        durationTab.setSelected(true);
        styleStatTab(durationTab, true);
        styleStatTab(repsTab, false);
        durationTab.selectedProperty().addListener((o, was, isNow) -> styleStatTab(durationTab, isNow));
        repsTab.selectedProperty().addListener((o, was, isNow) -> styleStatTab(repsTab, isNow));

        ComboBox<String> rangeBox = new ComboBox<>();
        rangeBox.getItems().addAll("Last 12 weeks", "Last 4 weeks", "Last 6 months");
        rangeBox.setValue("Last 12 weeks");

        Region tabSpacer = new Region();
        HBox.setHgrow(tabSpacer, Priority.ALWAYS);
        HBox tabsRow = new HBox(8, durationTab, repsTab, tabSpacer, rangeBox);
        tabsRow.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(tabsRow, new Insets(12, 0, 20, 0));

        Label bigStat = new Label("0 min");
        bigStat.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");
        Label bigStatCaption = new Label("This week");
        bigStatCaption.setStyle("-fx-font-size: 13px; -fx-text-fill: " + MUTED_COLOR + ";");
        HBox bigStatRow = new HBox(10, bigStat, bigStatCaption);
        bigStatRow.setAlignment(Pos.BASELINE_LEFT);
        VBox.setMargin(bigStatRow, new Insets(0, 0, 16, 0));

        VBox emptyState = new VBox();
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPrefHeight(220);
        emptyState.setStyle("-fx-background-color: " + PAGE_BG + "; -fx-background-radius: 8; "
                + "-fx-border-color: #e2e8f0; -fx-border-style: dashed; -fx-border-radius: 8;");
        Label emptyLabel = new Label("No workout data yet");
        emptyLabel.setStyle("-fx-text-fill: " + MUTED_COLOR + "; -fx-font-size: 13px;");
        emptyState.getChildren().add(emptyLabel);

        card.getChildren().addAll(title, tabsRow, bigStatRow, emptyState);
        return card;
    }

    private void styleStatTab(ToggleButton tab, boolean selected) {
        if (selected) {
            tab.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT
                    + "; -fx-font-weight: bold; -fx-border-color: transparent transparent " + ACCENT
                    + " transparent; -fx-border-width: 0 0 2 0; -fx-background-radius: 0;");
        } else {
            tab.setStyle("-fx-background-color: transparent; -fx-text-fill: " + MUTED_COLOR
                    + "; -fx-border-color: transparent; -fx-background-radius: 0;");
        }
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
        int firstDayColumn = firstOfMonth.getDayOfWeek().getValue() % 7;
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

    private String nullToDash(String s) {
        return s == null || s.isEmpty() ? "-" : s;
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

    private String formatEnum(String name) {
        String s = name.toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_COLOR + ";");
        return l;
    }

    // ---------- Create / edit form ----------

    /** existingUser is null when creating a profile for the first time. */
    private Node buildFormView(User existingUser) {
        boolean isNew = existingUser == null;

        TextField firstNameField = new TextField(isNew ? "" : existingUser.getFirstName());
        firstNameField.setPromptText("Alex");
        firstNameField.setPrefHeight(38);

        TextField lastNameField = new TextField(isNew ? "" : existingUser.getLastName());
        lastNameField.setPromptText("Rivera");
        lastNameField.setPrefHeight(38);

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        genderBox.setValue(isNew ? null : existingUser.getGender());
        genderBox.setPromptText("Select gender");
        genderBox.setMaxWidth(Double.MAX_VALUE);
        genderBox.setPrefHeight(38);

        TextField emailField = new TextField(isNew ? "" : existingUser.getEmail());
        emailField.setPromptText("alex@example.com");
        emailField.setPrefHeight(38);

        DatePicker dobPicker = new DatePicker(isNew ? null : existingUser.getDateOfBirth());
        dobPicker.setPromptText("DD/MM/YYYY");
        dobPicker.setMaxWidth(Double.MAX_VALUE);
        dobPicker.setPrefHeight(38);

        TextField heightField = new TextField(isNew ? "" : String.valueOf(existingUser.getHeightCm()));
        heightField.setPromptText("175");
        heightField.setPrefHeight(38);

        TextField weightField = new TextField(isNew ? "" : String.valueOf(existingUser.getWeightKg()));
        weightField.setPromptText("70");
        weightField.setPrefHeight(38);

        ComboBox<FitnessLevel> fitnessBox = new ComboBox<>();
        fitnessBox.getItems().addAll(FitnessLevel.values());
        fitnessBox.setValue(isNew ? FitnessLevel.BEGINNER : existingUser.getFitnessLevel());
        fitnessBox.setMaxWidth(Double.MAX_VALUE);
        fitnessBox.setPrefHeight(38);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        VBox page = new VBox(4);
        page.setPadding(new Insets(32, 40, 32, 40));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: " + PAGE_BG + ";");

        Label title = new Label(isNew ? "Create your profile" : "Edit your profile");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + TITLE_COLOR + ";");

        Label subtitle = new Label(isNew
                ? "Tell us about yourself so we can personalise your plan."
                : "Update your details to keep your plan accurate.");
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

        grid.add(fieldLabel("First name"), 0, 0);
        grid.add(fieldLabel("Last name"), 1, 0);
        grid.add(firstNameField, 0, 1);
        grid.add(lastNameField, 1, 1);

        grid.add(fieldLabel("Gender"), 0, 2);
        grid.add(fieldLabel("Email"), 1, 2);
        grid.add(genderBox, 0, 3);
        grid.add(emailField, 1, 3);

        grid.add(fieldLabel("Date of birth"), 0, 4);
        grid.add(fieldLabel("Fitness level"), 1, 4);
        grid.add(dobPicker, 0, 5);
        grid.add(fitnessBox, 1, 5);

        grid.add(fieldLabel("Height (cm)"), 0, 6);
        grid.add(fieldLabel("Weight (kg)"), 1, 6);
        grid.add(heightField, 0, 7);
        grid.add(weightField, 1, 7);

        Button saveButton = new Button(isNew ? "Create profile" : "Save changes");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setPrefHeight(42);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        saveButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        saveButton.setOnAction(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String gender = genderBox.getValue();
            String email = emailField.getText().trim();
            LocalDate dob = dobPicker.getValue();
            String heightText = heightField.getText().trim();
            String weightText = weightField.getText().trim();
            FitnessLevel level = fitnessBox.getValue();

            if (firstName.isEmpty() || lastName.isEmpty() || gender == null || email.isEmpty()
                    || dob == null || heightText.isEmpty() || weightText.isEmpty() || level == null) {
                errorLabel.setText("Please fill in every field before continuing.");
                errorLabel.setVisible(true);
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                errorLabel.setText("Enter a valid email address.");
                errorLabel.setVisible(true);
                return;
            }
            if (dob.isAfter(LocalDate.now())) {
                errorLabel.setText("Date of birth can't be in the future.");
                errorLabel.setVisible(true);
                return;
            }

            try {
                double height = Double.parseDouble(heightText);
                double weight = Double.parseDouble(weightText);

                if (height <= 0 || weight <= 0) {
                    errorLabel.setText("Height and weight must be positive numbers.");
                    errorLabel.setVisible(true);
                    return;
                }

                User user = new User(CURRENT_USERNAME, firstName, lastName, gender, email,
                        dob, level, height, weight);

                try {
                    userDAO.save(user);
                } catch (RuntimeException dbEx) {
                    errorLabel.setText("Couldn't save profile: " + dbEx.getMessage());
                    errorLabel.setVisible(true);
                    return;
                }

                refresh();

            } catch (NumberFormatException ex) {
                errorLabel.setText("Height and weight must be valid numbers.");
                errorLabel.setVisible(true);
            }
        });

        HBox buttonRow = new HBox(12, saveButton);
        if (!isNew) {
            Button cancelButton = new Button("Cancel");
            cancelButton.setMaxWidth(Double.MAX_VALUE);
            cancelButton.setPrefHeight(42);
            HBox.setHgrow(cancelButton, Priority.ALWAYS);
            cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT
                    + "; -fx-border-color: " + ACCENT + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 14px;");
            cancelButton.setOnAction(e -> refresh());
            buttonRow.getChildren().add(cancelButton);
        }

        card.getChildren().addAll(grid, errorLabel, buttonRow);
        page.getChildren().addAll(title, subtitle, card);
        return page;
    }
}