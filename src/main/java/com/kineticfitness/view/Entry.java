package com.kineticfitness.view;

import com.kineticfitness.model.BodyPart;

/**
 * One exercise row on the Workout History page. Sets and reps are kept as text because
 * they are edited in a table cell; {@link #totalReps()} treats unparseable text as zero.
 * The body part is the real {@link BodyPart} (null when none was recorded), so filters
 * compare values instead of display strings.
 */
public class Entry {
    private final Integer exerciseId;
    private final String date;
    private final String exercise;
    private String sets;
    private String reps;
    private final BodyPart bodyPart;

    public Entry(Integer exerciseId, String date, String exercise, String sets, String reps, BodyPart bodyPart) {
        this.exerciseId = exerciseId;
        this.date = date;
        this.exercise = exercise;
        this.sets = sets;
        this.reps = reps;
        this.bodyPart = bodyPart;
    }

    public Integer getExerciseId() { return exerciseId; }
    public String getDate() { return date; }
    public String getExercise() { return exercise; }
    public String getSets() { return sets; }
    public String getReps() { return reps; }
    public BodyPart getBodyPart() { return bodyPart; }

    public void setSets(String sets) { this.sets = sets; }
    public void setReps(String reps) { this.reps = reps; }

    public int totalReps() {
        try {
            return Integer.parseInt(sets.trim()) * Integer.parseInt(reps.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}