package com.example.jita
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.jita.data.AppDatabase
import com.example.jita.data.ListNameEntity
import com.example.jita.data.TaskEntity
import com.example.jita.ui.theme.JitaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.activity.compose.rememberLauncherForActivityResult
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import com.example.jita.data.ListNameDao
import com.example.jita.data.TaskDao
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import java.io.IOException
import java.io.InputStream
import androidx.compose.material.icons.filled.Description
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipFile
import com.example.jita.data.NoteDao
import com.example.jita.data.FolderDao
import com.example.jita.data.NoteEntity
import com.example.jita.data.FolderEntity
import android.os.PowerManager
import android.provider.Settings
import android.app.AlarmManager
import androidx.lifecycle.lifecycleScope
import com.example.jita.alarm.AlarmScheduler
import com.example.jita.model.toReminder
import androidx.compose.foundation.isSystemInDarkTheme


object AppDestinations {
    const val MAIN_SCREEN = "main"
    const val LISTS_SCREEN = "lists"
    const val POMODORO_SCREEN = "pomodoro"
    const val STATISTICS_SCREEN = "statistics"
    const val BACKUP_SCREEN = "backup"
    const val RESTORE_SCREEN = "restore"
    const val NOTES_SCREEN = "notes"
    const val NOTE_EDITOR_SCREEN = "note_editor/{noteId}"
    const val REMINDERS_SCREEN = "reminders"
    const val REMINDER_SETTINGS_SCREEN = "reminder_settings"
    const val REMINDER_EDITOR_SCREEN = "reminder_editor/{reminderId}"
    
    // Helper functions for parameterized navigation
    fun createNoteEditorRoute(noteId: Int = -1): String {
        return "note_editor/$noteId"
    }
    
    fun createReminderEditorRoute(reminderId: Int = -1): String {
        return "reminder_editor/$reminderId"
    }
}

// Extension function to move items within a SnapshotStateList
fun <T> SnapshotStateList<T>.move(from: Int, to: Int) {
    if (from == to) return
    // Ensure indices are valid before proceeding
    if (from < 0 || from >= size || to < 0 || to >= size) {
        println("Warning: Invalid move indices (from: $from, to: $to, size: $size)")
        return
    }
    val item = removeAt(from)
    add(to, item)
}

// Add the extension function to convert Task to TaskEntity
fun Task.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        dueDate = this.dueDate.timeInMillis,
        priority = this.priority.name,
        list = this.list,
        trackedTimeMillis = this.trackedTimeMillis,
        isTracking = this.isTracking,
        trackingStartTime = this.trackingStartTime,
        completed = this.completed,  // Add completed flag
        imagePaths = this.imagePaths,
        filePaths = this.filePaths,
        subtasks = this.subtasks,
        completedSubtasks = this.completedSubtasks
    )
}

// Helper function to get file name from URI
fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                fileName = cursor.getString(displayNameIndex)
            }
        }
    }
    return fileName
}

