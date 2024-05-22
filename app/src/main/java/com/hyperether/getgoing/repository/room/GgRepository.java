package com.hyperether.getgoing.repository.room;

import android.os.Handler;
import android.os.HandlerThread;

import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.repository.room.dao.NodeDao;
import com.hyperether.getgoing.repository.room.dao.RouteDao;
import com.hyperether.getgoing.repository.room.entity.Node;
import com.hyperether.getgoing.repository.room.entity.Route;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import androidx.lifecycle.LiveData;

public class GgRepository {
    private static GgRepository instance;
    private final NodeDao nodeDao;
    private final RouteDao routeDao;
    private final LiveData<List<Node>> allNodes;
    private Handler mHandler;

    private GgRepository() {
        AppDatabase db = AppDatabase.getInstance(GetGoingApp.getInstance().getApplicationContext());
        nodeDao = db.nodeDao();
        routeDao = db.routeDao();
        allNodes = nodeDao.getAllAsLiveData();
    }

    public static GgRepository getInstance() {
        if (instance == null) {
            instance = new GgRepository();
        }
        return instance;
    }

    public void daoInsertNode(Node node) {
        getHandler().post(() -> nodeDao.insertNode(node));
    }

    public LiveData<List<Node>> getAllNodes() {
        return allNodes;
    }

    public LiveData<List<Node>> getAllNodesById(long id) {
        return nodeDao.getAllByRouteIdAsLiveData(id);
    }

    public void insertRoute(final Route route, RouteAddedListener listener) {
        AtomicLong routeId = new AtomicLong();

        getHandler().post(() -> {
            routeId.set(routeDao.insertRoute(route));
            listener.onRouteAdded(routeId.get());
        });
    }

    public void updateRoute(Route route) {
        getHandler().post(() -> routeDao.updateRoute(route));
    }

    public void deleteRouteById(long id) {
        getHandler().post(() -> {
            routeDao.deleteRouteById(id);
        });
    }

    public LiveData<List<Route>> getAllRoutes() {
        return routeDao.getAll();
    }

    public void deleteNodesByRouteId(long id) {
        getHandler().post(() -> {
            nodeDao.deleteAllByRouteId(id);
        });
    }

    public void insertRouteInit(final Route dbRoute, List<Node> nodeList) {
        getHandler().post(() -> {
            long routeId = routeDao.insertRoute(dbRoute);
            LiveData<Route> route = routeDao.getRouteByIdAsLiveData(routeId);

            if (route != null) {
                for (Node currentNode : nodeList) {
                    daoInsertNode(new Node(0, currentNode.getLatitude(), currentNode.getLongitude(),
                            currentNode.getVelocity(), currentNode.getIndex(),
                            routeId));
                }
            }
        });
    }

    public LiveData<Route> getRouteByIdAsLiveData(long id) {
        return routeDao.getRouteByIdAsLiveData(id);
    }

    public LiveData<Route> getLastRouteAsLiveData() {
        return routeDao.getLatestRouteAsLiveData();
    }

    public void markLastNode() {
        getHandler().post(() -> {
            Node lastNode = nodeDao.getLastNode();
            lastNode.setLast(true);
            nodeDao.update(lastNode);
        });
    }

    public Route getLastRoute() {
        return routeDao.getLatestRoute();
    }

    private Handler getHandler() {
        if (mHandler == null) {
            HandlerThread mThread = new HandlerThread("db-thread");
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
        }
        return mHandler;
    }

    public void updateRouteDuration(long id, long duration) {
        getHandler().post(() -> {
            Route route = routeDao.getRouteById(id);
            if (route != null) {
                route.setDuration(duration);
                routeDao.updateRoute(route);
            }
        });
    }
}
