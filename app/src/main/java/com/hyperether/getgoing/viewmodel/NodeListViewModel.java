package com.hyperether.getgoing.viewmodel;

import android.app.Activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.SharedPref;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.Node;

import java.util.List;

public class NodeListViewModel extends ViewModel {


    private final MutableLiveData<Long> routeID = new MutableLiveData<>();
    private final MutableLiveData<List<Node>> nodesByRouteId = new MutableLiveData<>();

    public void setRouteID(long id) {
        this.routeID.setValue(id);
        GgRepository.getInstance().getAllNodesById(id).observeForever(dbNodes -> nodesByRouteId.postValue(dbNodes));
    }

    public LiveData<List<Node>> getNodeListById() {
        return nodesByRouteId;
    }

    public void continueTracking(Activity activity) {
        GetGoingApp.getInstance().getHandler().post(() -> {
            long id = GgRepository.getInstance().getLastRoute().getId();
            activity.runOnUiThread(() -> setRouteID(id));
        });
    }

    public long getChronometerLastTime() {
        return SharedPref.getLastTime();
    }

    public void setChronometerLastTime(long time) {
        SharedPref.setLastTime(time);
    }

    public long getBackgroundStartTime() {
        return SharedPref.getBackgroundStartTime();
    }

    public void setBackgroundStartTime(long currentTimeMillis) {
        SharedPref.setBackgroundStartTime(currentTimeMillis);
    }
}
