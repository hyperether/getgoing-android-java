package com.hyperether.getgoing.repository.room.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Route implements Parcelable {
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
    private double currentSpeed;
    @ColumnInfo
    private int activity_id;   // actitivy called for this route walk = 1, run = 2, ride = 3
    @ColumnInfo
    private long goal;

    public Route(long id, long duration, double energy, double length,
                 String date, double avgSpeed, double currentSpeed, int activity_id, long goal) {
        super();
        this.id = id;
        this.duration = duration;
        this.energy = energy;
        this.length = length;
        this.date = date;
        this.avgSpeed = avgSpeed;
        this.currentSpeed = currentSpeed;
        this.activity_id = activity_id;
        this.goal = goal;
    }

    protected Route(Parcel in) {
        id = in.readLong();
        duration = in.readLong();
        energy = in.readDouble();
        length = in.readDouble();
        date = in.readString();
        avgSpeed = in.readDouble();
        currentSpeed = in.readDouble();
        activity_id = in.readInt();
        goal = in.readLong();
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

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

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public int getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(int activity_id) {
        this.activity_id = activity_id;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public long getGoal() {
        return goal;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(duration);
        parcel.writeDouble(energy);
        parcel.writeDouble(length);
        parcel.writeString(date);
        parcel.writeDouble(avgSpeed);
        parcel.writeDouble(currentSpeed);
        parcel.writeInt(activity_id);
        parcel.writeLong(goal);
    }
}
