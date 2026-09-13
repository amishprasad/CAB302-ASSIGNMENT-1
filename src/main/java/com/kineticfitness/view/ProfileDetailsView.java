package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.time.Period;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class ProfileDetailsView implements Page {

    private static final String ACCENT = "#f97316";
    private static final String ACCENT_BG = "rgba(249,115,22,0.08)";
    private static final String TITLE_COLOR = "#1E293B";
    private static final String MUTED_COLOR = "#64748B";
    private static final String PAGE_BG = "#F1F5F9";

    private static final String[] WORKOUT_TYPES = {"Strength training", "Cardio", "Walking", "Yoga & mobility"};
    private static final String[] WORKOUT_DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

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
        if (store.dateOfBirth == null) return 0;
        return Period.between(store.dateOfBirth, LocalDate.now()).getYears();
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
        return page;
    }

    /**
     * TODO: no real workout data is available to ProfileView yet. This
     * shows the empty state (matching the reference design) until
     * Log Workout / Workout History expose a way to read logged
     * durations and reps from here — similar to how LocalProfileStore
     * shares profile data across pages.
     */
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
                statColumn("Date of Birth", store.dateOfBirth != null ? store.dateOfBirth.toString() : "DD/MM/YYYY"),
                statColumn("Height", String.format("%.0f cm", store.heightCm)),
                statColumn("Weight", String.format("%.0f kg", store.weightKg)),
                statColumn("Fitness Level", formatEnum(store.fitnessLevel.name()))
        );

        Separator divider = new Separator();
        VBox.setMargin(divider, new Insets(16, 0, 16, 0));

        // TODO: Workouts / This week come from workout-logging data owned
        // by another page (Log Workout / Workout History). Wired to 0 for
        // now until that data is available to read from here.
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

    private String formatEnum(String name) {
        String s = name.toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
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
        firstNameField.setPromptText("Alex");
        firstNameField.setMaxWidth(Double.MAX_VALUE);
        firstNameField.setPrefHeight(38);

        TextField emailField = new TextField(store.email);
        emailField.setPromptText("alex@example.com");
        emailField.setPrefHeight(38);

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

        ComboBox<LocalProfileStore.FitnessLevel> fitnessBox = new ComboBox<>();
        fitnessBox.getItems().addAll(LocalProfileStore.FitnessLevel.values());
        fitnessBox.setValue(store.fitnessLevel);
        fitnessBox.setMaxWidth(Double.MAX_VALUE);
        fitnessBox.setPrefHeight(38);

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

        grid.add(fieldLabel("First name"), 0, 2);
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

        Button continueButton = new Button("Continue");
        continueButton.setMaxWidth(Double.MAX_VALUE);
        continueButton.setPrefHeight(42);
        continueButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        continueButton.setOnAction(e -> {
            String name = firstNameField.getText().trim();
            String email = emailField.getText().trim();
            LocalProfileStore.Gender gender = genderBox.getValue();
            LocalDate dob = dobPicker.getValue();
            String heightText = heightField.getText().trim();
            String weightText = weightField.getText().trim();
            LocalProfileStore.FitnessLevel level = fitnessBox.getValue();

            if (name.isEmpty() || email.isEmpty() || gender == null || dob == null || heightText.isEmpty()
                    || weightText.isEmpty() || level == null) {
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
                double newHeight = Double.parseDouble(heightText);
                double newWeight = Double.parseDouble(weightText);
                if (newHeight <= 0 || newWeight <= 0) {
                    errorLabel.setText("Height and weight must be positive numbers.");
                    errorLabel.setVisible(true);
                    return;
                }

                store.firstName = name;
                store.email = email;
                store.gender = gender;
                store.photoPath = photoPathHolder[0];
                store.dateOfBirth = dob;
                store.heightCm = newHeight;
                store.weightKg = newWeight;
                store.fitnessLevel = level;
                store.experienceLevel = level; // sensible default, editable in step 2

                container.getChildren().setAll(buildStep2());

            } catch (NumberFormatException ex) {
                errorLabel.setText("Height and weight must be valid numbers.");
                errorLabel.setVisible(true);
            }
        });

        card.getChildren().addAll(grid, errorLabel, continueButton);

        return wizardShell("Create your profile", "Step 1 of 2", 0.5,
                "Tell us about yourself so we can personalise your plan.", card);
    }

    // ---------- Step 2: fitness goals ----------

    private Node buildStep2() {
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        VBox card = new VBox(18);
        card.setMaxWidth(760);
        card.setPadding(new Insets(28, 32, 28, 32));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        ToggleGroup goalGroup = new ToggleGroup();
        HBox goalRow = new HBox(10);
        for (LocalProfileStore.PrimaryGoal goal : LocalProfileStore.PrimaryGoal.values()) {
            ToggleButton btn = new ToggleButton(goal.display);
            btn.setUserData(goal);
            btn.setToggleGroup(goalGroup);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(44);
            HBox.setHgrow(btn, Priority.ALWAYS);
            boolean selected = goal == store.primaryGoal;
            styleGoalCard(btn, selected);
            btn.selectedProperty().addListener((obs, was, isNow) -> styleGoalCard(btn, isNow));
            if (selected) btn.setSelected(true);
            goalRow.getChildren().add(btn);
        }
        VBox goalSection = new VBox(8, fieldLabel("Primary goal"), goalRow);

        TextField targetWeightField = new TextField(store.targetWeightKg > 0 ? String.valueOf(store.targetWeightKg) : "");
        targetWeightField.setPromptText("75");
        targetWeightField.setPrefHeight(38);

        ComboBox<Integer> weeklyWorkoutBox = new ComboBox<>();
        for (int i = 1; i <= 7; i++) weeklyWorkoutBox.getItems().add(i);
        weeklyWorkoutBox.setValue(store.weeklyWorkoutGoal);
        weeklyWorkoutBox.setMaxWidth(Double.MAX_VALUE);
        weeklyWorkoutBox.setPrefHeight(38);
        weeklyWorkoutBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer n) { return n == null ? "" : n + " workouts per week"; }
            @Override public Integer fromString(String s) { return weeklyWorkoutBox.getValue(); }
        });

        GridPane row1 = twoColumnGrid();
        row1.add(fieldLabel("Target weight (kg)"), 0, 0);
        row1.add(fieldLabel("Weekly workout goal"), 1, 0);
        row1.add(targetWeightField, 0, 1);
        row1.add(weeklyWorkoutBox, 1, 1);

        ComboBox<Integer> durationBox = new ComboBox<>();
        for (int mins : new int[]{60, 120, 180, 240, 300, 360}) durationBox.getItems().add(mins);
        durationBox.setValue(store.weeklyExerciseDurationMinutes);
        durationBox.setMaxWidth(Double.MAX_VALUE);
        durationBox.setPrefHeight(38);
        durationBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer n) { return n == null ? "" : n + " minutes"; }
            @Override public Integer fromString(String s) { return durationBox.getValue(); }
        });

        ComboBox<LocalProfileStore.FitnessLevel> experienceBox = new ComboBox<>();
        experienceBox.getItems().addAll(LocalProfileStore.FitnessLevel.values());
        experienceBox.setValue(store.experienceLevel);
        experienceBox.setMaxWidth(Double.MAX_VALUE);
        experienceBox.setPrefHeight(38);

        GridPane row2 = twoColumnGrid();
        row2.add(fieldLabel("Weekly exercise duration"), 0, 0);
        row2.add(fieldLabel("Experience level"), 1, 0);
        row2.add(durationBox, 0, 1);
        row2.add(experienceBox, 1, 1);

        VBox typesSection = new VBox(8, fieldLabel("Preferred workout types"),
                buildChipRow(WORKOUT_TYPES, store.preferredWorkoutTypes));
        VBox daysSection = new VBox(8, fieldLabel("Preferred workout days"),
                buildChipRow(WORKOUT_DAYS, store.preferredWorkoutDays));

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setPrefHeight(42);
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT
                + "; -fx-border-color: " + ACCENT + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 14px;");
        backButton.setOnAction(e -> container.getChildren().setAll(buildStep1()));

        Button completeButton = new Button("Complete profile");
        completeButton.setMaxWidth(Double.MAX_VALUE);
        completeButton.setPrefHeight(42);
        completeButton.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        completeButton.setOnAction(e -> {
            LocalProfileStore.PrimaryGoal goal = (LocalProfileStore.PrimaryGoal) (goalGroup.getSelectedToggle() != null
                    ? goalGroup.getSelectedToggle().getUserData() : null);
            String targetWeightText = targetWeightField.getText().trim();

            if (goal == null || targetWeightText.isEmpty()) {
                errorLabel.setText("Please choose a primary goal and enter a target weight.");
                errorLabel.setVisible(true);
                return;
            }
            if (store.preferredWorkoutTypes.isEmpty() || store.preferredWorkoutDays.isEmpty()) {
                errorLabel.setText("Pick at least one workout type and one workout day.");
                errorLabel.setVisible(true);
                return;
            }
            try {
                double newTargetWeight = Double.parseDouble(targetWeightText);
                if (newTargetWeight <= 0) {
                    errorLabel.setText("Target weight must be a positive number.");
                    errorLabel.setVisible(true);
                    return;
                }

                store.primaryGoal = goal;
                store.targetWeightKg = newTargetWeight;
                store.weeklyWorkoutGoal = weeklyWorkoutBox.getValue();
                store.weeklyExerciseDurationMinutes = durationBox.getValue();
                store.experienceLevel = experienceBox.getValue();

                // TODO: once login/register is merged, persist store's
                // fields via a real UserProfileDAO / UserGoalsDAO here.

                container.getChildren().setAll(buildDetailsView());

            } catch (NumberFormatException ex) {
                errorLabel.setText("Target weight must be a valid number.");
                errorLabel.setVisible(true);
            }
        });

        HBox buttonRow = new HBox(12, backButton, completeButton);
        HBox.setHgrow(backButton, Priority.ALWAYS);
        HBox.setHgrow(completeButton, Priority.ALWAYS);

        Label reassurance = new Label("You can update these goals later from your profile.");
        reassurance.setStyle("-fx-font-size: 11px; -fx-text-fill: " + MUTED_COLOR + ";");
        HBox reassuranceRow = new HBox(reassurance);
        reassuranceRow.setAlignment(Pos.CENTER);

        card.getChildren().addAll(goalSection, row1, row2, typesSection, daysSection, errorLabel, buttonRow, reassuranceRow);

        return wizardShell("Set your fitness goals", "Step 2 of 2", 1.0,
                "Choose your goals and preferences to personalise your fitness plan.", card);
    }

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

    private Node buildChipRow(String[] options, java.util.Set<String> selectedSet) {
        HBox row = new HBox(8);
        for (String option : options) {
            ToggleButton chip = new ToggleButton(option);
            boolean isSelected = selectedSet.contains(option);
            chip.setSelected(isSelected);
            chip.setPrefHeight(34);
            styleChip(chip, isSelected);
            chip.selectedProperty().addListener((obs, was, isNow) -> {
                styleChip(chip, isNow);
                if (isNow) selectedSet.add(option);
                else selectedSet.remove(option);
            });
            row.getChildren().add(chip);
        }
        return row;
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

        TextField emailField = new TextField(store.email);
        emailField.setPrefHeight(38);

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

        ComboBox<LocalProfileStore.FitnessLevel> fitnessBox = new ComboBox<>();
        fitnessBox.getItems().addAll(LocalProfileStore.FitnessLevel.values());
        fitnessBox.setValue(store.fitnessLevel);
        fitnessBox.setMaxWidth(Double.MAX_VALUE);
        fitnessBox.setPrefHeight(38);

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

        grid.add(fieldLabel("First name"), 0, 2);
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
            try {
                String name = firstNameField.getText().trim();
                String email = emailField.getText().trim();
                LocalProfileStore.Gender gender = genderBox.getValue();
                LocalDate dob = dobPicker.getValue();
                double newHeight = Double.parseDouble(heightField.getText().trim());
                double newWeight = Double.parseDouble(weightField.getText().trim());

                if (name.isEmpty() || email.isEmpty() || gender == null || dob == null
                        || newHeight <= 0 || newWeight <= 0) {
                    errorLabel.setText("Please check all fields are filled in with valid values.");
                    errorLabel.setVisible(true);
                    return;
                }
                if (!email.contains("@") || !email.contains(".")) {
                    errorLabel.setText("Enter a valid email address.");
                    errorLabel.setVisible(true);
                    return;
                }

                store.firstName = name;
                store.email = email;
                store.gender = gender;
                store.photoPath = photoPathHolder[0];
                store.dateOfBirth = dob;
                store.heightCm = newHeight;
                store.weightKg = newWeight;
                store.fitnessLevel = fitnessBox.getValue();

                container.getChildren().setAll(buildDetailsView());

            } catch (NumberFormatException ex) {
                errorLabel.setText("Height and weight must be valid numbers.");
                errorLabel.setVisible(true);
            }
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