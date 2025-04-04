package com.example.jita

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.util.Locale

// Helper function to check if two Calendars represent the same day
fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

// Helper function to check if a Calendar is within the current week (Mon-Sun)
fun isThisWeek(cal: Calendar): Boolean {
    val today = Calendar.getInstance()
    val currentWeek = today.get(Calendar.WEEK_OF_YEAR)
    val currentYear = today.get(Calendar.YEAR)
    // Adjust for week starting on Monday if needed by locale, Calendar default might be Sunday
    // Simple check based on WEEK_OF_YEAR and YEAR
    return cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.WEEK_OF_YEAR) == currentWeek
}

// Helper function to check if a Calendar is within the current month
fun isThisMonth(cal: Calendar): Boolean {
    val today = Calendar.getInstance()
    return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
}

// Helper function to check if a Calendar is within the current year
fun isThisYear(cal: Calendar): Boolean {
    val today = Calendar.getInstance()
    return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavHostController,
    tasks: List<Task>,
) {
    // --- State ---
    var showFilterDialog by remember { mutableStateOf(false) }
    var showTimeTrackingFilterDialog by remember { mutableStateOf(false) } // New state for time tracking filter dialog

    // --- Main Filter State ---
    var selectedDateRange by remember { mutableStateOf(DateRange.ALL) }
    var selectedPriorities by remember { mutableStateOf(TaskPriority.values().toSet()) }
    var selectedLists by remember { mutableStateOf<Set<String>?>(null) } // null means all lists
    var startDate by remember { mutableStateOf<Calendar?>(null) }
    var endDate by remember { mutableStateOf<Calendar?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var minDurationString by remember { mutableStateOf("00:00:00") }
    var maxDurationString by remember { mutableStateOf("99:59:59") }

    // --- Time Tracking Filter State ---
    var timeTrackingSelectedDateRange by remember { mutableStateOf(DateRange.ALL) }
    var timeTrackingSelectedPriorities by remember { mutableStateOf(TaskPriority.values().toSet()) }
    var timeTrackingSelectedLists by remember { mutableStateOf<Set<String>?>(null) } // null means all lists
    var timeTrackingStartDate by remember { mutableStateOf<Calendar?>(null) }
    var timeTrackingEndDate by remember { mutableStateOf<Calendar?>(null) }
    var showTimeTrackingStartDatePicker by remember { mutableStateOf(false) } // New state for time tracking start date picker
    var showTimeTrackingEndDatePicker by remember { mutableStateOf(false) } // New state for time tracking end date picker

    // Convert duration strings to milliseconds for filtering (Main Filter)
    val minDurationMillis = remember(minDurationString) {
        parseTimeToMillis(minDurationString)
    }

    val maxDurationMillis = remember(maxDurationString) {
        if (maxDurationString == "99:59:59") Long.MAX_VALUE else parseTimeToMillis(maxDurationString)
    }

    // Get all available list names from tasks
    val allLists = remember(tasks) {
        tasks.mapNotNull { it.list }.toSet()
    }

    // Apply main filters to tasks for Task Summary
    val filteredTasks = remember(
        tasks, selectedDateRange, selectedPriorities, selectedLists,
        startDate, endDate, minDurationMillis, maxDurationMillis
    ) {
        tasks.filter { task ->
            // Date range filter
            val dateInRange = when (selectedDateRange) {
                DateRange.TODAY -> isSameDay(task.dueDate, Calendar.getInstance())
                DateRange.THIS_MONTH -> isThisMonth(task.dueDate)
                DateRange.THIS_YEAR -> isThisYear(task.dueDate)
                DateRange.CUSTOM -> {
                    val taskDate = task.dueDate.timeInMillis
                    // Start date check (inclusive): task date >= start date (00:00:00)
                    val isAfterStart = startDate == null || taskDate >= startDate!!.timeInMillis
                    // End date check (inclusive): task date < end date + 1 day (00:00:00)
                    val isBeforeEnd = endDate == null || taskDate < (endDate!!.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, 1) // Move to the start of the next day
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    isAfterStart && isBeforeEnd
                }
                DateRange.ALL -> true
            }

            // Priority filter
            val priorityMatches = selectedPriorities.contains(task.priority)

            // List filter
            val listMatches = selectedLists == null || task.list == null || selectedLists!!.contains(task.list)

            // Duration filter
            val durationInRange = task.trackedTimeMillis >= minDurationMillis &&
                                 (maxDurationMillis == Long.MAX_VALUE || task.trackedTimeMillis <= maxDurationMillis)

            dateInRange && priorityMatches && listMatches && durationInRange
        }
    }

    // Apply time tracking filters to tasks for Time Tracking Summary
    val filteredTimeTrackingTasks = remember(
        tasks, timeTrackingSelectedDateRange, timeTrackingSelectedPriorities, timeTrackingSelectedLists,
        timeTrackingStartDate, timeTrackingEndDate
    ) {
        tasks.filter { task ->
            // Date range filter for time tracking
            val dateInRange = when (timeTrackingSelectedDateRange) {
                DateRange.TODAY -> isSameDay(task.dueDate, Calendar.getInstance())
                DateRange.THIS_MONTH -> isThisMonth(task.dueDate)
                DateRange.THIS_YEAR -> isThisYear(task.dueDate)
                DateRange.CUSTOM -> {
                    val taskDate = task.dueDate.timeInMillis
                    // Start date check (inclusive): task date >= start date (00:00:00)
                    val isAfterStart = timeTrackingStartDate == null || taskDate >= timeTrackingStartDate!!.timeInMillis
                    // End date check (inclusive): task date < end date + 1 day (00:00:00)
                    val isBeforeEnd = timeTrackingEndDate == null || taskDate < (timeTrackingEndDate!!.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, 1) // Move to the start of the next day
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    isAfterStart && isBeforeEnd
                }
                DateRange.ALL -> true
            }

            // Priority filter for time tracking
            val priorityMatches = timeTrackingSelectedPriorities.contains(task.priority)

            // List filter for time tracking
            val listMatches = timeTrackingSelectedLists == null || task.list == null || timeTrackingSelectedLists!!.contains(task.list)

            // Only include tasks with tracked time > 0 for time tracking stats
            val hasTrackedTime = task.trackedTimeMillis > 0

            dateInRange && priorityMatches && listMatches && hasTrackedTime
        }
    }

    // --- Calculations with filtered tasks (Main Filter) ---
    val today = Calendar.getInstance()

    val totalTasks = filteredTasks.size
    val completedTasksOverall = filteredTasks.count { it.completed }
    val pendingTasksOverall = totalTasks - completedTasksOverall

    val completedToday = filteredTasks.count { it.completed && isSameDay(it.dueDate, today) }
    val completedThisMonth = filteredTasks.count { it.completed && isThisMonth(it.dueDate) }

    val overallCompletionRate = if (totalTasks > 0) {
        (completedTasksOverall.toDouble() / totalTasks * 100)
    } else {
        0.0
    }

    // --- Calculations for Priority Analysis (using ALL tasks) ---
    val unfilteredPriorityDistribution = remember(tasks) { // Calculate based on the original tasks list
        tasks.groupBy { it.priority }
            .mapValues { it.value.size }
            .toSortedMap(compareBy { it.ordinal })
    }

    // --- Time Tracking Calculations (using filteredTimeTrackingTasks) ---
    val totalTimeTrackedMillis = filteredTimeTrackingTasks.sumOf { it.trackedTimeMillis }
    val totalTimeFormatted = formatTime(totalTimeTrackedMillis)

    // Note: These specific time period calculations now also respect the time tracking filters
    val timeTrackedTodayMillis = filteredTimeTrackingTasks.filter { isSameDay(it.dueDate, today) }
                                      .sumOf { it.trackedTimeMillis }
    val timeTrackedTodayFormatted = formatTime(timeTrackedTodayMillis)

    val timeTrackedThisMonthMillis = filteredTimeTrackingTasks.filter { isThisMonth(it.dueDate) }
                                         .sumOf { it.trackedTimeMillis }
    val timeTrackedThisMonthFormatted = formatTime(timeTrackedThisMonthMillis)

    val timeTrackedThisYearMillis = filteredTimeTrackingTasks.filter { isThisYear(it.dueDate) }
                                        .sumOf { it.trackedTimeMillis }
    val timeTrackedThisYearFormatted = formatTime(timeTrackedThisYearMillis)

    // --- UI ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistics",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Task Summary Card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Title row with filter icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Task Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { showFilterDialog = true },
                            modifier = Modifier.size(32.dp) // Make the icon button smaller
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter Statistics",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Active filters section (only show if filters are applied)
                    if (selectedDateRange != DateRange.ALL ||
                        selectedPriorities.size != TaskPriority.values().size ||
                        selectedLists != null ||
                        minDurationMillis > 0 ||
                        maxDurationMillis < Long.MAX_VALUE) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Active filters:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            // Horizontal scrollable row for filter chips
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Show date range filter if not ALL
                                if (selectedDateRange != DateRange.ALL) {
                                    SuggestionChip(
                                        onClick = { showFilterDialog = true },
                                        label = {
                                            Text(
                                                when (selectedDateRange) {
                                                    DateRange.CUSTOM -> {
                                                        val formatter = SimpleDateFormat("MM/dd", Locale.getDefault())
                                                        val start = startDate?.let { formatter.format(it.time) } ?: "Start"
                                                        val end = endDate?.let { formatter.format(it.time) } ?: "End"
                                                        "$start - $end"
                                                    }
                                                    else -> selectedDateRange.label
                                                }
                                            )
                                        }
                                    )
                                }

                                // Show priority filter if not all priorities
                                if (selectedPriorities.size != TaskPriority.values().size) {
                                    SuggestionChip(
                                        onClick = { showFilterDialog = true },
                                        label = { Text("${selectedPriorities.size} priority/priorities") }
                                    )
                                }

                                // Show list filter if specific lists selected
                                if (selectedLists != null) {
                                    SuggestionChip(
                                        onClick = { showFilterDialog = true },
                                        label = { Text("${selectedLists!!.size} list(s)") }
                                    )
                                }

                                // Show duration filter if custom range
                                if (minDurationMillis > 0 || maxDurationMillis < Long.MAX_VALUE) {
                                    SuggestionChip(
                                        onClick = { showFilterDialog = true },
                                        label = { Text("Duration filter") }
                                    )
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // Task summary stats
                    StatRow("Total Tasks:", "$totalTasks")
                    StatRow("Completed:", "$completedTasksOverall")
                    StatRow("Pending:", "$pendingTasksOverall")
                    StatRow("Completion Rate:", String.format("%.1f%%", overallCompletionRate))
                }
            }

            // --- Priority Analysis ---
            StatCard(title = "Tasks by Priority") {
                // Add bar chart visualization using unfiltered data
                if (unfilteredPriorityDistribution.isNotEmpty()) {
                    val maxCount = unfilteredPriorityDistribution.values.maxOrNull() ?: 0

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(120.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            unfilteredPriorityDistribution.forEach { (priority, count) ->
                                val barHeightFraction = if (maxCount > 0) count.toFloat() / maxCount else 0f

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    // Bar only - removed all text labels
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .fillMaxHeight(barHeightFraction)
                                            .background(
                                                color = priority.color.copy(alpha = 0.7f),
                                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // Text representation using unfiltered data
                unfilteredPriorityDistribution.forEach { (priority, count) ->
                    StatRow("${priority.label}:", "$count", priority.color)
                }
                if (unfilteredPriorityDistribution.isEmpty() && tasks.isEmpty()) {
                    Text("No tasks yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else if (unfilteredPriorityDistribution.isEmpty() && tasks.isNotEmpty()) {
                     Text("No tasks with priorities assigned.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

             // --- Time Tracking Summary ---
            StatCard(
                title = "Time Tracking",
                // Add action icon to the StatCard
                actionIcon = {
                    IconButton(
                        onClick = { showTimeTrackingFilterDialog = true },
                        modifier = Modifier.size(32.dp) // Make the icon button smaller
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList, // Using FilterList icon for consistency
                            contentDescription = "Filter Time Tracking",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                // Active time tracking filters section
                if (timeTrackingSelectedDateRange != DateRange.ALL ||
                    timeTrackingSelectedPriorities.size != TaskPriority.values().size ||
                    timeTrackingSelectedLists != null) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Active filters:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        // Horizontal scrollable row for filter chips
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Show date range filter if not ALL
                            if (timeTrackingSelectedDateRange != DateRange.ALL) {
                                SuggestionChip(
                                    onClick = { showTimeTrackingFilterDialog = true },
                                    label = {
                                        Text(
                                            when (timeTrackingSelectedDateRange) {
                                                DateRange.CUSTOM -> {
                                                    val formatter = SimpleDateFormat("MM/dd", Locale.getDefault())
                                                    val start = timeTrackingStartDate?.let { formatter.format(it.time) } ?: "Start"
                                                    val end = timeTrackingEndDate?.let { formatter.format(it.time) } ?: "End"
                                                    "$start - $end"
                                                }
                                                else -> timeTrackingSelectedDateRange.label
                                            }
                                        )
                                    }
                                )
                            }

                            // Show priority filter if not all priorities
                            if (timeTrackingSelectedPriorities.size != TaskPriority.values().size) {
                                SuggestionChip(
                                    onClick = { showTimeTrackingFilterDialog = true },
                                    label = { Text("${timeTrackingSelectedPriorities.size} priority/priorities") }
                                )
                            }

                            // Show list filter if specific lists selected
                            if (timeTrackingSelectedLists != null) {
                                SuggestionChip(
                                    onClick = { showTimeTrackingFilterDialog = true },
                                    label = { Text("${timeTrackingSelectedLists!!.size} list(s)") }
                                )
                            }
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }

                // Display time tracking stats using the filtered data
                if (filteredTimeTrackingTasks.isEmpty() && tasks.any { it.trackedTimeMillis > 0 }) {
                     Text("No tracked time matches the current filters.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else if (tasks.none { it.trackedTimeMillis > 0 }) {
                     Text("No time tracked yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    StatRow("Total Time Tracked:", totalTimeFormatted)
                    StatRow("Today's Time:", timeTrackedTodayFormatted)
                    StatRow("This Month's Time:", timeTrackedThisMonthFormatted)
                    StatRow("This Year's Time:", timeTrackedThisYearFormatted)
                }
            }

            // Add extra space at the bottom
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- Date pickers for Main Filter ---
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = startDate?.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        startDate = calendar
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = endDate?.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        endDate = calendar
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- Date pickers for Time Tracking Filter ---
    if (showTimeTrackingStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = timeTrackingStartDate?.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showTimeTrackingStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        timeTrackingStartDate = calendar // Update time tracking start date
                    }
                    showTimeTrackingStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeTrackingStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimeTrackingEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = timeTrackingEndDate?.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showTimeTrackingEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        timeTrackingEndDate = calendar // Update time tracking end date
                    }
                    showTimeTrackingEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeTrackingEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- Main Filter Dialog ---
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Filter Statistics") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date Range Filter
                    Text("Date Range", fontWeight = FontWeight.Bold)
                    Column {
                        DateRange.values().forEach { range ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDateRange == range,
                                    onClick = { selectedDateRange = range }
                                )
                                Text(
                                    text = range.label,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        // Custom date range pickers (only show if CUSTOM is selected)
                        if (selectedDateRange == DateRange.CUSTOM) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, top = 8.dp),
                            ) {
                                // Start date button - increased width by using fillMaxWidth instead of weight
                                Button(
                                    onClick = { showStartDatePicker = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = if (startDate != null) {
                                            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(startDate!!.time)
                                        } else {
                                            "Start Date"
                                        },
                                        maxLines = 1
                                    )
                                }

                                // End date button - increased width by using fillMaxWidth instead of weight
                                Button(
                                    onClick = { showEndDatePicker = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (endDate != null) {
                                            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(endDate!!.time)
                                        } else {
                                            "End Date"
                                        },
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Divider()

                    // Priority Filter
                    Text("Priority", fontWeight = FontWeight.Bold)
                    Column {
                        TaskPriority.values().forEach { priority ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedPriorities.contains(priority),
                                    onCheckedChange = { checked ->
                                        selectedPriorities = if (checked) {
                                            selectedPriorities + priority
                                        } else {
                                            selectedPriorities - priority
                                        }
                                    }
                                )
                                Text(
                                    text = priority.label,
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = priority.color
                                )
                            }
                        }
                    }

                    Divider()

                    // Lists Filter (only show if there are lists)
                    if (allLists.isNotEmpty()) {
                        Text("Lists", fontWeight = FontWeight.Bold)

                        // All Lists option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedLists == null,
                                onCheckedChange = { checked ->
                                    selectedLists = if (checked) null else emptySet()
                                }
                            )
                            Text(
                                text = "All Lists",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        // Only show individual lists if "All Lists" is not selected
                        if (selectedLists != null) {
                            Column {
                                allLists.forEach { list ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedLists!!.contains(list),
                                            onCheckedChange = { checked ->
                                                selectedLists = if (checked) {
                                                    selectedLists!! + list
                                                } else {
                                                    selectedLists!! - list
                                                }
                                            }
                                        )
                                        Text(
                                            text = list,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Divider()
                    }

                    // Duration Filter with HH:MM:SS format
                    Text("Duration (HH:MM:SS)", fontWeight = FontWeight.Bold)

                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        // Minimum duration
                        Text("Minimum duration:")
                        OutlinedTextField(
                            value = minDurationString,
                            onValueChange = {
                                // Validate format (simple validation)
                                if (it.isEmpty() || it.matches(Regex("^\\d{0,2}:\\d{0,2}:\\d{0,2}$"))) {
                                    minDurationString = it
                                }
                            },
                            placeholder = { Text("00:00:00") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Maximum duration
                        Text("Maximum duration:")
                        OutlinedTextField(
                            value = maxDurationString,
                            onValueChange = {
                                // Validate format (simple validation)
                                if (it.isEmpty() || it.matches(Regex("^\\d{0,2}:\\d{0,2}:\\d{0,2}$"))) {
                                    maxDurationString = it
                                }
                            },
                            placeholder = { Text("99:59:59") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            "Format: HH:MM:SS (hours:minutes:seconds)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showFilterDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Reset all filters
                        selectedDateRange = DateRange.ALL
                        selectedPriorities = TaskPriority.values().toSet()
                        selectedLists = null
                        startDate = null
                        endDate = null
                        minDurationString = "00:00:00"
                        maxDurationString = "99:59:59"
                        showFilterDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkRed)
                ) {
                    Text("Reset")
                }
            }
        )
    }

    // --- Time Tracking Filter Dialog ---
    if (showTimeTrackingFilterDialog) {
        AlertDialog(
            onDismissRequest = { showTimeTrackingFilterDialog = false },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Filter Time Tracking") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date Range Filter
                    Text("Date Range", fontWeight = FontWeight.Bold)
                    Column {
                        DateRange.values().forEach { range ->
                            // Exclude "All Time" for time tracking filter if desired, or keep it
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = timeTrackingSelectedDateRange == range,
                                    onClick = { timeTrackingSelectedDateRange = range } // Use time tracking state
                                )
                                Text(
                                    text = range.label,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        // Custom date range pickers (only show if CUSTOM is selected)
                        if (timeTrackingSelectedDateRange == DateRange.CUSTOM) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, top = 8.dp),
                            ) {
                                // Start date button
                                Button(
                                    onClick = { showTimeTrackingStartDatePicker = true }, // Show time tracking date picker
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = if (timeTrackingStartDate != null) { // Use time tracking state
                                            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(timeTrackingStartDate!!.time)
                                        } else {
                                            "Start Date"
                                        },
                                        maxLines = 1
                                    )
                                }

                                // End date button
                                Button(
                                    onClick = { showTimeTrackingEndDatePicker = true }, // Show time tracking date picker
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (timeTrackingEndDate != null) { // Use time tracking state
                                            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(timeTrackingEndDate!!.time)
                                        } else {
                                            "End Date"
                                        },
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Divider()

                    // Priority Filter
                    Text("Priority", fontWeight = FontWeight.Bold)
                    Column {
                        TaskPriority.values().forEach { priority ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = timeTrackingSelectedPriorities.contains(priority), // Use time tracking state
                                    onCheckedChange = { checked ->
                                        timeTrackingSelectedPriorities = if (checked) { // Use time tracking state
                                            timeTrackingSelectedPriorities + priority
                                        } else {
                                            timeTrackingSelectedPriorities - priority
                                        }
                                    }
                                )
                                Text(
                                    text = priority.label,
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = priority.color
                                )
                            }
                        }
                    }

                    Divider()

                    // Lists Filter (only show if there are lists)
                    if (allLists.isNotEmpty()) {
                        Text("Lists", fontWeight = FontWeight.Bold)

                        // All Lists option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = timeTrackingSelectedLists == null, // Use time tracking state
                                onCheckedChange = { checked ->
                                    timeTrackingSelectedLists = if (checked) null else emptySet() // Use time tracking state
                                }
                            )
                            Text(
                                text = "All Lists",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        // Only show individual lists if "All Lists" is not selected
                        if (timeTrackingSelectedLists != null) { // Use time tracking state
                            Column {
                                allLists.forEach { list ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = timeTrackingSelectedLists!!.contains(list), // Use time tracking state
                                            onCheckedChange = { checked ->
                                                timeTrackingSelectedLists = if (checked) { // Use time tracking state
                                                    timeTrackingSelectedLists!! + list
                                                } else {
                                                    timeTrackingSelectedLists!! - list
                                                }
                                            }
                                        )
                                        Text(
                                            text = list,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        // No Divider needed here if it's the last section
                    }
                    // Note: Duration filter is not included in the time tracking filter as requested
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTimeTrackingFilterDialog = false }, // Close this dialog
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Reset time tracking filters
                        timeTrackingSelectedDateRange = DateRange.ALL
                        timeTrackingSelectedPriorities = TaskPriority.values().toSet()
                        timeTrackingSelectedLists = null
                        timeTrackingStartDate = null
                        timeTrackingEndDate = null
                        showTimeTrackingFilterDialog = false // Close this dialog
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkRed)
                ) {
                    Text("Reset")
                }
            }
        )
    }
}

// Enum for date range filter options
enum class DateRange(val label: String) {
    TODAY("Today"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom Range"),
    ALL("All Time")
}

// Reusable Composable for a Statistics Card
@Composable
fun StatCard(
    title: String,
    actionIcon: (@Composable RowScope.() -> Unit)? = null, // Optional action icon slot
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Or use theme color
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row( // Use Row for title and potential action icon
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(end = 8.dp) // Allow title to take space
                )
                // Display the action icon if provided
                actionIcon?.invoke(this)
            }
            // Inject the specific stats content
            content()
        }
    }
}

// Reusable Composable for a single statistic row
@Composable
fun StatRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}