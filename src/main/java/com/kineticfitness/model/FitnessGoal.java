package com.kineticfitness.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * One user's fitness goal: what they are working toward, by when, and whether
 * they got there.
 *
 * <p>The class is immutable. Completing a goal returns a new instance via
 * {@link #achievedOn(LocalDate)} rather than mutating this one, so a goal can
 * never be half-updated and every field can stay final.</p>
 *
 * <p>All the rules about a goal live here rather than in the screen that draws
 * it: whether the target has been reached, how long is left, and how early or
 * late it was completed. Date-dependent methods take {@code today} as a
 * parameter instead of calling {@link LocalDate#now()}, which is what makes
 * them testable.</p>
 *
 * <p>US-12 &mdash; Set a fitness goal. US-13 &mdash; Track goal progress.
 * US-14 &mdash; Mark a goal achieved.</p>
 */
public final class FitnessGoal {

    private final GoalType type;
    private final double targetWeightKg;
    private final int weeklyWorkouts;
    private final int weeklyMinutes;
    private final FitnessLevel experienceLevel;
    private final Set<String> preferredWorkoutTypes;
    private final Set<String> preferredWorkoutDays;
    private final LocalDate startDate;
    private final LocalDate targetDate;
    private final LocalDate achievedDate;

    /**
     * @param achievedDate the day the goal was completed, or null while it is still active
     * @throws IllegalArgumentException if the target date is not after the start date,
     *                                  or the target weight is not positive
     */
    public FitnessGoal(GoalType type,
                       double targetWeightKg,
                       int weeklyWorkouts,
                       int weeklyMinutes,
                       FitnessLevel experienceLevel,
                       Set<String> preferredWorkoutTypes,
                       Set<String> preferredWorkoutDays,
                       LocalDate startDate,
                       LocalDate targetDate,
                       LocalDate achievedDate) {

        this.type = Objects.requireNonNull(type, "goal type");
        this.experienceLevel = Objects.requireNonNull(experienceLevel, "experience level");
        this.startDate = Objects.requireNonNull(startDate, "start date");
        this.targetDate = Objects.requireNonNull(targetDate, "target date");

        if (targetWeightKg <= 0) {
            throw new IllegalArgumentException("target weight must be positive");
        }
        if (!targetDate.isAfter(startDate)) {
            throw new IllegalArgumentException("target date must be after the start date");
        }

        this.targetWeightKg = targetWeightKg;
        this.weeklyWorkouts = weeklyWorkouts;
        this.weeklyMinutes = weeklyMinutes;
        this.achievedDate = achievedDate;
        this.preferredWorkoutTypes = unmodifiableCopy(preferredWorkoutTypes);
        this.preferredWorkoutDays = unmodifiableCopy(preferredWorkoutDays);
    }

    private static Set<String> unmodifiableCopy(Set<String> source) {
        return source == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(source));
    }

    // ---------- rules ----------

    /** Whether this goal has already been completed. */
    public boolean isAchieved() {
        return achievedDate != null;
    }

    /**
     * Whether the given weight satisfies this goal's target. Delegates to the
     * {@link GoalType}, so each kind of goal brings its own comparison.
     */
    public boolean isReachedBy(double currentWeightKg) {
        return type.isReached(currentWeightKg, targetWeightKg);
    }

    /**
     * A copy of this goal marked completed on the given day.
     *
     * @throws IllegalStateException if it is already achieved
     */
    public FitnessGoal achievedOn(LocalDate date) {
        if (isAchieved()) {
            throw new IllegalStateException("goal was already achieved on " + achievedDate);
        }
        return new FitnessGoal(type, targetWeightKg, weeklyWorkouts, weeklyMinutes,
                experienceLevel, preferredWorkoutTypes, preferredWorkoutDays,
                startDate, targetDate, Objects.requireNonNull(date, "achieved date"));
    }

    /** Whole days from {@code today} until the target date; negative once it has passed. */
    public long daysRemaining(LocalDate today) {
        return ChronoUnit.DAYS.between(today, targetDate);
    }

    /** Whether the target date has passed without the goal being achieved. */
    public boolean isOverdue(LocalDate today) {
        return !isAchieved() && today.isAfter(targetDate);
    }

    /** The whole span the user gave themselves, in days. */
    public long durationDays() {
        return ChronoUnit.DAYS.between(startDate, targetDate);
    }

    /**
     * How many days before the target date the goal was completed. Negative when
     * it was completed late.
     *
     * @throws IllegalStateException if the goal is not achieved
     */
    public long daysAheadOfTarget() {
        if (!isAchieved()) {
            throw new IllegalStateException("goal has not been achieved");
        }
        return ChronoUnit.DAYS.between(achievedDate, targetDate);
    }

    // ---------- accessors ----------

    public GoalType getType() { return type; }
    public double getTargetWeightKg() { return targetWeightKg; }
    public int getWeeklyWorkouts() { return weeklyWorkouts; }
    public int getWeeklyMinutes() { return weeklyMinutes; }
    public FitnessLevel getExperienceLevel() { return experienceLevel; }
    public Set<String> getPreferredWorkoutTypes() { return preferredWorkoutTypes; }
    public Set<String> getPreferredWorkoutDays() { return preferredWorkoutDays; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getTargetDate() { return targetDate; }
    public LocalDate getAchievedDate() { return achievedDate; }

    @Override
    public String toString() {
        return "FitnessGoal[" + type + " to " + targetWeightKg + "kg by " + targetDate
                + (isAchieved() ? ", achieved " + achievedDate : ", active") + "]";
    }
}
