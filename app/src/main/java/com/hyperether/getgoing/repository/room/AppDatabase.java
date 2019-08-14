package com.hyperether.getgoing.repository.room;

import com.hyperether.getgoing.repository.room.dao.DbNodeDao;
import com.hyperether.getgoing.repository.room.dao.DbRouteDao;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Created by Slobodan on 7/11/2017.
 */
@Database(entities = {DbNode.class, DbRoute.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DbNodeDao dbNodeDao();

    public abstract DbRouteDao dbRouteDao();
}
