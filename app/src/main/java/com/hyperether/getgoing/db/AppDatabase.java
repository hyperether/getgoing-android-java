package com.hyperether.getgoing.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Slobodan on 7/11/2017.
 */
@Database(entities = {DbNode.class, DbRoute.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DbNodeDao dbNodeDao();

    public abstract DbRouteDao dbRouteDao();
}
