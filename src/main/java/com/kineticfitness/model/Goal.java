package com.kineticfitness.model;

public class Goal {
    private String description;
    private int targetValue;
    private int currentValue;

    public Goal(String description, int targetValue) {
        this.description = description;
        this.targetValue = targetValue;
        this.currentValue = 0;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }

    public int getCurrentValue() { return currentValue; }
    public void addProgress(int amount) { currentValue += amount; }

    public boolean isAchieved() { return currentValue >= targetValue; }

    public double progressPercent() {
        if (targetValue == 0) return 0;
        return (currentValue * 100.0) / targetValue;
    }
}
