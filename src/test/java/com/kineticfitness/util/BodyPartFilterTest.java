package com.kineticfitness.util;

import com.kineticfitness.model.BodyPart;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BodyPartFilterTest {

    @Test
    void allMatchesEveryBodyPartAndNone() {
        BodyPartFilter all = BodyPartFilter.all();
        for (BodyPart part : BodyPart.values()) {
            assertTrue(all.matches(part));
        }
        assertTrue(all.matches(null));
    }

    @Test
    void partFilterMatchesOnlyThatBodyPart() {
        BodyPartFilter chest = BodyPartFilter.forPart(BodyPart.CHEST);
        assertTrue(chest.matches(BodyPart.CHEST));
        assertFalse(chest.matches(BodyPart.LEGS));
    }

    @Test
    void partFilterRejectsAMissingBodyPart() {
        assertFalse(BodyPartFilter.forPart(BodyPart.CHEST).matches(null));
    }

    @Test
    void applyKeepsOnlyMatchingItemsInTheirOriginalOrder() {
        List<String> names = List.of("bench", "squat", "fly", "mystery");
        List<BodyPart> parts = Arrays.asList(BodyPart.CHEST, BodyPart.LEGS, BodyPart.CHEST, null);
        Function<String, BodyPart> partOf = name -> parts.get(names.indexOf(name));

        List<String> result = BodyPartFilter.forPart(BodyPart.CHEST).apply(names, partOf);

        assertEquals(List.of("bench", "fly"), result);
    }

    @Test
    void applyDoesNotModifyTheInputList() {
        List<BodyPart> input = new ArrayList<>(List.of(BodyPart.CHEST, BodyPart.LEGS));
        BodyPartFilter.forPart(BodyPart.LEGS).apply(input, p -> p);
        assertEquals(2, input.size());
    }

    @Test
    void applyOnAnEmptyListGivesAnEmptyList() {
        assertTrue(BodyPartFilter.all().apply(new ArrayList<BodyPart>(), p -> p).isEmpty());
    }

    @Test
    void labelsAreReadableText() {
        assertEquals("All", BodyPartFilter.all().label());
        assertEquals("Chest", BodyPartFilter.forPart(BodyPart.CHEST).label());
        assertEquals("Shoulders", BodyPartFilter.forPart(BodyPart.SHOULDERS).label());
    }

    @Test
    void standardListStartsWithAll() {
        assertEquals("All", BodyPartFilter.standard().get(0).label());
    }

    @Test
    void standardListHasOneFilterPerBodyPartSoNewPartsNeedNoUiChanges() {
        List<BodyPartFilter> standard = BodyPartFilter.standard();
        assertEquals(BodyPart.values().length + 1, standard.size());
        for (BodyPart part : BodyPart.values()) {
            assertTrue(standard.stream().anyMatch(f -> f != standard.get(0) && f.matches(part)),
                    "no filter targets " + part);
        }
    }
}