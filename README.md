# Kinetic Fitness

Desktop fitness tracking app for CAB302 (Agile Software Engineering).
Built with Java 21, JavaFX and Maven.

## Requirements
- JDK 21 (Amazon Corretto 21 recommended)
- Maven (or use IntelliJ's bundled Maven)

## Project structure
```
src/main/java/module-info.java             # module declaration
src/main/java/com/kineticfitness/Main.java # JavaFX entry point
src/main/java/com/kineticfitness/model/    # domain classes (User, Workout, Exercise, Goal, FitnessLevel)
src/test/java/com/kineticfitness/model/    # JUnit 5 tests
```

## Team workflow
- Branch off `main` for every feature: `git checkout -b feature/<name>-<feature>`
- Open a Pull Request, get one teammate to review, then merge.
- Never commit directly to `main`.
