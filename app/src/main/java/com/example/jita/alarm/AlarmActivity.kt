package com.example.jita.alarm

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.jita.MainActivity
import com.example.jita.data.AppDatabase
import com.example.jita.model.toReminder
import kotlinx.coroutines.launch

class AlarmActivity : ComponentActivity() {
    
    private var alarmService: AlarmService? = null
    private var bound = false

    // Service connection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AlarmService.LocalBinder
            alarmService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            alarmService = null
            bound = false
        }
    }

    private val viewModel: AlarmViewModel by viewModels {
        AlarmViewModelFactory(
            AppDatabase.getDatabase(applicationContext).reminderDao(),
            intent.getIntExtra(EXTRA_REMINDER_ID, -1)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set window flags to show over lock screen
        setupWindowFlags()

        // Bind to the alarm service
        bindAlarmService()

        setContent {
            val reminderState by viewModel.reminderWithTasks.collectAsState(null)
            
            reminderState?.let { reminderWithTasks ->
                AlarmNotificationScreen(
                    reminder = reminderWithTasks.reminder,
                    attachedTasks = reminderWithTasks.tasks,
                    onDeleteClick = {
                        lifecycleScope.launch {
                            // Delete reminder from database
                            viewModel.deleteReminder()
                            
                            // Stop alarm service
                            alarmService?.stopAlarm()
                            
                            // Close this activity
                            navigateToMainAndFinish()
                        }
                    },
                    onDismissClick = {
                        // Stop alarm but keep reminder
                        alarmService?.stopAlarm()
                        navigateToMainAndFinish()
                    }
                )
            }
        }
    }

    private fun setupWindowFlags() {
        // Ensure the screen turns on and bypasses keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            
            // Ensure it appears above keyguard
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            // For older versions
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }

    private fun bindAlarmService() {
        val intent = Intent(this, AlarmService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun navigateToMainAndFinish() {
        // Navigate to the main activity
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unbind from service
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
} 