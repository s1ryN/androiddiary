package com.example.denik;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {RecordEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecordDao recordDao();
}
