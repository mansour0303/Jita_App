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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    navController: NavHostController,
    tasks: List<Task>
) {
    // State for the reminder settings
    var selectedHour by remember { mutableStateOf(6) }
    var selectedMinute by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().apply { 
        add(Calendar.DAY_OF_YEAR, 1) // Default to tomorrow
    }) }
    var reminderName by remember { mutableStateOf("") }
    var reminderMessage by remember { mutableStateOf("") }
    var alarmSoundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    
    // State for selected alarm sound
    var selectedSoundUri by remember { mutableStateOf<Uri?>(null) }
    var selectedSoundName by remember { mutableStateOf("Default Alarm") }
    
    // Media player for sound preview
    val context = LocalContext.current
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
    var attachedTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var showAttachTasksDialog by remember { mutableStateOf(false) }
    var taskDialogDate by remember { mutableStateOf(Calendar.getInstance()) }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var showTaskDatePicker by remember { mutableStateOf(false) }
    
    // Date formatter
    val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    
    // Date picker dialog for reminder date
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.timeInMillis
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
            initialSelectedDateMillis = taskDialogDate.timeInMillis
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Set Reminder",
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
                // Time picker wheels
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(200.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    // Hour wheel
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        WheelPicker(
                            items = (0..23).map { String.format("%02d", it) },
                            initialIndex = selectedHour,
                            onSelectionChanged = { selectedHour = it }
                        )
                    }
                    
                    // Colon
                    Text(
                        text = ":",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Minute wheel
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        WheelPicker(
                            items = (0..59).map { String.format("%02d", it) },
                            initialIndex = selectedMinute,
                            onSelectionChanged = { selectedMinute = it }
                        )
                    }
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
                        // Save reminder logic would go here
                        // We would save the selectedSoundUri and vibrationEnabled settings
                        navController.navigateUp() 
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
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

@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int = 0,
    onSelectionChanged: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val coroutineScope = rememberCoroutineScope()
    
    // Total item height
    val itemHeight = 40.dp
    
    // The number of visible items
    val visibleItems = 5
    
    // Track the currently centered item
    val currentItem = remember { mutableStateOf(initialIndex) }
    
    // Create snapping behavior
    val snapBehavior = rememberSnapFlingBehavior(
        lazyListState = listState
    )
    
    // Keep track of current selection
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
        if (visibleItemsInfo.isNotEmpty()) {
            val centerPos = listState.layoutInfo.viewportEndOffset / 2
            val centerItem = visibleItemsInfo.minByOrNull { 
                kotlin.math.abs((it.offset + it.size / 2) - centerPos) 
            }
            centerItem?.let {
                val selectedIndex = it.index - 1 // Adjust for the top padding item
                if (selectedIndex >= 0 && selectedIndex < items.size && currentItem.value != selectedIndex) {
                    currentItem.value = selectedIndex
                    onSelectionChanged(selectedIndex)
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .height(itemHeight * visibleItems)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Center selector highlight
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )
        
        // Wheel items
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = snapBehavior
        ) {
            // Add padding at the beginning so center item can be at the top
            item { Spacer(modifier = Modifier.height(itemHeight * (visibleItems / 2))) }
            
            items(items.size) { index ->
                // Calculate how far this item is from the center
                val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
                val layoutInfo = listState.layoutInfo
                
                val centerPos = layoutInfo.viewportEndOffset / 2
                val itemInfo = visibleItemsInfo.find { it.index == index + 1 } // Adjust for top padding item
                
                // Calculate alpha based on distance from center
                val alpha = if (itemInfo != null) {
                    val itemCenter = itemInfo.offset + itemInfo.size / 2
                    val distanceFromCenter = kotlin.math.abs(itemCenter - centerPos)
                    val maxDistance = layoutInfo.viewportEndOffset / 2
                    
                    // Scale from 1.0 (at center) to 0.3 (at edges)
                    (1.0f - (distanceFromCenter.toFloat() / maxDistance) * 0.7f).coerceIn(0.3f, 1.0f)
                } else {
                    0.3f
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        fontSize = 20.sp,
                        fontWeight = if (index == currentItem.value) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Add padding at the end so center item can be at the bottom
            item { Spacer(modifier = Modifier.height(itemHeight * (visibleItems / 2))) }
        }
    }
} 