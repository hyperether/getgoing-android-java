package com.hyperether.getgoing.repository.room;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.lifecycle.LiveData;

import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.repository.room.dao.DbNodeDao;
import com.hyperether.getgoing.repository.room.dao.DbRouteDao;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class GgRepository {

    private static GgRepository instance;

    private AppDatabase db;
    private DbNodeDao nodeDao;
    private DbRouteDao routeDao;
    private LiveData<List<DbNode>> allNodes;

    private Handler mHandler;

    public GgRepository() {
        db = AppDatabase.getInstance(GetGoingApp.getInstance().getApplicationContext());
        nodeDao = db.dbNodeDao();
        routeDao = db.dbRouteDao();
        allNodes = nodeDao.getAllAsLiveData();
    }

    public static GgRepository getInstance() {
        if (instance == null) {
            instance = new GgRepository();
        }
        return instance;
    }

    public void daoInsertNode(DbNode node) {
        nodeDao.insertNode(node);
    }

    public LiveData<List<DbNode>> getAllNodes() {
        return allNodes;
    }

    public long insertRoute(final DbRoute dbRoute) {
        AtomicLong routeId = new AtomicLong();

        new Thread(() -> {
            routeId.set(routeDao.insertRoute(dbRoute));
//            LiveData<DbRoute> route = routeDao.getRouteByIdAsLiveData(routeId);

//            if (route.getValue() != null) {
//                for (DbNode currentNode : nodeList) {
//                    daoInsertNode(new DbNode(0, currentNode.getLatitude(), currentNode.getLongitude(),
//                                    currentNode.getVelocity(), currentNode.getIndex(),
//                                    routeId));
//                }
//            }
        }).start();

        return routeId.get();
    }

    public void insertRouteInit(final DbRoute dbRoute, List<DbNode> nodeList) {
        getRepoHandler().post(new Runnable() {
            @Override
            public void run() {
                long routeId;

                routeId = routeDao.insertRoute(dbRoute);
                LiveData<DbRoute> route = routeDao.getRouteByIdAsLiveData(routeId);

                if (route != null) {
                    for (DbNode currentNode : nodeList) {
                        daoInsertNode(new DbNode(0, currentNode.getLatitude(), currentNode.getLongitude(),
                                currentNode.getVelocity(), currentNode.getIndex(),
                                routeId));
                    }
                }
            }
        });

    }

    public Handler getRepoHandler() {
        if (mHandler == null) {
            HandlerThread mThread = new HandlerThread("db-thread");
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
        }
        return mHandler;
    }

}
