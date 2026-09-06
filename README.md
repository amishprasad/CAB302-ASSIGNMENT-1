# Kinetic Fitness — Early Prototype

This is a **very early, console-based prototype** of the Kinetic Fitness desktop application. It exists to prove out the core data model and "Must have" feature logic before any GUI, persistence, or AI work begins.

## What this prototype includes

Only the **Must have** features from the project's feature table are implemented:

- Enter fitness level and set a goal (currently weekly session target)
- Realistic goal suggestion based on stated fitness level
- Log workouts, steps, sets, and reps (user-inputted)
- Progress summarised into a simple visual (ASCII bar graph)

## What this prototype does NOT include (yet)

- No GUI — this is a console/CLI app only
- No data persistence — all data resets when the app closes
- No AI recommendations, chatbot, dashboard, wearable sync, gamification, or any Should/Could-have features
- No automated test suite yet

These are intentionally out of scope for this first commit and will be layered in incrementally.

## Project structure

```
kinetic-fitness/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    └── main/
        └── java/
            └── com/
                └── kineticfitness/
                    ├── Main.java                # CLI entry point
                    ├── model/
                    │   ├── FitnessLevel.java
                    │   ├── GoalTimeframe.java
                    │   ├── Goal.java
                    │   ├── Workout.java
                    │   └── User.java
                    └── service/
                        └── FitnessTracker.java  # Core logic (goal suggestions, progress graph)
```

## Requirements

- Java 17 or later (JDK, not just JRE)
- Maven (optional — plain `javac` works fine too)

## Running it

### Option A: Plain javac (no Maven needed)

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.kineticfitness.Main
```

### Option B: Maven

```bash
mvn compile exec:java -Dexec.mainClass="com.kineticfitness.Main"
```

or build a runnable jar:

```bash
mvn package
java -jar target/kinetic-fitness.jar
```

## Example session

```
=== Welcome to Kinetic Fitness (Prototype) ===
Enter your name: Maya
Enter your fitness level (BEGINNER, INTERMEDIATE, ADVANCED): INTERMEDIATE

Based on your fitness level (INTERMEDIATE), a realistic weekly target is 4 session(s).
Set your weekly session goal (press Enter to accept suggestion of 4):

--- Menu ---
1. Log a workout
2. View progress summary
3. Exit
Choose an option: 1
Activity type (e.g. Run, Strength, Steps): Run
Sets (0 if not applicable): 0
Reps (0 if not applicable): 0
Steps (0 if not applicable): 3000
Duration in minutes: 25
Logged: 2026-09-06 | Run | sets=0 reps=0 steps=3000 duration=25min

--- Menu ---
1. Log a workout
2. View progress summary
3. Exit
Choose an option: 2

--- Progress Summary for Maya ---
WEEKLY     General activity     [#####---------------] 1/4
Total workouts logged: 1
```

## Roadmap (next steps, not yet built)

1. Swap console I/O for a real desktop GUI (JavaFX or Swing)
2. Add persistence (local file or embedded DB) so data survives restarts
3. Support multiple concurrent goals per timeframe (daily/weekly/monthly/annual), not just one
4. Build out Should-have features: user profiles, activity reminders, visual reports, dashboard/homepage, FAQ/help page, AI recommendations, chatbot
5. Layer in Could-have features once the above are stable: gamification, wearable sync, calorie tracking, offline mode, etc.

## License

Not yet decided — add a LICENSE file before making the repo public if needed.
