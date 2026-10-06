package com.kineticfitness.model;

/**
 * The kind of fitness goal a user is working toward, and the rule that decides
 * when their current weight has reached it.
 *
 * <p>Each constant overrides {@link #isReached(double, double)} with its own
 * comparison, so adding a goal type means adding a constant here rather than
 * extending a switch statement somewhere in the UI. "Improve fitness" has no
 * weight target at all, which is why {@link #isMeasurable()} exists.</p>
 *
 * <p>US-12 &mdash; Set a fitness goal. US-14 &mdash; Mark a goal achieved.</p>
 */
public enum GoalType {

    /** Reached once the user is at or below the target. */
    LOSE_WEIGHT("Lose weight") {
        @Override
        public boolean isReached(double currentWeightKg, double targetWeightKg) {
            return currentWeightKg <= targetWeightKg;
        }
    },

    /** Reached once the user is at or above the target. */
    GAIN_MUSCLE("Gain muscle") {
        @Override
        public boolean isReached(double currentWeightKg, double targetWeightKg) {
            return currentWeightKg >= targetWeightKg;
        }
    },

    /** No weight target, so it can only be completed by hand. */
    IMPROVE_FITNESS("Improve fitness") {
        @Override
        public boolean isReached(double currentWeightKg, double targetWeightKg) {
            return false;
        }

        @Override
        public boolean isMeasurable() {
            return false;
        }
    },

    /** Reached while the user stays within {@link #MAINTAIN_TOLERANCE_KG} of the target. */
    MAINTAIN_WEIGHT("Maintain weight") {
        @Override
        public boolean isReached(double currentWeightKg, double targetWeightKg) {
            return Math.abs(currentWeightKg - targetWeightKg) <= MAINTAIN_TOLERANCE_KG;
        }
    };

    /** How far either side of the target still counts as maintaining it. */
    public static final double MAINTAIN_TOLERANCE_KG = 1.0;

    private final String display;

    GoalType(String display) {
        this.display = display;
    }

    /**
     * Decides whether a weight reading satisfies this goal.
     *
     * @param currentWeightKg the user's weight right now
     * @param targetWeightKg  the weight they set as their target
     * @return true when this goal's own rule is satisfied
     */
    public abstract boolean isReached(double currentWeightKg, double targetWeightKg);

    /**
     * Whether progress toward this goal can be measured from weight alone.
     * A goal that is not measurable must be completed manually by the user.
     */
    public boolean isMeasurable() {
        return true;
    }

    /** The label shown in the UI, e.g. "Lose weight". */
    public String display() {
        return display;
    }
}
