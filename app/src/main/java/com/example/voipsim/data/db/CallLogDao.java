package com.example.voipsim.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CallLogDao {
    @Insert void insert(CallLogEntity e);
    @Query("SELECT * FROM call_logs ORDER BY startTime DESC") LiveData<List<CallLogEntity>> getAll();
}
