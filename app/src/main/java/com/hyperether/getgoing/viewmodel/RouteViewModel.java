package com.hyperether.getgoing.viewmodel;

import android.app.Activity;
import android.app.Application;

import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.Node;
import com.hyperether.getgoing.repository.room.entity.Route;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class RouteViewModel extends AndroidViewModel {

    private final LiveData<List<Route>> routeList;
    private final MutableLiveData<Long> routeID = new MutableLiveData<>();
    private final MutableLiveData<Route> route = new MutableLiveData<>();

    public LiveData<Route> getRouteByIdAsLiveData() {
        return route;
    }

    public void setRouteID(long id) {
        routeID.setValue(id);
        GgRepository.getInstance().getRouteByIdAsLiveData(id).observeForever(dbRoute -> route.postValue(dbRoute));
    }

    public RouteViewModel(@NonNull Application application) {
        super(application);
        routeList = GgRepository.getInstance().getAllRoutes();
    }

    public LiveData<List<Route>> getAllRoutes() {
        return routeList;
    }

    public LiveData<List<Node>> getNodeListById(long id) {
        return GgRepository.getInstance().getAllNodesById(id);
    }

    public void removeRouteById(long id) {
        GgRepository.getInstance().deleteNodesByRouteId(id);
        GgRepository.getInstance().deleteRouteById(id);
    }

    public void continueTracking(Activity activity) {
        GetGoingApp.getInstance().getHandler().post(() -> {
            long id = GgRepository.getInstance().getLastRoute().getId();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setRouteID(id);
                    getNodeListById(id);
                    getRouteByIdAsLiveData();
                }
            });
        });
    }
}
