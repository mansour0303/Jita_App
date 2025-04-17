package com.example.jita.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeLog(timeLog: TimeLogEntryEntity): Long

    @Update
    suspend fun updateTimeLog(timeLog: TimeLogEntryEntity)

    @Delete
    suspend fun deleteTimeLog(timeLog: TimeLogEntryEntity)

    @Query("SELECT * FROM time_logs WHERE id = :id")
    suspend fun getTimeLogById(id: Int): TimeLogEntryEntity?

    @Query("SELECT * FROM time_logs WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getTimeLogsForTask(taskId: Int): Flow<List<TimeLogEntryEntity>>

    @Query("SELECT * FROM time_logs ORDER BY startTime DESC")
    fun getAllTimeLogs(): Flow<List<TimeLogEntryEntity>>

    @Query("SELECT * FROM time_logs WHERE startTime >= :startTime AND endTime <= :endTime ORDER BY startTime DESC")
    fun getTimeLogsInTimeRange(startTime: Long, endTime: Long): Flow<List<TimeLogEntryEntity>>

    @Query("DELETE FROM time_logs WHERE taskId = :taskId")
    suspend fun deleteTimeLogsForTask(taskId: Int)

    @Query("DELETE FROM time_logs")
    suspend fun deleteAllTimeLogs()

    /**
     * Get all time logs for a task as a non-flow list for processing
     */
    @Query("SELECT * FROM time_logs WHERE taskId = :taskId")
    suspend fun getTimeLogsForTaskAsList(taskId: Int): List<TimeLogEntryEntity>
} 