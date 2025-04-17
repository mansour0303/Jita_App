package com.example.jita.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_logs",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TimeLogEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskId: Int,
    val type: String, // Stored as a string, will be enum name: "TASK_TIMER", "POMODORO_WORK", etc.
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long
) 