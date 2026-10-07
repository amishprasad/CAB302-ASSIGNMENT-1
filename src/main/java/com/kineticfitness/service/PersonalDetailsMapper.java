package com.kineticfitness.service;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.Gender;
import com.kineticfitness.model.PersonalDetails;
import com.kineticfitness.view.LocalProfileStore;

import java.util.Optional;

/**
 * Translates between {@link PersonalDetails} and the flat fields on
 * {@link LocalProfileStore}, the shared state {@code ProfileDAO} persists.
 *
 * <p>Same role as {@link ProfileGoalMapper}: it lets the profile screen work with
 * a validated domain object without changing the storage every other screen reads.</p>
 */
public final class PersonalDetailsMapper {

    private PersonalDetailsMapper() {
    }

    /**
     * Builds the details from the stored profile.
     *
     * @return the details, or empty when the profile is incomplete or holds values
     *         that no longer satisfy {@link PersonalDetails}' own rules
     */
    public static Optional<PersonalDetails> toDetails(LocalProfileStore store) {
        if (store == null || !store.hasPersonalDetails()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new PersonalDetails(
                    store.firstName,
                    store.email,
                    Gender.valueOf(store.gender.name()),
                    store.photoPath,
                    store.dateOfBirth,
                    store.heightCm,
                    store.weightKg,
                    FitnessLevel.valueOf(store.fitnessLevel.name())));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    /**
     * Writes the details back onto the store, ready for {@code ProfileDAO.save}.
     *
     * @param seedExperienceLevel true on first creation, when the goals screen has
     *                            no experience level of its own yet
     */
    public static void apply(PersonalDetails details, LocalProfileStore store,
                             boolean seedExperienceLevel) {
        store.firstName = details.getFullName();
        store.email = details.getEmail();
        store.gender = LocalProfileStore.Gender.valueOf(details.getGender().name());
        store.photoPath = details.getPhotoPath();
        store.dateOfBirth = details.getDateOfBirth();
        store.heightCm = details.getHeightCm();
        store.weightKg = details.getWeightKg();
        store.fitnessLevel = LocalProfileStore.FitnessLevel.valueOf(
                details.getFitnessLevel().name());

        if (seedExperienceLevel) {
            store.experienceLevel = store.fitnessLevel;
        }
    }
}
