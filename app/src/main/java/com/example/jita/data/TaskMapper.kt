package com.example.jita.data

import com.example.jita.Task
import com.example.jita.TaskPriority
import java.util.Calendar

/**
 * Converts a TaskEntity from the database to a Task domain model
 */
fun TaskEntity.toTask(): Task {
    val dueDateCalendar = Calendar.getInstance().apply { timeInMillis = dueDate }
    return Task(
        id = id,
        name = name,
        description = description,
        dueDate = dueDateCalendar,
        priority = try {
            TaskPriority.valueOf(priority)
        } catch (e: IllegalArgumentException) {
            TaskPriority.MEDIUM // Default fallback
        },
        list = list,
        trackedTimeMillis = trackedTimeMillis,
        isTracking = isTracking,
        trackingStartTime = trackingStartTime,
        completed = completed,
        imagePaths = imagePaths,
        filePaths = filePaths,
        subtasks = subtasks
    )
}

/**
 * Converts a Task domain model to a TaskEntity for database storage
 */
fun Task.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        name = name,
        description = description,
        dueDate = dueDate.timeInMillis,
        priority = priority.name,
        list = list,
        trackedTimeMillis = trackedTimeMillis,
        isTracking = isTracking,
        trackingStartTime = trackingStartTime,
        completed = completed,
        imagePaths = imagePaths,
        filePaths = filePaths,
        subtasks = subtasks
    )
} 