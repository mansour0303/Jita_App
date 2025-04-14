package com.example.jita.alarm

import android.app.ActivityOptions
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.example.jita.alarm.AlarmActivity

/**
 * BroadcastReceiver that handles scheduled alarm events.
 * This receiver is triggered when a reminder's scheduled time is reached.
 */
class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received alarm broadcast: ${intent.action}")
        
        // Get the reminder ID from the intent
        val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1)
        if (reminderId == -1) {
            Log.e(TAG, "Invalid reminder ID received")
            return
        }
        
        // IMPORTANT: Get a wake lock before doing any work
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or 
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "Jita:AlarmReceiverWakeLock"
        )
        wakeLock.acquire(10*60*1000L) // 10 minutes max
        
        // Start foreground service first - this is critical
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, AlarmService::class.java).apply {
                    putExtra(AlarmService.EXTRA_REMINDER_ID, reminderId)
                })
            } else {
                context.startService(Intent(context, AlarmService::class.java).apply {
                    putExtra(AlarmService.EXTRA_REMINDER_ID, reminderId)
                })
            }
            
            Log.d(TAG, "AlarmService started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AlarmService", e)
        }
        
        // Try multiple methods to start the alarm activity
        try {
            // Method 1: Start activity with dedicated action
            val alarmActivityIntent = Intent("com.example.jita.action.ALARM_TRIGGERED").apply {
                setPackage(context.packageName)
                setClassName(context.packageName, "com.example.jita.alarm.AlarmActivity")
                putExtra(AlarmActivity.EXTRA_REMINDER_ID, reminderId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                         Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                         Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            
            // Try both methods of starting the activity
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val options = ActivityOptions.makeBasic().apply {
                        setPendingIntentBackgroundActivityStartMode(
                            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        )
                    }
                    context.startActivity(alarmActivityIntent, options.toBundle())
                } else {
                    context.startActivity(alarmActivityIntent)
                }
                Log.d(TAG, "Successfully started alarm activity directly")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start activity directly, trying PendingIntent", e)
                
                // Method 2: Use pending intent
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    reminderId,
                    alarmActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                pendingIntent.send()
            }
        } catch (e: Exception) {
            Log.e(TAG, "All methods to start alarm activity failed", e)
        }
            
        // Make sure to release the wake lock
        if (wakeLock.isHeld) {
            wakeLock.release()
            Log.d(TAG, "WakeLock released")
        }
    }
} 