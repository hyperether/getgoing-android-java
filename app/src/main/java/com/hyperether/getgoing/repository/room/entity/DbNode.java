package com.hyperether.getgoing.repository.room.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = DbRoute.class,
        parentColumns = "id",
        childColumns = "routeId",
        onDelete = CASCADE),
        indices = {@Index(value = "routeId")
        })
public class DbNode implements Parcelable {
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

    public DbNode(long id, double latitude, double longitude, float velocity,
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

    private DbNode(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        velocity = in.readFloat();
        index = in.readLong();
        routeId = in.readLong();
        id = in.readLong();
    }

    public static final Parcelable.Creator<DbNode> CREATOR
            = new Parcelable.Creator<DbNode>() {
        @Override
        public DbNode createFromParcel(Parcel in) {
            return new DbNode(in);
        }

        @Override
        public DbNode[] newArray(int size) {
            return new DbNode[size];
        }
    };
}
