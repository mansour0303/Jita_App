package com.example.jita.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jita.Task
import com.example.jita.TaskPriority
import com.example.jita.data.ReminderDao
import com.example.jita.data.TaskDao
import com.example.jita.model.Reminder
import com.example.jita.model.toReminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

// Data class to hold a reminder with its associated tasks
data class ReminderWithTasks(
    val reminder: Reminder,
    val tasks: List<Task>
)

class AlarmViewModel(
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao,
    private val reminderId: Int
) : ViewModel() {

    private val _reminderWithTasks = MutableStateFlow<ReminderWithTasks?>(null)
    val reminderWithTasks: StateFlow<ReminderWithTasks?> = _reminderWithTasks

    init {
        loadReminderAndTasks()
    }

    private fun loadReminderAndTasks() {
        viewModelScope.launch {
            try {
                val reminderEntity = reminderDao.getReminderById(reminderId)
                if (reminderEntity != null) {
                    val reminder = reminderEntity.toReminder()
                    
                    // Load the actual tasks from TaskDao
                    val tasks = mutableListOf<Task>()
                    if (reminder.attachedTaskIds.isNotEmpty()) {
                        // Get all tasks from database
                        val allTasks = taskDao.getAllTasksAsList()
                        
                        // Filter the tasks that are attached to this reminder
                        for (taskId in reminder.attachedTaskIds) {
                            val task = allTasks.find { it.id == taskId }
                            if (task != null) {
                                // Convert TaskEntity to Task domain model
                                val calendar = Calendar.getInstance().apply {
                                    timeInMillis = task.dueDate
                                }
                                
                                tasks.add(
                                    Task(
                                        id = task.id,
                                        name = task.name,
                                        description = task.description,
                                        dueDate = calendar,
                                        priority = try {
                                            TaskPriority.valueOf(task.priority)
                                        } catch (e: Exception) {
                                            TaskPriority.MEDIUM
                                        },
                                        list = task.list,
                                        trackedTimeMillis = task.trackedTimeMillis,
                                        isTracking = task.isTracking,
                                        trackingStartTime = task.trackingStartTime,
                                        completed = task.completed,
                                        imagePaths = task.imagePaths,
                                        filePaths = task.filePaths,
                                        subtasks = task.subtasks,
                                        completedSubtasks = task.completedSubtasks
                                    )
                                )
                            }
                        }
                    }
                    
                    _reminderWithTasks.value = ReminderWithTasks(
                        reminder = reminder,
                        tasks = tasks
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Function to delete the reminder
    suspend fun deleteReminder() {
        try {
            reminderDao.deleteReminderById(reminderId)
        } catch (e: Exception) {
            // Handle error
        }
    }
}

class AlarmViewModelFactory(
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao,
    private val reminderId: Int
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            return AlarmViewModel(reminderDao, taskDao, reminderId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 