package com.example.voipsim.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database singleton access.
 */
@Database(entities = {CallLogEntity.class}, version = 1)
public abstract class AppDb extends RoomDatabase {
    public abstract CallLogDao dao();
    private static volatile AppDb s;
    public static AppDb i(Context c) {
        if (s == null) {
            synchronized (AppDb.class) {
                if (s == null) {
                    s = Room.databaseBuilder(c.getApplicationContext(), AppDb.class, "voip-db").build();
                }
            }
        }
        return s;
    }
}
