package com.hyperether.getgoing.repository.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hyperether.getgoing.repository.room.entity.Node;

import java.util.List;

/**
 * Created by Slobodan on 7/11/2017.
 */
@Dao
public interface NodeDao {

    @Query("SELECT * FROM Node")
    List<Node> getAll();

    @Query("SELECT * FROM Node")
    LiveData<List<Node>> getAllAsLiveData();

    @Query("SELECT * FROM Node WHERE routeId = :id")
    LiveData<List<Node>> getAllByRouteIdAsLiveData(long id);

    @Insert
    void insertNode(Node node);

    @Query("SELECT * FROM Node ORDER BY id DESC LIMIT 1")
    Node getLastNode();

    @Query("DELETE FROM Node WHERE routeId = :id")
    void deleteAllByRouteId(long id);

    @Update
    void update(Node node);

}
