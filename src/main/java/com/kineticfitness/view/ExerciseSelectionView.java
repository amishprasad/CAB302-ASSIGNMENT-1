package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ExerciseSelectionView implements Page {

    private static final String ORANGE = "#F97316";
    private static final String CONTENT_BG = "#F1F5F9";
    private static final String TITLE = "#1E293B";
    private static final String SUBTITLE = "#64748B";

    private static final String[] FILTERS = {
            "All", "Chest", "Back", "Legs", "Arms", "Shoulders", "Core"
    };

    private final List<ExerciseInfo> allExercises = List.of(
            // ---------- Chest ----------
            new ExerciseInfo("Bench Press", "Chest", "Barbell flat bench press, the standard chest builder.",
                    "Pectoralis major · Triceps · Anterior deltoids"),
            new ExerciseInfo("Incline Bench Press", "Chest", "Barbell press on an incline bench.",
                    "Upper pectoralis · Anterior deltoids · Triceps"),
            new ExerciseInfo("Decline Bench Press", "Chest", "Barbell press on a decline bench.",
                    "Lower pectoralis · Triceps"),
            new ExerciseInfo("Dumbbell Bench Press", "Chest", "Flat press with dumbbells for a deeper stretch.",
                    "Pectoralis major · Triceps · Anterior deltoids"),
            new ExerciseInfo("Incline Dumbbell Press", "Chest", "Dumbbell press on an incline bench.",
                    "Upper pectoralis · Anterior deltoids · Triceps"),
            new ExerciseInfo("Push-Up", "Chest", "Bodyweight chest press from the floor.",
                    "Pectoralis major · Triceps · Core"),
            new ExerciseInfo("Incline Push-Up", "Chest", "Push-up with hands raised — an easier variation.",
                    "Lower pectoralis · Triceps"),
            new ExerciseInfo("Decline Push-Up", "Chest", "Push-up with the feet raised.",
                    "Upper pectoralis · Anterior deltoids · Triceps"),
            new ExerciseInfo("Diamond Push-Up", "Chest", "Push-up with the hands close together.",
                    "Triceps · Pectoralis major"),
            new ExerciseInfo("Dumbbell Fly", "Chest", "Wide arc with dumbbells to stretch and isolate the chest.",
                    "Pectoralis major · Anterior deltoids"),
            new ExerciseInfo("Incline Dumbbell Fly", "Chest", "Fly performed on an incline bench.",
                    "Upper pectoralis · Anterior deltoids"),
            new ExerciseInfo("Cable Crossover", "Chest", "Cables drawn across the body at chest height.",
                    "Pectoralis major · Anterior deltoids"),
            new ExerciseInfo("Low Cable Fly", "Chest", "Cable fly moving from low to high.",
                    "Upper pectoralis · Anterior deltoids"),
            new ExerciseInfo("High Cable Fly", "Chest", "Cable fly moving from high to low.",
                    "Lower pectoralis"),
            new ExerciseInfo("Chest Dip", "Chest", "Bodyweight dip with a forward lean.",
                    "Lower pectoralis · Triceps · Anterior deltoids"),
            new ExerciseInfo("Machine Chest Press", "Chest", "Seated machine press with a fixed path.",
                    "Pectoralis major · Triceps"),
            new ExerciseInfo("Pec Deck", "Chest", "Machine fly through a fixed arc.",
                    "Pectoralis major"),
            new ExerciseInfo("Svend Press", "Chest", "Plate squeezed between the palms and pressed forward.",
                    "Pectoralis major"),
            new ExerciseInfo("Floor Press", "Chest", "Press from the floor, shortening the range of motion.",
                    "Pectoralis major · Triceps"),
            new ExerciseInfo("Dumbbell Pullover", "Chest", "Arc over the head while lying across a bench.",
                    "Pectoralis major · Latissimus dorsi · Serratus anterior"),
            new ExerciseInfo("Guillotine Press", "Chest", "Bench press to the neck with the elbows flared.",
                    "Upper pectoralis · Anterior deltoids"),
            new ExerciseInfo("Reverse-Grip Bench Press", "Chest", "Bench press taken with an underhand grip.",
                    "Upper pectoralis · Triceps"),
            new ExerciseInfo("Smith Machine Press", "Chest", "Press on a fixed vertical track.",
                    "Pectoralis major · Triceps"),
            new ExerciseInfo("Plyometric Push-Up", "Chest", "Explosive push-up leaving the floor.",
                    "Pectoralis major · Triceps · Core"),
            new ExerciseInfo("Single-Arm Cable Press", "Chest", "Press one arm at a time against a cable.",
                    "Pectoralis major · Anterior deltoids · Core"),

            // ---------- Back ----------
            new ExerciseInfo("Deadlift", "Back", "Full posterior chain barbell lift from the floor.",
                    "Erector spinae · Glutes · Hamstrings · Trapezius"),
            new ExerciseInfo("Sumo Deadlift", "Back", "Wide-stance deadlift with a more upright torso.",
                    "Glutes · Quadriceps · Erector spinae"),
            new ExerciseInfo("Rack Pull", "Back", "Partial deadlift starting from pins.",
                    "Erector spinae · Trapezius · Glutes"),
            new ExerciseInfo("Pull-Up", "Back", "Bodyweight vertical pull with an overhand grip.",
                    "Latissimus dorsi · Biceps · Rhomboids"),
            new ExerciseInfo("Chin-Up", "Back", "Vertical pull with an underhand grip.",
                    "Latissimus dorsi · Biceps"),
            new ExerciseInfo("Lat Pulldown", "Back", "Cable pull to the upper chest.",
                    "Latissimus dorsi · Biceps · Rhomboids"),
            new ExerciseInfo("Close-Grip Pulldown", "Back", "Pulldown with a narrow neutral grip.",
                    "Latissimus dorsi · Biceps"),
            new ExerciseInfo("Straight-Arm Pulldown", "Back", "Cable pushdown with the arms locked straight.",
                    "Latissimus dorsi · Triceps"),
            new ExerciseInfo("Single-Arm Lat Pulldown", "Back", "Cable pulldown worked one side at a time.",
                    "Latissimus dorsi · Biceps"),
            new ExerciseInfo("Barbell Row", "Back", "Bent-over row building mid-back thickness.",
                    "Latissimus dorsi · Rhomboids · Trapezius · Biceps"),
            new ExerciseInfo("Pendlay Row", "Back", "Row from a dead stop on the floor each rep.",
                    "Latissimus dorsi · Rhomboids · Trapezius"),
            new ExerciseInfo("Dumbbell Row", "Back", "Single-arm row braced on a bench.",
                    "Latissimus dorsi · Rhomboids · Biceps"),
            new ExerciseInfo("Meadows Row", "Back", "Single-arm row using a landmine bar.",
                    "Latissimus dorsi · Rhomboids · Trapezius"),
            new ExerciseInfo("Seated Cable Row", "Back", "Horizontal cable pull for the mid-back.",
                    "Rhomboids · Latissimus dorsi · Biceps"),
            new ExerciseInfo("T-Bar Row", "Back", "Row with a landmine bar and a close grip.",
                    "Latissimus dorsi · Rhomboids · Trapezius"),
            new ExerciseInfo("Inverted Row", "Back", "Bodyweight row hanging under a fixed bar.",
                    "Rhomboids · Latissimus dorsi · Biceps"),
            new ExerciseInfo("Chest-Supported Row", "Back", "Row lying face-down on an incline bench.",
                    "Rhomboids · Latissimus dorsi"),
            new ExerciseInfo("Renegade Row", "Back", "Row performed from a plank position.",
                    "Latissimus dorsi · Core · Rhomboids"),
            new ExerciseInfo("Back Extension", "Back", "Hip extension over a bench or roman chair.",
                    "Erector spinae · Glutes · Hamstrings"),
            new ExerciseInfo("Good Morning", "Back", "Hip hinge with a barbell across the back.",
                    "Erector spinae · Hamstrings · Glutes"),
            new ExerciseInfo("Seal Row", "Back", "Row lying face-down on a raised bench.",
                    "Latissimus dorsi · Rhomboids · Trapezius"),
            new ExerciseInfo("Kroc Row", "Back", "Heavy, high-rep single-arm dumbbell row.",
                    "Latissimus dorsi · Trapezius · Biceps"),
            new ExerciseInfo("Cable Pullover", "Back", "Standing pullover on a high cable.",
                    "Latissimus dorsi"),
            new ExerciseInfo("Gorilla Row", "Back", "Alternating row from kettlebells on the floor.",
                    "Latissimus dorsi · Rhomboids · Core"),
            new ExerciseInfo("Hyperextension Hold", "Back", "Isometric hold at the top of a back extension.",
                    "Erector spinae · Glutes"),

            // ---------- Legs ----------
            new ExerciseInfo("Back Squat", "Legs", "Barbell squat with the bar across the upper back.",
                    "Quadriceps · Glutes · Hamstrings · Core"),
            new ExerciseInfo("Front Squat", "Legs", "Squat with the barbell racked on the shoulders.",
                    "Quadriceps · Glutes · Core"),
            new ExerciseInfo("Goblet Squat", "Legs", "Squat holding a dumbbell at the chest.",
                    "Quadriceps · Glutes"),
            new ExerciseInfo("Bulgarian Split Squat", "Legs", "Single-leg squat with the rear foot elevated.",
                    "Quadriceps · Glutes"),
            new ExerciseInfo("Lunges", "Legs", "Walking or stationary lunge, worked per leg.",
                    "Quadriceps · Glutes · Hamstrings"),
            new ExerciseInfo("Reverse Lunge", "Legs", "Step backward into a lunge, easier on the knees.",
                    "Glutes · Quadriceps"),
            new ExerciseInfo("Walking Lunge", "Legs", "Continuous lunges moving forward.",
                    "Quadriceps · Glutes · Hamstrings"),
            new ExerciseInfo("Step-Up", "Legs", "Step onto a box or bench, one leg at a time.",
                    "Quadriceps · Glutes"),
            new ExerciseInfo("Leg Press", "Legs", "Machine press through a fixed track.",
                    "Quadriceps · Glutes · Hamstrings"),
            new ExerciseInfo("Hack Squat", "Legs", "Machine squat with the back supported.",
                    "Quadriceps · Glutes"),
            new ExerciseInfo("Romanian Deadlift", "Legs", "Hip hinge with a slight knee bend.",
                    "Hamstrings · Glutes · Erector spinae"),
            new ExerciseInfo("Stiff-Leg Deadlift", "Legs", "Hinge with the legs kept nearly straight.",
                    "Hamstrings · Glutes"),
            new ExerciseInfo("Lying Leg Curl", "Legs", "Machine knee flexion lying face-down.",
                    "Hamstrings"),
            new ExerciseInfo("Seated Leg Curl", "Legs", "Machine knee flexion from a seated position.",
                    "Hamstrings"),
            new ExerciseInfo("Leg Extension", "Legs", "Machine isolation for the quadriceps.",
                    "Quadriceps"),
            new ExerciseInfo("Hip Thrust", "Legs", "Barbell hip extension with the back on a bench.",
                    "Glutes · Hamstrings"),
            new ExerciseInfo("Glute Bridge", "Legs", "Hip extension performed on the floor.",
                    "Glutes · Hamstrings"),
            new ExerciseInfo("Standing Calf Raise", "Legs", "Calf raise with the knees straight.",
                    "Gastrocnemius · Soleus"),
            new ExerciseInfo("Seated Calf Raise", "Legs", "Calf raise with the knees bent.",
                    "Soleus"),
            new ExerciseInfo("Box Jump", "Legs", "Explosive two-footed jump onto a box.",
                    "Quadriceps · Glutes · Calves"),
            new ExerciseInfo("Sissy Squat", "Legs", "Knee-dominant squat leaning backwards.",
                    "Quadriceps"),
            new ExerciseInfo("Nordic Curl", "Legs", "Eccentric hamstring lowering with the ankles fixed.",
                    "Hamstrings"),
            new ExerciseInfo("Adductor Machine", "Legs", "Seated squeeze for the inner thighs.",
                    "Adductors"),
            new ExerciseInfo("Abductor Machine", "Legs", "Seated press for the outer hips.",
                    "Gluteus medius · Abductors"),
            new ExerciseInfo("Jump Squat", "Legs", "Bodyweight squat finished with an explosive jump.",
                    "Quadriceps · Glutes · Calves"),

            // ---------- Arms ----------
            new ExerciseInfo("Barbell Curl", "Arms", "Standing curl with a straight barbell.",
                    "Biceps brachii · Brachialis"),
            new ExerciseInfo("Dumbbell Curl", "Arms", "Alternating or simultaneous dumbbell curl.",
                    "Biceps brachii · Brachialis"),
            new ExerciseInfo("Hammer Curl", "Arms", "Curl with a neutral grip throughout.",
                    "Brachialis · Brachioradialis · Biceps brachii"),
            new ExerciseInfo("EZ-Bar Curl", "Arms", "Curl on an angled bar, easier on the wrists.",
                    "Biceps brachii · Brachialis"),
            new ExerciseInfo("Preacher Curl", "Arms", "Curl with the upper arms braced on a pad.",
                    "Biceps brachii · Brachialis"),
            new ExerciseInfo("Incline Dumbbell Curl", "Arms", "Curl lying back on an incline bench.",
                    "Biceps brachii"),
            new ExerciseInfo("Concentration Curl", "Arms", "Seated single-arm curl braced against the thigh.",
                    "Biceps brachii"),
            new ExerciseInfo("Cable Curl", "Arms", "Curl against constant cable tension.",
                    "Biceps brachii · Brachialis"),
            new ExerciseInfo("Spider Curl", "Arms", "Curl hanging over the top of an incline bench.",
                    "Biceps brachii"),
            new ExerciseInfo("Reverse Curl", "Arms", "Curl with an overhand grip.",
                    "Brachioradialis · Brachialis"),
            new ExerciseInfo("Close-Grip Bench Press", "Arms", "Bench press with a narrow grip.",
                    "Triceps brachii · Pectoralis major"),
            new ExerciseInfo("Tricep Dip", "Arms", "Bodyweight dip with an upright torso.",
                    "Triceps brachii · Anterior deltoids"),
            new ExerciseInfo("Bench Dip", "Arms", "Dip with the hands behind you on a bench.",
                    "Triceps brachii"),
            new ExerciseInfo("Tricep Pushdown", "Arms", "Cable pushdown with a straight bar.",
                    "Triceps brachii"),
            new ExerciseInfo("Rope Pushdown", "Arms", "Pushdown with a rope, spread at the bottom.",
                    "Triceps brachii"),
            new ExerciseInfo("Skull Crusher", "Arms", "Lying barbell extension to the forehead.",
                    "Triceps brachii"),
            new ExerciseInfo("Overhead Tricep Extension", "Arms", "Extension with the arms overhead.",
                    "Triceps brachii"),
            new ExerciseInfo("Tricep Kickback", "Arms", "Bent-over single-arm extension.",
                    "Triceps brachii"),
            new ExerciseInfo("Wrist Curl", "Arms", "Seated forearm flexion over the knees.",
                    "Wrist flexors"),
            new ExerciseInfo("Reverse Wrist Curl", "Arms", "Seated forearm extension over the knees.",
                    "Wrist extensors"),
            new ExerciseInfo("Zottman Curl", "Arms", "Curl up, rotate, then lower with an overhand grip.",
                    "Biceps brachii · Brachioradialis"),
            new ExerciseInfo("Drag Curl", "Arms", "Curl keeping the bar dragging up the torso.",
                    "Biceps brachii"),
            new ExerciseInfo("JM Press", "Arms", "Hybrid of a close-grip press and a skull crusher.",
                    "Triceps brachii"),
            new ExerciseInfo("Single-Arm Cable Extension", "Arms", "Overhead extension worked one arm at a time.",
                    "Triceps brachii"),
            new ExerciseInfo("Farmer's Carry", "Arms", "Walk carrying heavy weights at the sides.",
                    "Forearms · Trapezius · Core"),

            // ---------- Shoulders ----------
            new ExerciseInfo("Overhead Press", "Shoulders", "Standing barbell press from the shoulders.",
                    "Anterior deltoids · Lateral deltoids · Triceps"),
            new ExerciseInfo("Seated Dumbbell Press", "Shoulders", "Overhead press seated with back support.",
                    "Anterior deltoids · Lateral deltoids · Triceps"),
            new ExerciseInfo("Arnold Press", "Shoulders", "Overhead press with a rotating grip.",
                    "Anterior deltoids · Lateral deltoids · Triceps"),
            new ExerciseInfo("Push Press", "Shoulders", "Overhead press driven with the legs.",
                    "Anterior deltoids · Triceps · Quadriceps"),
            new ExerciseInfo("Machine Shoulder Press", "Shoulders", "Seated machine press with a fixed path.",
                    "Anterior deltoids · Triceps"),
            new ExerciseInfo("Landmine Press", "Shoulders", "Angled single-arm press with a landmine bar.",
                    "Anterior deltoids · Upper pectoralis"),
            new ExerciseInfo("Lateral Raise", "Shoulders", "Dumbbell raise out to the side.",
                    "Lateral deltoids"),
            new ExerciseInfo("Cable Lateral Raise", "Shoulders", "Side raise against constant cable tension.",
                    "Lateral deltoids"),
            new ExerciseInfo("Leaning Lateral Raise", "Shoulders", "Side raise leaning away from a support.",
                    "Lateral deltoids"),
            new ExerciseInfo("Machine Lateral Raise", "Shoulders", "Seated machine side raise.",
                    "Lateral deltoids"),
            new ExerciseInfo("Front Raise", "Shoulders", "Dumbbell raise to the front.",
                    "Anterior deltoids"),
            new ExerciseInfo("Plate Front Raise", "Shoulders", "Front raise holding a weight plate.",
                    "Anterior deltoids"),
            new ExerciseInfo("Rear Delt Fly", "Shoulders", "Bent-over fly targeting the rear deltoids.",
                    "Rear deltoids · Rhomboids"),
            new ExerciseInfo("Reverse Pec Deck", "Shoulders", "Machine fly reversed for the rear deltoids.",
                    "Rear deltoids · Rhomboids"),
            new ExerciseInfo("Face Pull", "Shoulders", "Rope pulled to the face at eye level.",
                    "Rear deltoids · Trapezius · Rhomboids"),
            new ExerciseInfo("Upright Row", "Shoulders", "Vertical pull to chest height.",
                    "Lateral deltoids · Trapezius"),
            new ExerciseInfo("Barbell Shrug", "Shoulders", "Shoulder elevation holding a barbell.",
                    "Trapezius"),
            new ExerciseInfo("Dumbbell Shrug", "Shoulders", "Shoulder elevation holding dumbbells.",
                    "Trapezius"),
            new ExerciseInfo("Pike Push-Up", "Shoulders", "Bodyweight press from a piked position.",
                    "Anterior deltoids · Triceps"),
            new ExerciseInfo("Cuban Rotation", "Shoulders", "External rotation drill for shoulder health.",
                    "Rotator cuff · Rear deltoids"),
            new ExerciseInfo("Handstand Push-Up", "Shoulders", "Inverted bodyweight press against a wall.",
                    "Anterior deltoids · Triceps"),
            new ExerciseInfo("Behind-the-Neck Press", "Shoulders", "Barbell press from behind the head.",
                    "Lateral deltoids · Anterior deltoids · Triceps"),
            new ExerciseInfo("Bradford Press", "Shoulders", "Press alternating front and back over the head.",
                    "Anterior deltoids · Lateral deltoids"),
            new ExerciseInfo("Y-Raise", "Shoulders", "Raise into a Y shape on an incline bench.",
                    "Lower trapezius · Rear deltoids"),
            new ExerciseInfo("Bus Driver", "Shoulders", "Plate rotated side to side at arm's length.",
                    "Anterior deltoids"),

            // ---------- Core ----------
            new ExerciseInfo("Plank", "Core", "Isometric hold on the forearms and toes.",
                    "Transverse abdominis · Rectus abdominis · Obliques"),
            new ExerciseInfo("Side Plank", "Core", "Isometric hold balanced on one forearm.",
                    "Obliques · Transverse abdominis"),
            new ExerciseInfo("Hollow Body Hold", "Core", "Isometric hold in a hollowed position.",
                    "Rectus abdominis · Transverse abdominis"),
            new ExerciseInfo("Sit-Up", "Core", "Full trunk flexion from the floor.",
                    "Rectus abdominis · Hip flexors"),
            new ExerciseInfo("Crunch", "Core", "Partial trunk flexion, shoulders off the floor.",
                    "Rectus abdominis"),
            new ExerciseInfo("Cable Crunch", "Core", "Kneeling crunch against a cable.",
                    "Rectus abdominis · Obliques"),
            new ExerciseInfo("Bicycle Crunch", "Core", "Alternating knee-to-elbow crunch.",
                    "Obliques · Rectus abdominis"),
            new ExerciseInfo("Russian Twist", "Core", "Seated rotation from side to side.",
                    "Obliques · Rectus abdominis"),
            new ExerciseInfo("Woodchopper", "Core", "Diagonal cable chop across the body.",
                    "Obliques · Transverse abdominis"),
            new ExerciseInfo("Pallof Press", "Core", "Anti-rotation press against a cable.",
                    "Obliques · Transverse abdominis"),
            new ExerciseInfo("Hanging Leg Raise", "Core", "Straight-leg raise hanging from a bar.",
                    "Lower rectus abdominis · Hip flexors"),
            new ExerciseInfo("Hanging Knee Raise", "Core", "Knee raise hanging from a bar.",
                    "Lower rectus abdominis · Hip flexors"),
            new ExerciseInfo("Lying Leg Raise", "Core", "Leg raise performed on the floor.",
                    "Lower rectus abdominis · Hip flexors"),
            new ExerciseInfo("Flutter Kick", "Core", "Small alternating leg kicks while lying down.",
                    "Lower rectus abdominis · Hip flexors"),
            new ExerciseInfo("V-Up", "Core", "Legs and torso raised together to meet.",
                    "Rectus abdominis · Hip flexors"),
            new ExerciseInfo("Toe Touch", "Core", "Reach for the toes with the legs raised.",
                    "Upper rectus abdominis"),
            new ExerciseInfo("Mountain Climber", "Core", "Dynamic plank driving the knees forward.",
                    "Rectus abdominis · Obliques · Hip flexors"),
            new ExerciseInfo("Ab Wheel Rollout", "Core", "Rollout from the knees with a wheel.",
                    "Rectus abdominis · Transverse abdominis · Latissimus dorsi"),
            new ExerciseInfo("Dead Bug", "Core", "Opposite arm and leg extended while lying down.",
                    "Transverse abdominis · Rectus abdominis"),
            new ExerciseInfo("Bird Dog", "Core", "Opposite arm and leg extended on all fours.",
                    "Erector spinae · Transverse abdominis · Glutes"),
            new ExerciseInfo("Reverse Crunch", "Core", "Hips curled off the floor towards the chest.",
                    "Lower rectus abdominis"),
            new ExerciseInfo("Dragon Flag", "Core", "Body lowered straight, pivoting from the shoulders.",
                    "Rectus abdominis · Transverse abdominis"),
            new ExerciseInfo("Windshield Wiper", "Core", "Legs swept side to side while hanging.",
                    "Obliques · Rectus abdominis"),
            new ExerciseInfo("L-Sit", "Core", "Seated hold with the legs extended off the floor.",
                    "Rectus abdominis · Hip flexors"),
            new ExerciseInfo("Suitcase Carry", "Core", "Walk carrying weight on one side only.",
                    "Obliques · Transverse abdominis")


    );

    private final VBox exerciseListContainer = new VBox(10);
    private final List<Button> chipButtons = new ArrayList<>();

    @Override
    public String label() {
        return "Exercise Library";
    }

    @Override
    public Node getContent() {
        Label title = new Label("Exercise Library");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label subtitle = new Label("Browse exercises by body part before logging a workout.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SUBTITLE + ";");

        renderExercises("All");

        VBox content = new VBox(16, title, subtitle, buildFilterChips(), exerciseListContainer);
        content.setPadding(new Insets(32, 40, 32, 40));
        content.setStyle("-fx-background-color: " + CONTENT_BG + ";");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: " + CONTENT_BG + "; -fx-background: " + CONTENT_BG + ";");
        return scrollPane;
    }

    private HBox buildFilterChips() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        for (String filter : FILTERS) {
            Button chip = new Button(filter);
            chip.setPadding(new Insets(6, 16, 6, 16));
            applyChipStyle(chip, filter.equals("All"));
            chip.setOnAction(e -> selectFilter(filter));
            chipButtons.add(chip);
            row.getChildren().add(chip);
        }
        return row;
    }

    private void applyChipStyle(Button chip, boolean active) {
        if (active) {
            chip.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                    + " -fx-font-weight: bold; -fx-background-radius: 16; -fx-font-size: 13px;");
        } else {
            chip.setStyle("-fx-background-color: white; -fx-text-fill: " + TITLE + ";"
                    + " -fx-border-color: #E2E8F0; -fx-border-radius: 16; -fx-background-radius: 16;"
                    + " -fx-font-size: 13px;");
        }
    }

    private void selectFilter(String filter) {
        for (Button chip : chipButtons) {
            applyChipStyle(chip, chip.getText().equals(filter));
        }
        renderExercises(filter);
    }

    /**
     * Pure filtering logic (unit-testable): "All" returns everything, otherwise matches body part.
     */
    public static List<ExerciseInfo> filterByPart(List<ExerciseInfo> exercises, String part) {
        if ("All".equals(part)) {
            return new ArrayList<>(exercises);
        }
        return exercises.stream()
                .filter(ex -> ex.getBodyPart().equals(part))
                .collect(Collectors.toList());
    }

    private void renderExercises(String filter) {
        exerciseListContainer.getChildren().clear();
        for (ExerciseInfo exercise : filterByPart(allExercises, filter)) {
            exerciseListContainer.getChildren().add(buildExerciseCard(exercise));
        }
    }

    private VBox buildExerciseCard(ExerciseInfo exercise) {
        Label nameLabel = new Label(exercise.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TITLE + ";");

        Label bodyPartTag = new Label(exercise.getBodyPart());
        bodyPartTag.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;"
                + " -fx-background-color: " + ORANGE + "; -fx-background-radius: 10; -fx-padding: 2 8 2 8;");

        Label descriptionLabel = new Label(exercise.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + SUBTITLE + ";");

        Label musclesLabel = new Label("Muscles worked: " + exercise.getMuscles());
        musclesLabel.setWrapText(true);
        musclesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569; -fx-font-style: italic;");

        VBox card = new VBox(6, nameLabel, bodyPartTag, descriptionLabel, musclesLabel);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + " -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        return card;
    }

    /**
     * Display-only exercise info for browsing (name, body part, description).
     */
    public static class ExerciseInfo {
        private final String name;
        private final String bodyPart;
        private final String description;
        private final String muscles;

        public ExerciseInfo(String name, String bodyPart, String description, String muscles) {
            this.name = name;
            this.bodyPart = bodyPart;
            this.description = description;
            this.muscles = muscles;
        }

        public String getName() {
            return name;
        }

        public String getBodyPart() {
            return bodyPart;
        }

        public String getDescription() {
            return description;
        }

        public String getMuscles() {
            return muscles;
        }
    }
}