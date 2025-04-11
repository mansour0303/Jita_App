package com.example.jita.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: Int): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY updatedAt DESC")
    fun getNotesByFolder(folderId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    @Query("""
        SELECT n.* FROM notes n 
        INNER JOIN folders f ON n.folderId = f.id 
        WHERE f.parentId IS NULL 
        ORDER BY n.updatedAt DESC
    """)
    fun getNotesInRootFolders(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE folderId = :rootFolderId ORDER BY updatedAt DESC")
    fun getNotesInRootFolder(rootFolderId: Int): Flow<List<NoteEntity>>
} 