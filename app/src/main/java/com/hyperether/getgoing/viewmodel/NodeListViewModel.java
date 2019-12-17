package com.hyperether.getgoing.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;

import java.util.List;

public class NodeListViewModel extends ViewModel {

    private MutableLiveData<List<DbNode>> nodeList;

    public LiveData<List<DbNode>> getNodeList() {
        return GgRepository.getInstance().getAllNodes();
    }

    public LiveData<List<DbNode>> getNodeListById(long id) {
        return GgRepository.getInstance().getAllNodesById(id);
    }
}
