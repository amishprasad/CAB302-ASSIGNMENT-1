package com.kineticfitness.service;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.Gender;
import com.kineticfitness.view.LocalProfileStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the one definition of "this profile is filled in".
 *
 * <p>{@code LocalProfileStore.hasPersonalDetails()} answers true as soon as a date
 * of birth is set, while {@link com.kineticfitness.model.PersonalDetails} refuses to
 * exist unless every field is present and in range. A screen that trusts the first
 * draws a profile the second says is not a profile. These tests pin down a single
 * answer, in the mapper that already stands between the two.</p>
 */
class PersonalDetailsMapperTest {

    private final LocalProfileStore store = LocalProfileStore.getInstance();

    @BeforeEach
    void reset() {
        store.clear();
    }

    /** Fills every field the domain object requires. */
    private void fillCompletely() {
        store.firstName = "Alex Taylor";
        store.email = "alex@example.com";
        store.gender = LocalProfileStore.Gender.OTHER;
        store.dateOfBirth = LocalDate.of(1998, 3, 14);
        store.heightCm = 175;
        store.weightKg = 72;
        store.fitnessLevel = LocalProfileStore.FitnessLevel.BEGINNER;
    }

    @Test
    void aFullyFilledProfileIsComplete() {
        fillCompletely();
        assertTrue(PersonalDetailsMapper.isComplete(store));
    }

    @Test
    void aDateOfBirthAloneIsNotAProfile() {
        store.dateOfBirth = LocalDate.of(1998, 3, 14);
        assertFalse(PersonalDetailsMapper.isComplete(store),
                "a date of birth with no name, height or weight is not a filled-in profile");
    }

    @Test
    void anEmptyStoreIsNotComplete() {
        assertFalse(PersonalDetailsMapper.isComplete(store));
    }

    @Test
    void aMissingNameMakesItIncomplete() {
        fillCompletely();
        store.firstName = "";
        assertFalse(PersonalDetailsMapper.isComplete(store));
    }

    @Test
    void aHeightOutsideTheAllowedRangeMakesItIncomplete() {
        fillCompletely();
        store.heightCm = 0;
        assertFalse(PersonalDetailsMapper.isComplete(store));
    }

    @Test
    void aWeightOutsideTheAllowedRangeMakesItIncomplete() {
        fillCompletely();
        store.weightKg = 0;
        assertFalse(PersonalDetailsMapper.isComplete(store));
    }

    /**
     * The property that matters: completeness and "can this become a domain object"
     * must never disagree, whatever the stored values are.
     */
    @Test
    void completenessAgreesWithWhetherTheDetailsCanBeBuilt() {
        fillCompletely();
        assertEquals(PersonalDetailsMapper.toDetails(store).isPresent(),
                PersonalDetailsMapper.isComplete(store));

        store.heightCm = 0;
        assertEquals(PersonalDetailsMapper.toDetails(store).isPresent(),
                PersonalDetailsMapper.isComplete(store));
    }
}
