package com.kineticfitness.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;

/** Formatting of user input, kept independent of JavaFX for testing. */
public final class EntryFormats {
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private EntryFormats() { }

    public static String dateDraft(String text) { return mask(text, true); }
    public static String timeDraft(String text) { return mask(text, false); }

    private static String mask(String text, boolean date) {
        if (text == null) return "";
        if (!text.matches(date ? "[0-9/]*" : "[0-9:]*")) return text;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.length() > (date ? 8 : 4)) return text;
        // Preserve short hours such as 6:30 instead of turning them into 63:0.
        if (!date && text.indexOf(':') == 1) return text;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            result.append(digits.charAt(i));
            if (i == 1 || date && i == 3) result.append(date ? '/' : ':');
        }
        return result.toString();
    }

    /** Strict parsing avoids silently converting 31 February to another date. */
    public static Optional<LocalDate> parseDate(String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        try {
            return Optional.of(LocalDate.parse(dateDraft(text.trim()), DATE));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    public record Edit(String text, int caret) { }

    public static Edit edit(String old, int start, int end, String replacement, boolean date) {
        return edit(old, start, end, replacement, date, end);
    }

    /** Keeps the caret next to the same digit when editing, deleting or pasting. */
    public static Edit edit(String old, int start, int end, String replacement, boolean date, int oldCaret) {
        int caret = start + replacement.length();
        String next = old.substring(0, start) + replacement + old.substring(end);
        char separator = date ? '/' : ':';
        if (replacement.isEmpty() && end - start == 1 && old.charAt(start) == separator) {
            // Backspace over a separator removes the digit before it; Delete
            // removes the next digit. Otherwise formatting would put it back.
            if (oldCaret == end && start > 0) {
                next = old.substring(0, start - 1) + old.substring(end);
                caret = start - 1;
            } else if (end < old.length()) {
                next = old.substring(0, start) + old.substring(end + 1);
                caret = start;
            }
        }
        if (!next.matches(date ? "[0-9/]*" : "[0-9:]*")
                || !date && next.indexOf(':') == 1) {
            return new Edit(next, caret);
        }
        int digitCaret = countDigitsBefore(next, caret);
        String formatted = mask(next, date);
        if (replacement.isEmpty()) {
            while (formatted.endsWith(String.valueOf(separator))) {
                formatted = formatted.substring(0, formatted.length() - 1);
            }
        }
        return new Edit(formatted, caretAfterDigits(formatted, digitCaret, separator));
    }

    private static int countDigitsBefore(String text, int caret) {
        return (int) text.substring(0, Math.min(caret, text.length())).chars()
                .filter(Character::isDigit).count();
    }

    private static int caretAfterDigits(String text, int digitCount, char separator) {
        int mapped = 0, digitsSeen = 0;
        while (mapped < text.length() && digitsSeen < digitCount) {
            if (Character.isDigit(text.charAt(mapped))) digitsSeen++;
            mapped++;
        }
        if (mapped < text.length() && text.charAt(mapped) == separator) mapped++;
        return mapped;
    }
}
