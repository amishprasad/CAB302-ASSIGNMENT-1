package com.kineticfitness.service;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Comparator;

public final class InputSuggestions {
    private InputSuggestions() { }
    public record Suggestion(String value, String keywords) { }

    public static List<String> match(String input, Collection<Suggestion> options) {
        if (input == null || input.isBlank()) return List.of();
        String query = input.strip().toLowerCase(Locale.ROOT);
        LinkedHashMap<String, Suggestion> unique = new LinkedHashMap<>();
        for (Suggestion option : options) {
            if (option == null || option.value() == null || option.value().isBlank()) continue;
            String name = option.value().toLowerCase(Locale.ROOT);
            String searchable = name + " " + (option.keywords() == null ? "" : option.keywords().toLowerCase(Locale.ROOT));
            if (java.util.Arrays.stream(query.split("\\s+")).allMatch(searchable::contains)) {
                unique.putIfAbsent(name, option);
            }
        }
        return unique.values().stream()
                .sorted(Comparator.comparingInt(o -> o.value().toLowerCase(Locale.ROOT).startsWith(query) ? 0 : 1))
                .map(Suggestion::value).limit(8).toList();
    }
}
