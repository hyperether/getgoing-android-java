package com.hyperether.getgoing.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = DbRoute.class,
        parentColumns = "id",
        childColumns = "route_idroute",
        onDelete = CASCADE
))
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
    @ColumnInfo(name = "route_idroute")
    private long route_id; // foreign key

    public DbNode(long id, double latitude, double longitude, float velocity,
                  long index, long route_id) {
        super();
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.velocity = velocity;
        this.index = index;
        this.route_id = route_id;
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

    public long getRoute_id() {
        return route_id;
    }

    public void setRouteId(long route_id) {
        this.route_id = route_id;
    }


    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // TODO Auto-generated method stub
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeFloat(velocity);
        out.writeLong(index);
        out.writeLong(route_id);
        out.writeLong(id);
    }

    private DbNode(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        velocity = in.readFloat();
        index = in.readLong();
        route_id = in.readLong();
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
