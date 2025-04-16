package com.example.jita.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "tasks")
@TypeConverters(StringListConverter::class)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val dueDate: Long,
    val priority: String,
    val list: String?,
    val trackedTimeMillis: Long = 0,
    val isTracking: Boolean = false,
    val trackingStartTime: Long = 0,
    val completed: Boolean = false,
    val imagePaths: List<String> = emptyList(),
    val filePaths: List<String> = emptyList(),
    val subtasks: List<String> = emptyList()
)