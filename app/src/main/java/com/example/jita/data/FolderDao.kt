package com.example.jita.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("SELECT * FROM folders WHERE id = :folderId")
    fun getFolderById(folderId: Int): Flow<FolderEntity?>

    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY name ASC")
    fun getRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentId = :parentId ORDER BY name ASC")
    fun getSubFolders(parentId: Int): Flow<List<FolderEntity>>
} 