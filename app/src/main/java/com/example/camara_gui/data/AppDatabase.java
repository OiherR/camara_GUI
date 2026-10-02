package com.example.camara_gui.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
@Database(
        entities = {Photo.class},
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PhotoDao photoDao();
    private static volatile AppDatabase INSTANCE;
    public static AppDatabase getDatabase(Context context) {

        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "photo_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}