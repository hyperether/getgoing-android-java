package com.hyperether.getgoing.viewmodel;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;

import java.util.List;

public class RouteViewModel extends AndroidViewModel {

    private final LiveData<List<DbRoute>> routeList;
    private final MutableLiveData<Long> routeID = new MutableLiveData<>();
    private final MutableLiveData<DbRoute> route = new MutableLiveData<>();

    public LiveData<DbRoute> getRouteByIdAsLiveData(long id) {
        return route;
    }

    public void setRouteID(long id) {
        routeID.setValue(id);
        route.setValue(GgRepository.getInstance().getRouteByIdAsLiveData(id).getValue());
    }

    public RouteViewModel(@NonNull Application application) {
        super(application);
        routeList = GgRepository.getInstance().getAllRoutes();
    }

    public LiveData<List<DbRoute>> getAllRoutes() {
        return routeList;
    }

    public LiveData<List<DbNode>> getNodeListById(long id) {
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
                    getRouteByIdAsLiveData(id);
                }
            });
        });
    }
}
