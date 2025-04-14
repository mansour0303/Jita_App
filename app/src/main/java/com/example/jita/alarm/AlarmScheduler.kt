package com.example.jita.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.jita.model.Reminder
import java.util.*

/**
 * Utility class for scheduling and managing alarms for reminders.
 */
class AlarmScheduler(private val context: Context) {
    
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    /**
     * Schedule an alarm for the given reminder.
     * @param reminder The reminder to schedule an alarm for
     */
    fun scheduleAlarm(reminder: Reminder) {
        Log.d(TAG, "Scheduling alarm for reminder: ${reminder.id} at ${reminder.time.time}")
        
        val pendingIntent = createPendingIntent(reminder.id)
        
        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // For Android 12+ where exact alarm permission is required
            Log.w(TAG, "Cannot schedule exact alarms. Using inexact timing.")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.time.timeInMillis,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android 6.0+ use setExactAndAllowWhileIdle
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.time.timeInMillis,
                pendingIntent
            )
        } else {
            // For earlier versions
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminder.time.timeInMillis,
                pendingIntent
            )
        }
        
        Log.d(TAG, "Alarm scheduled successfully for reminder: ${reminder.id}")
    }

    /**
     * Cancel the alarm for the given reminder ID.
     * @param reminderId The ID of the reminder to cancel
     */
    fun cancelAlarm(reminderId: Int) {
        Log.d(TAG, "Cancelling alarm for reminder: $reminderId")
        
        val pendingIntent = createPendingIntent(reminderId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        
        Log.d(TAG, "Alarm cancelled successfully for reminder: $reminderId")
    }

    /**
     * Reschedules an alarm for the updated reminder.
     * This first cancels any existing alarm and then schedules a new one.
     * @param reminder The updated reminder
     */
    fun updateAlarm(reminder: Reminder) {
        cancelAlarm(reminder.id)
        scheduleAlarm(reminder)
    }

    /**
     * Create a PendingIntent for the AlarmReceiver.
     * @param reminderId The ID of the reminder
     * @return A PendingIntent for the AlarmReceiver
     */
    private fun createPendingIntent(reminderId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        
        // Use unique request code based on reminder ID to avoid conflicts
        return PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or getPendingIntentMutabilityFlag()
        )
    }

    /**
     * Get the appropriate PendingIntent mutability flag based on Android version.
     */
    private fun getPendingIntentMutabilityFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    companion object {
        private const val TAG = "AlarmScheduler"
    }
} 