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
    private LiveData<List<DbNode>> allNodesById;

    private Handler mHandler;

    private GgRepository() {
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
        getRepoHandler().post(() -> nodeDao.insertNode(node));
    }

    public LiveData<List<DbNode>> getAllNodes() {
        return allNodes;
    }

    public LiveData<List<DbNode>> getAllNodesById(long id) {
        allNodesById = nodeDao.getAllByRouteIdAsLiveData(id);
        return allNodesById;
    }

    public void insertRoute(final DbRoute dbRoute, DbRouteAddedCallback listener) {
        AtomicLong routeId = new AtomicLong();

        getRepoHandler().post(() -> {
            routeId.set(routeDao.insertRoute(dbRoute));
            listener.onRouteAdded(routeId.get());
        });
    }

    public void updateRoute(DbRoute dbRoute) {
        getRepoHandler().post(() -> routeDao.updateRoute(dbRoute));
    }

    public void deleteRouteById(long id) {
        getRepoHandler().post(() -> {
            routeDao.deleteRouteById(id);
        });
    }

    public LiveData<List<DbRoute>> getAllRoutes() {
        return routeDao.getAll();
    }

    public void deleteNodesByRouteId(long id) {
        getRepoHandler().post(() -> {
            nodeDao.deleteAllByRouteId(id);
        });
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

    public LiveData<DbRoute> getRouteByIdAsLiveData(long id) {
        return routeDao.getRouteByIdAsLiveData(id);
    }

    public LiveData<DbRoute> getLastRouteAsLiveData() {
        return routeDao.getLatestRouteAsLiveData();
    }

    public void markLastNode() {
        getRepoHandler().post(new Runnable() {
            @Override
            public void run() {
                DbNode lastNode = nodeDao.getLastNode();
                lastNode.setLast(true);
                nodeDao.update(lastNode);
            }
        });
    }

    public DbRoute getLastRoute() {
        return routeDao.getLatestRoute();
    }

    private Handler getRepoHandler() {
        if (mHandler == null) {
            HandlerThread mThread = new HandlerThread("db-thread");
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
        }
        return mHandler;
    }
}
