package com.example.denik;

import android.app.Application;

import androidx.room.Room;

public class Diary extends Application {
    private static AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "my_records_db"
                )
                // POZOR: allowMainThreadQueries() není ideální pro produkci,
                // ale pro ukázku zatím stačí.
                .allowMainThreadQueries()
                .build();
    }

    public static AppDatabase getDb() {
        return db;
    }
}
