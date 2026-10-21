package com.kineticfitness.service;

import java.util.Collection;
import java.util.List;

public final class InputSuggestions {
    private InputSuggestions() { }
    public record Suggestion(String value, String keywords) { }

    public static List<String> match(String input, Collection<Suggestion> options) {
        return List.of();
    }
}
