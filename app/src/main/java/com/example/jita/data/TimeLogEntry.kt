package com.example.jita.data

data class TimeLogEntry(
    val id: Int = 0,
    val taskId: Int,
    val type: LogType,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long
) {
    enum class LogType {
        TASK_TIMER,       // Regular task timer
        POMODORO_WORK,    // Pomodoro work session
        POMODORO_SHORT_BREAK, // Pomodoro short break
        POMODORO_LONG_BREAK   // Pomodoro long break
    }
} 