package com.kineticfitness.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Workout {
    private Integer id;
    private String name;
    private LocalDate date;
    private final List<Exercise> exercises = new ArrayList<>();

    public Workout(LocalDate date) {
        this.date = date;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<Exercise> getExercises() { return exercises; }
    public void addExercise(Exercise exercise) { exercises.add(exercise); }

    /** Total repetitions across every exercise in this workout. */
    public int totalReps() {
        return exercises.stream().mapToInt(Exercise::totalReps).sum();
    }
}