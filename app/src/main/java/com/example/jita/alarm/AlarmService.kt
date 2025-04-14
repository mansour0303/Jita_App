package com.example.jita.alarm

import android.app.ActivityOptions
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.jita.MainActivity
import com.example.jita.R
import com.example.jita.Task
import com.example.jita.data.AppDatabase
import com.example.jita.model.Reminder
import com.example.jita.model.toReminder
import kotlinx.coroutines.*
import java.io.IOException

class AlarmService : Service() {
    private val TAG = "AlarmService"
    private val CHANNEL_ID = "alarm_notification_channel"
    private val NOTIFICATION_ID = 1001
    private val WAKELOCK_TIMEOUT = 5 * 60 * 1000L // 5 minutes timeout

    private lateinit var wakeLock: PowerManager.WakeLock
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isPlaying = false
    private var coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Define vibration pattern: 0 delay, 500ms vibrate, 500ms sleep, repeat
    private val vibrationPattern = longArrayOf(0, 500, 500)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Initialize vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        // Initialize wake lock to keep screen on
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or 
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "Jita:AlarmWakeLock"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Alarm service started with flag: $flags and startId: $startId")

        // Get reminder ID from intent
        val reminderId = intent?.getIntExtra(EXTRA_REMINDER_ID, -1) ?: -1
        if (reminderId == -1) {
            Log.e(TAG, "Invalid reminder ID")
            stopSelf()
            return Service.START_NOT_STICKY
        }

        // Acquire wake lock with longer timeout
        if (!wakeLock.isHeld) {
            wakeLock.acquire(10 * 60 * 1000L) // 10 minutes timeout
            Log.d(TAG, "WakeLock acquired")
        }

        // Show a persistent notification immediately - this is crucial for foreground service
        val notification = createForegroundNotification(reminderId)
        startForeground(NOTIFICATION_ID, notification.build())

        // Load reminder and start alarm
        coroutineScope.launch {
            try {
                val reminderDao = AppDatabase.getDatabase(applicationContext).reminderDao()
                val reminderEntity = reminderDao.getReminderById(reminderId)
                
                if (reminderEntity == null) {
                    Log.e(TAG, "Reminder not found: $reminderId")
                    stopSelf()
                    return@launch
                }
                
                val reminder = reminderEntity.toReminder()
                val tasks = loadAttachedTasks(reminder)
                
                withContext(Dispatchers.Main) {
                    // Start the alarm sound and vibration
                    startAlarm(reminder)
                    
                    // Launch AlarmActivity directly, in addition to the notification
                    launchAlarmActivity(reminderId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting alarm", e)
                stopSelf()
            }
        }

        // Return START_REDELIVER_INTENT to ensure the service restarts if killed
        return Service.START_REDELIVER_INTENT
    }

    private suspend fun loadAttachedTasks(reminder: Reminder): List<Task> {
        // Load tasks from TaskDao here...
        // This is a placeholder - you'll need to implement the actual task loading logic
        return emptyList()
    }

    private fun startAlarm(reminder: Reminder) {
        // Start playing sound if enabled
        if (reminder.alarmSoundEnabled) {
            playAlarmSound(reminder.soundUri)
        }
        
        // Start vibration if enabled
        if (reminder.vibrationEnabled) {
            startVibration()
        }
        
        isPlaying = true
    }

    private fun playAlarmSound(soundUri: Uri?) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                
                // Use either provided sound URI or default alarm sound
                if (soundUri != null) {
                    setDataSource(applicationContext, soundUri)
                } else {
                    val defaultUri = Uri.parse("android.resource://${packageName}/raw/default_alarm")
                    setDataSource(applicationContext, defaultUri)
                }
                
                isLooping = true
                prepare()
                start()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error playing alarm sound", e)
            // Fallback to system default sound
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Error playing fallback alarm sound", e2)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up media player", e)
        }
    }

    private fun startVibration() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        vibrationPattern, 
                        0 // Repeat indefinitely, starting from index 0
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(vibrationPattern, 0) // Repeat indefinitely
            }
        }
    }

    fun stopAlarm() {
        // Stop media player
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        
        // Stop vibration
        vibrator?.cancel()
        
        isPlaying = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Notifications"
            val descriptionText = "Notifications for alarm alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun launchAlarmActivity(reminderId: Int) {
        val alarmActivityIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                     Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val options = ActivityOptions.makeBasic().apply {
                setPendingIntentBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
            }
            startActivity(alarmActivityIntent, options.toBundle())
        } else {
            startActivity(alarmActivityIntent)
        }
    }

    private fun createForegroundNotification(reminderId: Int): NotificationCompat.Builder {
        // Create a full-screen intent that will show the alarm activity even when the screen is locked
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or
                     Intent.FLAG_ACTIVITY_SINGLE_TOP or
                     Intent.FLAG_FROM_BACKGROUND)
        }
        
        // Create ActivityOptions to explicitly allow background activity starts
        val activityOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 (API 34)
            ActivityOptions.makeBasic().apply {
                setPendingIntentBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
                // Also set the creator mode for Android 15+
                if (Build.VERSION.SDK_INT >= 35) { // Android 15 is expected to be API 35
                    try {
                        val method = ActivityOptions::class.java.getMethod(
                            "setPendingIntentCreatorBackgroundActivityStartMode", 
                            Int::class.java
                        )
                        method.invoke(this, ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to set creator mode", e)
                    }
                }
            }.toBundle()
        } else null
        
        // Create PendingIntent with the ActivityOptions
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fullScreenPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent.getActivity(
                this,
                reminderId * 100, // Use a different request code from the regular PendingIntent
                fullScreenIntent,
                flags,
                activityOptions
            )
        } else {
            PendingIntent.getActivity(
                this,
                reminderId * 100,
                fullScreenIntent,
                flags
            )
        }
        
        // Create regular content intent (for notification tap)
        val contentIntent = PendingIntent.getActivity(
            this,
            reminderId,
            fullScreenIntent.clone() as Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Alarm")
            .setContentText("Alarm is playing. Tap to view.")
            .setPriority(NotificationCompat.PRIORITY_MAX) // Use MAX priority
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true) // This is crucial
            .setContentIntent(contentIntent)
            .setAutoCancel(false)
            .setSound(null) // We'll play sounds ourselves
            .setVibrate(null) // We'll handle vibration ourselves
    }

    override fun onBind(intent: Intent?): IBinder? {
        return LocalBinder()
    }

    inner class LocalBinder : Binder() {
        fun getService(): AlarmService = this@AlarmService
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        
        // Release wake lock if held
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        
        // Cancel coroutines
        coroutineScope.cancel()
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"

        fun startAlarm(context: Context, reminderId: Int) {
            val intent = Intent(context, AlarmService::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminderId)
            }
            
            // Start service on newer Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopAlarm(context: Context) {
            context.stopService(Intent(context, AlarmService::class.java))
        }
    }
}