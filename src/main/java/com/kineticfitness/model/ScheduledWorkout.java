package com.kineticfitness.model;

public class ScheduledWorkout {
    private String name;
    private String date;
    private String time;
    private String duration;

    public ScheduledWorkout(String name, String date, String time, String duration) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.duration = duration;
    }

    public String getName() { return name; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getDuration() { return duration; }
}