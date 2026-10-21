package com.kineticfitness.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InputSuggestionsTest {
    @Test void matchesNamesAndBodyPartsWithNamePrefixesFirst() {
        var options = List.of(new InputSuggestions.Suggestion("Bench Press", "Chest"),
                new InputSuggestions.Suggestion("Chest Dip", "Chest"),
                new InputSuggestions.Suggestion("Deadlift", "Back"));
        assertEquals(List.of("Chest Dip", "Bench Press"), InputSuggestions.match("che", options));
        assertEquals(List.of("Bench Press"), InputSuggestions.match("bench pr", options));
    }

    @Test void handlesCustomNamesEmptyQueriesAndMissingKeywords() {
        var options = List.of(new InputSuggestions.Suggestion("Bench Press", null));
        assertTrue(InputSuggestions.match("My own exercise", options).isEmpty());
        assertTrue(InputSuggestions.match(" ", options).isEmpty());
        assertTrue(InputSuggestions.match(null, options).isEmpty());
        assertEquals(List.of("Bench Press"), InputSuggestions.match("bench", options));
    }

    @Test void ignoresCaseRemovesDuplicatesAndLimitsTheMenu() {
        var options = java.util.stream.IntStream.range(0, 20)
                .mapToObj(i -> new InputSuggestions.Suggestion("Workout " + i, "")).toList();
        assertEquals(8, InputSuggestions.match("WORKOUT", options).size());
        assertEquals(List.of("Push Day"), InputSuggestions.match("push", List.of(
                new InputSuggestions.Suggestion("Push Day", ""),
                new InputSuggestions.Suggestion("push day", ""))));
    }
}
