package com.kineticfitness.service;

import com.kineticfitness.model.Exercise;
import com.kineticfitness.model.Workout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Turns a user's saved workouts into CSV text for the Settings page's export.
 *
 * <p>Kept out of the view so the format can be unit tested and the file chooser code
 * stays the only JavaFX in the export path. Output has one row per exercise, oldest
 * workout first, so it opens as a flat table in Excel or Sheets.</p>
 *
 * <p>Fields follow RFC 4180 quoting. Free-text names that start with {@code = + - @}
 * are prefixed with an apostrophe so a spreadsheet shows them as text instead of
 * running them as a formula (CSV injection).</p>
 */
public final class ExportService {

    private static final String NEWLINE = "\r\n";
    private static final String HEADER = "Date,Exercise,Body Part,Sets,Reps,Total Reps";

    private ExportService() {
    }

    /**
     * Builds the CSV for the given workouts, header row first.
     * A workout with no exercises still gets a row carrying its date.
     */
    public static String workoutsToCsv(List<Workout> workouts) {
        List<Workout> ordered = new ArrayList<>(workouts);
        ordered.removeIf(Objects::isNull);
        ordered.sort(Comparator.comparing(Workout::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

        StringBuilder csv = new StringBuilder(HEADER).append(NEWLINE);
        for (Workout workout : ordered) {
            String date = workout.getDate() == null ? "" : workout.getDate().toString();
            if (workout.getExercises().isEmpty()) {
                csv.append(row(date, "", "", "", "", ""));
                continue;
            }
            for (Exercise exercise : workout.getExercises()) {
                String bodyPart = exercise.getBodyPart() == null ? "" : exercise.getBodyPart().name();
                csv.append(row(date,
                        text(exercise.getName()),
                        bodyPart,
                        String.valueOf(exercise.getSets()),
                        String.valueOf(exercise.getReps()),
                        String.valueOf(exercise.totalReps())));
            }
        }
        return csv.toString();
    }

    /**
     * A safe default file name such as {@code kinetic-fitness-workouts-alex-2026-10-05.csv}.
     * Anything in the username other than letters, digits, {@code _} and {@code -}
     * becomes an underscore, so it can never form a path or an illegal file name.
     */
    public static String fileName(String username, LocalDate date) {
        String safe = username == null ? "" : username.trim().replaceAll("[^A-Za-z0-9_-]", "_");
        if (safe.isEmpty()) {
            safe = "user";
        }
        return "kinetic-fitness-workouts-" + safe + "-" + date + ".csv";
    }

    private static String row(String... fields) {
        List<String> quoted = new ArrayList<>();
        for (String field : fields) {
            quoted.add(quote(field));
        }
        return String.join(",", quoted) + NEWLINE;
    }

    /** Free text from the user: defuse a leading formula character, then quote as needed. */
    private static String text(String value) {
        if (value == null) {
            return "";
        }
        if (!value.isEmpty() && "=+-@\t\r".indexOf(value.charAt(0)) >= 0) {
            return "'" + value;
        }
        return value;
    }

    private static String quote(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}