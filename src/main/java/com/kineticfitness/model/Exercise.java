package com.kineticfitness.model;

public class Exercise {
    private String name;
    private int sets;
    private int reps;
    private BodyPart bodyPart;

    public Exercise(String name, int sets, int reps) {
        this(name, sets, reps, null);
    }

    public Exercise(String name, int sets, int reps, BodyPart bodyPart) {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.bodyPart = bodyPart;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public BodyPart getBodyPart() { return bodyPart; }
    public void setBodyPart(BodyPart bodyPart) { this.bodyPart = bodyPart; }

    /** Total repetitions across all sets. */
    public int totalReps() {
        return sets * reps;
    }
}
