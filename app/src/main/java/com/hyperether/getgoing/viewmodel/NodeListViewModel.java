package com.hyperether.getgoing.viewmodel;

import android.app.Activity;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.SharedPref;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;

import java.util.List;

public class NodeListViewModel extends ViewModel {

    private MutableLiveData<Long> routeID = new MutableLiveData<Long>();
    private LiveData<List<DbNode>> nodesByRouteId = Transformations.switchMap(routeID, new Function<Long, LiveData<List<DbNode>>>() {
        @Override
        public LiveData<List<DbNode>> apply(Long input) {
            return GgRepository.getInstance().getAllNodesById(input);
        }
    });

    public void setRouteID(long id) {
        this.routeID.setValue(id);
    }

    public LiveData<List<DbNode>> getNodeListById(long id) {
        return nodesByRouteId;
    }

    public void continueTracking(Activity activity) {
        GetGoingApp.getInstance().getHandler().post(() -> {
            long id = GgRepository.getInstance().getLastRoute().getId();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setRouteID(id);
                    getNodeListById(id);
                }
            });
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
