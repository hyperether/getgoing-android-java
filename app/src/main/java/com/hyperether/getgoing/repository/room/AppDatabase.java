package com.hyperether.getgoing.repository.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hyperether.getgoing.repository.room.dao.NodeDao;
import com.hyperether.getgoing.repository.room.dao.RouteDao;
import com.hyperether.getgoing.repository.room.entity.Node;
import com.hyperether.getgoing.repository.room.entity.Route;

/**
 * Created by Slobodan on 7/11/2017.
 */
@Database(entities = {Node.class, Route.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "getgoing_db";
    private static AppDatabase instance;

    public abstract NodeDao nodeDao();

    public abstract RouteDao routeDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
