package com.kineticfitness.view;

import java.util.List;

public class Session {
    private final Integer workoutId;
    private String name;
    private final String dateTitle;
    private final List<Entry> entries;

    public Session(Integer workoutId, String name, String dateTitle, List<Entry> entries) {
        this.workoutId = workoutId;
        this.name = name;
        this.dateTitle = dateTitle;
        this.entries = entries;
    }

    public Integer getWorkoutId() {
        return workoutId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateTitle() {
        return dateTitle;
    }

    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public String getDisplayTitle() {
        return hasName() ? name : dateTitle;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public int totalReps() {
        return entries.stream().mapToInt(Entry::totalReps).sum();
    }
}
