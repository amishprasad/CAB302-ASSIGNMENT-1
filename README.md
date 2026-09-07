# Kinetic Fitness — Basic App Prototype (Windowed)

This is a **very early, GUI-based prototype** of the Kinetic Fitness desktop application, built with Java Swing. Its only purpose is to demonstrate a working window with sidebar navigation between the app's planned screens.

## What this prototype includes

- A real desktop window (Swing) that opens on launch
- A sidebar with 5 navigation buttons: **Dashboard, Goals, Workout Log, Progress, Profile**
- Clicking a sidebar button switches the visible screen (CardLayout) — no page reload, just an in-app view swap
- Each screen has placeholder layout/fields reflecting a "Must have" feature area, so the shape of the app is visible even though nothing is functional yet

## What this prototype does NOT include (yet)

- **No backend logic** — form fields and buttons don't save, calculate, or persist anything. Buttons like "Save Goal" and "Log Workout" just show a placeholder dialog confirming they're not wired up yet.
- No data persistence — nothing survives closing the app
- No AI recommendations, chatbot, dashboard metrics, wearable sync, gamification, or any Should/Could-have features
- No automated test suite yet

This is intentional — the goal of this commit is *only* to prove the window shell and navigation work, before any real logic is added.

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
                    ├── Main.java                 # Entry point — launches the GUI
                    └── ui/
                        ├── MainFrame.java         # Main window: sidebar + CardLayout content area
                        ├── NavigationPanel.java   # Sidebar buttons
                        ├── DashboardPanel.java    # Placeholder screen
                        ├── GoalsPanel.java        # Placeholder screen
                        ├── WorkoutLogPanel.java   # Placeholder screen
                        ├── ProgressPanel.java     # Placeholder screen
                        └── ProfilePanel.java      # Placeholder screen
```

## Requirements

- Java 17 or later (a full JDK with GUI/AWT support — not a headless-only JDK)
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

A window titled **"Kinetic Fitness (Prototype)"** should open with a dark sidebar on the left and a content area on the right. Click any sidebar button to switch screens.

> Note: this was built and compile-verified in a headless CI-style environment, so it hasn't been visually screenshotted — but it uses only standard Swing components, so it will render normally on any machine with a standard desktop JDK.

## Roadmap (next steps, not yet built)

1. Wire up real logic behind the Goals and Workout Log forms (currently just UI placeholders)
2. Add persistence (local file or embedded DB) so data survives restarts
3. Make the Dashboard and Progress screens reflect real user data instead of static placeholders
4. Build out Should-have features: user profiles, activity reminders, visual reports, FAQ/help page, AI recommendations, chatbot
5. Layer in Could-have features once the above are stable: gamification, wearable sync, calorie tracking, offline mode, etc.

## License

Not yet decided — add a LICENSE file before making the repo public if needed.
