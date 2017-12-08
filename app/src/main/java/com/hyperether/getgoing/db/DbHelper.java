package com.hyperether.getgoing.db;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by Slobodan on 7/11/2017.
 */
public class DbHelper {

    private static final String DATABASE_NAME = "getgoing_db";
    private static DbHelper instance;
    private Handler mHandler;

    private AppDatabase db;

    private DbHelper(Context ctxt) {
        db = Room.databaseBuilder(ctxt, AppDatabase.class, DATABASE_NAME).build();
    }

    public static DbHelper getInstance(Context ctxt) {
        if (instance == null)
            instance = new DbHelper(ctxt);
        return instance;
    }

    public AppDatabase getAppDatabase() {
        return db;
    }

    // TODO: add threading here!
    public void insertRoute(final DbRoute dbRoute,
                            final List<DbNode> nodeList) {
        getDbHandler().post(new Runnable() {
            @Override
            public void run() {
                long routeId = db.dbRouteDao().insertRoute(dbRoute);
                Flowable<DbRoute> route = db.dbRouteDao().getRouteById(routeId);

                if (route != null) {
                    Iterator<DbNode> it = nodeList.iterator();
                    while (it.hasNext()) {
                        DbNode currentNode = it.next();
                        db.dbNodeDao().insertNode(
                                new DbNode(0, currentNode.getLatitude(), currentNode.getLongitude(),
                                        currentNode.getVelocity(), currentNode.getIndex(),
                                        routeId));
                    }
                }
            }
        });
    }

    private Handler getDbHandler() {
        if (mHandler == null) {
            HandlerThread mThread = new HandlerThread("db-thread");
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
        }
        return mHandler;
    }
}
