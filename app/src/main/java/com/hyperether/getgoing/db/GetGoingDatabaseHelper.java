package com.hyperether.getgoing.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GetGoingDatabaseHelper extends SQLiteOpenHelper {

    // Definition of table route
    public static final String TABLE_ROUTE = "route";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_ENERGY = "energy";
    public static final String COLUMN_LENGTH = "length";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_AVGSPEED = "avgspeed";            // new column since v42
    public static final String COLUMN_ACTIVITYID = "activity_id";        // new column since v42

    // Definition of table node
    public static final String TABLE_NODE = "node";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_VELOCITY = "velocity";
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_ROUTE_ID = "route_idroute";

    private static final String DATABASE_NAME = "getgoing.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statements
    private static final String DATABASE_CREATE = "create table "
            + TABLE_ROUTE + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_DURATION
            + " integer not null, " + COLUMN_ENERGY
            + " double not null, " + COLUMN_LENGTH
            + " double not null, " + COLUMN_DATE
            + " datetime not null, " + COLUMN_AVGSPEED
            + " double not null, " + COLUMN_ACTIVITYID
            + " integer not null );";

    private static final String DATABASE_CREATE2 = "create table "
            + TABLE_NODE + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_LATITUDE
            + " double not null, " + COLUMN_LONGITUDE
            + " double not null, " + COLUMN_VELOCITY
            + " float not null, " + COLUMN_NUMBER
            + " integer not null, " + COLUMN_ROUTE_ID
            + " integer not null, FOREIGN KEY(" + COLUMN_ROUTE_ID + ") REFERENCES route(" + COLUMN_ID + "));";


    public GetGoingDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        database.execSQL(DATABASE_CREATE2);

    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        Log.w(GetGoingDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
        onCreate(db);
    }

}
