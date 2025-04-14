package com.example.jita

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.jita.data.ReminderDao
import com.example.jita.model.Reminder
import com.example.jita.model.toReminder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.jita.alarm.AlarmScheduler
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    navController: NavHostController,
    reminderDao: ReminderDao,
    tasks: List<Task> = emptyList()
) {
    // Collect reminders from the database
    val reminders = reminderDao.getAllReminders().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    
    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }
    
    // Search related states
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Date filter related states
    var showDateFilterDialog by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Calendar?>(null) }
    var endDate by remember { mutableStateOf<Calendar?>(null) }
    var isFilterActive by remember { mutableStateOf(false) }
    
    // State for date picker in filter dialog
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // Date formatter
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Function to check if a reminder date is within the filter range
    val isDateInRange: (Calendar) -> Boolean = { date ->
        val isAfterStart = startDate?.let { start -> !date.before(start) } ?: true
        val isBeforeEnd = endDate?.let { end -> !date.after(end) } ?: true
        isAfterStart && isBeforeEnd
    }
    
    // Filter reminders based on search query and date filter
    val filteredReminders = reminders.value
        .map { it.toReminder() }
        .filter { reminder ->
            // Search filter
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                reminder.name.contains(searchQuery, ignoreCase = true) ||
                reminder.message.contains(searchQuery, ignoreCase = true)
            }
            
            // Date filter
            val matchesDateFilter = if (!isFilterActive) {
                true
            } else {
                isDateInRange(reminder.time)
            }
            
            matchesSearch && matchesDateFilter
        }
    
    // Initialize alarm scheduler
    val context = LocalContext.current
    val alarmScheduler = remember { AlarmScheduler(context) }
    
    // Date filter dialog
    if (showDateFilterDialog) {
        AlertDialog(
            onDismissRequest = { showDateFilterDialog = false },
            title = { Text("Filter Reminders by Date") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // Start date selector
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = startDate?.let { dateFormatter.format(it.time) } ?: "Select Start Date"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // End date selector
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = endDate?.let { dateFormatter.format(it.time) } ?: "Select End Date"
                            )
                        }
                    }
                    
                    if (isFilterActive) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Clear filter button
                        OutlinedButton(
                            onClick = {
                                startDate = null
                                endDate = null
                                isFilterActive = false
                                showDateFilterDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Clear Filter")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Apply filter only if at least one date is selected
                        isFilterActive = startDate != null || endDate != null
                        showDateFilterDialog = false
                    }
                ) {
                    Text("Apply Filter")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDateFilterDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Start date picker dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate?.timeInMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = Calendar.getInstance().apply {
                                timeInMillis = millis
                                // Set to beginning of day
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // End date picker dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.timeInMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Calendar.getInstance().apply {
                                timeInMillis = millis
                                // Set to end of day
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    Scaffold(
        topBar = {
            if (isSearchActive) {
                // Search bar when search is active
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    placeholder = { Text("Search reminders...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Search"
                            )
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                // Regular top app bar when search is not active
                TopAppBar(
                    title = { 
                        Text(
                            text = "Reminders",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Filter Icon
                        IconButton(onClick = { showDateFilterDialog = true }) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter Reminders"
                                )
                                // Show indicator dot when filter is active
                                if (isFilterActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.tertiary,
                                                shape = CircleShape
                                            )
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                        
                        // Search Icon
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Reminders"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    navController.navigate(AppDestinations.REMINDER_SETTINGS_SCREEN) 
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reminder",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (filteredReminders.isEmpty()) {
            // Empty state or no search/filter results
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when {
                            searchQuery.isNotBlank() -> Icons.Default.SearchOff
                            isFilterActive -> Icons.Default.FilterList
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when {
                            searchQuery.isNotBlank() -> "No matching reminders"
                            isFilterActive -> "No reminders in selected date range"
                            else -> "No reminders yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            searchQuery.isNotBlank() -> "Try a different search term"
                            isFilterActive -> {
                                val dateRange = buildString {
                                    startDate?.let { append(dateFormatter.format(it.time)) }
                                    append(" - ")
                                    endDate?.let { append(dateFormatter.format(it.time)) }
                                }
                                "Current filter: $dateRange"
                            }
                            else -> "Tap the + button to create a reminder"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Show clear filter button when filter is active
                    if (isFilterActive) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                startDate = null
                                endDate = null
                                isFilterActive = false
                            }
                        ) {
                            Text("Clear Filter")
                        }
                    }
                }
            }
        } else {
            // List of reminders (filtered by search/date if applicable)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Show active filter indicator
                if (isFilterActive) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = buildString {
                                            append("Filtered: ")
                                            startDate?.let { append(dateFormatter.format(it.time)) } ?: append("Any")
                                            append(" - ")
                                            endDate?.let { append(dateFormatter.format(it.time)) } ?: append("Any")
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        startDate = null
                                        endDate = null
                                        isFilterActive = false
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Filter",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Reminder items
                items(filteredReminders) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onDeleteClick = {
                            reminderToDelete = reminder
                            showDeleteDialog = true
                        },
                        onEditClick = {
                            // Navigate to edit screen with the reminder ID
                            navController.navigate(AppDestinations.createReminderEditorRoute(reminder.id))
                        },
                        tasks = tasks
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog && reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                reminderToDelete = null
            },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete this reminder?") },
            confirmButton = {
                Button(
                    onClick = {
                        reminderToDelete?.let { reminder ->
                            coroutineScope.launch {
                                // Cancel the alarm
                                alarmScheduler.cancelAlarm(reminder.id)
                                
                                // Delete from database
                                reminderDao.deleteReminderById(reminder.id)
                            }
                        }
                        showDeleteDialog = false
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        reminderToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    tasks: List<Task> = emptyList()
) {
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    
    // State to track if the attached tasks section is expanded
    var isTasksSectionExpanded by remember { mutableStateOf(false) }
    
    // Filter to get only the tasks attached to this reminder
    val attachedTasks = tasks.filter { task -> reminder.attachedTaskIds.contains(task.id) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEditClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with name and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reminder.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Time and date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${timeFormatter.format(reminder.time.time)} - ${dateFormatter.format(reminder.time.time)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Message
            if (reminder.message.isNotBlank()) {
                Text(
                    text = reminder.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Collapsible Attached Tasks section
            if (attachedTasks.isNotEmpty()) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Clickable header for expanding/collapsing
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { isTasksSectionExpanded = !isTasksSectionExpanded })
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Attached Tasks (${attachedTasks.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Icon(
                        imageVector = if (isTasksSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isTasksSectionExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Expandable content
                if (isTasksSectionExpanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 26.dp) // Align with the header text
                    ) {
                        attachedTasks.forEach { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 0.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    // Task name and priority
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Priority indicator
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = when (task.priority) {
                                                        TaskPriority.HIGH -> Color.Red
                                                        TaskPriority.MEDIUM -> Color(0xFFFFA500) // Orange
                                                        TaskPriority.LOW -> Color.Green
                                                        else -> Color.Blue
                                                    },
                                                    shape = CircleShape
                                                )
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = task.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    
                                    // Date and list name (if available)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Due date
                                        val dueDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = dueDateFormat.format(task.dueDate.time),
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
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Description (if available)
                                    if (task.description.isNotEmpty()) {
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Icons for sound and vibration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (reminder.alarmSoundEnabled) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Sound enabled",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                if (reminder.vibrationEnabled) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Vibration enabled",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
} 