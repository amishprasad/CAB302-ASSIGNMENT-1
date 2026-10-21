package com.kineticfitness.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class EntryFormats {
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private EntryFormats() { }
    public static String dateDraft(String text) { return text; }
    public static String timeDraft(String text) { return text; }
    public static Optional<LocalDate> parseDate(String text) { return Optional.empty(); }
    public record Edit(String text, int caret) { }
    public static Edit edit(String old, int start, int end, String replacement, boolean date) {
        return edit(old, start, end, replacement, date, end);
    }
    public static Edit edit(String old, int start, int end, String replacement, boolean date, int oldCaret) {
        return new Edit(old.substring(0, start) + replacement + old.substring(end), start + replacement.length());
    }
}
