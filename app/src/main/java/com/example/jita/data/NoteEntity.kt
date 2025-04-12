package com.example.jita.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val folderId: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val color: String? = null,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val styles: String? = null,
    val checkboxItems: String? = null,
    val voiceRecordings: String? = null,
    val fileAttachments: String? = null,  // Added new field for file attachments
    val imageAttachments: String? = null  // Added new field for image attachments
) 