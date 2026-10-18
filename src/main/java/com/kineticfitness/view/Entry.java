package com.kineticfitness.view;

public class Entry {
    private final Integer exerciseId;
    private final String date;
    private final String exercise;
    private String sets;
    private String reps;
    private final String bodyPart;

    public Entry(Integer exerciseId, String date, String exercise, String sets, String reps, String bodyPart) {
        this.exerciseId = exerciseId;
        this.date = date;
        this.exercise = exercise;
        this.sets = sets;
        this.reps = reps;
        this.bodyPart = bodyPart;
    }

    public Integer getExerciseId() {
        return exerciseId;
    }

    public String getDate() {
        return date;
    }

    public String getExercise() {
        return exercise;
    }

    public String getSets() {
        return sets;
    }

    public String getReps() {
        return reps;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public void setSets(String sets) {
        this.sets = sets;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }


    public int totalReps() {
        try {
            return Integer.parseInt(sets.trim()) * Integer.parseInt(reps.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
