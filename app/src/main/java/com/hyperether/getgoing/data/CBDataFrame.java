package com.hyperether.getgoing.data;

import android.os.Parcel;
import android.os.Parcelable;

public class CBDataFrame implements Parcelable {

    private int profileId;
    private String profileName;
    private int measurementSystemId;
    private int age;
    private int weight;

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public int getMeasurementSystemId() {
        return measurementSystemId;
    }

    public void setMeasurementSystemId(int measurementSystemId) {
        this.measurementSystemId = measurementSystemId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.profileId);
        out.writeString(this.profileName);
        out.writeInt(this.measurementSystemId);
        out.writeInt(this.weight);
        out.writeInt(this.age);
    }

    public static final Parcelable.Creator<CBDataFrame> CREATOR = new Parcelable.Creator<CBDataFrame>() {
        @Override
        public CBDataFrame createFromParcel(Parcel in) {
            return new CBDataFrame(in);
        }

        @Override
        public CBDataFrame[] newArray(int size) {
            return new CBDataFrame[size];
        }
    };

    public CBDataFrame(Parcel in) {
        this.profileId = in.readInt();
        this.profileName = in.readString();
        this.measurementSystemId = in.readInt();
        this.weight = in.readInt();
        this.age = in.readInt();
    }

    public CBDataFrame() {

    }

}
