package com.kineticfitness;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.Goal;
import com.kineticfitness.model.GoalTimeframe;
import com.kineticfitness.model.User;
import com.kineticfitness.model.Workout;
import com.kineticfitness.service.FitnessTracker;

import java.util.Scanner;

/**
 * Kinetic Fitness - Very Early Prototype (Console Version)
 *
 * Demonstrates the core "Must have" features only:
 *  - User enters fitness level and goals (daily/weekly/monthly/annual)
 *  - Realistic goal suggestion
 *  - Workout / steps / sets / reps tracking (user-inputted)
 *  - Summarisation of progress into a simple visual (ASCII) graph
 *
 * This is a starting point for the real desktop application, not a
 * finished product. No persistence, GUI, or AI features yet.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FitnessTracker tracker = new FitnessTracker();

        System.out.println("=== Welcome to Kinetic Fitness (Prototype) ===");

        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();

        FitnessLevel level = promptFitnessLevel(scanner);
        User user = new User(name, level);

        int suggested = tracker.suggestRealisticWeeklySessions(level);
        System.out.println("\nBased on your fitness level (" + level + "), a realistic weekly target is "
                + suggested + " session(s).");

        System.out.print("Set your weekly session goal (press Enter to accept suggestion of "
                + suggested + "): ");
        String goalInput = scanner.nextLine().trim();
        int weeklyTarget = goalInput.isEmpty() ? suggested : Integer.parseInt(goalInput);

        user.addGoal(new Goal(GoalTimeframe.WEEKLY, "General activity", weeklyTarget));

        boolean running = true;
        while (running) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Log a workout");
            System.out.println("2. View progress summary");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    logWorkout(scanner, tracker, user);
                    break;
                case "2":
                    System.out.println(tracker.buildProgressGraph(user));
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, try again.");
            }
        }

        System.out.println("Thanks for using Kinetic Fitness. Keep moving!");
        scanner.close();
    }

    private static FitnessLevel promptFitnessLevel(Scanner scanner) {
        while (true) {
            System.out.print("Enter your fitness level (BEGINNER, INTERMEDIATE, ADVANCED): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return FitnessLevel.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Not recognised. Please type BEGINNER, INTERMEDIATE, or ADVANCED.");
            }
        }
    }

    private static void logWorkout(Scanner scanner, FitnessTracker tracker, User user) {
        System.out.print("Activity type (e.g. Run, Strength, Steps): ");
        String type = scanner.nextLine().trim();

        int sets = readInt(scanner, "Sets (0 if not applicable): ");
        int reps = readInt(scanner, "Reps (0 if not applicable): ");
        int steps = readInt(scanner, "Steps (0 if not applicable): ");
        int duration = readInt(scanner, "Duration in minutes: ");

        Workout workout = new Workout(type, sets, reps, steps, duration);
        tracker.logWorkout(user, workout);

        System.out.println("Logged: " + workout);
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return input.isEmpty() ? 0 : Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}
