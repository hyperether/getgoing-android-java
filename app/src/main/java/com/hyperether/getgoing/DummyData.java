package com.hyperether.getgoing;

import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;

import java.util.ArrayList;
import java.util.List;

public class DummyData {


    // TODO dummy data - delete this - Ivana

    public static void insertDummyData() {

        List<DbNode> nodeList1 = new ArrayList<>();
        nodeList1.add(new DbNode(0, 45.243823, 19.841508, 1, 0, 1));
        nodeList1.add(new DbNode(0, 45.243899, 19.842162, 1, 1, 1));
        nodeList1.add(new DbNode(0, 45.244067, 19.842655, 1, 2, 1));
        nodeList1.add(new DbNode(0, 45.243708, 19.842821, 1, 3, 1));
        nodeList1.add(new DbNode(0, 45.243833, 19.843304, 1, 4, 1));

        List<DbNode> nodeList2 = new ArrayList<>();
        nodeList2.add(new DbNode(0, 45.243708, 19.847392, 2, 0, 2));
        nodeList2.add(new DbNode(0, 45.243289, 19.847451, 2, 1, 2));
        nodeList2.add(new DbNode(0, 45.242758, 19.847532, 2, 2, 2));
        nodeList2.add(new DbNode(0, 45.242414, 19.846652, 2, 3, 2));

        List<DbNode> nodeList3 = new ArrayList<>();
        nodeList3.add(new DbNode(0, 45.242301, 19.845338, 3, 0, 3));
        nodeList3.add(new DbNode(0, 45.243389, 19.844748, 3, 1, 3));
        nodeList3.add(new DbNode(0, 45.244095, 19.844362, 3, 2, 3));
        nodeList3.add(new DbNode(0, 45.243729, 19.842887, 3, 3, 3));
        nodeList3.add(new DbNode(0, 45.243091, 19.843155, 3, 4, 3));


        DbRoute dbRoute = new DbRoute(0, 15, 0, 180, "17.01.2020", 0, 1, 200);
        GgRepository.getInstance().insertRouteInit(dbRoute, nodeList1);

        dbRoute = new DbRoute(0, 6, 0, 50, "18.01.2020", 0, 2, 150);
        GgRepository.getInstance().insertRouteInit(dbRoute, nodeList1);

        dbRoute = new DbRoute(0, 10, 0, 220, "19.01.2020", 0, 1, 300);
        GgRepository.getInstance().insertRouteInit(dbRoute, nodeList2);

        dbRoute = new DbRoute(0, 20, 0, 200, "20.01.2020", 0, 2, 200);
        GgRepository.getInstance().insertRouteInit(dbRoute, nodeList2);

        dbRoute = new DbRoute(0, 5, 0, 100, "22.01.2020", 0, 3, 150);
        GgRepository.getInstance().insertRouteInit(dbRoute, nodeList2);

        dbRoute = new DbRoute(0, 7, 0, 260, "23.01.2020", 0, 1, 300);
        GgRepository.getInstance().insertRouteInit(dbRoute, nodeList3);

        dbRoute = new DbRoute(0, 30, 0, 90, "24.01.2020", 0, 2, 200);
        GgRepository.getInstance().insertRouteInit(dbRoute, nodeList3);

        dbRoute = new DbRoute(0, 0, 0, 50, "25.01.2020", 0, 3, 150);
        GgRepository.getInstance().insertRouteInit(dbRoute, nodeList3);

    }
}
