package com.kineticfitness.model;

import java.util.List;

/**
 * A measurable quantity of a logged workout, and the figures a statistics screen
 * reports about it.
 *
 * <p>Each constant knows how to read itself off a {@link Workout}, so adding a
 * third measure means adding a constant here rather than extending a conditional
 * in whatever screen happens to draw the chart. The aggregates are defined once
 * in terms of {@link #of(Workout)} and inherited by every constant.</p>
 *
 * <p>Workout duration is deliberately absent: a workout records its date and its
 * exercises' sets and reps, and nothing in the app stores how long it took.</p>
 *
 * <p>US-13 &mdash; Track goal progress.</p>
 */
public enum WorkoutMetric {

    /** Every repetition performed, counting each set. */
    REPS("Reps", "reps") {
        @Override
        public int of(Workout workout) {
            return workout.totalReps();
        }
    },

    /** Every set performed, whatever the repetitions within it. */
    SETS("Sets", "sets") {
        @Override
        public int of(Workout workout) {
            return workout.getExercises().stream()
                    .mapToInt(Exercise::getSets)
                    .sum();
        }
    };

    private final String tabLabel;
    private final String unit;

    WorkoutMetric(String tabLabel, String unit) {
        this.tabLabel = tabLabel;
        this.unit = unit;
    }

    /** This metric's value for one workout. */
    public abstract int of(Workout workout);

    /** The label on the tab that selects this metric, e.g. "Reps". */
    public String tabLabel() {
        return tabLabel;
    }

    /** The word for one unit of this metric in running text, e.g. "sets". */
    public String unit() {
        return unit;
    }

    /** The sum across every workout given; zero for an empty list. */
    public int totalAcross(List<Workout> workouts) {
        return workouts.stream().mapToInt(this::of).sum();
    }

    /** The largest single-workout value; zero for an empty list. */
    public int highestIn(List<Workout> workouts) {
        return workouts.stream().mapToInt(this::of).max().orElse(0);
    }

    /**
     * The mean per workout, rounded down. Zero for an empty list rather than a
     * division by zero, because a screen asking for an average of nothing is
     * asking a reasonable question.
     */
    public int averageAcross(List<Workout> workouts) {
        return workouts.isEmpty() ? 0 : totalAcross(workouts) / workouts.size();
    }
}
