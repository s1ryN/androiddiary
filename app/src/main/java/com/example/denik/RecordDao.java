package com.example.denik;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecordDao {
    @Query("SELECT * FROM records")
    List<RecordEntity> getAll();

    @Insert
    long insert(RecordEntity record);

    @Update
    void update(RecordEntity record);

    @Delete
    void delete(RecordEntity record);

    @Query("SELECT * FROM records WHERE id = :someId LIMIT 1")
    RecordEntity getById(long someId);

}