// Helper extension function to convert JSONArray to List
fun JSONArray.toList(): List<Any> {
    val list = mutableListOf<Any>()
    for (i in 0 until this.length()) {
        list.add(this.get(i))
    }
    return list
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get database instance
        val database = AppDatabase.getDatabase(applicationContext)
        val listNameDao = database.listNameDao()
        val taskDao = database.taskDao()
        val noteDao = database.noteDao()  // Add this line
        val folderDao = database.folderDao()  // Add this line
        val reminderDao = database.reminderDao()  // Add this line

        // Request necessary permissions for alarms to work in background
        requestPermissions()

        // Ensure all alarms are scheduled properly
        checkAndRestoreAlarms()

        setContent {
            // --- Coroutine Scope ---
            val scope = rememberCoroutineScope() // Scope for launching db operations

            // --- Hoisted State ---
            // Observe list names from the database using Flow and collectAsState
            // We store the Entity here to easily get the ID for updates/deletes
            val listNameEntities by listNameDao.getAllListNames().collectAsState(initial = emptyList())
            // Derive the simple list of names for UI components that need it
            val listNames = remember(listNameEntities) {
                listNameEntities.map { it.name }.toMutableStateList()
            }

            // Observe all tasks from the database
            val allTasksFromDb by taskDao.getAllTasks().collectAsState(initial = emptyList())
            // Convert TaskEntity to Task for the UI state
            val tasks = remember(allTasksFromDb) {
                allTasksFromDb.map { entity ->
                    // Convert TaskEntity to Task
                    val dueDateCalendar = Calendar.getInstance().apply { timeInMillis = entity.dueDate } // Convert Long to Calendar
                    Task(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        dueDate = dueDateCalendar, // Use the converted Calendar
                        priority = try { // Safely convert String to Enum
                            TaskPriority.valueOf(entity.priority)
                        } catch (e: IllegalArgumentException) {
                            Log.e("TaskMapping", "Invalid priority string found in DB: ${entity.priority}")
                            TaskPriority.MEDIUM // Default fallback
                        },
                        list = entity.list,
                        // Restore tracking state from DB
                        isTracking = entity.isTracking,
                        trackedTimeMillis = entity.trackedTimeMillis,
                        trackingStartTime = entity.trackingStartTime,
                        completed = entity.completed,  // Add completed flag
                        imagePaths = entity.imagePaths,
                        filePaths = entity.filePaths,
                        subtasks = entity.subtasks,
                        completedSubtasks = entity.completedSubtasks
                    )
                }.toMutableStateList()
            }

            // Hoist selectedDate state to be shared
            var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
            // Function to check if two dates are the same day (can be kept here or moved to a util file)
            val isSameDay = { date1: Calendar, date2: Calendar ->
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                        date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                        date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
            }

            // --- Callbacks for ListsScreen (Updated for Database) ---
            val onAddList = { name: String ->
                if (name.isNotBlank() && listNames.none { it.equals(name, ignoreCase = true) }) {
                    scope.launch(Dispatchers.IO) { // Use IO dispatcher for DB operations
                        listNameDao.insertListName(ListNameEntity(name = name))
                    }
                }
            }
            val onEditList = { index: Int, newName: String ->
                // Find the original entity based on the index (fragile if list order changes rapidly)
                // It's better to pass the original name or ID from the UI if possible
                if (newName.isNotBlank() && index >= 0 && index < listNameEntities.size) {
                    val originalEntity = listNameEntities[index]
                    if (listNames.none { it.equals(newName, ignoreCase = true) && it != originalEntity.name }) {
                        scope.launch(Dispatchers.IO) {
                            // Update the ListNameEntity
                            listNameDao.updateListName(originalEntity.copy(name = newName))
                            // Update associated tasks
                            taskDao.updateTasksListName(originalEntity.name, newName)
                        }
                    }
                }
            }
            val onDeleteList = { index: Int ->
                if (index >= 0 && index < listNameEntities.size) {
                    val entityToDelete = listNameEntities[index]
                    scope.launch(Dispatchers.IO) {
                        listNameDao.deleteListNameByName(entityToDelete.name) // Delete by name
                        // Also delete tasks associated with this list
                        taskDao.deleteTasksByListName(entityToDelete.name)
                    }
                }
            }
            // Persisting drag-and-drop order requires storing an orderIndex in the DB
            // and updating multiple rows in a transaction. For simplicity, this is omitted.
            // The list will revert to alphabetical order on restart.
            val onMoveList = { fromIndex: Int, toIndex: Int ->
                // This only affects the current UI state, not the persistent order
                listNames.move(fromIndex, toIndex)
                // To persist order:
                // 1. Add `orderIndex: Int` to ListNameEntity
                // 2. Query DAO with `ORDER BY orderIndex ASC`
                // 3. Implement a DAO method `updateListOrder(List<ListNameEntity>)`
                // 4. In that method, iterate through the reordered list and update `orderIndex`
                //    for each entity within a transaction.
                println("Warning: List move is temporary and not persisted.")
            }
            // --- End Callbacks ---

            // --- Callbacks for Tasks (passed to MainScreen and PomodoroScreen) ---
            val onAddTask: (Task) -> Unit = { task -> // Explicitly define return type as Unit
                scope.launch(Dispatchers.IO) {
                    taskDao.insertTask(task.toTaskEntity())
                }
            }
            val onDeleteTask: (Task) -> Unit = { task -> // Explicitly define return type as Unit
                scope.launch(Dispatchers.IO) {
                    val entityToDelete = allTasksFromDb.find { it.id == task.id }
                    entityToDelete?.let { taskDao.deleteTask(it) }
                }
            }
            val onUpdateTask: (Task) -> Unit = { task -> // Explicitly define return type as Unit
                scope.launch(Dispatchers.IO) {
                    taskDao.updateTask(task.toTaskEntity())
                }
            }
            // Callback to update the shared selectedDate
            val onDateSelected = { newDate: Calendar ->
                selectedDate = newDate
            }
            // --- End Callbacks ---

            JitaTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.MAIN_SCREEN
                ) {
                    composable(AppDestinations.MAIN_SCREEN) {
                        MainScreen(
                            navController = navController,
                            listNames = listNames, // Pass derived list names
                            tasks = tasks, // Pass tasks derived from DB
                            selectedDate = selectedDate, // Pass shared selected date
                            onDateSelected = onDateSelected, // Pass date selection callback
                            isSameDay = isSameDay, // Pass date comparison logic
                            onAddTask = onAddTask,
                            onDeleteTask = onDeleteTask,
                            onUpdateTask = onUpdateTask
                        )
                    }
                    composable(AppDestinations.LISTS_SCREEN) {
                        ListsScreen(
                            navController = navController,
                            listNames = listNames, // Pass derived list names
                            tasks = tasks, // Add parameter to access tasks
                            onAddList = onAddList,
                            onEditList = onEditList,
                            onDeleteList = onDeleteList,
                            onMoveList = onMoveList
                        )
                    }
                    composable(AppDestinations.POMODORO_SCREEN) {
                        PomodoroScreen(
                            navController = navController,
                            tasks = tasks, // Pass tasks
                            selectedDate = selectedDate, // Pass shared selected date
                            onDateSelected = onDateSelected, // Pass date selection callback
                            isSameDay = isSameDay, // Pass date comparison logic
                            onUpdateTask = onUpdateTask // Pass task update callback
                        )
                    }
                    // Add composable for the new Statistics screen
                    composable(AppDestinations.STATISTICS_SCREEN) {
                        // Call the StatisticsScreen composable here
                        StatisticsScreen(
                            navController = navController,
                            tasks = tasks // Pass the tasks list
                            // Remove selectedDate and onDateSelected as they are not used yet
                            // selectedDate = selectedDate,
                            // onDateSelected = onDateSelected
                        )
                    }
                    // Add composable for the new Backup screen
                    composable(AppDestinations.BACKUP_SCREEN) {
                        BackupScreen(
                            navController = navController,
                            listNameEntities = listNameEntities,
                            tasks = tasks,
                            noteDao = noteDao,  // Add parameter for noteDao
                            folderDao = folderDao  // Add parameter for folderDao
                        )
                    }
                    // Add composable for the Restore screen
                    composable(AppDestinations.RESTORE_SCREEN) {
                        RestoreScreen(
                            navController = navController,
                            listNameDao = listNameDao,
                            taskDao = taskDao,
                            noteDao = noteDao,  // Add parameter for noteDao
                            folderDao = folderDao  // Add parameter for folderDao
                        )
                    }
                    composable(AppDestinations.NOTES_SCREEN) {
                        NotesScreen(
                            navController = navController,
                            noteDao = noteDao,  // Add this line
                            folderDao = folderDao  // Add this line
                        )
                    }
                    // Add composable for the Reminders screen
                    composable(AppDestinations.REMINDERS_SCREEN) {
                        RemindersScreen(
                            navController = navController,
                            reminderDao = reminderDao,  // Pass the reminderDao
                            tasks = tasks   // Pass the tasks list
                        )
                    }
                    // Add composable for the ReminderSettings screen
                    composable(AppDestinations.REMINDER_SETTINGS_SCREEN) {
                        ReminderSettingsScreen(
                            navController = navController,
                            tasks = tasks,
                            reminderDao = reminderDao  // Add this parameter
                        )
                    }
                    // Add composable for editing existing reminders
                    composable(
                        route = AppDestinations.REMINDER_EDITOR_SCREEN,
                        arguments = listOf(
                            navArgument("reminderId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val reminderId = backStackEntry.arguments?.getInt("reminderId") ?: -1
                        ReminderSettingsScreen(
                            navController = navController,
                            tasks = tasks,
                            reminderDao = reminderDao,
                            reminderId = reminderId
                        )
                    }
                    composable(
                        route = AppDestinations.NOTE_EDITOR_SCREEN,
                        arguments = listOf(
                            navArgument("noteId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                        val currentFolderId = if (noteId == -1) {
                            // If creating a new note, use the current folder ID from the notes screen
                            navController.previousBackStackEntry?.savedStateHandle?.get<Int>("currentFolderId")
                        } else {
                            // If editing, we'll get the folder ID from the note itself
                            null
                        }
                        
                        NoteEditorScreen(
                            navController = navController,
                            noteDao = noteDao,
                            noteId = noteId,
                            folderId = currentFolderId,
                            taskDao = taskDao // Add taskDao parameter
                        )
                    }
                }
            }
        }
    }

    // Add this after onCreate or in a separate method
    private fun requestPermissions() {
        // Request battery optimization exception
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = packageName
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    // Request the user to disable battery optimization for this app
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to request ignore battery optimizations", e)
                }
            }
        }
        
        // Request system alert window permission if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            try {
                // Request the permission to draw over other apps
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to request overlay permission", e)
            }
        }
        
        // Check for alarm permissions on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    // Direct the user to the exact alarm settings
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to request exact alarm permission", e)
                }
            }
        }
    }

    private fun checkAndRestoreAlarms() {
        try {
            val alarmScheduler = AlarmScheduler(this)
            
            // In a coroutine, check all alarms and reschedule them if needed
            lifecycleScope.launch(Dispatchers.IO) {
                val reminderDao = AppDatabase.getDatabase(applicationContext).reminderDao()
                val reminders = reminderDao.getAllRemindersAsList()
                
                // Current time for filtering out passed reminders
                val currentTime = Calendar.getInstance().timeInMillis
                
                for (reminderEntity in reminders) {
                    val reminder = reminderEntity.toReminder()
                    if (reminder.time.timeInMillis > currentTime) {
                        Log.d("MainActivity", "Ensuring alarm is scheduled for reminder: ${reminder.id}")
                        // Cancel any existing alarm and reschedule it
                        alarmScheduler.updateAlarm(reminder)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking alarms", e)
        }
    }
}

// --- Screen for Lists ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    navController: NavHostController,
    listNames: List<String>,
    tasks: List<Task>, // Add parameter to access tasks
    onAddList: (String) -> Unit,
    onEditList: (Int, String) -> Unit,
    onDeleteList: (Int) -> Unit,
    onMoveList: (Int, Int) -> Unit // Still accept move for UI, but it's not persisted
) {

    // --- State for Dialogs (remains local to ListsScreen) ---
    var showAddListDialog by remember { mutableStateOf(false) }
    var newListName by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var listToDeleteIndex by remember { mutableStateOf<Int?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var listToEditIndex by remember { mutableStateOf<Int?>(null) }
    var editedListName by rememberSaveable { mutableStateOf("") }

    // --- State for Drag and Drop (remains local) ---
    val lazyListState = rememberLazyListState()
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragInitialOffset by remember { mutableStateOf(Offset.Zero) }
    val itemHeights = remember { mutableStateMapOf<Int, Int>() }
    var potentialTargetIndex by remember { mutableStateOf<Int?>(null) }
    val density = LocalDensity.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lists",
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
                actions = {
                    IconButton(onClick = {}, enabled = false) { /* Invisible */ }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newListName = "" // Reset field
                    showAddListDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add List", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->

        // --- List Display with Drag and Drop ---
        LazyColumn(
            state = lazyListState, // Attach state for scroll and layout info
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(listNames, key = { index, item -> item }) { index, name ->
                val isDragging = index == draggedItemIndex
                val currentTargetIdx = potentialTargetIndex

                // --- Calculate offset for non-dragged items ---
                var nonDragItemOffsetTarget = 0f
                val spacingPx = with(density) { 8.dp.toPx() }

                if (!isDragging && draggedItemIndex != null && currentTargetIdx != null) {
                    val startIdx = draggedItemIndex!!
                    val draggedItemHeight = itemHeights[startIdx]?.toFloat() ?: 0f

                    if (draggedItemHeight > 0) {
                        if (startIdx < currentTargetIdx) {
                            if (index in (startIdx + 1)..currentTargetIdx) {
                                nonDragItemOffsetTarget = -draggedItemHeight - spacingPx
                            }
                        } else if (startIdx > currentTargetIdx) {
                            if (index in currentTargetIdx until startIdx) {
                                nonDragItemOffsetTarget = draggedItemHeight + spacingPx
                            }
                        }
                    }
                }

                // Animate the offset for non-dragged items for smooth transition
                val animatedNonDragItemOffset by animateFloatAsState(
                    targetValue = nonDragItemOffsetTarget,
                    label = "NonDragItemOffsetAnimation"
                )

                // Calculate the offset for the dragged item based on gesture
                val gestureOffsetY = if (isDragging) dragOffset.y - dragInitialOffset.y else 0f

                // Final offset to apply: gesture offset for dragging item, animated shift for others
                val finalOffsetY = if (isDragging) gestureOffsetY else animatedNonDragItemOffset

                Box(
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            translationY = finalOffsetY
                        }
                        .onGloballyPositioned { coordinates ->
                            itemHeights[index] = coordinates.size.height
                        }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    if (draggedItemIndex == null) {
                                        draggedItemIndex = index
                                        dragInitialOffset = offset
                                        dragOffset = offset
                                        potentialTargetIndex = index
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    if (index == draggedItemIndex) {
                                        change.consume()
                                        dragOffset += dragAmount

                                        val layoutInfo = lazyListState.layoutInfo
                                        val currentDragGestureOffsetY = dragOffset.y - dragInitialOffset.y
                                        val draggedItemInfo = layoutInfo.visibleItemsInfo.find { it.index == draggedItemIndex }

                                        if (draggedItemInfo != null) {
                                            val draggedItemCurrentY = draggedItemInfo.offset + currentDragGestureOffsetY
                                            val draggedItemCenterY = draggedItemCurrentY + (draggedItemInfo.size / 2f)

                                            val targetItem = layoutInfo.visibleItemsInfo
                                                .filterNot { it.index == draggedItemIndex }
                                                .minByOrNull { info ->
                                                    val infoCenterY = info.offset + (info.size / 2f)
                                                    abs(infoCenterY - draggedItemCenterY)
                                                }

                                            potentialTargetIndex = if (targetItem != null) {
                                                val targetCenterY = targetItem.offset + (targetItem.size / 2f)
                                                if (draggedItemCenterY > targetCenterY) {
                                                    // Adjust target index calculation if needed based on visual feedback
                                                    targetItem.index + if (draggedItemIndex!! < targetItem.index) 0 else 1
                                                } else {
                                                    targetItem.index - if (draggedItemIndex!! > targetItem.index) 0 else 0
                                                }
                                            } else {
                                                // Handle edge cases (dragging to top/bottom)
                                                val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull { it.index != draggedItemIndex }
                                                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull { it.index != draggedItemIndex }

                                                when {
                                                    firstVisible != null && draggedItemCenterY < (firstVisible.offset + (firstVisible.size / 2f)) -> firstVisible.index
                                                    lastVisible != null && draggedItemCenterY > (lastVisible.offset + (lastVisible.size / 2f)) -> lastVisible.index + 1
                                                    else -> index // Fallback
                                                }
                                            }
                                            // Ensure potential target index stays within bounds
                                            potentialTargetIndex = potentialTargetIndex?.coerceIn(0, listNames.size -1) // Coerce to valid list index

                                        } else {
                                            Log.w("DragAndDrop", "Dragged item info became null during drag for index $draggedItemIndex")
                                            // Reset state if item info is lost
                                            draggedItemIndex = null
                                            potentialTargetIndex = null
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (index == draggedItemIndex) {
                                        val startIndex = draggedItemIndex!!
                                        // Use the final potentialTargetIndex, ensuring it's not null and within bounds
                                        val endIndex = potentialTargetIndex?.coerceIn(0, listNames.size -1) ?: startIndex

                                        if (startIndex != endIndex) {
                                            // Call the move callback for UI update (not persisted)
                                            onMoveList(startIndex, endIndex)
                                        }

                                        // Reset drag state regardless of move
                                        draggedItemIndex = null
                                        potentialTargetIndex = null
                                        dragOffset = Offset.Zero
                                        dragInitialOffset = Offset.Zero
                                    }
                                },
                                onDragCancel = {
                                    if (index == draggedItemIndex) {
                                        // Reset drag state on cancel
                                        draggedItemIndex = null
                                        potentialTargetIndex = null
                                        dragOffset = Offset.Zero
                                        dragInitialOffset = Offset.Zero
                                    }
                                }
                            )
                        }
                ) {
                    ListCard(
                        listName = name,
                        isDragging = isDragging,
                        taskCount = tasks.count { it.list == name }, // Pass task count to ListCard
                        onEditClick = {
                            listToEditIndex = index
                            // Pre-fill dialog with the current name from the list
                            editedListName = listNames.getOrElse(index) { "" }
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            listToDeleteIndex = index
                            showDeleteConfirmDialog = true
                        }
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    // Add List Dialog
    if (showAddListDialog) {
        AlertDialog(
            onDismissRequest = { showAddListDialog = false; newListName = "" },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("Create New List") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("List Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary, 
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary, 
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    // Add check for existing names (case-insensitive)
                    isError = listNames.any { it.equals(newListName.trim(), ignoreCase = true) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmedName = newListName.trim()
                        onAddList(trimmedName) // Call the callback provided by MainActivity
                        showAddListDialog = false
                        newListName = "" // Clear field
                    },
                    // Disable if blank or name already exists (case-insensitive)
                    enabled = newListName.isNotBlank() && listNames.none { it.equals(newListName.trim(), ignoreCase = true) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddListDialog = false; newListName = "" },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Cancel") }
            }
        )
    }

    // Edit List Dialog
    if (showEditDialog && listToEditIndex != null) {
        val originalName = listNames.getOrNull(listToEditIndex!!) ?: ""
        AlertDialog(
            onDismissRequest = { showEditDialog = false; listToEditIndex = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("Edit List Name") },
            text = {
                OutlinedTextField(
                    value = editedListName,
                    onValueChange = { editedListName = it },
                    label = { Text("New List Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary, 
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary, 
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    // Add check for existing names (case-insensitive), excluding the original name
                    isError = listNames.any { it.equals(editedListName.trim(), ignoreCase = true) && !it.equals(originalName, ignoreCase = true) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmedName = editedListName.trim()
                        listToEditIndex?.let { index ->
                            onEditList(index, trimmedName) // Call the callback
                        }
                        showEditDialog = false
                        listToEditIndex = null
                        // editedListName = "" // Don't clear here, might be needed if dialog reappears quickly
                    },
                    // Disable if blank or name already exists (case-insensitive), excluding original
                    enabled = editedListName.isNotBlank() && listNames.none { it.equals(editedListName.trim(), ignoreCase = true) && !it.equals(originalName, ignoreCase = true) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false; listToEditIndex = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && listToDeleteIndex != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false; listToDeleteIndex = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete the list '${listNames.getOrNull(listToDeleteIndex!!)}'? This will also delete all tasks associated with this list.") }, // Updated warning
            confirmButton = {
                TextButton(
                    onClick = {
                        listToDeleteIndex?.let { index ->
                            onDeleteList(index) // Call the callback
                        }
                        showDeleteConfirmDialog = false
                        listToDeleteIndex = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false; listToDeleteIndex = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Cancel") }
            }
        )
    }
}

// --- Simple Composable for the List Card ---
@Composable
fun ListCard(
    listName: String,
    isDragging: Boolean,
    taskCount: Int = 0,  // Add parameter for task count with default value
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f) 
            else 
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // Add task count text
                Text(
                    text = "$taskCount task${if (taskCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit List",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete List",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    listNames: List<String>,
    tasks: List<Task>,
    selectedDate: Calendar, // Receive selectedDate
    onDateSelected: (Calendar) -> Unit, // Receive callback
    isSameDay: (Calendar, Calendar) -> Boolean, // Receive comparison logic
    onAddTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onUpdateTask: (Task) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Add search state
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Task>>(emptyList()) }

    val drawerItems = listOf(
        AppDestinations.MAIN_SCREEN to "Calendar",
        AppDestinations.LISTS_SCREEN to "Lists",
        AppDestinations.POMODORO_SCREEN to "Pomodoro",
        AppDestinations.STATISTICS_SCREEN to "Statistics",
        AppDestinations.NOTES_SCREEN to "Notes", // Moved Notes above Backup
        AppDestinations.BACKUP_SCREEN to "Backup",
        AppDestinations.RESTORE_SCREEN to "Restore",
        AppDestinations.REMINDERS_SCREEN to "Reminders"
    )

    // Local state for the Add Task Dialog form
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskName by rememberSaveable { mutableStateOf("") }
    var newTaskDescription by rememberSaveable { mutableStateOf("") }
    var newTaskDate by rememberSaveable { mutableStateOf(Calendar.getInstance()) } // Keep Calendar instance
    var newTaskPriority by rememberSaveable { mutableStateOf(TaskPriority.MEDIUM) }
    var newTaskList by rememberSaveable { mutableStateOf<String?>(null) }
    var newTaskImagePaths by rememberSaveable { mutableStateOf<List<String>>(emptyList()) } // Changed to list
    var newTaskFilePaths by rememberSaveable { mutableStateOf<List<String>>(emptyList()) } // Changed to list
    var showDatePicker by remember { mutableStateOf(false) }
    var isListDropdownExpanded by remember { mutableStateOf(false) }

    // Add state for delete confirmation dialog
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    // Add state for edit task dialog
    var showEditTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Add state for the task completion popup
    var showTaskCompletionPopup by remember { mutableStateOf(false) }
    var completedTaskName by remember { mutableStateOf<String?>(null) } // To display task name in popup

    // Add state for collapsible calendar
    var isCalendarExpanded by remember { mutableStateOf(true) } // Start with calendar expanded
    
    // Filter tasks for the selected date using the passed-in state and function
    val filteredTasks = remember(tasks, selectedDate) {
        tasks.filter { task -> isSameDay(task.dueDate, selectedDate) }
            .sortedWith(compareBy<Task> { it.priority }.thenBy { it.name }) // Sort by priority then name
    }

    // Force recomposition every second when any task is being tracked
    val isAnyTaskTracking = tasks.any { it.isTracking }
    var tickerState by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Add file and image picker launchers
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            // Copy file to app's files directory
            val fileName = "jita_image_${System.currentTimeMillis()}.jpg"
            val targetDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "jita_files")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val targetFile = File(targetDir, fileName)
            
            try {
                context.contentResolver.openInputStream(selectedUri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                // Add to list instead of replacing
                newTaskImagePaths = newTaskImagePaths + targetFile.absolutePath
                Toast.makeText(context, "Image attached successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ImagePicker", "Error copying image", e)
                Toast.makeText(context, "Failed to attach image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            // Get file name from URI
            val fileName = getFileNameFromUri(context, selectedUri) ?: "jita_file_${System.currentTimeMillis()}"
            val targetDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "jita_files")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val targetFile = File(targetDir, fileName)
            
            try {
                context.contentResolver.openInputStream(selectedUri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                // Add to list instead of replacing
                newTaskFilePaths = newTaskFilePaths + targetFile.absolutePath
                Toast.makeText(context, "File attached successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("FilePicker", "Error copying file", e)
                Toast.makeText(context, "Failed to attach file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Add permission request launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        // Remove toast notifications about permissions
    }
    
    // Function to check and request permissions
    fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }
    
    // Check permissions on initialization
    LaunchedEffect(Unit) {
        checkAndRequestPermissions()
    }

    // This LaunchedEffect is crucial for updating the timer display
    LaunchedEffect(isAnyTaskTracking) {
        if (isAnyTaskTracking) {
            while (true) {
                delay(50) // Update 20 times per second for very smooth updates
                tickerState = System.currentTimeMillis() // Trigger recomposition
            }
        }
    }

    // Search function
    LaunchedEffect(searchQuery, tasks) {
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            searchResults = tasks.filter { task ->
                task.name.lowercase().contains(query) || 
                task.description.lowercase().contains(query)
            }
        } else {
            searchResults = emptyList()
        }
    }

    // Calculate total tracked time for the selected date
    val totalTrackedTime = remember(tasks, selectedDate, tickerState) {
        tasks.filter { task -> isSameDay(task.dueDate, selectedDate) }
            .sumOf { task ->
                if (task.isTracking) {
                    task.trackedTimeMillis + (System.currentTimeMillis() - task.trackingStartTime)
                } else {
                    task.trackedTimeMillis
                }
            }
    }

    // Format the total tracked time
    val formattedTotalTime = formatTime(totalTrackedTime)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
                Spacer(Modifier.height(12.dp))
                drawerItems.forEach { (route, label) ->
                    NavigationDrawerItem(
                        icon = { /* Optional Icon */ },
                        label = { Text(label) },
                        selected = route == currentRoute,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (route != currentRoute) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (showSearch) {
                    // Search TopAppBar
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search tasks...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                showSearch = false
                                searchQuery = ""
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Close Search"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                } else {
                    // Regular TopAppBar
                    TopAppBar(
                        title = {
                            Text(
                                text = "JITA",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Open Navigation Menu"
                                )
                            }
                        },
                        actions = {
                            // Add search icon
                            IconButton(onClick = { showSearch = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Tasks",
                                    tint = MaterialTheme.colorScheme.onPrimary
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
                        // Reset form fields
                        newTaskName = ""
                        newTaskDescription = ""
                        // Default new task date to the currently selected date on the calendar
                        newTaskDate = selectedDate.clone() as Calendar // Use passed-in selectedDate
                        newTaskPriority = TaskPriority.MEDIUM
                        newTaskList = null // Reset list selection
                        isListDropdownExpanded = false // Ensure dropdown is closed
                        showAddTaskDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add Task",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Show search results if search is active and has results
                if (showSearch && searchQuery.isNotBlank()) {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    if (searchResults.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No results found for \"$searchQuery\"",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { task ->
                                SearchResultItem(
                                    task = task,
                                    onClick = {
                                        // Navigate to the task's date
                                        onDateSelected(task.dueDate)
                                        // Close search
                                        showSearch = false
                                        searchQuery = ""
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Regular calendar and task view
                    
                    // Calendar section with collapsible header
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header row with date and collapse/expand button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Date display
                            val headerDateFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            Text(
                                text = headerDateFormatter.format(selectedDate.time),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (isSystemInDarkTheme()) Color.White else Color(0xFF6C4CE0)
                            )
                            
                            // Toggle button
                            IconButton(onClick = { isCalendarExpanded = !isCalendarExpanded }) {
                                Icon(
                                    imageVector = if (isCalendarExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (isCalendarExpanded) "Collapse Calendar" else "Expand Calendar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Animated calendar visibility
                        AnimatedVisibility(visible = isCalendarExpanded) {
                            // Pass the selected date and update callback to WeekCalendar
                            WeekCalendar(
                                selectedDate = selectedDate, // Use passed-in selectedDate
                                onDateSelected = onDateSelected, // Use passed-in callback
                                tasks = tasks // Pass all tasks to the calendar
                            )
                        }
                    }

                    // Format the date for the header using "MMMM d"
                    val headerDateFormatter = SimpleDateFormat("MMMM d", Locale.getDefault())
                    val headerText = if (totalTrackedTime > 0) {
                        "Tasks for ${headerDateFormatter.format(selectedDate.time)} ($formattedTotalTime)"
                    } else {
                        "Tasks for ${headerDateFormatter.format(selectedDate.time)}"
                    }

                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Display filtered tasks or empty message
                    if (filteredTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize() // Fill remaining space
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // --- Edit Start ---
                            // Use a Column to stack the text and the GIF
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "No tasks for this day. Click + to add a task.",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp)) // Add space between text and GIF
                                // Add the bun.gif using the existing GifImage composable
                                GifImage(
                                    modifier = Modifier.size(135.dp), // Adjust size as needed
                                    drawableResId = R.drawable.bun // Specify the bun GIF
                                )
                            }
                            // --- Edit End ---
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize() // Fill remaining space
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp) // Add padding at the bottom
                        ) {
                            items(filteredTasks, key = { task -> task.id }) { task -> // Use task.id as key
                                // Get context inside the item scope where it's needed
                                val context = LocalContext.current
                                TaskCard(
                                    task = task,
                                    onDeleteClick = {
                                        // Show confirmation dialog instead of deleting immediately
                                        taskToDelete = task
                                        showDeleteConfirmDialog = true
                                    },
                                    onEditClick = {
                                        // Set the task to edit and show edit dialog
                                        taskToEdit = task
                                        // Pre-fill the edit form with task data
                                        newTaskName = task.name
                                        newTaskDescription = task.description
                                        newTaskDate = task.dueDate.clone() as Calendar
                                        newTaskPriority = task.priority
                                        newTaskList = task.list
                                        // Initialize attachment paths with current task data
                                        newTaskImagePaths = task.imagePaths
                                        newTaskFilePaths = task.filePaths
                                        showEditTaskDialog = true
                                    },
                                    onClick = {
                                        // Same as onEditClick
                                        taskToEdit = task
                                        newTaskName = task.name
                                        newTaskDescription = task.description
                                        newTaskDate = task.dueDate.clone() as Calendar
                                        newTaskPriority = task.priority
                                        newTaskList = task.list
                                        showEditTaskDialog = true
                                    },
                                    onTrackingToggle = { isTracking ->
                                        // Add more detailed logging
                                        Log.d("MainScreen", "Tracking toggled for task: ${task.name}, isTracking: $isTracking")

                                        val updatedTask = if (isTracking) {
                                            // Start tracking
                                            task.copy(
                                                isTracking = true,
                                                trackingStartTime = System.currentTimeMillis()
                                            )
                                        } else {
                                            // Stop tracking and update total time
                                            val elapsedTime = System.currentTimeMillis() - task.trackingStartTime
                                            task.copy(
                                                isTracking = false,
                                                trackedTimeMillis = task.trackedTimeMillis + elapsedTime
                                            )
                                        }

                                        // Add more logging to verify the updated task
                                        Log.d("MainScreen", "Updated task: ${updatedTask.name}, isTracking: ${updatedTask.isTracking}, trackedTime: ${updatedTask.trackedTimeMillis}")

                                        // Call the callback to update the task
                                        onUpdateTask(updatedTask)
                                    },
                                    onCompletedChange = { isCompleted ->
                                        // Create updated task with new completed state
                                        val updatedTask = task.copy(completed = isCompleted)
                                        // Call the callback to update the task
                                        onUpdateTask(updatedTask)

                                        // --- Additions for completion feedback ---
                                        if (isCompleted) {
                                            // Show popup
                                            completedTaskName = task.name // Store name for popup
                                            showTaskCompletionPopup = true
                                            // Show toast using the context obtained within this scope
                                            Toast.makeText(context, "Nice Job!", Toast.LENGTH_SHORT).show()
                                        }
                                        // --- End additions ---
                                    },
                                    currentTimeMillis = tickerState, // Pass current time for live updates
                                    onUpdateSubtasks = { updatedTask ->
                                        // Call the callback to update the task
                                        onUpdateTask(updatedTask)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("Create New Task") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()), // Add scroll modifier
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Task Name
                    OutlinedTextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        label = { Text("Task Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary, 
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary, 
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Description
                    OutlinedTextField(
                        value = newTaskDescription,
                        onValueChange = { newTaskDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary, 
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary, 
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Date Picker Trigger
                    val dateFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date: ", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormatter.format(newTaskDate.time),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true }
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(4.dp)) // Add border
                                .padding(vertical = 8.dp, horizontal = 12.dp), // Add padding
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Priority Selection
                    Text("Priority:", color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PriorityOption(
                            priority = TaskPriority.HIGH,
                            selected = newTaskPriority == TaskPriority.HIGH,
                            onClick = { newTaskPriority = TaskPriority.HIGH }
                        )
                        PriorityOption(
                            priority = TaskPriority.MEDIUM,
                            selected = newTaskPriority == TaskPriority.MEDIUM,
                            onClick = { newTaskPriority = TaskPriority.MEDIUM }
                        )
                        PriorityOption(
                            priority = TaskPriority.LOW,
                            selected = newTaskPriority == TaskPriority.LOW,
                            onClick = { newTaskPriority = TaskPriority.LOW }
                        )
                        PriorityOption(
                            priority = TaskPriority.VERY_LOW,
                            selected = newTaskPriority == TaskPriority.VERY_LOW,
                            onClick = { newTaskPriority = TaskPriority.VERY_LOW }
                        )
                    }

                    // List Selection Dropdown
                    Text("List:", color = MaterialTheme.colorScheme.onSurface)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newTaskList ?: "None", // Display selected list or "None"
                            onValueChange = { }, // Read-only
                            readOnly = true,
                            label = { Text("Select List (Optional)") },
                            trailingIcon = {
                                IconButton(onClick = { isListDropdownExpanded = !isListDropdownExpanded }) {
                                    Icon(
                                        if (isListDropdownExpanded)
                                            Icons.Filled.KeyboardArrowUp
                                        else
                                            Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Toggle List Dropdown"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isListDropdownExpanded = true }, // Open dropdown on click
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                // Make it look read-only
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = false // Disable direct text input
                        )

                        DropdownMenu(
                            expanded = isListDropdownExpanded,
                            onDismissRequest = { isListDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f) // Adjust width as needed
                        ) {
                            // "None" option
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    newTaskList = null
                                    isListDropdownExpanded = false
                                }
                            )

                            // Options from listNames
                            if (listNames.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "No lists available. Create lists first.",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = { },
                                    enabled = false
                                )
                            } else {
                                listNames.forEach { listName ->
                                    DropdownMenuItem(
                                        text = { Text(listName) },
                                        onClick = {
                                            newTaskList = listName
                                            isListDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Attachment buttons
                    Text("Attachments:", color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Image button
                        Button(
                            onClick = { 
                                imagePickerLauncher.launch("image/*") 
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add, 
                                    contentDescription = "Add Image",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Image")
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // File button
                        Button(
                            onClick = { 
                                filePickerLauncher.launch(arrayOf("*/*"))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Add File",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("File")
                            }
                        }
                    }

// Show attachment status
                    Column(  // Changed from Row to Column
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        val currentImagePaths = newTaskImagePaths
                        val currentFilePaths = newTaskFilePaths

                        // Display images
                        if (currentImagePaths.isNotEmpty()) {
                            Text(
                                text = "Attached Images:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            
                            // List all images with individual delete buttons
                            currentImagePaths.forEachIndexed { index, path ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        // Image thumbnail if possible
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(path)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Image Thumbnail",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = path.substringAfterLast('/'),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                // Simplified approach: just remove the item at this index
                                                val updatedPaths = currentImagePaths.toMutableList()
                                                updatedPaths.removeAt(index)
                                                newTaskImagePaths = updatedPaths
                                                Log.d("EditTask", "Removed image at index $index, remaining: ${updatedPaths.size}")
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Remove Image",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Add "Remove All" button if there are multiple images
                            if (currentImagePaths.size > 1) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    TextButton(
                                        onClick = { 
                                            Log.d("EditTask", "Clearing all images")
                                            newTaskImagePaths = emptyList()
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Remove All Images",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Remove All",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        // Display files
                        if (currentFilePaths.isNotEmpty()) {
                            Text(
                                text = "Attached Files:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            
                            // List all files with individual delete buttons
                            currentFilePaths.forEachIndexed { index, path ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Description,
                                            contentDescription = "File Attachment",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = path.substringAfterLast('/'),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                // Simplified approach: just remove the item at this index
                                                val updatedPaths = currentFilePaths.toMutableList()
                                                updatedPaths.removeAt(index)
                                                newTaskFilePaths = updatedPaths
                                                Log.d("EditTask", "Removed file at index $index, remaining: ${updatedPaths.size}")
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Remove File",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Add "Remove All" button if there are multiple files
                            if (currentFilePaths.size > 1) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    TextButton(
                                        onClick = { 
                                            Log.d("EditTask", "Clearing all files")
                                            newTaskFilePaths = emptyList()
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Remove All Files",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Remove All",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTaskName.isNotBlank()) {
                            // Create Task object (without ID initially)
                            val newTask = Task(
                                name = newTaskName.trim(),
                                description = newTaskDescription.trim(),
                                dueDate = newTaskDate.clone() as Calendar, // Clone to avoid mutation issues
                                priority = newTaskPriority,
                                list = newTaskList,
                                imagePaths = newTaskImagePaths,  // Changed from single path to list
                                filePaths = newTaskFilePaths    // Changed from single path to list
                            )
                            onAddTask(newTask) // Call the callback to handle DB insertion
                            showAddTaskDialog = false
                            // Reset attachment paths for next time
                            newTaskImagePaths = emptyList()
                            newTaskFilePaths = emptyList()
                        }
                    },
                    enabled = newTaskName.isNotBlank(), // Basic validation
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddTaskDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Cancel") }
            }
        )
    }

    // Edit Task Dialog (similar to Add Task Dialog but with pre-filled data)
    if (showEditTaskDialog && taskToEdit != null) {
        // Add state for time editing
        var editedTimeString by remember(taskToEdit) {
            mutableStateOf(formatTime(taskToEdit!!.trackedTimeMillis))
        }
        var timeError by remember { mutableStateOf<String?>(null) }
        
        // Instead of separate states, initialize newTaskImagePaths and newTaskFilePaths directly
        // This ensures we're working with the same variables in both Add and Edit dialogs
        LaunchedEffect(taskToEdit) {
            // Initialize attachment paths with the task's current paths when dialog opens
            newTaskImagePaths = taskToEdit!!.imagePaths
            newTaskFilePaths = taskToEdit!!.filePaths
            
            Log.d("EditTask", "Initializing with images: ${newTaskImagePaths.size}, files: ${newTaskFilePaths.size}")
        }

        AlertDialog(
            onDismissRequest = {
                Log.d("EditTask", "Dismissing dialog, resetting state")
                showEditTaskDialog = false
                taskToEdit = null // Resets editedImagePaths/editedFilePaths via remember key change
                // Reset temporary picker state used by Add/Edit dialogs
                newTaskImagePaths = emptyList()
                newTaskFilePaths = emptyList()
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("Edit Task") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()), // Add scroll modifier
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Task Name
                    OutlinedTextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        label = { Text("Task Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary, 
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary, 
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Description
                    OutlinedTextField(
                        value = newTaskDescription,
                        onValueChange = { newTaskDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary, 
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary, 
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Date Picker Trigger
                    val dateFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date: ", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormatter.format(newTaskDate.time),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true }
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(4.dp)) // Add border
                                .padding(vertical = 8.dp, horizontal = 12.dp), // Add padding
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Priority Selection
                    Text("Priority:", color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PriorityOption(
                            priority = TaskPriority.HIGH,
                            selected = newTaskPriority == TaskPriority.HIGH,
                            onClick = { newTaskPriority = TaskPriority.HIGH }
                        )
                        PriorityOption(
                            priority = TaskPriority.MEDIUM,
                            selected = newTaskPriority == TaskPriority.MEDIUM,
                            onClick = { newTaskPriority = TaskPriority.MEDIUM }
                        )
                        PriorityOption(
                            priority = TaskPriority.LOW,
                            selected = newTaskPriority == TaskPriority.LOW,
                            onClick = { newTaskPriority = TaskPriority.LOW }
                        )
                        PriorityOption(
                            priority = TaskPriority.VERY_LOW,
                            selected = newTaskPriority == TaskPriority.VERY_LOW,
                            onClick = { newTaskPriority = TaskPriority.VERY_LOW }
                        )
                    }

                    // List Selection Dropdown
                    Text("List:", color = MaterialTheme.colorScheme.onSurface)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newTaskList ?: "None", // Display selected list or "None"
                            onValueChange = { }, // Read-only
                            readOnly = true,
                            label = { Text("Select List (Optional)") },
                            trailingIcon = {
                                IconButton(onClick = { isListDropdownExpanded = !isListDropdownExpanded }) {
                                    Icon(
                                        if (isListDropdownExpanded)
                                            Icons.Filled.KeyboardArrowUp
                                        else
                                            Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Toggle List Dropdown"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isListDropdownExpanded = true }, // Open dropdown on click
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                // Make it look read-only
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = false // Disable direct text input
                        )

                        DropdownMenu(
                            expanded = isListDropdownExpanded,
                            onDismissRequest = { isListDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f) // Adjust width as needed
                        ) {
                            // "None" option
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    newTaskList = null
                                    isListDropdownExpanded = false
                                }
                            )

                            // Options from listNames
                            if (listNames.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "No lists available. Create lists first.",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = { },
                                    enabled = false
                                )
                            } else {
                                listNames.forEach { listName ->
                                    DropdownMenuItem(
                                        text = { Text(listName) },
                                        onClick = {
                                            newTaskList = listName
                                            isListDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Attachment buttons
                    Text("Attachments:", color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Image button
                        Button(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Image",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Image")
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // File button
                        Button(
                            onClick = {
                                filePickerLauncher.launch(arrayOf("*/*"))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings, // Using Settings icon for file
                                    contentDescription = "Add File",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("File")
                            }
                        }
                    }

                    // Show attachment status and remove buttons
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) { // Align Start
                        // Image attachment status and remove button
                        val currentImagePaths = newTaskImagePaths ?: taskToEdit!!.imagePaths // Determine currently active paths
                        if (currentImagePaths.isNotEmpty()) {
                            Text(
                                "Attached Images:", 
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            
                            // List each image with its own delete button
                            currentImagePaths.forEachIndexed { index, path ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        // Image thumbnail if possible
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(path)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Image Thumbnail",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = path.substringAfterLast('/'),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                // Simplified approach: just remove the item at this index
                                                val updatedPaths = currentImagePaths.toMutableList()
                                                updatedPaths.removeAt(index)
                                                newTaskImagePaths = updatedPaths
                                                Log.d("EditTask", "Removed image at index $index, remaining: ${updatedPaths.size}")
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Remove Image",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Add "Remove All" button if there are multiple images
                            if (currentImagePaths.size > 1) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    TextButton(
                                        onClick = { 
                                            Log.d("EditTask", "Clearing all images")
                                            newTaskImagePaths = emptyList()
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Remove All Images",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Remove All",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        // File attachment status and remove button
                        val currentFilePaths = newTaskFilePaths ?: taskToEdit!!.filePaths // Determine currently active paths
                        if (currentFilePaths.isNotEmpty()) {
                            Text(
                                "Attached Files:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            
                            // List each file with its own delete button
                            currentFilePaths.forEachIndexed { index, path ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Description,
                                            contentDescription = "File Attachment",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = path.substringAfterLast('/'),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                // Simplified approach: just remove the item at this index
                                                val updatedPaths = currentFilePaths.toMutableList()
                                                updatedPaths.removeAt(index)
                                                newTaskFilePaths = updatedPaths
                                                Log.d("EditTask", "Removed file at index $index, remaining: ${updatedPaths.size}")
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Remove File",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Add "Remove All" button if there are multiple files
                            if (currentFilePaths.size > 1) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    TextButton(
                                        onClick = { 
                                            Log.d("EditTask", "Clearing all files")
                                            newTaskFilePaths = emptyList()
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Remove All Files",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Remove All",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // After list selection dropdown, add time editor
                    Text("Tracked Time:", color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(
                        value = editedTimeString,
                        onValueChange = { input ->
                            editedTimeString = input
                            // Validate time format (HH:MM:SS)
                            timeError = if (input.matches(Regex("^\\d{2}:\\d{2}:\\d{2}$"))) {
                                null
                            } else {
                                "Use format: 00:00:00 (hours:minutes:seconds)"
                            }
                        },
                        label = { Text("Time (HH:MM:SS)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = timeError != null,
                        supportingText = { timeError?.let { Text(it) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, 
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary, 
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary, 
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTaskName.isNotBlank() && taskToEdit != null && timeError == null) {
                            // Parse the edited time string to milliseconds
                            val timeMillis = parseTimeToMillis(editedTimeString)

                            Log.d("EditTask", "Saving task with imagePaths: ${newTaskImagePaths.size}, filePaths: ${newTaskFilePaths.size}")
                            
                            // Create updated Task object with the same ID
                            val updatedTask = Task(
                                id = taskToEdit!!.id, // Keep the same ID
                                name = newTaskName.trim(),
                                description = newTaskDescription.trim(),
                                dueDate = newTaskDate.clone() as Calendar,
                                priority = newTaskPriority,
                                list = newTaskList,
                                trackedTimeMillis = timeMillis, // Use the edited time
                                isTracking = taskToEdit!!.isTracking,
                                trackingStartTime = taskToEdit!!.trackingStartTime,
                                completed = taskToEdit!!.completed,
                                imagePaths = newTaskImagePaths, // Use new images if selected, otherwise use edited (which might be null now)
                                filePaths = newTaskFilePaths    // Use new files if selected, otherwise use edited (which might be null now)
                            )

                            // Delete the old task and add the updated one
                            onDeleteTask(taskToEdit!!)
                            onAddTask(updatedTask)

                            showEditTaskDialog = false
                            taskToEdit = null
                            // Reset temporary picker state
                            newTaskImagePaths = emptyList()
                            newTaskFilePaths = emptyList()
                        }
                    },
                    enabled = newTaskName.isNotBlank() && timeError == null,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        Log.d("EditTask", "Cancel button clicked, resetting state")
                        showEditTaskDialog = false
                        taskToEdit = null // Resets editedImagePaths/editedFilePaths
                        // Reset temporary picker state
                        newTaskImagePaths = emptyList()
                        newTaskFilePaths = emptyList()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && taskToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                taskToDelete = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete the task '${taskToDelete?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToDelete?.let { task ->
                            onDeleteTask(task) // Call the callback
                        }
                        showDeleteConfirmDialog = false
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Cancel") }
            }
        )
    }

    // Date Picker Dialog (ensure it updates the correct state variable if needed,
    // though it seems to update newTaskDate which is fine)
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = newTaskDate, // This is correct for the dialog's purpose
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { selectedCal ->
                newTaskDate = selectedCal // Update the dialog's date state
                showDatePicker = false
            }
        )
    }

    // --- Add Task Completion Popup ---
    if (showTaskCompletionPopup) {
        AlertDialog(
            onDismissRequest = { showTaskCompletionPopup = false; completedTaskName = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Well Done!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GifImage( // Reuse the GifImage composable
                        modifier = Modifier.size(200.dp),
                        drawableResId = R.drawable.nice // Use nice.gif instead of well.gif
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "You've completed the task!", // Keep this text
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    // Remove the section displaying the task name
                    /*
                    completedTaskName?.let { name ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    */
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTaskCompletionPopup = false; completedTaskName = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Continue")
                }
            }
        )
    }
    // --- End Task Completion Popup ---
}

// --- Pomodoro Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavHostController,
    tasks: List<Task>,
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    isSameDay: (Calendar, Calendar) -> Boolean,
    onUpdateTask: (Task) -> Unit
) {
    // --- Pomodoro State ---
    var workDurationMinutes by rememberSaveable { mutableIntStateOf(25) }
    var shortBreakDurationMinutes by rememberSaveable { mutableIntStateOf(5) }
    var longBreakDurationMinutes by rememberSaveable { mutableIntStateOf(15) }
    val cyclesBeforeLongBreak = 4

    var pomodoroState by remember { mutableStateOf(PomodoroMode.Idle) }
    var timeLeftInMillis by rememberSaveable { mutableStateOf(workDurationMinutes * 60 * 1000L) }
    var timerRunning by rememberSaveable { mutableStateOf(false) }
    var cycleCount by rememberSaveable { mutableIntStateOf(0) } // Completed work cycles

    var selectedTask by remember { mutableStateOf<Task?>(null) }

    // Add state for completion popup
    var showCompletionPopup by remember { mutableStateOf(false) }

    // --- Calendar Collapse State ---
    var isCalendarExpanded by remember { mutableStateOf(false) } // Changed to false for collapsed by default

    // --- Settings Dialog State ---
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Filter tasks for the selected date
    val tasksForSelectedDate = remember(tasks, selectedDate) {
        tasks
            .filter { task -> isSameDay(task.dueDate, selectedDate) && !task.completed }
            .sortedBy { it.name }
    }

    // --- Timer Logic ---
    LaunchedEffect(timerRunning, timeLeftInMillis) {
        if (timerRunning && timeLeftInMillis > 0) {
            delay(1000L) // Wait for 1 second
            timeLeftInMillis -= 1000L
        } else if (timerRunning && timeLeftInMillis <= 0) {
            // Timer finished
            timerRunning = false

            // Determine next state based on current state
            pomodoroState = when (pomodoroState) {
                PomodoroMode.Work -> {
                    // After work session, increment cycle count and update task time
                    if (selectedTask != null) {
                        val task = selectedTask!!
                        val updatedTask = task.copy(
                            trackedTimeMillis = task.trackedTimeMillis + (workDurationMinutes * 60 * 1000L)
                        )
                        onUpdateTask(updatedTask)
                        selectedTask = updatedTask

                        // Show completion popup after work session
                        showCompletionPopup = true
                    }

                    // After work, check if we need a long break
                    if ((cycleCount + 1) % cyclesBeforeLongBreak == 0) {
                        // This was the 4th work session, go to long break
                        PomodoroMode.LongBreak
                    } else {
                        // This was work session 1, 2, or 3, go to short break
                        PomodoroMode.ShortBreak
                    }
                }
                PomodoroMode.ShortBreak -> {
                    // After short break, go to next work session and increment cycle
                    cycleCount++
                    PomodoroMode.Work
                }
                PomodoroMode.LongBreak -> {
                    // After long break, reset to first work session of next set
                    cycleCount++
                    PomodoroMode.Work
                }
                PomodoroMode.Idle -> PomodoroMode.Work
            }

            // Set time for the next state
            timeLeftInMillis = when (pomodoroState) {
                PomodoroMode.Work -> workDurationMinutes * 60 * 1000L
                PomodoroMode.ShortBreak -> shortBreakDurationMinutes * 60 * 1000L
                PomodoroMode.LongBreak -> longBreakDurationMinutes * 60 * 1000L
                PomodoroMode.Idle -> workDurationMinutes * 60 * 1000L
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pomodoro Timer",
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
                actions = {
                    // Settings button
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Timer Settings"
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Calendar Toggle and Display Area
            Column {
                // Row for the "Selected Date" text and the toggle button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // --- Date Display Logic ---
                    // Gregorian Date Formatting
                    val gregorianDateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
                    val gregorianDateString = gregorianDateFormat.format(selectedDate.time)

                    // Combined Date Text
                    Text(
                        text = gregorianDateString,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // --- End Date Display Logic ---

                    // Toggle Button
                    IconButton(onClick = { isCalendarExpanded = !isCalendarExpanded }) {
                        Icon(
                            imageVector = if (isCalendarExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isCalendarExpanded) "Collapse Calendar" else "Expand Calendar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // AnimatedVisibility for the Calendar
                AnimatedVisibility(visible = isCalendarExpanded) {
                    WeekCalendar(
                        selectedDate = selectedDate,
                        onDateSelected = { newDate ->
                            // Reset timer if date changes
                            timerRunning = false
                            pomodoroState = PomodoroMode.Idle
                            cycleCount = 0
                            timeLeftInMillis = workDurationMinutes * 60 * 1000L
                            selectedTask = null // Deselect task
                            onDateSelected(newDate) // Call the callback to update the shared state
                        },
                        tasks = tasks // Pass all tasks for dot indicators
                    )
                }
            } // End Calendar Section

            Spacer(modifier = Modifier.height(16.dp))

            // Task Selection Section
            Text(
                text = "Select a Task to Track",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Task Selection Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (tasksForSelectedDate.isNotEmpty()) {
                    items(tasksForSelectedDate) { task ->
                        TaskSelectionChip(
                            task = task,
                            isSelected = selectedTask?.id == task.id,
                            onSelect = { selectedTask = task }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "No tasks for this date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timer Display
            PomodoroTimerDisplay(
                timeLeftInMillis = timeLeftInMillis,
                pomodoroState = pomodoroState,
                cycleCount = cycleCount,
                timerRunning = timerRunning // Add timerRunning parameter
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Controls
            PomodoroControls(
                timerRunning = timerRunning,
                pomodoroState = pomodoroState,
                hasSelectedTask = selectedTask != null,
                onStartPause = {
                    if (pomodoroState == PomodoroMode.Idle && !timerRunning) {
                        // If starting from idle, set to work mode
                        pomodoroState = PomodoroMode.Work
                        timeLeftInMillis = workDurationMinutes * 60 * 1000L
                    }
                    timerRunning = !timerRunning
                },
                onReset = {
                    timerRunning = false
                    pomodoroState = PomodoroMode.Idle
                    cycleCount = 0
                    timeLeftInMillis = workDurationMinutes * 60 * 1000L
                },
                onSkip = {
                    timerRunning = false

                    // Determine next state based on current state
                    pomodoroState = when (pomodoroState) {
                        PomodoroMode.Work -> {
                            // If skipping work mode, add partial time to task
                            if (selectedTask != null) {
                                val task = selectedTask!!
                                val elapsedTime = (workDurationMinutes * 60 * 1000L) - timeLeftInMillis
                                if (elapsedTime > 0) {
                                    // Only update if some time was spent
                                    val updatedTask = task.copy(
                                        trackedTimeMillis = task.trackedTimeMillis + elapsedTime
                                    )
                                    onUpdateTask(updatedTask)
                                    selectedTask = updatedTask
                                }
                            }

                            // After work, check if we need a long break
                            if ((cycleCount + 1) % cyclesBeforeLongBreak == 0) {
                                // This was the 4th work session, go to long break
                                PomodoroMode.LongBreak
                            } else {
                                // This was work session 1, 2, or 3, go to short break
                                PomodoroMode.ShortBreak
                            }
                        }
                        PomodoroMode.ShortBreak -> {
                            // After short break, go to next work session and increment cycle
                            cycleCount++
                            PomodoroMode.Work
                        }
                        PomodoroMode.LongBreak -> {
                            // After long break, reset to first work session of next set
                            cycleCount++
                            PomodoroMode.Work
                        }
                        PomodoroMode.Idle -> PomodoroMode.Work
                    }

                    // Set time for the next state
                    timeLeftInMillis = when (pomodoroState) {
                        PomodoroMode.Work -> workDurationMinutes * 60 * 1000L
                        PomodoroMode.ShortBreak -> shortBreakDurationMinutes * 60 * 1000L
                        PomodoroMode.LongBreak -> longBreakDurationMinutes * 60 * 1000L
                        PomodoroMode.Idle -> workDurationMinutes * 60 * 1000L
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        PomodoroSettingsDialog(
            workDuration = workDurationMinutes,
            shortBreakDuration = shortBreakDurationMinutes,
            longBreakDuration = longBreakDurationMinutes,
            onDismiss = { showSettingsDialog = false },
            onSave = { work, shortBreak, longBreak ->
                // Only update if timer is not running to avoid disrupting active sessions
                if (!timerRunning) {
                    workDurationMinutes = work
                    shortBreakDurationMinutes = shortBreak
                    longBreakDurationMinutes = longBreak

                    // Update current timer if in idle state
                    if (pomodoroState == PomodoroMode.Idle) {
                        timeLeftInMillis = workDurationMinutes * 60 * 1000L
                    }
                }
                showSettingsDialog = false
            }
        )
    }

    // Completion Popup
    if (showCompletionPopup) {
        AlertDialog(
            onDismissRequest = { showCompletionPopup = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Well Done!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GifImage(
                        modifier = Modifier.size(200.dp),
                        drawableResId = R.drawable.well // Use well.gif
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "You've completed a work session!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    selectedTask?.let { task ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Task: ${task.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showCompletionPopup = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Continue")
                }
            }
        )
    }
}

@Composable
fun PomodoroSettingsDialog(
    workDuration: Int,
    shortBreakDuration: Int,
    longBreakDuration: Int,
    onDismiss: () -> Unit,
    onSave: (workMinutes: Int, shortBreakMinutes: Int, longBreakMinutes: Int) -> Unit
) {
    var workMinutes by remember { mutableIntStateOf(workDuration) }
    var shortBreakMinutes by remember { mutableIntStateOf(shortBreakDuration) }
    var longBreakMinutes by remember { mutableIntStateOf(longBreakDuration) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pomodoro Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Work Duration Slider
                Column {
                    Text(
                        text = "Work Duration: $workMinutes minutes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = workMinutes.toFloat(),
                        onValueChange = { workMinutes = it.toInt() },
                        valueRange = 1f..60f, // Changed from 5f to 1f to allow 1 minute minimum
                        steps = 59, // (60-1) = 59 steps for 1-minute increments
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Short Break Duration Slider
                Column {
                    Text(
                        text = "Short Break: $shortBreakMinutes minutes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = shortBreakMinutes.toFloat(),
                        onValueChange = { shortBreakMinutes = it.toInt() },
                        valueRange = 1f..15f,
                        steps = 14, // (15-1)/1 = 14 steps
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Long Break Duration Slider
                Column {
                    Text(
                        text = "Long Break: $longBreakMinutes minutes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = longBreakMinutes.toFloat(),
                        onValueChange = { longBreakMinutes = it.toInt() },
                        valueRange = 5f..30f,
                        steps = 5, // (30-5)/5 = 5 steps
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(workMinutes, shortBreakMinutes, longBreakMinutes) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

// --- Pomodoro Related Composables ---

enum class PomodoroMode {
    Idle, Work, ShortBreak, LongBreak
}

@Composable
fun TaskSelectionChip(
    task: Task,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else Color.White.copy(alpha = if (task.isTracking) 1f else 0.5f),
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary
            else Color.Black.copy(alpha = if (task.isTracking) 1f else 0.5f)
        )
    ) {
        Text(
            text = task.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun PomodoroTimerDisplay(
    timeLeftInMillis: Long,
    pomodoroState: PomodoroMode,
    cycleCount: Int,
    timerRunning: Boolean // Add timerRunning parameter
) {
    val minutes = (timeLeftInMillis / 1000 / 60).toInt()
    val seconds = (timeLeftInMillis / 1000 % 60).toInt()
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val modeText = when (pomodoroState) {
        PomodoroMode.Idle -> "" // Return empty string when Idle
        PomodoroMode.Work -> "Work"
        PomodoroMode.ShortBreak -> "Short Break"
        PomodoroMode.LongBreak -> "Long Break"
    }
    val modeColor = when (pomodoroState) {
        PomodoroMode.Work -> MaterialTheme.colorScheme.error
        PomodoroMode.ShortBreak, PomodoroMode.LongBreak -> MaterialTheme.colorScheme.primary
        PomodoroMode.Idle -> Color.Gray // Keep color logic, though text is empty
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Display crowi.gif when idle, time.gif when running, or pause icon when paused
        Box( // Use a Box to maintain consistent layout height
            modifier = Modifier
                .size(220.dp)
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Show crowi.gif when idle
                pomodoroState == PomodoroMode.Idle -> {
                    GifImage(
                        modifier = Modifier.fillMaxSize(),
                        drawableResId = R.drawable.crowi // Show crowi GIF
                    )
                }
                // Show time.gif when timer is running (and not idle)
                timerRunning -> {
                    GifImage(
                        modifier = Modifier.fillMaxSize(),
                        drawableResId = R.drawable.time // Show time GIF
                    )
                }
                // Show pause icon when timer is paused (and not idle)
                else -> {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = "Timer Paused",
                        tint = MaterialTheme.colorScheme.primary, // Use primary color for pause icon
                        modifier = Modifier.size(130.dp) // Adjust size as needed
                    )
                }
            }
        }

        Text(
            text = modeText,
            style = MaterialTheme.typography.headlineSmall,
            color = modeColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = timeFormatted,
            fontSize = 72.sp, // Larger font for timer
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Cycle ${cycleCount + 1}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// Add GifImage composable to display the GIF animation
@Composable
fun GifImage(
    modifier: Modifier = Modifier,
    drawableResId: Int // Add parameter for the drawable resource ID
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(drawableResId) // Use the passed drawableResId
            .crossfade(true)
            .build(),
        contentDescription = "Timer Animation",
        modifier = modifier.size(180.dp),  // Increased from 120.dp to 180.dp
        imageLoader = imageLoader,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun PomodoroControls(
    timerRunning: Boolean,
    pomodoroState: PomodoroMode,
    hasSelectedTask: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset Button
        TextButton(onClick = onReset, enabled = hasSelectedTask) {
            Text("Reset", color = if (hasSelectedTask) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
        }

        // Start/Pause Button (Large FAB style)
        if (hasSelectedTask) {
            FloatingActionButton(
                onClick = onStartPause,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(72.dp) // Make it larger
            ) {
                Icon(
                    imageVector = if (timerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (timerRunning) "Pause Timer" else "Start Timer",
                    modifier = Modifier.size(36.dp) // Larger icon
                )
            }
        } else {
            // Disabled version (gray button that doesn't respond to clicks)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.onBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Start Timer (Disabled)",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Skip Button
        TextButton(onClick = onSkip, enabled = hasSelectedTask && !timerRunning) {
            Text("Skip", color = if (hasSelectedTask && !timerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
        }
    }
}

// Task data class - Add ID
data class Task(
    val id: Int = 0,
    val name: String,
    val description: String,
    val dueDate: Calendar,
    val priority: TaskPriority,
    val list: String? = null,
    val isTracking: Boolean = false,
    val trackedTimeMillis: Long = 0,
    val trackingStartTime: Long = 0,
    val completed: Boolean = false,
    val imagePaths: List<String> = emptyList(),  // Changed from single path to list
    val filePaths: List<String> = emptyList(),    // Changed from single path to list
    val subtasks: List<String> = emptyList(),     // Added subtasks as a list of strings
    val completedSubtasks: List<Int> = emptyList() // Added to track which subtasks are completed by their index
)

// Task priority enum
enum class TaskPriority(val color: Color, val label: String) {
    HIGH(Color.Red, "High"),
    MEDIUM(Color(0xFFFF9800), "Medium"),  // Orange
    LOW(Color.Blue, "Low"),
    VERY_LOW(Color.Gray, "Very Low")
}

// Priority option composable
@Composable
fun PriorityOption(
    priority: TaskPriority,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (selected) priority.color.copy(alpha = 0.2f) else Color.Transparent,
                    shape = CircleShape
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(priority.color, CircleShape)
            )
        }
        Text(
            text = priority.label,
            fontSize = 12.sp,
            color = if (selected) priority.color else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Task card with rounded corners - Updated with better padding
@Composable
fun TaskCard(
    task: Task,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onTrackingToggle: (Boolean) -> Unit = {},
    onCompletedChange: (Boolean) -> Unit = {},
    currentTimeMillis: Long = 0,
    onUpdateSubtasks: (Task) -> Unit = { _ -> }
) {
    val dateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val context = LocalContext.current
    // Add missing variable to store current download source path
    var currentDownloadSource by remember { mutableStateOf<String?>(null) }
    
    // Variables for subtask dialog
    var showSubtaskDialog by remember { mutableStateOf(false) }
    var subtaskName by remember { mutableStateOf("") }
    var subtasksExpanded by remember { mutableStateOf(false) }
    
    // Variables for subtask deletion confirmation
    var showDeleteSubtaskDialog by remember { mutableStateOf(false) }
    var subtaskToDelete by remember { mutableStateOf<Pair<Int, String>?>(null) }
    
    // Variables for subtask editing
    var showEditSubtaskDialog by remember { mutableStateOf(false) }
    var subtaskToEdit by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var editedSubtaskName by remember { mutableStateOf("") }

    // Use StartActivityForResult to have more control over the save intent
    val downloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { destinationUri ->
                // Fix: Use currentDownloadSource which contains the selected file path
                if (currentDownloadSource != null) {
                    handleDownload(context, currentDownloadSource!!, destinationUri)
                } else {
                    // Fallback to the first attachment if no specific file was selected
                    val sourcePath = task.imagePaths.firstOrNull() ?: task.filePaths.firstOrNull()
                    if (sourcePath != null) {
                        handleDownload(context, sourcePath, destinationUri)
                    } else {
                        Toast.makeText(context, "No attachment source found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Create a separate composable for the timer to ensure it recomposes independently
    @Composable
    fun TaskTimer(task: Task, currentTimeMillis: Long) {
        // The currentTimeMillis parameter is used to force recomposition
        val displayTime = if (task.isTracking) {
            val activeTime = System.currentTimeMillis() - task.trackingStartTime
            formatTime(task.trackedTimeMillis + activeTime)
        } else {
            formatTime(task.trackedTimeMillis)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = "Tracked Time",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayTime,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Add Subtask Dialog
    if (showSubtaskDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSubtaskDialog = false
                subtaskName = ""
            },
            title = { Text("Add Subtask") },
            text = {
                Column {
                    OutlinedTextField(
                        value = subtaskName,
                        onValueChange = { subtaskName = it },
                        label = { Text("Subtask Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (subtaskName.isNotBlank()) {
                            // Create updated task with new subtask added
                            val updatedSubtasks = task.subtasks + subtaskName
                            val updatedTask = task.copy(subtasks = updatedSubtasks)
                            onUpdateSubtasks(updatedTask)
                            subtaskName = ""
                            showSubtaskDialog = false
                            // Auto-expand the subtasks section when a new one is added
                            subtasksExpanded = true
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSubtaskDialog = false
                    subtaskName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Subtask Confirmation Dialog
    if (showDeleteSubtaskDialog && subtaskToDelete != null) {
        val (index, name) = subtaskToDelete!!
        AlertDialog(
            onDismissRequest = { 
                showDeleteSubtaskDialog = false 
                subtaskToDelete = null
            },
            title = { Text("Delete Subtask") },
            text = { Text("Are you sure you want to delete the subtask \"$name\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Remove this subtask
                        val updatedSubtasks = task.subtasks.toMutableList().apply {
                            removeAt(index)
                        }
                        
                        // Update the completedSubtasks list to account for the removed subtask
                        // We need to:
                        // 1. Remove the index of the deleted subtask if it was completed
                        // 2. Decrement indices greater than the deleted index
                        val updatedCompletedSubtasks = task.completedSubtasks
                            .filter { it != index } // Remove this index if it exists
                            .map { if (it > index) it - 1 else it } // Decrement higher indices
                        
                        val updatedTask = task.copy(
                            subtasks = updatedSubtasks,
                            completedSubtasks = updatedCompletedSubtasks
                        )
                        onUpdateSubtasks(updatedTask)
                        showDeleteSubtaskDialog = false
                        subtaskToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteSubtaskDialog = false 
                        subtaskToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Edit Subtask Dialog
    if (showEditSubtaskDialog && subtaskToEdit != null) {
        val (index, currentName) = subtaskToEdit!!
        
        AlertDialog(
            onDismissRequest = { 
                showEditSubtaskDialog = false 
                subtaskToEdit = null 
            },
            title = { Text("Edit Subtask") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedSubtaskName,
                        onValueChange = { editedSubtaskName = it },
                        label = { Text("Subtask Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editedSubtaskName.isNotBlank()) {
                            // Update the subtask name
                            val updatedSubtasks = task.subtasks.toMutableList().apply {
                                set(index, editedSubtaskName)
                            }
                            // The completedSubtasks list doesn't need to change
                            // since we're only updating the name, not the index
                            val updatedTask = task.copy(subtasks = updatedSubtasks)
                            onUpdateSubtasks(updatedTask)
                            showEditSubtaskDialog = false
                            subtaskToEdit = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showEditSubtaskDialog = false 
                        subtaskToEdit = null 
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else
                task.priority.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Add checkbox
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { isChecked ->
                        onCompletedChange(isChecked)
                    },
                    modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                )

                // Priority indicator
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(12.dp)
                        .background(task.priority.color, CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Task details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (task.completed) 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else null
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Display tracked time below description
                    if (task.trackedTimeMillis > 0 || task.isTracking) {
                        Spacer(modifier = Modifier.height(6.dp))
                        // Use the separate timer composable that will recompose independently
                        TaskTimer(task = task, currentTimeMillis = currentTimeMillis)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        
                        // Show list name if available
                        task.list?.let { listName ->
                            Text(
                                text = listName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Play/Pause button with improved clickability
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (task.isTracking) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                                else 
                                    Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                Log.d("TaskCard", "Play/Pause clicked directly for task: ${task.name}, current tracking: ${task.isTracking}")
                                onTrackingToggle(!task.isTracking)
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (task.isTracking) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (task.isTracking) "Stop Tracking" else "Start Tracking",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Add three-dot menu
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                    IconButton(
                            onClick = { showMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Task") },
                                onClick = {
                                    onDeleteClick()
                                    showMenu = false
                                },
                                leadingIcon = {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete Task",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Subtask") },
                                onClick = {
                                    showMenu = false
                                    showSubtaskDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.AddCircleOutline,
                                        contentDescription = "Add Subtask",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Subtasks section (similar pattern to attachments)
            if (task.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Collapsible header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { subtasksExpanded = !subtasksExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (subtasksExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (subtasksExpanded) "Collapse Subtasks" else "Expand Subtasks",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Subtasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Subtasks content
                    AnimatedVisibility(visible = subtasksExpanded) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            task.subtasks.forEachIndexed { index, subtask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, top = 4.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Check if this subtask's index is in the completed list
                                    val isSubtaskCompleted = index in task.completedSubtasks
                                    val subtaskChecked = remember { mutableStateOf(isSubtaskCompleted) }
                                    
                                    Checkbox(
                                        checked = subtaskChecked.value,
                                        onCheckedChange = { isChecked ->
                                            subtaskChecked.value = isChecked
                                            
                                            // Update the task with the new completed subtasks list
                                            val updatedCompletedSubtasks = if (isChecked) {
                                                // Add this index if not already in the list
                                                if (index in task.completedSubtasks) {
                                                    task.completedSubtasks
                                                } else {
                                                    task.completedSubtasks + index
                                                }
                                            } else {
                                                // Remove this index if it's in the list
                                                task.completedSubtasks.filter { it != index }
                                            }
                                            
                                            // Create updated task with the new completedSubtasks list
                                            val updatedTask = task.copy(completedSubtasks = updatedCompletedSubtasks)
                                            
                                            // Save the updated task
                                            onUpdateSubtasks(updatedTask)
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = subtask,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        // Apply strikethrough if the subtask is completed
                                        textDecoration = if (subtaskChecked.value) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                // Prepare for editing
                                                subtaskToEdit = Pair(index, subtask)
                                                editedSubtaskName = subtask
                                                showEditSubtaskDialog = true
                                            }
                                    )
                                    IconButton(
                                        onClick = {
                                            // Show confirmation dialog
                                            subtaskToDelete = Pair(index, subtask)
                                            showDeleteSubtaskDialog = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete Subtask",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val hasAttachments = task.imagePaths.isNotEmpty() || task.filePaths.isNotEmpty()
            var attachmentsExpanded by remember { mutableStateOf(false) }

            if (hasAttachments) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Collapsible header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { attachmentsExpanded = !attachmentsExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (attachmentsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (attachmentsExpanded) "Collapse Attachments" else "Expand Attachments",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Attachments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Attachments content
                    AnimatedVisibility(visible = attachmentsExpanded) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Images section
                            if (task.imagePaths.isNotEmpty()) {
                                Text(
                                    "Images:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(task.imagePaths) { path ->
                                        Box(contentAlignment = Alignment.TopEnd) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(path)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Attached Image",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { openImageFile(context, path) }
                                            )
                                            
                                            IconButton(
                                                onClick = {
                                                    val fileName = path.substringAfterLast('/')
                                                    val mimeType = getMimeType(fileName)

                                                    // Fix: Ensure we pass the correct file path to the download handler
                                                    currentDownloadSource = path
                                                    
                                                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                                        addCategory(Intent.CATEGORY_OPENABLE)
                                                        type = mimeType
                                                        putExtra(Intent.EXTRA_TITLE, fileName)
                                                    }
                                                    downloadLauncher.launch(intent)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "Download Image",
                                                    tint = Color.White,
                                                    modifier = Modifier
                                                        .background(
                                                            color = Color.Black.copy(alpha = 0.5f),
                                                            shape = CircleShape
                                                        )
                                                        .padding(4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Files section
                            if (task.filePaths.isNotEmpty()) {
                                Text(
                                    "Files:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    task.filePaths.forEach { path ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { openFile(context, path) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Description,
                                                    contentDescription = "File Attachment",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = path.substringAfterLast('/'),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        val fileName = path.substringAfterLast('/')
                                                        val mimeType = getMimeType(fileName)
                                                        
                                                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                                            addCategory(Intent.CATEGORY_OPENABLE)
                                                            type = mimeType
                                                            putExtra(Intent.EXTRA_TITLE, fileName)
                                                        }
                                                        downloadLauncher.launch(intent)
                                                        currentDownloadSource = path
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Download,
                                                        contentDescription = "Download File",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Add this helper function
private fun handleDownload(context: Context, sourcePath: String, destinationUri: Uri) {
    Log.d("Download", "Starting download. Source: $sourcePath, Destination: $destinationUri")
    
    try {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) {
            Toast.makeText(context, "Source file not found: $sourcePath", Toast.LENGTH_SHORT).show()
            return
        }
        
        Log.d("Download", "Source file exists: ${sourceFile.exists()}, size: ${sourceFile.length()}")
        
        // Determine the MIME type
        val mimeType = getMimeType(sourceFile.name)
        Log.d("Download", "File MIME type: $mimeType")
        
        // Open streams with correct mode
        context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
            FileInputStream(sourceFile).use { inputStream ->
                // Use a buffer for efficient copying
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesCopied: Long = 0
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesCopied += bytesRead
                }
                
                outputStream.flush()
                Log.d("Download", "Successfully copied $totalBytesCopied bytes")
                Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT).show()
            }
        } ?: throw IOException("Could not open output stream for destination URI")
    } catch (e: Exception) {
        Log.e("Download", "Error downloading file: ${e.message}", e)
        Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

// Date picker dialog
@Composable
fun DatePickerDialog(
    initialDate: Calendar,
    onDismissRequest: () -> Unit,
    onDateSelected: (Calendar) -> Unit
) {
    val selectedDate = remember { mutableStateOf(initialDate.clone() as Calendar) }
    val currentYear = selectedDate.value.get(Calendar.YEAR)
    val currentMonth = selectedDate.value.get(Calendar.MONTH)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("Select Date") },
        text = {
            Column {
                // Month and Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newDate = selectedDate.value.clone() as Calendar
                        newDate.add(Calendar.MONTH, -1)
                        selectedDate.value = newDate
                    }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                    }

                    val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    Text(
                        text = monthYearFormatter.format(selectedDate.value.time),
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = {
                        val newDate = selectedDate.value.clone() as Calendar
                        newDate.add(Calendar.MONTH, 1)
                        selectedDate.value = newDate
                    }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
                    }
                }

                // Days of week header
                val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Calendar grid
                val calendar = selectedDate.value.clone() as Calendar
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                val today = Calendar.getInstance()
                val selectedDay = selectedDate.value.get(Calendar.DAY_OF_MONTH)

                // Create calendar grid
                for (i in 0 until 6) { // Max 6 weeks in a month view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (j in 0 until 7) { // 7 days in a week
                            val day = i * 7 + j - firstDayOfMonth + 1

                            if (day in 1..daysInMonth) {
                                val isSelected = day == selectedDay
                                val isToday = today.get(Calendar.YEAR) == currentYear &&
                                        today.get(Calendar.MONTH) == currentMonth &&
                                        today.get(Calendar.DAY_OF_MONTH) == day

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            val newDate = selectedDate.value.clone() as Calendar
                                            newDate.set(Calendar.DAY_OF_MONTH, day)
                                            selectedDate.value = newDate
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            } else {
                                // Empty space for days outside current month
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDateSelected(selectedDate.value) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Cancel")
            }
        }
    )
}

val PinkHighlight = Color(0xFFFFC1E0)

@Composable
fun WeekCalendar(
    selectedDate: Calendar = Calendar.getInstance(),
    onDateSelected: (Calendar) -> Unit = {},
    tasks: List<Task> = emptyList() // Add tasks parameter to check for tasks on specific days
) {
    var currentCalendar by remember { mutableStateOf(selectedDate.clone() as Calendar) }
    var showMonthYearDialog by remember { mutableStateOf(false) }

    val today = Calendar.getInstance()

    val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayFormatter = SimpleDateFormat("d", Locale.getDefault())

    // Function to check if a date has tasks
    val hasTasksOnDate = { date: Calendar ->
        tasks.any { task ->
            date.get(Calendar.YEAR) == task.dueDate.get(Calendar.YEAR) &&
                    date.get(Calendar.MONTH) == task.dueDate.get(Calendar.MONTH) &&
                    date.get(Calendar.DAY_OF_MONTH) == task.dueDate.get(Calendar.DAY_OF_MONTH)
        }
    }

    val updateCalendar = { year: Int, month: Int ->
        val newCalendar = currentCalendar.clone() as Calendar
        newCalendar.set(Calendar.YEAR, year)
        newCalendar.set(Calendar.MONTH, month)
        newCalendar.set(Calendar.DAY_OF_MONTH, 1)
        currentCalendar = newCalendar
    }

    val changeMonth = { amount: Int ->
        val newCalendar = currentCalendar.clone() as Calendar
        newCalendar.add(Calendar.MONTH, amount)
        currentCalendar = newCalendar
    }

    val resetToToday = {
        currentCalendar = Calendar.getInstance()
        onDateSelected(Calendar.getInstance())
    }

    val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = currentCalendar.clone() as Calendar
    firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)

    // Get the correct day of week (1 = Sunday, 2 = Monday, etc.)
    val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)

    // Calculate empty cells correctly (0-based index)
    val emptyCellsBefore = startDayOfWeek - 1

    val calendarDays = List(emptyCellsBefore) { null } + (1..daysInMonth).toList()

    // Use standard day abbreviations that match Calendar.DAY_OF_WEEK
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { changeMonth(-1) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = resetToToday) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Go to Today"
                    )
                }

                Text(
                    text = monthYearFormatter.format(currentCalendar.time),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { showMonthYearDialog = true }
                        .padding(horizontal = 4.dp)
                )
            }

            IconButton(onClick = { changeMonth(1) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(30.dp),
            userScrollEnabled = false
        ) {
            items(daysOfWeek) { day ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            userScrollEnabled = false
        ) {
            items(calendarDays) { day ->
                if (day != null) {
                    val dayCalendar = currentCalendar.clone() as Calendar
                    dayCalendar.set(Calendar.DAY_OF_MONTH, day)

                    val isToday = today.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            today.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                            today.get(Calendar.DAY_OF_MONTH) == day

                    val isSelected = selectedDate.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            selectedDate.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                            selectedDate.get(Calendar.DAY_OF_MONTH) == day

                    // Check if this day has tasks
                    val hasTasks = hasTasksOnDate(dayCalendar)

                    val backgroundColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val textColor = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .clickable {
                                val clickedCalendar = currentCalendar.clone() as Calendar
                                clickedCalendar.set(Calendar.DAY_OF_MONTH, day)
                                onDateSelected(clickedCalendar)
                            }
                            .background(
                                color = backgroundColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Get the primary color outside of drawBehind
                            val primaryColor = MaterialTheme.colorScheme.primary

                            Text(
                                text = day.toString(),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = textColor,
                                modifier = if (hasTasks) {
                                    // Add blue underline for days with tasks using drawBehind
                                    Modifier.drawBehind {
                                        // Draw a line under the text using the captured color
                                        drawLine(
                                            color = primaryColor, // Use the captured color here
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 2f
                                        )
                                    }
                                } else Modifier
                            )
                            // Removed Jalali date text
                        }
                    }
                } else {
                    Box(modifier = Modifier.aspectRatio(1f).padding(2.dp))
                }
            }
        }
    }

    if (showMonthYearDialog) {
        MonthYearPickerDialog(
            initialCalendar = currentCalendar,
            onDismissRequest = { showMonthYearDialog = false },
            onConfirmation = { selectedYear, selectedMonth ->
                updateCalendar(selectedYear, selectedMonth)
                showMonthYearDialog = false
            }
        )
    }
}

@Composable
fun MonthYearPickerDialog(
    initialCalendar: Calendar,
    onDismissRequest: () -> Unit,
    onConfirmation: (year: Int, month: Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialCalendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(initialCalendar.get(Calendar.MONTH)) }

    val monthNames = remember { DateFormatSymbols(Locale.getDefault()).months }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearRange = remember { (currentYear - 10)..3000 }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("Select Month and Year") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectedYear > yearRange.first) selectedYear--
                    }, enabled = selectedYear > yearRange.first) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Year", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(selectedYear.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = {
                        if (selectedYear < yearRange.last) selectedYear++
                    }, enabled = selectedYear < yearRange.last) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Year", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Text("Month:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(monthNames.size -1) { monthIndex ->
                        if (monthNames[monthIndex].isNotEmpty()) {
                            val isSelected = monthIndex == selectedMonth
                            TextButton(
                                onClick = { selectedMonth = monthIndex },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = monthNames[monthIndex].take(3),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmation(selectedYear, selectedMonth) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Cancel")
            }
        }
    )
}

fun getJalaliDateString(gregorianCalendar: java.util.Calendar): String {
    try {
        val gregorianYear = gregorianCalendar.get(Calendar.YEAR)
        val gregorianMonth = gregorianCalendar.get(Calendar.MONTH) + 1
        val gregorianDay = gregorianCalendar.get(Calendar.DAY_OF_MONTH)

        val jalaliDate = gregorianToJalali(gregorianYear, gregorianMonth, gregorianDay)

        return String.format(Locale.getDefault(), "%02d/%02d", jalaliDate[2], jalaliDate[1])
    } catch (e: Exception) {
        e.printStackTrace()
        return "--/--"
    }
}

fun gregorianToJalali(gregorianYear: Int, gregorianMonth: Int, gregorianDay: Int): IntArray {
    val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    var jy = 0
    var jm: Int
    var jd: Int

    val gy = gregorianYear - 1600
    val gm = gregorianMonth - 1
    val gd = gregorianDay - 1

    var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400

    var i = 0
    while (i < gm) {
        gDayNo += gregorianDaysInMonth[i]
        i++
    }

    if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0)) || (gy % 400 == 0))
        gDayNo++

    gDayNo += gd

    var jDayNo = gDayNo - 79

    val jNp = jDayNo / 12053
    jDayNo %= 12053

    jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
    jDayNo %= 1461

    if (jDayNo >= 366) {
        jy += (jDayNo - 1) / 365
        jDayNo = (jDayNo - 1) % 365
    }

    i = 0
    while (i < 11 && jDayNo >= jalaliDaysInMonth[i]) {
        jDayNo -= jalaliDaysInMonth[i]
        i++
    }

    jm = i + 1
    jd = jDayNo + 1

    return intArrayOf(jy, jm, jd)
}

val gregorianDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

val jalaliDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    JitaTheme {
        // Preview needs dummy data or a way to mock the DAO/callbacks
        val dummyTasks = listOf(
            Task(1, "Task 1", "Desc 1", Calendar.getInstance(), TaskPriority.HIGH, "Work"),
            Task(2, "Task 2", "Desc 2", Calendar.getInstance(), TaskPriority.MEDIUM, null)
        )
        val dummyLists = listOf("Work", "Personal")
        MainScreen(
            navController = rememberNavController(),
            listNames = dummyLists,
            tasks = dummyTasks,
            selectedDate = Calendar.getInstance(),
            onDateSelected = {},
            isSameDay = { _, _ -> false },
            onAddTask = {},
            onDeleteTask = {},
            onUpdateTask = {}
        )
    }
}

// Improved time formatting function to ensure HH:MM:SS format
fun formatTime(timeMillis: Long): String {
    val totalSeconds = timeMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

// Add this function to parse time string (HH:MM:SS) to milliseconds
fun parseTimeToMillis(timeString: String): Long {
    try {
        val parts = timeString.split(":")
        if (parts.size != 3) return 0L

        val hours = parts[0].toLongOrNull() ?: 0L
        val minutes = parts[1].toLongOrNull() ?: 0L
        val seconds = parts[2].toLongOrNull() ?: 0L

        return (hours * 3600 + minutes * 60 + seconds) * 1000
    } catch (e: Exception) {
        return 0L
    }
}

// Add the BackupScreen composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavHostController,
    listNameEntities: List<ListNameEntity>,
    tasks: List<Task>,
    noteDao: NoteDao,  // Add parameter for noteDao
    folderDao: FolderDao  // Add parameter for folderDao
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Add state for notes and folders
    val notes = noteDao.getAllNotes().collectAsState(initial = emptyList()).value
    val folders = folderDao.getAllFolders().collectAsState(initial = emptyList()).value
    
    // Helper function to copy all note attachments
    fun copyNoteAttachments(sourceDir: File, destDir: File, filesCopied: MutableSet<String>): Int {
        var count = 0
        if (sourceDir.exists() && sourceDir.isDirectory) {
            // Copy files from attachments directory
            val attachmentsDir = File(sourceDir, "attachments")
            if (attachmentsDir.exists() && attachmentsDir.isDirectory) {
                attachmentsDir.listFiles()?.forEach { sourceFile ->
                    val destFile = File(destDir, sourceFile.name)
                    sourceFile.copyTo(destFile, overwrite = true)
                    filesCopied.add(sourceFile.name)
                    count++
                }
            }
            
            // Copy image attachments
            val imagesDir = File(sourceDir, "images")
            if (imagesDir.exists() && imagesDir.isDirectory) {
                imagesDir.listFiles()?.forEach { sourceFile ->
                    val destFile = File(destDir, sourceFile.name)
                    sourceFile.copyTo(destFile, overwrite = true)
                    filesCopied.add(sourceFile.name)
                    count++
                }
            }
        }
        return count
    }
    
    // Helper function to extract and copy voice recordings from notes
    fun copyVoiceRecordings(notes: List<NoteEntity>, destDir: File, filesCopied: MutableSet<String>): Int {
        var count = 0
        notes.forEach { note ->
            try {
                note.voiceRecordings?.let { recordingsJson ->
                    val recordingsArray = JSONArray(recordingsJson)
                    for (i in 0 until recordingsArray.length()) {
                        val recordingObj = recordingsArray.getJSONObject(i)
                        val filePath = recordingObj.optString("filePath", "")
                        if (filePath.isNotEmpty()) {
                            val sourceFile = File(filePath)
                            if (sourceFile.exists()) {
                                val destFile = File(destDir, sourceFile.name)
                                sourceFile.copyTo(destFile, overwrite = true)
                                filesCopied.add(sourceFile.name)
                                count++
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BackupScreen", "Error copying voice recording: ${e.message}")
            }
        }
        return count
    }
    
    // Create launcher for document creation - changed to .zip extension
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            isExporting = true
            exportMessage = null
            
            // Create JSON data
            val backupData = JSONObject().apply {
                // Add lists
                val listsArray = JSONArray()
                listNameEntities.forEach { list ->
                    listsArray.put(JSONObject().apply {
                        put("id", list.id)
                        put("name", list.name)
                    })
                }
                put("lists", listsArray)
                
                // Add tasks
                val tasksArray = JSONArray()
                tasks.forEach { task ->
                    tasksArray.put(JSONObject().apply {
                        put("id", task.id)
                        put("name", task.name)
                        put("description", task.description)
                        put("dueDate", task.dueDate.timeInMillis)
                        put("priority", task.priority.name)
                        put("list", task.list)
                        put("trackedTimeMillis", task.trackedTimeMillis)
                        put("completed", task.completed)
                        
                        // Add image and file paths as arrays
                        put("imagePaths", JSONArray().apply {
                            task.imagePaths.forEach { put(it) }
                        })
                        put("filePaths", JSONArray().apply {
                            task.filePaths.forEach { put(it) }
                        })
                        // Add subtasks as array
                        put("subtasks", JSONArray().apply {
                            task.subtasks.forEach { put(it) }
                        })
                        // Add completedSubtasks as array
                        put("completedSubtasks", JSONArray().apply {
                            task.completedSubtasks.forEach { put(it) }
                        })
                    })
                }
                put("tasks", tasksArray)
                
                // Add folders
                val foldersArray = JSONArray()
                folders.forEach { folder ->
                    foldersArray.put(JSONObject().apply {
                        put("id", folder.id)
                        put("name", folder.name)
                        put("parentId", folder.parentId)
                        put("createdAt", folder.createdAt)
                        put("color", folder.color)
                    })
                }
                put("folders", foldersArray)
                
                // Add notes
                val notesArray = JSONArray()
                notes.forEach { note ->
                    notesArray.put(JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("content", note.content)
                        put("folderId", note.folderId)
                        put("createdAt", note.createdAt)
                        put("updatedAt", note.updatedAt)
                        put("color", note.color)
                        put("isArchived", note.isArchived)
                        put("isPinned", note.isPinned)
                        put("isDeleted", note.isDeleted)
                        put("styles", note.styles)
                        put("checkboxItems", note.checkboxItems)
                        put("voiceRecordings", note.voiceRecordings)
                        put("fileAttachments", note.fileAttachments)
                        put("imageAttachments", note.imageAttachments)
                    })
                }
                put("notes", notesArray)
            }
            
            scope.launch(Dispatchers.IO) {
                try {
                    // Create a temporary directory to store files for zipping
                    val tempDir = File(context.cacheDir, "backup_temp")
                    if (tempDir.exists()) {
                        tempDir.deleteRecursively()
                    }
                    tempDir.mkdirs()
                    
                    // Save JSON data to a file
                    val jsonFile = File(tempDir, "data.json")
                    FileOutputStream(jsonFile).use { outputStream ->
                        outputStream.write(backupData.toString(2).toByteArray())
                    }
                    
                    // Copy all attachment files
                    val filesDir = File(tempDir, "files")
                    filesDir.mkdirs()
                    
                    val filesCopied = mutableSetOf<String>()
                    
                    // Copy all task attachments
                    tasks.forEach { task ->
                        // Copy image files
                        task.imagePaths.forEach { imagePath ->
                            val sourceFile = File(imagePath)
                            if (sourceFile.exists()) {
                                val destFile = File(filesDir, sourceFile.name)
                                sourceFile.copyTo(destFile, overwrite = true)
                                filesCopied.add(sourceFile.name)
                            }
                        }
                        
                        // Copy other files
                        task.filePaths.forEach { filePath ->
                            val sourceFile = File(filePath)
                            if (sourceFile.exists()) {
                                val destFile = File(filesDir, sourceFile.name)
                                sourceFile.copyTo(destFile, overwrite = true)
                                filesCopied.add(sourceFile.name)
                            }
                        }
                    }
                    
                    // Copy all note attachments
                    val noteAttachmentsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "jita_notes")
                    val noteAttachmentsCount = copyNoteAttachments(noteAttachmentsDir, filesDir, filesCopied)
                    
                    // Copy note voice recordings
                    val voiceRecordingsCount = copyVoiceRecordings(notes, filesDir, filesCopied)
                    
                    // Create ZIP file
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        ZipOutputStream(outputStream.buffered()).use { zipOut ->
                            // Add JSON file to ZIP
                            addFileToZip(zipOut, jsonFile, "data.json")
                            
                            // Add all files in the files directory
                            val filesInDir = filesDir.listFiles()
                            filesInDir?.forEach { file ->
                                addFileToZip(zipOut, file, "files/${file.name}")
                            }
                        }
                    }
                    
                    // Clean up temp directory
                    tempDir.deleteRecursively()
                    
                    // Summary counts
                    val noteCount = notes.size
                    val folderCount = folders.size
                    
                    exportMessage = "Backup successfully saved with ${tasks.size} tasks, $noteCount notes, $folderCount folders, and ${filesCopied.size} attachments!"
                } catch (e: Exception) {
                    Log.e("BackupScreen", "Error exporting data", e)
                    exportMessage = "Error: ${e.localizedMessage}"
                } finally {
                    isExporting = false
                    showExportDialog = true
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Backup Data",
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Remove the header text and add some space instead
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Create a backup of your lists, tasks, notes, folders, and all associated files. This backup can be restored later if needed.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Data Summary",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${listNameEntities.size}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lists",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${tasks.size}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tasks",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${tasks.count { it.completed }}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Add note and folder counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${notes.size}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${folders.size}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Folders",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Export button
            Button(
                onClick = {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(Date())
                    createDocumentLauncher.launch("JITA_Backup_$timestamp.zip")
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isExporting) "Exporting..." else "Export Backup",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Add more space between button and GIF
            Spacer(modifier = Modifier.height(10.dp))
            
            // GIF image - Replace bun with rab
            GifImage(
                modifier = Modifier.size(160.dp),  // Reduced from 200.dp to 160.dp
                drawableResId = R.drawable.rab
            )
        }
    }
    
    // Result dialog
    if (showExportDialog && exportMessage != null) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup Result") },
            text = { Text(exportMessage!!) },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// Helper function to add a file to a ZIP
private fun addFileToZip(zipOut: ZipOutputStream, file: File, entryPath: String) {
    FileInputStream(file).use { fis ->
        val zipEntry = ZipEntry(entryPath)
        zipOut.putNextEntry(zipEntry)
        
        val buffer = ByteArray(1024)
        var length: Int
        while (fis.read(buffer).also { length = it } > 0) {
            zipOut.write(buffer, 0, length)
        }
        
        zipOut.closeEntry()
    }
}

// Add the RestoreScreen composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreScreen(
    navController: NavHostController,
    listNameDao: ListNameDao,
    taskDao: TaskDao,
    noteDao: NoteDao,  // Add parameter for noteDao
    folderDao: FolderDao  // Add parameter for folderDao
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isImporting by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    // Updated to store lists, tasks, completed tasks, attachments, notes, and folders
    data class ImportStats(
        val lists: Int = 0,
        val tasks: Int = 0,
        val completedTasks: Int = 0,
        val attachments: Int = 0,
        val notes: Int = 0,
        val folders: Int = 0
    )
    var importStats by remember { mutableStateOf(ImportStats()) }
    
    // Helper function to update attachment paths in JSON
    fun updateAttachmentPaths(attachmentsJson: String?, restoredFiles: Map<String, String>): String? {
        if (attachmentsJson.isNullOrEmpty()) return attachmentsJson
        
        try {
            val attachmentsArray = JSONArray(attachmentsJson)
            val updatedArray = JSONArray()
            
            for (i in 0 until attachmentsArray.length()) {
                val attachmentObj = attachmentsArray.getJSONObject(i)
                val updatedObj = JSONObject(attachmentObj.toString())
                
                // Update file path if it exists in restored files
                if (updatedObj.has("filePath")) {
                    val originalPath = updatedObj.getString("filePath")
                    val fileName = originalPath.substringAfterLast('/')
                    val newPath = restoredFiles[fileName]
                    if (newPath != null) {
                        updatedObj.put("filePath", newPath)
                    }
                }
                
                updatedArray.put(updatedObj)
            }
            
            return updatedArray.toString()
        } catch (e: Exception) {
            Log.e("RestoreScreen", "Error updating attachment paths: ${e.message}")
            return attachmentsJson
        }
    }
    
    // Create launcher for document selection - updated to support ZIP files
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isImporting = true
            importMessage = null
            
            scope.launch(Dispatchers.IO) {
                try {
                    // Create temporary directory to extract files
                    val tempDir = File(context.cacheDir, "restore_temp")
                    if (tempDir.exists()) {
                        tempDir.deleteRecursively()
                    }
                    tempDir.mkdirs()
                    
                    // Create a directory for extracted files
                    val extractedFilesDir = File(tempDir, "files")
                    extractedFilesDir.mkdirs()
                    
                    // Create a temp file for the zip
                    val zipFile = File(tempDir, "backup.zip")
                    
                    // Copy the ZIP file to temporary location
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(zipFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    // Extract the ZIP contents
                    extractZipFile(zipFile, tempDir)
                    
                    // Read the extracted JSON file
                    val jsonFile = File(tempDir, "data.json")
                    if (!jsonFile.exists()) {
                        throw Exception("Invalid backup file: Missing data.json")
                    }
                    
                    val jsonString = jsonFile.readText()
                    val jsonObject = JSONObject(jsonString)
                    
                    // Prepare target directory for restored files
                    val targetDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "jita_files")
                    if (!targetDir.exists()) {
                        targetDir.mkdirs()
                    }
                    
                    // Prepare target directory for notes attachments
                    val noteAttachmentsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "jita_notes")
                    if (!noteAttachmentsDir.exists()) {
                        noteAttachmentsDir.mkdirs()
                    }
                    
                    // Create subdirectories for various note attachments
                    val noteAttachmentsSubdirs = File(noteAttachmentsDir, "attachments")
                    if (!noteAttachmentsSubdirs.exists()) {
                        noteAttachmentsSubdirs.mkdirs()
                    }
                    
                    val noteImagesSubdir = File(noteAttachmentsDir, "images")
                    if (!noteImagesSubdir.exists()) {
                        noteImagesSubdir.mkdirs()
                    }
                    
                    // Move extracted files to app's storage
                    val extractedFiles = File(tempDir, "files").listFiles() ?: emptyArray()
                    val restoredFiles = mutableMapOf<String, String>() // filename -> full path
                    
                    extractedFiles.forEach { sourceFile ->
                        // Determine if this is a voice recording or attachment based on filename pattern
                        val targetFile = when {
                            sourceFile.name.startsWith("voice_recording_") -> {
                                // Voice recording - place in noteAttachmentsDir
                                File(noteAttachmentsDir, sourceFile.name)
                            }
                            else -> {
                                // General attachment - place in regular files directory
                                File(targetDir, sourceFile.name)
                            }
                        }
                        
                        sourceFile.copyTo(targetFile, overwrite = true)
                        restoredFiles[sourceFile.name] = targetFile.absolutePath
                    }
                    
                    // Parse lists
                    val listsArray = jsonObject.optJSONArray("lists") ?: JSONArray()
                    val listEntities = mutableListOf<ListNameEntity>()
                    for (i in 0 until listsArray.length()) {
                        val listObj = listsArray.getJSONObject(i)
                        listEntities.add(
                            ListNameEntity(
                                id = if (listObj.has("id")) listObj.getInt("id") else 0,
                                name = listObj.getString("name")
                            )
                        )
                    }
                    
                    // Parse tasks
                    val tasksArray = jsonObject.optJSONArray("tasks") ?: JSONArray()
                    val taskEntities = mutableListOf<TaskEntity>()
                    var completedTaskCount = 0
                    
                    for (i in 0 until tasksArray.length()) {
                        val taskObj = tasksArray.getJSONObject(i)
                        val isCompleted = taskObj.optBoolean("completed", false)
                        
                        // Process and update paths for attachments
                        val imagePaths = mutableListOf<String>()
                        val imagePathsArray = taskObj.optJSONArray("imagePaths")
                        if (imagePathsArray != null) {
                        for (j in 0 until imagePathsArray.length()) {
                            val originalPath = imagePathsArray.getString(j)
                            val fileName = originalPath.substringAfterLast('/')
                                val updatedPath = restoredFiles[fileName]
                                if (updatedPath != null) {
                                    imagePaths.add(updatedPath)
                                }
                            }
                        }
                        
                        val filePaths = mutableListOf<String>()
                        val filePathsArray = taskObj.optJSONArray("filePaths")
                        if (filePathsArray != null) {
                        for (j in 0 until filePathsArray.length()) {
                            val originalPath = filePathsArray.getString(j)
                            val fileName = originalPath.substringAfterLast('/')
                                val updatedPath = restoredFiles[fileName]
                                if (updatedPath != null) {
                                    filePaths.add(updatedPath)
                                }
                            }
                        }
                        
                        // Process subtasks
                        val subtasks = mutableListOf<String>()
                        val subtasksArray = taskObj.optJSONArray("subtasks")
                        if (subtasksArray != null) {
                            for (j in 0 until subtasksArray.length()) {
                                subtasks.add(subtasksArray.getString(j))
                            }
                        }
                        
                        // Process completedSubtasks
                        val completedSubtasks = mutableListOf<Int>()
                        val completedSubtasksArray = taskObj.optJSONArray("completedSubtasks")
                        if (completedSubtasksArray != null) {
                            for (j in 0 until completedSubtasksArray.length()) {
                                completedSubtasks.add(completedSubtasksArray.getInt(j))
                            }
                        }
                        
                        if (isCompleted) {
                            completedTaskCount++
                        }

                        taskEntities.add(
                            TaskEntity(
                                id = if (taskObj.has("id")) taskObj.getInt("id") else 0,
                                name = taskObj.getString("name"),
                                description = taskObj.getString("description"),
                                dueDate = taskObj.getLong("dueDate"),
                                priority = taskObj.getString("priority"),
                                list = if (taskObj.isNull("list")) null else taskObj.getString("list"),
                                trackedTimeMillis = taskObj.optLong("trackedTimeMillis", 0),
                                isTracking = taskObj.optBoolean("isTracking", false),
                                trackingStartTime = taskObj.optLong("trackingStartTime", 0),
                                completed = isCompleted,
                                imagePaths = imagePaths,
                                filePaths = filePaths,
                                subtasks = subtasks,
                                completedSubtasks = completedSubtasks
                            )
                        )
                    }
                    
                    // Parse folders
                    val folderEntities = mutableListOf<FolderEntity>()
                    jsonObject.optJSONArray("folders")?.let { foldersArray ->
                        for (i in 0 until foldersArray.length()) {
                            val folderObj = foldersArray.getJSONObject(i)
                            folderEntities.add(
                                FolderEntity(
                                    id = if (folderObj.has("id")) folderObj.getInt("id") else 0,
                                    name = folderObj.getString("name"),
                                    parentId = if (folderObj.isNull("parentId")) null else folderObj.getInt("parentId"),
                                    createdAt = folderObj.optLong("createdAt", System.currentTimeMillis()),
                                    color = if (folderObj.isNull("color")) null else folderObj.getString("color")
                                )
                            )
                        }
                    }
                    
                    // Parse notes
                    val noteEntities = mutableListOf<NoteEntity>()
                    jsonObject.optJSONArray("notes")?.let { notesArray ->
                        for (i in 0 until notesArray.length()) {
                            val noteObj = notesArray.getJSONObject(i)
                            
                            // Process attachments to update paths
                            val processedVoiceRecordings = updateAttachmentPaths(
                                noteObj.optString("voiceRecordings"), 
                                restoredFiles
                            )
                            
                            val processedFileAttachments = updateAttachmentPaths(
                                noteObj.optString("fileAttachments"), 
                                restoredFiles
                            )
                            
                            val processedImageAttachments = updateAttachmentPaths(
                                noteObj.optString("imageAttachments"), 
                                restoredFiles
                            )
                            
                            noteEntities.add(
                                NoteEntity(
                                    id = if (noteObj.has("id")) noteObj.getInt("id") else 0,
                                    title = noteObj.getString("title"),
                                    content = noteObj.getString("content"),
                                    folderId = if (noteObj.isNull("folderId")) null else noteObj.getInt("folderId"),
                                    createdAt = noteObj.optLong("createdAt", System.currentTimeMillis()),
                                    updatedAt = noteObj.optLong("updatedAt", System.currentTimeMillis()),
                                    color = if (noteObj.isNull("color")) null else noteObj.getString("color"),
                                    isArchived = noteObj.optBoolean("isArchived", false),
                                    isPinned = noteObj.optBoolean("isPinned", false),
                                    isDeleted = noteObj.optBoolean("isDeleted", false),
                                    styles = if (noteObj.isNull("styles")) null else noteObj.getString("styles"),
                                    checkboxItems = if (noteObj.isNull("checkboxItems")) null else noteObj.getString("checkboxItems"),
                                    voiceRecordings = processedVoiceRecordings,
                                    fileAttachments = processedFileAttachments,
                                    imageAttachments = processedImageAttachments
                                )
                            )
                        }
                    }
                    
                    try {
                        // Clear existing data
                        taskDao.deleteAllTasks()
                        listNameDao.deleteAllListNames()
                        
                        // Only delete folders and notes if we have them in the backup
                        if (folderEntities.isNotEmpty() || noteEntities.isNotEmpty()) {
                            folderDao.deleteAllFolders()
                            noteDao.deleteAllNotes()
                        }
                        
                        // Insert new data (folders first, then notes)
                        folderEntities.forEach { folderDao.insertFolder(it) }
                        noteEntities.forEach { noteDao.insertNote(it) }
                        
                        // Insert list names and tasks
                        listEntities.forEach { listNameDao.insertListName(it) }
                        taskEntities.forEach { taskDao.insertTask(it) }
                        
                        // Update stats
                        val stats = ImportStats(
                            lists = listEntities.size,
                            tasks = taskEntities.size,
                            completedTasks = completedTaskCount,
                            attachments = restoredFiles.size,
                            notes = noteEntities.size,
                            folders = folderEntities.size
                        )
                        
                        withContext(Dispatchers.Main) {
                            importStats = stats
                            importMessage = "Data restored successfully!"
                            isImporting = false
                            showImportDialog = true
                        }
                    } catch (e: Exception) {
                        Log.e("RestoreScreen", "Database error", e)
                        withContext(Dispatchers.Main) {
                            importMessage = "Database error: ${e.localizedMessage}"
                            isImporting = false
                            showImportDialog = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RestoreScreen", "Error importing data", e)
                    importMessage = "Error: ${e.localizedMessage}"
                } finally {
                    withContext(Dispatchers.Main) {
                        isImporting = false
                        showImportDialog = true
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
                        text = "Restore Data",
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Remove the header text and add some space instead
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Restore your lists, tasks, notes, folders, and attachments from a previously created backup file. This will replace all current data with the data from the backup file.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Warning Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Warning",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Restoring data will replace all your current lists, tasks, notes, and folders. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Import button
            Button(
                onClick = {
                    openDocumentLauncher.launch(arrayOf("application/zip"))
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isImporting) "Importing..." else "Select Backup File",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // GIF image
            GifImage(
                modifier = Modifier.size(160.dp),
                drawableResId = R.drawable.cat
            )
        }
    }
    
    // Result dialog
    if (showImportDialog && importMessage != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore Result") },
            text = { 
                Column {
                    Text(importMessage!!)
                    
                    if (importMessage!!.startsWith("Data restored")) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Restored data summary:", fontWeight = FontWeight.Bold)
                        Text("Lists: ${importStats.lists}")
                        Text("Tasks: ${importStats.tasks}")
                        Text("Completed tasks: ${importStats.completedTasks}")
                        
                        if (importStats.folders > 0) {
                            Text("Folders: ${importStats.folders}")
                        }
                        
                        if (importStats.notes > 0) {
                            Text("Notes: ${importStats.notes}")
                        }
                        
                        // Show attachments count from importStats
                        if (importStats.attachments > 0) {
                            Text("Attachments: ${importStats.attachments}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showImportDialog = false
                        // Navigate back to main screen if successful
                        if (importMessage!!.startsWith("Data restored")) {
                            navController.navigate(AppDestinations.MAIN_SCREEN) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

// Helper function to extract ZIP file
private fun extractZipFile(zipFile: File, destDirectory: File) {
    java.util.zip.ZipFile(zipFile).use { zip ->
        val entries = zip.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val entryDestination = File(destDirectory, entry.name)
            
            // Create parent directories if they don't exist
            if (entry.isDirectory) {
                entryDestination.mkdirs()
                continue
            }
            
            entryDestination.parentFile?.mkdirs()
            
            // Extract the file
            zip.getInputStream(entry).use { input ->
                FileOutputStream(entryDestination).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

// Add a new composable for search results
@Composable
fun SearchResultItem(
    task: Task,
    onClick: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = task.priority.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(task.priority.color, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Task details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Show date
                Text(
                    text = dateFormatter.format(task.dueDate.time),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go to task",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Opens an image file using the default viewer
 */
private fun openImageFile(context: Context, imagePath: String) {
    try {
        val imageFile = File(imagePath)
        if (!imageFile.exists()) {
            Toast.makeText(context, "Image file not found", Toast.LENGTH_SHORT).show()
            return
        }
        
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "View Image")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "No app available to view images", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("ImageViewer", "Error opening image: ${e.localizedMessage}")
        Toast.makeText(context, "Error opening image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Opens a file using the appropriate app based on MIME type
 */
private fun openFile(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File not found: ${file.name}", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Log file details for debugging
        Log.d("FileOpener", "Opening file: ${file.absolutePath}")
        Log.d("FileOpener", "File exists: ${file.exists()}, size: ${file.length()}")
        
        val mimeType = getMimeType(file.name)
        Log.d("FileOpener", "Detected MIME type: $mimeType")
        
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        Log.d("FileOpener", "Content URI: $contentUri")
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        // Check if there's an app that can handle this file type
        val resolveInfo = context.packageManager.queryIntentActivities(intent, 0)
        if (resolveInfo.isEmpty()) {
            Log.e("FileOpener", "No app found to open file with MIME type: $mimeType")
            
            // Try with a more generic MIME type
            when {
                mimeType.startsWith("audio/") -> {
                    intent.setDataAndType(contentUri, "audio/*")
                    Toast.makeText(context, "Trying to open as generic audio file", Toast.LENGTH_SHORT).show()
                }
                mimeType.startsWith("video/") -> {
                    intent.setDataAndType(contentUri, "video/*")
                    Toast.makeText(context, "Trying to open as generic video file", Toast.LENGTH_SHORT).show()
                }
                mimeType.startsWith("image/") -> {
                    intent.setDataAndType(contentUri, "image/*")
                    Toast.makeText(context, "Trying to open as generic image file", Toast.LENGTH_SHORT).show()
                }
                mimeType.startsWith("application/") -> {
                    // Special handling for PDF files
                    if (file.name.endsWith(".pdf", ignoreCase = true)) {
                        intent.setDataAndType(contentUri, "application/pdf")
                        Toast.makeText(context, "Trying to open as PDF", Toast.LENGTH_SHORT).show()
                    } else {
                        intent.setDataAndType(contentUri, "*/*")
                        Toast.makeText(context, "Trying to open with any available app", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    intent.setDataAndType(contentUri, "*/*")
                    Toast.makeText(context, "Trying to open with any available app", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        val chooser = Intent.createChooser(intent, "Open File")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("FileOpener", "Error starting activity: ${e.localizedMessage}")
            Toast.makeText(context, "No app found to open this file type", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Log.e("FileOpener", "Error opening file: ${e.localizedMessage}", e)
        Toast.makeText(context, "Error opening file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

/**
 * Gets MIME type from file name/extension
 */
private fun getMimeType(fileName: String): String {
    return when {
        // Documents
        fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
        fileName.endsWith(".doc", ignoreCase = true) -> "application/msword"
        fileName.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        fileName.endsWith(".xls", ignoreCase = true) -> "application/vnd.ms-excel"
        fileName.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        fileName.endsWith(".ppt", ignoreCase = true) -> "application/vnd.ms-powerpoint"
        fileName.endsWith(".pptx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        fileName.endsWith(".txt", ignoreCase = true) -> "text/plain"
        
        // Images
        fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        fileName.endsWith(".png", ignoreCase = true) -> "image/png"
        fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
        fileName.endsWith(".bmp", ignoreCase = true) -> "image/bmp"
        fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
        
        // Audio
        fileName.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
        fileName.endsWith(".wav", ignoreCase = true) -> "audio/wav"
        fileName.endsWith(".ogg", ignoreCase = true) -> "audio/ogg"
        fileName.endsWith(".flac", ignoreCase = true) -> "audio/flac"
        fileName.endsWith(".aac", ignoreCase = true) -> "audio/aac"
        fileName.endsWith(".m4a", ignoreCase = true) -> "audio/m4a"
        
        // Video
        fileName.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
        fileName.endsWith(".3gp", ignoreCase = true) -> "video/3gpp"
        fileName.endsWith(".webm", ignoreCase = true) -> "video/webm"
        fileName.endsWith(".mkv", ignoreCase = true) -> "video/x-matroska"
        
        // Archives
        fileName.endsWith(".zip", ignoreCase = true) -> "application/zip"
        fileName.endsWith(".rar", ignoreCase = true) -> "application/x-rar-compressed"
        fileName.endsWith(".7z", ignoreCase = true) -> "application/x-7z-compressed"
        
        // Generic fallback - let the system try to figure it out
        else -> "*/*"
    }
}

// Helper data class for stats with 4 values
data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)