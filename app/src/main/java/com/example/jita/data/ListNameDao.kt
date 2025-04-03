package com.example.jita.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListNameDao {
    // Use Flow to observe changes automatically
    @Query("SELECT * FROM list_names ORDER BY name ASC") // Simple alphabetical order for now
    fun getAllListNames(): Flow<List<ListNameEntity>>

    // Get names as simple strings (useful for initial load)
    @Query("SELECT name FROM list_names ORDER BY name ASC")
    suspend fun getAllListNamesSimple(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListName(listName: ListNameEntity)

    @Update
    suspend fun updateListName(listName: ListNameEntity)

    // Need a way to delete by name if we don't store the ID in the UI state
    @Query("DELETE FROM list_names WHERE name = :name")
    suspend fun deleteListNameByName(name: String)

    // Find a list by name to update it (needed if we only pass name/index from UI)
    @Query("SELECT * FROM list_names WHERE name = :name LIMIT 1")
    suspend fun getListByName(name: String): ListNameEntity?

    // If persisting order, you'd need methods to update orderIndex
} 