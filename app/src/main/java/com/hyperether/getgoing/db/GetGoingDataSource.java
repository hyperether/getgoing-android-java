package com.hyperether.getgoing.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class GetGoingDataSource {
    // Database fields
    private SQLiteDatabase database;
    private GetGoingDatabaseHelper dbHelper;
    private String[] allRouteColumns = {GetGoingDatabaseHelper.COLUMN_ID,
            GetGoingDatabaseHelper.COLUMN_DURATION, GetGoingDatabaseHelper.COLUMN_ENERGY,
            GetGoingDatabaseHelper.COLUMN_LENGTH,
            GetGoingDatabaseHelper.COLUMN_DATE, GetGoingDatabaseHelper.COLUMN_AVGSPEED,
            GetGoingDatabaseHelper.COLUMN_ACTIVITYID};

    private String[] allNodeColumns = {GetGoingDatabaseHelper.COLUMN_ID,
            GetGoingDatabaseHelper.COLUMN_LATITUDE, GetGoingDatabaseHelper.COLUMN_LONGITUDE,
            GetGoingDatabaseHelper.COLUMN_VELOCITY,
            GetGoingDatabaseHelper.COLUMN_NUMBER, GetGoingDatabaseHelper.COLUMN_ROUTE_ID};

    public GetGoingDataSource(Context context) {
        dbHelper = new GetGoingDatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Create a route
     */
    public DbRoute createRoute(long duration, double energy, double length, String date,
                               double avgspeed, int activity_id) {
        DbRoute newRoute = null;
        ContentValues values = new ContentValues();
        values.put(GetGoingDatabaseHelper.COLUMN_DURATION, duration);
        values.put(GetGoingDatabaseHelper.COLUMN_ENERGY, energy);
        values.put(GetGoingDatabaseHelper.COLUMN_LENGTH, length);
        values.put(GetGoingDatabaseHelper.COLUMN_DATE, date);
        values.put(GetGoingDatabaseHelper.COLUMN_AVGSPEED, avgspeed);
        values.put(GetGoingDatabaseHelper.COLUMN_ACTIVITYID, activity_id);

        long insertId = database.insert(GetGoingDatabaseHelper.TABLE_ROUTE, null, values);

        Cursor cursor = database.query(GetGoingDatabaseHelper.TABLE_ROUTE,
                allRouteColumns, GetGoingDatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                newRoute = cursorToRoute(cursor);
            }
            cursor.close();
        }
        return newRoute;
    }

    /**
     * Delete a particular route
     */
    public void deleteRoute(DbRoute route) {
        long id = route.getId();
        System.out.println("Route deleted with id: " + id);
        database.delete(GetGoingDatabaseHelper.TABLE_ROUTE, GetGoingDatabaseHelper.COLUMN_ID
                + " = " + id, null);
    }

    /**
     * Delete a particular route by id
     */
    public void deleteRouteById(long id) {
        // delete all nodes attached to this route
        deleteRouteNodes(id);
        // delete the route by id
        database.delete(GetGoingDatabaseHelper.TABLE_ROUTE, GetGoingDatabaseHelper.COLUMN_ID
                + " = " + id, null);
    }

    /**
     * Get all routes from database
     */
    public List<DbRoute> getAllRoutes() {
        List<DbRoute> routes = new ArrayList<DbRoute>();

        Cursor cursor = database.query(GetGoingDatabaseHelper.TABLE_ROUTE,
                allRouteColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DbRoute route = cursorToRoute(cursor);
            routes.add(route);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return routes;
    }

    /**
     * Get route with particular id
     */
    public DbRoute getRoute(long route_id) {

        // Get the cursor to route with a particular id
        Cursor cursor = database.query(GetGoingDatabaseHelper.TABLE_ROUTE,
                allRouteColumns, GetGoingDatabaseHelper.COLUMN_ID + " = " + route_id, null, null,
                null, null);

        cursor.moveToFirst();
        DbRoute route = null;
        if (cursor.getCount() > 0) {
            route = cursorToRoute(cursor);
        }

        // make sure to close the cursor
        cursor.close();
        return route;
    }

    /**
     * Database cursor to route converting function
     */
    private DbRoute cursorToRoute(Cursor cursor) {
        DbRoute route = new DbRoute(cursor.getLong(0), cursor.getLong(1), cursor.getDouble(2)
                , cursor.getDouble(3), cursor.getString(4), cursor.getDouble(5), cursor.getInt(6));

        return route;
    }

    /**
     * Create a node
     */
    public DbNode createNode(double latitude, double longitude, float velocity, long number,
                             long route_id) {

        ContentValues values = new ContentValues();
        values.put(GetGoingDatabaseHelper.COLUMN_LATITUDE, latitude);
        values.put(GetGoingDatabaseHelper.COLUMN_LONGITUDE, longitude);
        values.put(GetGoingDatabaseHelper.COLUMN_VELOCITY, velocity);
        values.put(GetGoingDatabaseHelper.COLUMN_NUMBER, number);
        values.put(GetGoingDatabaseHelper.COLUMN_ROUTE_ID, route_id);

        long insertId = database.insert(GetGoingDatabaseHelper.TABLE_NODE, null, values);

        Cursor cursor = database.query(GetGoingDatabaseHelper.TABLE_NODE,
                allNodeColumns, GetGoingDatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();

        DbNode newNode = cursorToNode(cursor);
        cursor.close();
        return newNode;
    }

    /**
     * Delete a particular node
     */
    public void deleteNode(DbNode node) {
        long id = node.getId();
        System.out.println("Route deleted with id: " + id);
        database.delete(GetGoingDatabaseHelper.TABLE_NODE, GetGoingDatabaseHelper.COLUMN_ID
                + " = " + id, null);
    }

    /**
     * Delete all nodes within the requested route
     */
    public void deleteRouteNodes(long route_id) {

        database.delete(GetGoingDatabaseHelper.TABLE_NODE, GetGoingDatabaseHelper.COLUMN_ROUTE_ID
                + " = " + route_id, null);
    }

    /**
     * Get all nodes in the database
     */
    public List<DbNode> getAllNodes() {
        List<DbNode> nodes = new ArrayList<DbNode>();

        Cursor cursor = database.query(GetGoingDatabaseHelper.TABLE_NODE,
                allNodeColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DbNode node = cursorToNode(cursor);
            nodes.add(node);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return nodes;
    }

    /**
     * Get all nodes contained in the requested route
     */
    public List<DbNode> getRouteNodes(long route_id) {
        List<DbNode> nodes = new ArrayList<DbNode>();

        Cursor cursor = database.query(GetGoingDatabaseHelper.TABLE_NODE, allNodeColumns,
                GetGoingDatabaseHelper.COLUMN_ROUTE_ID + " = " + route_id, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DbNode node = cursorToNode(cursor);
            nodes.add(node);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return nodes;
    }

    /**
     * Database cursor to node converting function
     */
    private DbNode cursorToNode(Cursor cursor) {
        DbNode node = new DbNode(cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2),
                cursor.getFloat(3)
                , cursor.getLong(4), cursor.getLong(5));

        return node;
    }
}