package com.hyperether.getgoing.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.repository.room.DbHelper;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;

import java.util.List;

public class RouteViewModel extends ViewModel {

    private MutableLiveData<List<DbRoute>> routeList;

    public LiveData<List<DbRoute>> getRouteList() {
        if (routeList == null) {
            routeList = new MutableLiveData<List<DbRoute>>();
            loadRoutes();
        }
        return routeList;
    }

    private void loadRoutes() {
        // Do an asynchronous operation to fetch routeList.
        Context ctxt = GetGoingApp.getInstance().getApplicationContext();
        DbHelper.getInstance(ctxt).getRoutes(new DbHelper.OnDataLoadedListener() {
            @Override
            public void onLoad(List<DbRoute> routes) {
                routeList.postValue(routes);
            }
        });
    }

    public LiveData<List<DbNode>> getNodeListById(long id) {
        return GgRepository.getInstance().getAllNodesById(id);
    }
}
