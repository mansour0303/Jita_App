package com.example.jita.data

object TimeLogMapper {
    fun toEntity(timeLog: TimeLogEntry): TimeLogEntryEntity {
        return TimeLogEntryEntity(
            id = timeLog.id,
            taskId = timeLog.taskId,
            type = timeLog.type.name,
            startTime = timeLog.startTime,
            endTime = timeLog.endTime,
            durationMillis = timeLog.durationMillis
        )
    }

    fun fromEntity(entity: TimeLogEntryEntity): TimeLogEntry {
        return TimeLogEntry(
            id = entity.id,
            taskId = entity.taskId,
            type = TimeLogEntry.LogType.valueOf(entity.type),
            startTime = entity.startTime,
            endTime = entity.endTime,
            durationMillis = entity.durationMillis
        )
    }

    fun fromEntities(entities: List<TimeLogEntryEntity>): List<TimeLogEntry> {
        return entities.map { fromEntity(it) }
    }
} 