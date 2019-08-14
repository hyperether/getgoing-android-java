package com.hyperether.getgoing.manager;

import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.repository.room.entity.DbNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikola on 11/07/17.
 */

public class CacheManager {

    private static CacheManager instance = null;

    private List<DbNode> mRoute = new ArrayList<>();
    private String timeElapsed;
    private Double distanceCumulative = 0.0;
    private Double kcalCumulative;
    private Double velocity;
    private Double velocityAvg;
    private CBDataFrame obDataFrameLocal;
    private long timeCumulative;
    private int secondsCumulative;
    private long time = 0; // time between to position updates

    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    public void addRouteNode(DbNode dbNode) {
        mRoute.add(dbNode);
    }

    public List<DbNode> getmRoute() {
        return mRoute;
    }

    public void setmRoute(List<DbNode> mRoute) {
        this.mRoute = mRoute;
    }

    public void clearmRoute() {
        this.mRoute.clear();
    }

    public String getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(String timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public Double getDistanceCumulative() {
        return distanceCumulative;
    }

    public void setDistanceCumulative(Double distanceCumulative) {
        this.distanceCumulative = distanceCumulative;
    }

    public Double getKcalCumulative() {
        return kcalCumulative;
    }

    public void setKcalCumulative(Double kcalCumulative) {
        this.kcalCumulative = kcalCumulative;
    }

    public Double getVelocity() {
        return velocity;
    }

    public void setVelocity(Double velocity) {
        this.velocity = velocity;
    }

    public Double getVelocityAvg() {
        return velocityAvg;
    }

    public void setVelocityAvg(Double velocityAvg) {
        this.velocityAvg = velocityAvg;
    }

    public CBDataFrame getObDataFrameLocal() {
        return obDataFrameLocal;
    }

    public void setObDataFrameLocal(CBDataFrame obDataFrameLocal) {
        this.obDataFrameLocal = obDataFrameLocal;
    }

    public long getTimeCumulative() {
        return timeCumulative;
    }

    public void setTimeCumulative(long timeCumulative) {
        this.timeCumulative = timeCumulative;
    }

    public int getSecondsCumulative() {
        return secondsCumulative;
    }

    public void setSecondsCumulative(int secondsCumulative) {
        this.secondsCumulative = secondsCumulative;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}