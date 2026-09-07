# Kinetic Fitness — Basic App Prototype

This is a very early, GUI-based prototype of the Kinetic Fitness desktop application, built with Java Swing. Its only purpose is to demonstrate a working window with sidebar navigation between the app's planned screens.

## What this prototype includes

- A real desktop window (Swing) that opens on launch
- A sidebar with 5 navigation buttons: Dashboard, Goals, Workout Log, Progress, Profile
- Clicking a sidebar button switches the visible screen (CardLayout) — no page reload, just an in-app view swap
- Each screen has placeholder layout/fields reflecting a "Must have" feature area, so the shape of the app is visible even though nothing is functional yet

## What this prototype does NOT include (yet)

- No backend logic — form fields and buttons don't save, calculate, or persist anything. Buttons like "Save Goal" and "Log Workout" just show a placeholder dialog confirming they're not wired up yet.
- No data persistence
- No automated test suite yet

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

## Roadmap

1. Wire up real logic behind the Goals and Workout Log forms (currently just UI placeholders)
2. Add persistence (local file or embedded DB) so data survives restarts
3. Make the Dashboard and Progress screens reflect real user data instead of static placeholders
4. Build out Should-have features: user profiles, activity reminders, visual reports, FAQ/help page, AI recommendations, chatbot
5. Layer in Could-have features once the above are stable: gamification, wearable sync, calorie tracking, offline mode, etc.


