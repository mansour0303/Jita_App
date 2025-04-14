package com.example.jita.model

import android.net.Uri
import com.example.jita.data.ReminderEntity
import java.util.*

/**
 * Convert a Reminder to ReminderEntity for storage
 */
fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = this.id,
        name = this.name,
        message = this.message,
        timeInMillis = this.time.timeInMillis,
        alarmSoundEnabled = this.alarmSoundEnabled,
        vibrationEnabled = this.vibrationEnabled,
        soundUri = this.soundUri?.toString(),
        soundName = this.soundName,
        attachedTaskIds = this.attachedTaskIds.joinToString(",")
    )
}

/**
 * Convert a ReminderEntity to Reminder for UI
 */
fun ReminderEntity.toReminder(): Reminder {
    return Reminder(
        id = this.id,
        name = this.name,
        message = this.message,
        time = Calendar.getInstance().apply { timeInMillis = this@toReminder.timeInMillis },
        alarmSoundEnabled = this.alarmSoundEnabled,
        vibrationEnabled = this.vibrationEnabled,
        soundUri = this.soundUri?.let { Uri.parse(it) },
        soundName = this.soundName,
        attachedTaskIds = if (this.attachedTaskIds.isBlank()) emptyList() else this.attachedTaskIds.split(",").map { it.toInt() }
    )
} 