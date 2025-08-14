package com.example.voipsim.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a single call log record.
 */
@Entity(tableName = "call_logs")
public class CallLogEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String caller;
    public long startTime;
    public long endTime;
    @NonNull public String type; // MISSED or ANSWERED
    public long durationSec;
}
