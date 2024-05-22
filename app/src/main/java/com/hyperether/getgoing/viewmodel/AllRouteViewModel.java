package com.hyperether.getgoing.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.Node;
import com.hyperether.getgoing.repository.room.entity.Route;

import java.util.List;

public class AllRouteViewModel extends ViewModel{
    public AllRouteViewModel() {

    }

    private LiveData<List<Route>> allRoutes;

    public AllRouteViewModel(@NonNull Application application) {
        allRoutes = GgRepository.getInstance().getAllRoutes();
    }

    public LiveData<List<Route>> getAllRoutes() {
        return allRoutes;
    }

    public LiveData<List<Node>> getNodeListById(long id) {
        return GgRepository.getInstance().getAllNodesById(id);
    }

    public void removeRouteById(long id) {
        GgRepository.getInstance().deleteNodesByRouteId(id);
        GgRepository.getInstance().deleteRouteById(id);
    }
}