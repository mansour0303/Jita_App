package com.example.jita.model

import android.net.Uri
import java.util.*

/**
 * Data class representing a reminder
 */
data class Reminder(
    val id: Int = 0,
    val name: String,
    val message: String,
    val time: Calendar,
    val alarmSoundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val soundUri: Uri? = null,
    val soundName: String? = null,
    val attachedTaskIds: List<Int> = emptyList()
) 