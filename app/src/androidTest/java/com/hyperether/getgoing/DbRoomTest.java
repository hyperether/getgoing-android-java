package com.hyperether.getgoing;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.hyperether.getgoing.db.AppDatabase;
import com.hyperether.getgoing.db.DbNode;
import com.hyperether.getgoing.db.DbNodeDao;
import com.hyperether.getgoing.db.DbRoute;
import com.hyperether.getgoing.db.DbRouteDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by nikola on 2.12.17..
 */
@RunWith(AndroidJUnit4.class)
public class DbRoomTest {

    private DbNodeDao nodeDao;
    private DbRouteDao routeDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        nodeDao = db.dbNodeDao();
        routeDao = db.dbRouteDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void writeRead() {
        db.dbNodeDao().deleteNodes();
        db.dbRouteDao().deleteRoutes();
        DbRoute route = new DbRoute(1,1,1,1, "1233",1,1);
        DbNode node = new DbNode(1,2,1,1,1, route.getId());
        routeDao.insertRoute(route);
        nodeDao.insertNode(node);
        List<DbNode> nodes = nodeDao.getAll();
        List<DbRoute> routes = routeDao.getAll();
    }


}
