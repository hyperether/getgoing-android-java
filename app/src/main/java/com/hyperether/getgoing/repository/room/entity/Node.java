package com.hyperether.getgoing.repository.room.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Route.class,
        parentColumns = "id",
        childColumns = "routeId",
        onDelete = CASCADE),
        indices = {@Index(value = "routeId")
        })
public class Node implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo
    private double latitude;
    @ColumnInfo
    private double longitude;
    @ColumnInfo
    private float velocity;
    @ColumnInfo(name = "number")
    private long index; // node index within a particular route
    @ColumnInfo
    private boolean last;

    private long routeId; // foreign key

    public Node(long id, double latitude, double longitude, float velocity,
                long index, long routeId) {
        super();
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.velocity = velocity;
        this.index = index;
        this.routeId = routeId;
        this.last = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeFloat(velocity);
        out.writeLong(index);
        out.writeLong(routeId);
        out.writeLong(id);
    }

    private Node(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        velocity = in.readFloat();
        index = in.readLong();
        routeId = in.readLong();
        id = in.readLong();
    }

    public static final Parcelable.Creator<Node> CREATOR
            = new Parcelable.Creator<Node>() {
        @Override
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        @Override
        public Node[] newArray(int size) {
            return new Node[size];
        }
    };
}
