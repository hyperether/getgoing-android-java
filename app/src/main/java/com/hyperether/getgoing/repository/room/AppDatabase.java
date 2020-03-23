package com.hyperether.getgoing.repository.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hyperether.getgoing.repository.room.dao.DbNodeDao;
import com.hyperether.getgoing.repository.room.dao.DbRouteDao;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;

/**
 * Created by Slobodan on 7/11/2017.
 */
@Database(entities = {DbNode.class, DbRoute.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "getgoing_db";
    private static AppDatabase instance;

    public abstract DbNodeDao dbNodeDao();

    public abstract DbRouteDao dbRouteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
