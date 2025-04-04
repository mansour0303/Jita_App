package com.example.jita.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

@Dao
interface TaskDao {
    // Use Flow to observe all tasks
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    // Get tasks for a specific date range (start/end of day)
    @Query("SELECT * FROM tasks WHERE dueDate >= :startOfDay AND dueDate <= :endOfDay ORDER BY priority ASC, name ASC")
    fun getTasksForDate(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Query to delete tasks associated with a specific list name when the list is deleted
    @Query("DELETE FROM tasks WHERE list = :listName")
    suspend fun deleteTasksByListName(listName: String)

    // Query to update tasks associated with a renamed list
    @Query("UPDATE tasks SET list = :newName WHERE list = :oldName")
    suspend fun updateTasksListName(oldName: String, newName: String)

    // Get tasks for a specific list name (handle null for tasks without a list)
    @Query("SELECT * FROM tasks WHERE list = :listName ORDER BY priority ASC, name ASC")
    fun getTasksByListName(listName: String): Flow<List<TaskEntity>>

    // Query for tasks where list is NULL
    @Query("SELECT * FROM tasks WHERE list IS NULL ORDER BY priority ASC, name ASC")
    fun getTasksWithNullList(): Flow<List<TaskEntity>>

    // Add this method to your TaskDao interface
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
} 