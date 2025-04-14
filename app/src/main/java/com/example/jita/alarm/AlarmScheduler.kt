package com.example.jita.alarm

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
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
        
        // First, cancel any existing alarm for this reminder
        try {
            cancelAlarm(reminder.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling existing alarm", e)
        }
        
        val pendingIntent = createPendingIntent(reminder.id)
        
        // Schedule the alarm with multiple approaches
        try {
            // Special case for Android 12+ where user permission is required for exact alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms. Using inexact timing.")
                // Try setAndAllowWhileIdle first
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.time.timeInMillis,
                    pendingIntent
                )
                return
            }
            
            // For Android 6.0+ try multiple methods in order of reliability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Method 1: Try to use AlarmClockInfo for high-priority alarms (most reliable)
                try {
                    Log.d(TAG, "Using setAlarmClock for high priority alarm")
                    val alarmClockInfo = AlarmClockInfo(
                        reminder.time.timeInMillis,
                        // Create a pending intent for our main activity
                        PendingIntent.getActivity(
                            context,
                            reminder.id * 10000,
                            context.packageManager.getLaunchIntentForPackage(context.packageName) 
                                ?: Intent(context, Class.forName("com.example.jita.MainActivity")),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    alarmManager.setAlarmClock(
                        alarmClockInfo,
                        pendingIntent
                    )
                    Log.d(TAG, "Alarm scheduled with AlarmClock API")
                    return
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting alarm with AlarmClock, trying alternative method", e)
                }
                
                // Method 2: Try setExactAndAllowWhileIdle
                try {
                    Log.d(TAG, "Using setExactAndAllowWhileIdle")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.time.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Alarm scheduled with setExactAndAllowWhileIdle")
                    return
                } catch (e: Exception) {
                    Log.e(TAG, "Error with setExactAndAllowWhileIdle, trying setExact", e)
                }
            }
            
            // Method 3: Fallback to setExact for all versions
            Log.d(TAG, "Using setExact as fallback")
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminder.time.timeInMillis,
                pendingIntent
            )
            Log.d(TAG, "Alarm scheduled with setExact")
        } catch (e: Exception) {
            // Last resort: use the basic set method which might not be exact
            Log.e(TAG, "All exact alarm methods failed, using basic set method", e)
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminder.time.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm scheduled with basic set method")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule alarm using any method", e)
            }
        }
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
            // Add flags to ensure the intent can be delivered even when app is closed
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            // Set action to make the intent explicit
            action = "com.example.jita.ALARM_TRIGGERED"
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