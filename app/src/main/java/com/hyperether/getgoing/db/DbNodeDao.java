package com.hyperether.getgoing.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by Slobodan on 7/11/2017.
 */
@Dao
public interface DbNodeDao {

    @Query("SELECT * FROM DbNode")
    List<DbNode> getAll();

    @Query("SELECT * FROM DbNode WHERE routeId = :id")
    List<DbNode> getAllByRouteId(long id);

    @Insert
    void insertNode(DbNode node);

    @Delete
    void deleteNodes(DbNode... nodes);
}
