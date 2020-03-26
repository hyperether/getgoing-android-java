package com.hyperether.getgoing.repository.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
    LiveData<List<DbNode>> getAllByRouteIdAsLiveData(long id);

    @Insert
    void insertNode(DbNode node);

    @Query("SELECT * FROM DbNode ORDER BY id DESC LIMIT 1")
    DbNode getLastNode();

    @Query("DELETE FROM DbNode WHERE routeId = :id")
    void deleteAllByRouteId(long id);

    @Update
    void update(DbNode dbNode);

}
