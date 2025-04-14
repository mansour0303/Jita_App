package com.example.jita.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.jita.data.AppDatabase
import com.example.jita.model.toReminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * BroadcastReceiver that is triggered when the device completes boot.
 * This is used to restore all scheduled alarms after device reboot.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed, restoring alarms")
            
            // Use a coroutine to access the database
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                // Get all reminders from the database
                val reminderDao = AppDatabase.getDatabase(context).reminderDao()
                val reminderEntities = reminderDao.getAllRemindersAsList()
                
                // Initialize the alarm scheduler
                val alarmScheduler = AlarmScheduler(context)
                
                // Current time for filtering out passed reminders
                val currentTime = Calendar.getInstance().timeInMillis
                
                // Schedule alarms for all future reminders
                for (entity in reminderEntities) {
                    val reminder = entity.toReminder()
                    
                    // Only schedule future alarms
                    if (reminder.time.timeInMillis > currentTime) {
                        Log.d(TAG, "Restoring alarm for reminder: ${reminder.id}")
                        alarmScheduler.scheduleAlarm(reminder)
                    } else {
                        Log.d(TAG, "Skipping passed reminder: ${reminder.id}")
                    }
                }
            }
        }
    }
} 