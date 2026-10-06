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

        @Override
        public String description() {
            return "Your goal is to lose weight and improve your overall fitness and health "
                    + "through regular exercise and a balanced routine.";
        }

        @Override
        public String motivation() {
            return "\u201CA healthier you is a happier you!\u201D";
        }
    },

    /** Reached once the user is at or above the target. */
    GAIN_MUSCLE("Gain muscle") {
        @Override
        public boolean isReached(double currentWeightKg, double targetWeightKg) {
            return currentWeightKg >= targetWeightKg;
        }

        @Override
        public String description() {
            return "Your goal is to build muscle and strength through consistent resistance "
                    + "training and steady progression.";
        }

        @Override
        public String motivation() {
            return "\u201CStrength comes from what you keep showing up for.\u201D";
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

        @Override
        public String description() {
            return "Your goal is to improve your endurance and general fitness through regular, "
                    + "varied exercise.";
        }

        @Override
        public String motivation() {
            return "\u201CEvery session counts, however small.\u201D";
        }
    },

    /** Reached while the user stays within {@link #MAINTAIN_TOLERANCE_KG} of the target. */
    MAINTAIN_WEIGHT("Maintain weight") {
        @Override
        public boolean isReached(double currentWeightKg, double targetWeightKg) {
            return Math.abs(currentWeightKg - targetWeightKg) <= MAINTAIN_TOLERANCE_KG;
        }

        @Override
        public String description() {
            return "Your goal is to maintain your current weight and stay consistent with a "
                    + "balanced routine.";
        }

        @Override
        public String motivation() {
            return "\u201CConsistency beats intensity.\u201D";
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

    /** A sentence explaining what this goal means, shown on the goal summary. */
    public abstract String description();

    /** A short encouragement suited to this goal. */
    public abstract String motivation();

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
