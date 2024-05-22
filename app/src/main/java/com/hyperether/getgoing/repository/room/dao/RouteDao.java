package com.hyperether.getgoing.repository.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hyperether.getgoing.repository.room.entity.Route;

import java.util.List;

@Dao
public interface RouteDao {

    @Query("SELECT * FROM Route")
    LiveData<List<Route>> getAll();

    @Insert
    long insertRoute(Route route);

    @Query("SELECT * FROM Route WHERE id = :id")
    Route getRouteById(long id);

    @Query("SELECT * FROM Route WHERE id = :id")
    LiveData<Route> getRouteByIdAsLiveData(long id);

    @Delete
    void deleteRoutes(Route... routes);

    @Query("DELETE FROM Route WHERE id = :id")
    void deleteRouteById(long id);

    @Query("SELECT * FROM Route WHERE goal > 0 ORDER BY id DESC LIMIT 1")
    LiveData<Route> getLatestRouteAsLiveData();

    @Query("SELECT * FROM Route WHERE goal > 0 ORDER BY id DESC LIMIT 1")
    Route getLatestRoute();

    @Update
    void updateRoute(Route route);

}
