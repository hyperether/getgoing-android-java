package com.hyperether.getgoing.repository.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DbRoute {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo
    private long duration; // duration in milliseconds
    @ColumnInfo
    private double energy;
    @ColumnInfo
    private double length;
    @ColumnInfo
    private String date;
    @ColumnInfo
    private double avgSpeed;  // average speed during this route
    @ColumnInfo
    private int activity_id;   // actitivy called for this route walk = 1, run = 2, ride = 3

    public DbRoute(long id, long duration, double energy, double length,
                   String date, double avgSpeed, int activity_id) {
        super();
        this.id = id;
        this.duration = duration;
        this.energy = energy;
        this.length = length;
        this.date = date;
        this.avgSpeed = avgSpeed;
        this.activity_id = activity_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public int getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(int activity_id) {
        this.activity_id = activity_id;
    }
}
