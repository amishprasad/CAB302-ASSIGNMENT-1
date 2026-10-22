# Kinetic Fitness

Kinetic Fitness is a JavaFX desktop application developed for CAB302 by Team Xforce. It brings fitness profiles, workout logging, scheduling, goals and progress tracking into one application.

## Main Features

- Account registration, login and logout.
- Create and edit personal profiles and fitness goals.
- Browse an exercise library and log workout sessions.
- View workout history and training statistics.
- Schedule, move, complete and skip workouts.
- Set, edit and dismiss activity reminders.
- Log meals, estimate nutrition for supported foods and remove meal entries.
- Autocomplete for exercise, workout and meal names.
- Automatic date/time formatting and input validation.
- Settings for units and notification preferences.

## Requirements

- JDK 21
- Apache Maven
- A desktop environment capable of displaying JavaFX windows

The first Maven build requires internet access to download dependencies. Terminal commands below require Maven to be installed and available on your PATH.

## Getting Started

Clone the repository and open its directory:

```bash
git clone https://github.com/amishprasad/CAB302-ASSIGNMENT-1.git
cd CAB302-ASSIGNMENT-1
```
Register an account, sign in and complete your profile.

The application creates its SQLite database automatically in the working directory. No separate database server is required.

## Testing and Documentation

Run automated tests:

```bash
mvn test
```

Generate Javadoc:

```bash
mvn javadoc:javadoc
```

Generated outputs:

| Output | Location |
| --- | --- |
| Test reports | `target/surefire-reports/` |
| JaCoCo coverage report | `target/site/jacoco/index.html` |
| Javadoc | `target/site/apidocs/index.html` |

Tests cover models, validation, calculations, database operations and reminder behaviour. 

## Continuous Integration

GitHub Actions builds and tests pushes to all branches and pull requests targeting `main`. The workflow also generates coverage reports and Javadoc.

Configuration: `.github/workflows/build.yml`

## Project Structure

```text
src/main/java/com/kineticfitness/
  db/         Database access
  model/      Application data and domain behaviour
  service/    Validation, reminders and input assistance
  session/    Current user session
  util/       Shared calculations and filtering
  view/       JavaFX screens and controls

src/test/java/   Automated tests
docs/           Project documentation and evidence
pom.xml         Maven build configuration
```

## Docs and test

- User testing: `docs/User-Test.md`
- Change history: `docs/change-log.md`
- Coverage evidence: `docs/Coverage-Log.md`
- Automated tests: `src/test/java/`
- CI configuration: `.github/workflows/build.yml`
- Refactoring and development history: repository commits and pull requests


## Team Workflow

Develop changes on feature or fix branches, open a pull request, review the changes and check CI results before merging into `main`.

## Team

Abin, Adriel, Amish, Ebin, Helvin, Jifin and Junxi.
