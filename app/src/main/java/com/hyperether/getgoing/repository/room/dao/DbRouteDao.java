package com.hyperether.getgoing.repository.room.dao;

import com.hyperether.getgoing.repository.room.entity.DbRoute;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

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
