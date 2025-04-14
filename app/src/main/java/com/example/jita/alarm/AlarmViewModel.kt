package com.example.jita.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jita.Task
import com.example.jita.TaskPriority
import com.example.jita.data.ReminderDao
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
                    
                    // TODO: In a real implementation, get the tasks from TaskDao
                    // For now, we'll create empty placeholder tasks with the IDs
                    val placeholderTasks = reminder.attachedTaskIds.map { taskId ->
                        Task(
                            id = taskId,
                            name = "Task #$taskId",
                            description = "This is a placeholder task",
                            dueDate = Calendar.getInstance(),
                            completed = false,
                            priority = TaskPriority.MEDIUM,
                            list = null
                        )
                    }
                    
                    _reminderWithTasks.value = ReminderWithTasks(
                        reminder = reminder,
                        tasks = placeholderTasks
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
    private val reminderId: Int
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            return AlarmViewModel(reminderDao, reminderId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 