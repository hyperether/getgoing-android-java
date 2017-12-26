package com.hyperether.getgoing.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface DbRouteDao {

    @Query("SELECT * FROM DbRoute")
    List<DbRoute> getAll();

    @Insert
    long insertRoute(DbRoute dbRoute);

    @Query("SELECT * FROM DbRoute WHERE id = :id")
    DbRoute getRouteById(long id);

    @Delete
    void deleteRoutes(DbRoute... routes);

    @Query("DELETE FROM DbRoute WHERE id = :id")
    void deleteRouteById(long id);
}
