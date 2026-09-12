package com.kineticfitness.model;

public class ScheduledWorkout {
    private final int id;
    private final String name;
    private final String date;
    private final String time;
    private final String duration;

    public ScheduledWorkout(String name, String date, String time, String duration) {
        this(0, name, date, time, duration);
    }

    public ScheduledWorkout(int id, String name, String date, String time, String duration) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.duration = duration;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getDuration() { return duration; }
}