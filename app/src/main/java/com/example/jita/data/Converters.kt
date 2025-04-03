package com.example.jita.data

import androidx.room.TypeConverter
import com.example.jita.TaskPriority
import java.util.Calendar

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return value?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }
    }

    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun fromPriority(value: String?): TaskPriority? {
        return value?.let { enumValueOf<TaskPriority>(it) }
    }

    @TypeConverter
    fun priorityToString(priority: TaskPriority?): String? {
        return priority?.name
    }
} 