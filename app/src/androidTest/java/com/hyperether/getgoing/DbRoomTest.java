package com.hyperether.getgoing;

import android.content.Context;

import com.hyperether.getgoing.repository.room.AppDatabase;
import com.hyperether.getgoing.repository.room.dao.DbNodeDao;
import com.hyperether.getgoing.repository.room.dao.DbRouteDao;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import androidx.room.Room;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

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
        DbRoute route = new DbRoute(1, 1, 1, 1, "1233", 1, 1);
        DbNode node = new DbNode(1, 2, 1, 1, 1, route.getId());
        routeDao.insertRoute(route);
        nodeDao.insertNode(node);
        List<DbNode> nodes = nodeDao.getAll();
        List<DbRoute> routes = routeDao.getAll();
    }


}
