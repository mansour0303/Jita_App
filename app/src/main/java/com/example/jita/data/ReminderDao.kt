package com.example.jita.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Entity class for storing reminders in the database
 */
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val message: String,
    val timeInMillis: Long,  // Calendar stored as timestamp
    val alarmSoundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val soundUri: String? = null,  // Uri stored as String
    val soundName: String? = null,
    val attachedTaskIds: String = ""  // List of IDs stored as comma-separated string
)

/**
 * Data Access Object for Reminder entities
 */
@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timeInMillis ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>
    
    @Query("SELECT * FROM reminders ORDER BY timeInMillis ASC")
    suspend fun getAllRemindersAsList(): List<ReminderEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long
    
    @Update
    suspend fun updateReminder(reminder: ReminderEntity)
    
    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
    
    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: Int)
    
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Int): ReminderEntity?
} 