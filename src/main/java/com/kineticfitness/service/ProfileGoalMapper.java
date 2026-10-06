package com.kineticfitness.service;

import com.kineticfitness.model.FitnessGoal;
import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.GoalType;
import com.kineticfitness.view.LocalProfileStore;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Translates between {@link FitnessGoal} and the flat fields on
 * {@link LocalProfileStore}.
 *
 * <p>The store is the shared state the whole app and its {@code ProfileDAO}
 * already read and write, and it is not this feature's to redesign. This adapter
 * lets the goals screen work with a real domain object while the persistence
 * layer keeps seeing the fields it expects, so neither side has to change for
 * the other.</p>
 *
 * <p>The two enums are matched by {@code name()}, which holds because
 * {@link GoalType} and {@link LocalProfileStore.PrimaryGoal} declare the same
 * constants &mdash; and {@link #toGoal} returns empty rather than throwing if
 * that ever stops being true.</p>
 */
public final class ProfileGoalMapper {

    private ProfileGoalMapper() {
    }

    /**
     * Builds a goal from the stored profile.
     *
     * @return the goal, or empty when no goal is set or the stored values can't
     *         form a valid one (for example a profile saved before target dates existed)
     */
    public static Optional<FitnessGoal> toGoal(LocalProfileStore store) {
        if (store == null || store.primaryGoal == null) {
            return Optional.empty();
        }
        if (store.goalStartDate == null || store.goalTargetDate == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new FitnessGoal(
                    GoalType.valueOf(store.primaryGoal.name()),
                    store.targetWeightKg,
                    store.weeklyWorkoutGoal,
                    store.weeklyExerciseDurationMinutes,
                    FitnessLevel.valueOf(store.experienceLevel.name()),
                    store.preferredWorkoutTypes,
                    store.preferredWorkoutDays,
                    store.goalStartDate,
                    store.goalTargetDate,
                    store.goalAchievedDate));
        } catch (IllegalArgumentException e) {
            // Unknown enum name, or stored dates that no longer satisfy the goal's
            // own invariants. Treat it as "no goal" rather than crashing the screen.
            return Optional.empty();
        }
    }

    /** Writes a goal back onto the store, ready for {@code ProfileDAO.save}. */
    public static void apply(FitnessGoal goal, LocalProfileStore store) {
        store.primaryGoal = LocalProfileStore.PrimaryGoal.valueOf(goal.getType().name());
        store.targetWeightKg = goal.getTargetWeightKg();
        store.weeklyWorkoutGoal = goal.getWeeklyWorkouts();
        store.weeklyExerciseDurationMinutes = goal.getWeeklyMinutes();
        store.experienceLevel = LocalProfileStore.FitnessLevel.valueOf(
                goal.getExperienceLevel().name());

        store.preferredWorkoutTypes.clear();
        store.preferredWorkoutTypes.addAll(goal.getPreferredWorkoutTypes());
        store.preferredWorkoutDays.clear();
        store.preferredWorkoutDays.addAll(goal.getPreferredWorkoutDays());

        store.goalStartDate = goal.getStartDate();
        store.goalTargetDate = goal.getTargetDate();
        store.goalAchievedDate = goal.getAchievedDate();
    }

    /** Removes the goal from the store, leaving the personal details untouched. */
    public static void clearGoal(LocalProfileStore store) {
        store.primaryGoal = null;
        store.targetWeightKg = 0;
        store.weeklyWorkoutGoal = 4;
        store.weeklyExerciseDurationMinutes = 240;
        store.experienceLevel = store.fitnessLevel;
        store.preferredWorkoutTypes.clear();
        store.preferredWorkoutDays.clear();
        store.goalStartDate = null;
        store.goalTargetDate = null;
        store.goalAchievedDate = null;
    }

    /**
     * Completes the stored goal if the current weight has reached its target.
     *
     * @return true when this call marked the goal achieved, so the caller knows
     *         to persist and re-render
     */
    public static boolean completeIfReached(LocalProfileStore store, LocalDate today) {
        Optional<FitnessGoal> current = toGoal(store);
        if (current.isEmpty()) {
            return false;
        }
        FitnessGoal goal = current.get();
        if (goal.isAchieved() || !goal.getType().isMeasurable() || store.weightKg <= 0) {
            return false;
        }
        if (!goal.isReachedBy(store.weightKg)) {
            return false;
        }
        apply(goal.achievedOn(today), store);
        return true;
    }
}
