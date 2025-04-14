package com.example.jita.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log

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
        Log.d(TAG, "Received alarm broadcast")
        
        // Get the reminder ID from the intent
        val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1)
        if (reminderId == -1) {
            Log.e(TAG, "Invalid reminder ID received")
            return
        }
        
        // Wake up the device
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or 
            PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Jita:AlarmReceiverWakeLock"
        )
        wakeLock.acquire(10*60*1000L) // 10 minutes max
        
        try {
            // Start the alarm service
            AlarmService.startAlarm(context, reminderId)
        } finally {
            // Release the wake lock
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
} 