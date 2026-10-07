package com.kineticfitness.model;

import com.kineticfitness.model.ExerciseCatalog.ExerciseInfo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the exercise catalogue.
 *
 * <p>These could not be written while the data and the filtering lived inside
 * {@code ExerciseSelectionView}: {@code module-info.java} does not open the {@code view}
 * package, so JUnit's reflection fails there. Extracting the catalogue into {@code model}
 * is what made this file possible.
 */
class ExerciseCatalogTest {

    @Test
    @DisplayName("the catalogue holds 150 exercises")
    void catalogueHolds150Exercises() {
        assertEquals(150, ExerciseCatalog.all().size());
    }

    @Test
    @DisplayName("every body part has an equal 25 exercises")
    void everyBodyPartHas25Exercises() {
        for (String part : List.of("Chest", "Back", "Legs", "Arms", "Shoulders", "Core")) {
            assertEquals(25, ExerciseCatalog.filterByPart(part).size(),
                    part + " should have 25 exercises");
        }
    }

    @Test
    @DisplayName("filtering by a body part returns only that body part")
    void filteringByPartReturnsOnlyThatPart() {
        for (ExerciseInfo exercise : ExerciseCatalog.filterByPart("Legs")) {
            assertEquals("Legs", exercise.bodyPart());
        }
    }

    @Test
    @DisplayName("the All filter returns the whole catalogue")
    void allFilterReturnsEverything() {
        assertEquals(ExerciseCatalog.all().size(),
                ExerciseCatalog.filterByPart(ExerciseCatalog.ALL).size());
    }

    @Test
    @DisplayName("a null filter is treated as All rather than throwing")
    void nullFilterReturnsEverything() {
        assertEquals(ExerciseCatalog.all().size(), ExerciseCatalog.filterByPart(null).size());
    }

    @Test
    @DisplayName("an unrecognised body part returns nothing, not everything")
    void unknownPartReturnsEmpty() {
        assertTrue(ExerciseCatalog.filterByPart("Forearms").isEmpty());
    }

    @Test
    @DisplayName("filtering is case sensitive, so a mistyped chip shows nothing")
    void filteringIsCaseSensitive() {
        assertTrue(ExerciseCatalog.filterByPart("chest").isEmpty());
    }

    @Test
    @DisplayName("every chip label except All matches real exercises")
    void everyChipLabelMatchesExercises() {
        for (String filter : ExerciseCatalog.filters()) {
            if (ExerciseCatalog.ALL.equals(filter)) {
                continue;
            }
            assertFalse(ExerciseCatalog.filterByPart(filter).isEmpty(),
                    "chip \"" + filter + "\" shows an empty library");
        }
    }

    @Test
    @DisplayName("the filter chips cover every exercise in the catalogue")
    void chipsCoverEveryExercise() {
        int covered = 0;
        for (String filter : ExerciseCatalog.filters()) {
            if (!ExerciseCatalog.ALL.equals(filter)) {
                covered += ExerciseCatalog.filterByPart(filter).size();
            }
        }
        assertEquals(ExerciseCatalog.all().size(), covered,
                "some exercises have a body part with no chip to reach them");
    }

    @Test
    @DisplayName("no exercise name appears twice")
    void exerciseNamesAreUnique() {
        Set<String> seen = new HashSet<>();
        for (ExerciseInfo exercise : ExerciseCatalog.all()) {
            assertTrue(seen.add(exercise.name()), "duplicate exercise: " + exercise.name());
        }
    }

    @Test
    @DisplayName("every exercise has a description and muscles worked")
    void everyExerciseIsFullyDescribed() {
        for (ExerciseInfo exercise : ExerciseCatalog.all()) {
            assertFalse(exercise.description().isBlank(),
                    exercise.name() + " has no description");
            assertFalse(exercise.muscles().isBlank(),
                    exercise.name() + " has no muscles worked");
        }
    }

    @Test
    @DisplayName("the catalogue itself cannot be modified by a caller")
    void catalogueIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> ExerciseCatalog.all().clear());
    }

    @Test
    @DisplayName("a filtered result is a fresh list, so sorting it cannot corrupt the catalogue")
    void filteredResultIsACopy() {
        List<ExerciseInfo> first = ExerciseCatalog.filterByPart("Core");
        List<ExerciseInfo> second = ExerciseCatalog.filterByPart("Core");

        first.clear();

        assertEquals(25, second.size());
        assertEquals(25, ExerciseCatalog.filterByPart("Core").size());
    }

    @Test
    @DisplayName("all() hands back the same instance rather than copying 150 entries per call")
    void allReturnsTheSharedList() {
        assertSame(ExerciseCatalog.all(), ExerciseCatalog.all());
    }
}