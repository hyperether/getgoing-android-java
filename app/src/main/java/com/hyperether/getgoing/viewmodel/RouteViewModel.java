package com.hyperether.getgoing.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;

import java.util.List;

public class RouteViewModel extends AndroidViewModel {

    private LiveData<List<DbRoute>> routeList;

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

    public LiveData<DbRoute> getRouteByIdAsLiveData(long id) {
        return GgRepository.getInstance().getRouteByIdAsLiveData(id);
    }

    public void removeRouteById(long id) {
        GgRepository.getInstance().deleteNodesByRouteId(id);
        GgRepository.getInstance().deleteRouteById(id);
    }
}
