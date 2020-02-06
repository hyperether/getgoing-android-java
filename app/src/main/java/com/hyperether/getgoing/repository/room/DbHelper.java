package com.hyperether.getgoing.repository.room;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.hyperether.getgoing.repository.room.entity.DbRoute;

import java.util.List;


/**
 * Created by Slobodan on 7/11/2017.
 * <p>
 * TODO Use GgRepository
 */
@Deprecated
public class DbHelper {

    public interface OnDataLoadedListener {
        void onLoad(List<DbRoute> routes);
    }

    private static DbHelper instance;
    private Handler mHandler;

    private AppDatabase db;

    private DbHelper(Context ctxt) {
        db = AppDatabase.getInstance(ctxt);
    }

    public static DbHelper getInstance(Context ctxt) {
        if (instance == null)
            instance = new DbHelper(ctxt);
        return instance;
    }

    public void getRoutes(final OnDataLoadedListener dataLoadListener) {
        getDbHandler().post(new Runnable() {
            public void run() {
                List<DbRoute> routes = db.dbRouteDao().getAll();
                dataLoadListener.onLoad(routes);
            }
        });
    }

    public Handler getDbHandler() {
        if (mHandler == null) {
            HandlerThread mThread = new HandlerThread("db-thread");
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
        }
        return mHandler;
    }
}
