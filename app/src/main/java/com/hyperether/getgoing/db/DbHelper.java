package com.hyperether.getgoing.db;

import android.arch.persistence.room.Room;
import android.content.Context;

/**
 * Created by Slobodan on 7/11/2017.
 */
public class DbHelper {

    private static final String DATABASE_NAME = "getgoing_db";
    private static DbHelper instance;

    private AppDatabase db;

    private DbHelper(Context ctxt) {
        db = Room.databaseBuilder(ctxt, AppDatabase.class, DATABASE_NAME).build();
    }

    public static DbHelper getInstance(Context ctxt) {
        if (instance == null)
            instance = new DbHelper(ctxt);
        return instance;
    }
}
