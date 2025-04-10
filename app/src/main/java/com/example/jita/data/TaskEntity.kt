package com.example.jita.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
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
    val imagePath: String? = null,
    val filePath: String? = null
)