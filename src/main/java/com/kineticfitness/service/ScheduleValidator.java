package com.kineticfitness.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * The rules the "Schedule New Workout" form must satisfy, and the parsing of the
 * free-text fields on it.
 *
 * <p>Previously the form accepted anything non-empty, so "banana" was a valid
 * date and was persisted as one. Everything here is a pure function taking
 * {@code today} as a parameter rather than calling {@code LocalDate.now()},
 * which is what makes the "not in the past" rule testable.</p>
 *
 * <p>US-10 &mdash; Schedule a workout.</p>
 */
public final class ScheduleValidator {

    /** Longest session the form will accept, in minutes. */
    public static final int MAX_DURATION_MINUTES = 600;

    private static final DateTimeFormatter[] TIME_FORMATS = {
            DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("h a", Locale.ENGLISH)
    };

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
    };

    private ScheduleValidator() {
    }

    /**
     * Validates the whole form in the order the fields appear, so the user is
     * told about the first problem rather than the last.
     *
     * @param name           the workout name as typed
     * @param date           the chosen date, or null if nothing was picked
     * @param timeText       the start time as typed
     * @param durationText   the duration in minutes as typed
     * @param today          the current date, passed in so this stays testable
     */
    public static ValidationResult validate(String name, LocalDate date, String timeText,
                                            String durationText, LocalDate today) {
        if (today == null) {
            throw new IllegalArgumentException("today is required");
        }
        if (isBlank(name)) {
            return ValidationResult.error("Please enter a workout name.");
        }
        if (date == null) {
            return ValidationResult.error("Please choose a date.");
        }
        if (date.isBefore(today)) {
            return ValidationResult.error("You can't schedule a workout in the past.");
        }
        if (isBlank(timeText)) {
            return ValidationResult.error("Please enter a start time.");
        }
        if (parseTime(timeText).isEmpty()) {
            return ValidationResult.error("Enter a start time like 18:00 or 6:30 PM.");
        }
        if (isBlank(durationText)) {
            return ValidationResult.error("Please enter a duration in minutes.");
        }
        OptionalInt duration = parseDurationMinutes(durationText);
        if (duration.isEmpty()) {
            return ValidationResult.error("Duration must be a whole number of minutes.");
        }
        if (duration.getAsInt() <= 0) {
            return ValidationResult.error("Duration must be at least 1 minute.");
        }
        if (duration.getAsInt() > MAX_DURATION_MINUTES) {
            return ValidationResult.error("Duration must be " + MAX_DURATION_MINUTES + " minutes or less.");
        }
        return ValidationResult.ok();
    }

    /**
     * Reads a start time, accepting 24-hour ("18:00") and 12-hour ("6:30 PM",
     * "6:30pm", "7 AM") spellings.
     *
     * @return the time, or empty if the text cannot be understood
     */
    public static Optional<LocalTime> parseTime(String timeText) {
        if (isBlank(timeText)) {
            return Optional.empty();
        }
        String normalised = timeText.trim()
                .toUpperCase(Locale.ENGLISH)
                .replaceAll("\\s+", " ")
                .replaceAll("(?<=[0-9])(AM|PM)", " $1");

        for (DateTimeFormatter format : TIME_FORMATS) {
            try {
                return Optional.of(LocalTime.parse(normalised, format));
            } catch (DateTimeParseException ignored) {
                // Try the next spelling.
            }
        }
        return Optional.empty();
    }

    /**
     * Reads a duration in whole minutes, tolerating a trailing unit
     * ("45", "45 mins", "45 minutes").
     *
     * @return the number of minutes, or empty if the text is not a whole number
     */
    public static OptionalInt parseDurationMinutes(String durationText) {
        if (isBlank(durationText)) {
            return OptionalInt.empty();
        }
        String digitsOnly = durationText.trim()
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("\\s*(mins?|minutes?)\\s*$", "")
                .trim();
        try {
            return OptionalInt.of(Integer.parseInt(digitsOnly));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    /**
     * Reads a date in ISO form or in either of the display formats this app has
     * written in the past. Used when loading rows written before dates were
     * stored properly.
     *
     * @return the date, or empty if the text cannot be understood
     */
    public static Optional<LocalDate> parseDate(String dateText) {
        if (isBlank(dateText)) {
            return Optional.empty();
        }
        String trimmed = dateText.trim();
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return Optional.of(LocalDate.parse(trimmed, format));
            } catch (DateTimeParseException ignored) {
                // Try the next spelling.
            }
        }
        return Optional.empty();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
