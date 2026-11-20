package com.example.healthmate.model;

/** Holds one daily health record read from or saved to SQLite. */
public class HealthRecord {
    public int id;
    public String date;
    public int steps;
    public int calories;
    public int water;

    public HealthRecord(int id, String date, int steps, int calories, int water) {
        this.id = id;
        this.date = date;
        this.steps = steps;
        this.calories = calories;
        this.water = water;
    }
}
