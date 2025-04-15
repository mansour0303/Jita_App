package com.example.jita

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.jita.data.ReminderDao
import com.example.jita.data.ReminderEntity
import com.example.jita.model.Reminder
import com.example.jita.model.toEntity
import com.example.jita.model.toReminder
import kotlinx.coroutines.launch
import com.example.jita.alarm.AlarmScheduler
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    navController: NavHostController,
    tasks: List<Task>,
    reminderDao: ReminderDao,
    reminderId: Int = -1
) {
    // Coroutine scope for database operations
    val coroutineScope = rememberCoroutineScope()
    
    // Track if screen is in edit mode
    val isEditMode = reminderId > 0
    
    // State to store the existing reminder if in edit mode
    val existingReminder = remember { mutableStateOf<Reminder?>(null) }
    
    // Initialize the alarm scheduler
    val context = LocalContext.current
    val alarmScheduler = remember { AlarmScheduler(context) }
    
    // Load existing reminder if in edit mode
    LaunchedEffect(reminderId) {
        if (isEditMode) {
            reminderDao.getReminderById(reminderId)?.let { entity ->
                existingReminder.value = entity.toReminder()
            }
        }
    }
    
    // State for the reminder settings - initialize with existing values if in edit mode
    var selectedHour by remember { mutableStateOf(
        if (isEditMode && existingReminder.value != null)
            existingReminder.value!!.time.get(Calendar.HOUR_OF_DAY)
        else 6
    ) }
    
    var selectedMinute by remember { mutableStateOf(
        if (isEditMode && existingReminder.value != null)
            existingReminder.value!!.time.get(Calendar.MINUTE)
        else 0
    ) }
    
    var selectedDate by remember { mutableStateOf(
        if (isEditMode && existingReminder.value != null)
            existingReminder.value!!.time
        else Calendar.getInstance().apply { 
            add(Calendar.DAY_OF_YEAR, 1) // Default to tomorrow
        }
    ) }
    
    var reminderName by remember { mutableStateOf(
        if (isEditMode && existingReminder.value != null)
            existingReminder.value!!.name
        else ""
    ) }
    
    var reminderMessage by remember { mutableStateOf(
        if (isEditMode && existingReminder.value != null)
            existingReminder.value!!.message
        else ""
    ) }
    
    var alarmSoundEnabled by remember { mutableStateOf(
        if (isEditMode && existingReminder.value != null)
            existingReminder.value!!.alarmSoundEnabled
        else true
    ) }
    
    var vibrationEnabled by remember { mutableStateOf(
        if (isEditMode && existingReminder.value != null)
            existingReminder.value!!.vibrationEnabled
        else true
    ) }
    
    // State for selected alarm sound
    var selectedSoundUri by remember { mutableStateOf<Uri?>(
        if (isEditMode && existingReminder.value != null)
            existingReminder.value!!.soundUri
        else null
    ) }
    
    var selectedSoundName by remember { mutableStateOf(
        if (isEditMode && existingReminder.value != null && existingReminder.value!!.soundName != null)
            existingReminder.value!!.soundName!!
        else "Default Alarm"
    ) }
    
    // State for attached tasks - initialize with existing tasks if in edit mode
    var attachedTasks by remember { mutableStateOf<List<Task>>(
        if (isEditMode && existingReminder.value != null)
            tasks.filter { existingReminder.value!!.attachedTaskIds.contains(it.id) }
        else emptyList()
    ) }
    
    // Update states when existing reminder is loaded
    LaunchedEffect(existingReminder.value) {
        existingReminder.value?.let { reminder ->
            selectedHour = reminder.time.get(Calendar.HOUR_OF_DAY)
            selectedMinute = reminder.time.get(Calendar.MINUTE)
            selectedDate = reminder.time
            reminderName = reminder.name
            reminderMessage = reminder.message
            alarmSoundEnabled = reminder.alarmSoundEnabled
            vibrationEnabled = reminder.vibrationEnabled
            selectedSoundUri = reminder.soundUri
            selectedSoundName = reminder.soundName ?: "Default Alarm"
            attachedTasks = tasks.filter { reminder.attachedTaskIds.contains(it.id) }
        }
    }
    
    // Media player for sound preview
    val mediaPlayer = remember { MediaPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    
    // For vibration testing
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    // Clean up media player when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }
    
    // Function to test vibration
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun testVibration() {
        if (vibrationEnabled) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
    
    // Function to play/stop sound preview
    fun toggleSoundPreview() {
        if (isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            isPlaying = false
        } else if (alarmSoundEnabled) {
            try {
                mediaPlayer.reset()
                if (selectedSoundUri != null) {
                    mediaPlayer.setDataSource(context, selectedSoundUri!!)
                } else {
                    // Use default system alarm sound or a bundled sound
                    val defaultUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                    mediaPlayer.setDataSource(context, defaultUri)
                }
                mediaPlayer.prepare()
                mediaPlayer.isLooping = true
                mediaPlayer.start()
                isPlaying = true
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // File picker for audio files
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedSoundUri = it
            // Get file name
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        selectedSoundName = cursor.getString(nameIndex)
                    }
                }
            }
        }
    }
    
    // State for attached tasks
    var showAttachTasksDialog by remember { mutableStateOf(false) }
    var taskDialogDate by remember { mutableStateOf(Calendar.getInstance()) }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var showTaskDatePicker by remember { mutableStateOf(false) }
    
    // Date formatter
    val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    
    // Current date in millis for validation
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    // Initialize the alarm scheduler
    // Date picker dialog for reminder date
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.timeInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= today
                }
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate.timeInMillis = millis
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Date picker dialog for task selection
    if (showTaskDatePicker) {
        val taskDatePickerState = rememberDatePickerState(
            initialSelectedDateMillis = taskDialogDate.timeInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= today
                }
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showTaskDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    taskDatePickerState.selectedDateMillis?.let { millis ->
                        taskDialogDate.timeInMillis = millis
                    }
                    showTaskDatePicker = false
                    // After selecting date, show the task selection dialog
                    showAttachTasksDialog = true
                }) {
                    Text("Show Tasks")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTaskDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = taskDatePickerState)
        }
    }
    
    // Function to check if two dates are the same day
    val isSameDay = { date1: Calendar, date2: Calendar ->
        date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
        date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
        date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
    }
    
    // Tasks selection dialog
    if (showAttachTasksDialog) {
        val tasksForSelectedDate = tasks.filter { task -> 
            isSameDay(task.dueDate, taskDialogDate)
        }
        
        // State for temporary selection in the dialog
        val tempSelectedTasks = remember { 
            mutableStateListOf<Task>().apply { addAll(attachedTasks) }
        }
        
        Dialog(
            onDismissRequest = { showAttachTasksDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Dialog title
                    Text(
                        text = "Select Tasks for ${dateFormatter.format(taskDialogDate.time)}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (tasksForSelectedDate.isEmpty()) {
                        // No tasks message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks found for this date",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Task list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(tasksForSelectedDate) { task ->
                                val isSelected = tempSelectedTasks.contains(task)
                                TaskSelectionItem(
                                    task = task,
                                    isSelected = isSelected,
                                    onToggleSelection = {
                                        if (isSelected) {
                                            tempSelectedTasks.remove(task)
                                        } else {
                                            tempSelectedTasks.add(task)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    // Buttons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showAttachTasksDialog = false }
                        ) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                attachedTasks = tempSelectedTasks.toList()
                                showAttachTasksDialog = false
                            }
                        ) {
                            Text("Attach")
                        }
                    }
                }
            }
        }
    }
    
    // State for the time picker
    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute,
        is24Hour = true
    )

    // Update selectedHour and selectedMinute when time changes
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        selectedHour = timePickerState.hour
        selectedMinute = timePickerState.minute
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEditMode) "Edit Reminder" else "New Reminder",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time picker section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Material 3 Time Input
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    TimeInput(
                        state = timePickerState,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            
            // Date picker button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = dateFormatter.format(selectedDate.time),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Reminder name
            OutlinedTextField(
                value = reminderName,
                onValueChange = { reminderName = it },
                label = { Text("Alarm name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            
            // Reminder message
            OutlinedTextField(
                value = reminderMessage,
                onValueChange = { reminderMessage = it },
                label = { Text("Alarm message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = false,
                maxLines = 3
            )
            
            // Attach tasks section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { showTaskDatePicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach Tasks",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Attach Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Show attached tasks
                    if (attachedTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            attachedTasks.forEachIndexed { index, task ->
                                // Task item row
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    // Task header row with name and remove button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Priority indicator and name
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = when (task.priority) {
                                                        TaskPriority.HIGH -> Color.Red
                                                        TaskPriority.MEDIUM -> Color(0xFFFFA500) // Orange
                                                        TaskPriority.LOW -> Color.Green
                                                        else -> Color.Blue // Handle any other priority values
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = task.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        IconButton(
                                            onClick = {
                                                attachedTasks = attachedTasks.filter { it.id != task.id }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Task",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    
                                    // Task details
                                    if (task.description.isNotBlank()) {
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(start = 20.dp, top = 4.dp)
                                        )
                                    }
                                    
                                    // Due date and list
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 20.dp, top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Date
                                        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = dateFormat.format(task.dueDate.time),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(16.dp))
                                        
                                        // List name (if available)
                                        if (!task.list.isNullOrBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.List,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = task.list!!,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Add divider between tasks (except after the last one)
                                if (index < attachedTasks.size - 1) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No tasks attached",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Alarm sound (now clickable to select a sound file)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (alarmSoundEnabled) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alarm sound",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = alarmSoundEnabled,
                            onCheckedChange = { 
                                alarmSoundEnabled = it 
                                if (!it && isPlaying) {
                                    // Stop playback if disabling sound
                                    toggleSoundPreview()
                                }
                            }
                        )
                    }
                    
                    if (alarmSoundEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Sound selection row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Launch audio file picker
                                    audioPickerLauncher.launch("audio/*")
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = selectedSoundName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = { toggleSoundPreview() }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Stop Preview" else "Play Preview",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            // Vibration (with a test button)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (vibrationEnabled) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vibration",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it }
                        )
                    }
                    
                    if (vibrationEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { testVibration() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Test Vibration")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Cancel and Save buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancel")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = {
                        if (selectedDate.timeInMillis < today) {
                            // Show date error
                            Toast.makeText(
                                context,
                                "Please select a future date",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Create reminder object
                            val reminderToSave = Reminder(
                                id = if (isEditMode) reminderId else 0, // Keep existing ID if editing
                                name = reminderName.ifBlank { "Reminder" },
                                message = reminderMessage,
                                time = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, selectedHour)
                                    set(Calendar.MINUTE, selectedMinute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                    // Set the date components
                                    set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                                    set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                                    set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                                },
                                alarmSoundEnabled = alarmSoundEnabled,
                                vibrationEnabled = vibrationEnabled,
                                soundUri = selectedSoundUri,
                                soundName = selectedSoundName,
                                attachedTaskIds = attachedTasks.map { it.id }
                            )
                            
                            // Save to database and schedule the alarm
                            coroutineScope.launch {
                                val savedId = reminderDao.insertReminder(reminderToSave.toEntity())
                                
                                // Schedule the alarm
                                val finalReminder = if (isEditMode) {
                                    reminderToSave
                                } else {
                                    // Use the generated ID for new reminders
                                    reminderToSave.copy(id = savedId.toInt())
                                }
                                
                                // Schedule or update the alarm
                                if (isEditMode) {
                                    alarmScheduler.updateAlarm(finalReminder)
                                } else {
                                    alarmScheduler.scheduleAlarm(finalReminder)
                                }
                                
                                navController.navigateUp()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEditMode) "Update" else "Save")
                }
            }
        }
    }
}

@Composable
fun TaskSelectionItem(
    task: Task,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .toggleable(
                value = isSelected,
                onValueChange = { onToggleSelection() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = when (task.priority) {
                            TaskPriority.HIGH -> Color.Red
                            TaskPriority.MEDIUM -> Color(0xFFFFA500) // Orange
                            TaskPriority.LOW -> Color.Green
                            else -> Color.Blue // Handle any other priority values
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Task details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() }
            )
        }
    }
} 