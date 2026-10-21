package com.kineticfitness.service;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Comparator;

public final class InputSuggestions {
    private static final int MAX_SUGGESTIONS = 8;
    private InputSuggestions() { }
    public record Suggestion(String value, String keywords) { }

    public static List<String> match(String input, Collection<Suggestion> options) {
        if (input == null || input.isBlank()) return List.of();
        String query = normalise(input.strip());
        String[] words = query.split("\\s+");
        LinkedHashMap<String, Suggestion> unique = new LinkedHashMap<>();
        for (Suggestion option : options) {
            if (option == null || option.value() == null || option.value().isBlank()) continue;
            String name = normalise(option.value());
            String searchable = name + " " + normalise(option.keywords());
            if (java.util.Arrays.stream(words).allMatch(searchable::contains)) {
                unique.putIfAbsent(name, option);
            }
        }
        return unique.values().stream()
                .sorted(Comparator.comparingInt(o -> normalise(o.value()).startsWith(query) ? 0 : 1))
                .map(Suggestion::value).limit(MAX_SUGGESTIONS).toList();
    }

    private static String normalise(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
