package com.hyperether.getgoing.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Slobodan on 7/11/2017.
 */
@Database(entities = {DbNode.class, DbRoute.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DbNodeDao dbNodeDao();

    public abstract DbRouteDao dbRouteDao();

    private static final String DATABASE_NAME = "getgoing_db";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room
                    .databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }


}
