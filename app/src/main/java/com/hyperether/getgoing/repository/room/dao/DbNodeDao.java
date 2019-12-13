package com.hyperether.getgoing.repository.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.hyperether.getgoing.repository.room.entity.DbNode;

import java.util.List;

/**
 * Created by Slobodan on 7/11/2017.
 */
@Dao
public interface DbNodeDao {

    @Query("SELECT * FROM DbNode")
    List<DbNode> getAll();

    @Query("SELECT * FROM DbNode")
    LiveData<List<DbNode>> getAllAsLiveData();

    @Query("SELECT * FROM DbNode WHERE routeId = :id")
    List<DbNode> getAllByRouteId(long id);

    @Query("SELECT * FROM DbNode WHERE routeId = :id")
    LiveData<List<DbNode>> getAllByRouteIdAsLiveData(long id);

    @Insert
    void insertNode(DbNode node);

    @Delete
    void deleteNodes(DbNode... nodes);
}
