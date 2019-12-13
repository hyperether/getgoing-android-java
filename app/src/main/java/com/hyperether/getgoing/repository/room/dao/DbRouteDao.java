package com.hyperether.getgoing.repository.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.hyperether.getgoing.repository.room.entity.DbRoute;

import java.util.List;

@Dao
public interface DbRouteDao {

    @Query("SELECT * FROM DbRoute")
    List<DbRoute> getAll();

    @Insert
    long insertRoute(DbRoute dbRoute);

    @Query("SELECT * FROM DbRoute WHERE id = :id")
    DbRoute getRouteById(long id);

    @Query("SELECT * FROM DbRoute WHERE id = :id")
    LiveData<DbRoute> getRouteByIdAsLiveData(long id);

    @Delete
    void deleteRoutes(DbRoute... routes);

    @Query("DELETE FROM DbRoute WHERE id = :id")
    void deleteRouteById(long id);

    @Query("SELECT * FROM DbRoute ORDER BY id DESC LIMIT 1")
    DbRoute getLatestRoute();
}
