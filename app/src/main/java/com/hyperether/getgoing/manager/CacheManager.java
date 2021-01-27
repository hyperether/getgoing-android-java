package com.hyperether.getgoing.manager;

import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.repository.room.entity.DbNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikola on 11/07/17.
 */
//Cuva podatke o trenutnom putovanju/pokretu
public class CacheManager {

    private static CacheManager instance = null;
//Lista cvorova (lista posecenih lokacija)
    private List<DbNode> mRoute = new ArrayList<>();
    //od pocetka do kraja
    private String timeElapsed;
    private Double distanceCumulative = 0.0;
    private Double kcalCumulative;
    private Double velocity;
    private Double velocityAvg;
    private CBDataFrame obDataFrameGlobal;
    //koliko vremena je tralajo putovanje
    private long timeCumulative;
    //koliko vremena je trajalo putovanje u sekundama
    private int secondsCumulative;
    private long time = 0; // time between to position updates
    private long currentRouteId = 0;
//trenutna ruta
    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    private CacheManager() {
        obDataFrameGlobal = CBDataFrame.getInstance();
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

    public CBDataFrame getObDataFrameGlobal() {
        return obDataFrameGlobal;
    }

    public void setObDataFrameLocal(CBDataFrame obDataFrameLocal) {
        this.obDataFrameGlobal = obDataFrameLocal;
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

    public long getCurrentRouteId() {
        return currentRouteId;
    }
//Kada ima nova putovanja, novi cvor, jer nam prosledjuje parametre.
    public void setCurrentRouteId(long currentRouteId) {
        this.currentRouteId = currentRouteId;
    }
}