package com.example.jita

//import androidx.compose.material3.AsyncImagePainter
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import android.widget.Toast // Add Toast import
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.activity.compose.rememberLauncherForActivityResult
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import com.example.jita.data.ListNameDao
import com.example.jita.data.TaskDao


// Define navigation routes
object AppDestinations {
    const val MAIN_SCREEN = "main"
    const val LISTS_SCREEN = "lists"
    const val POMODORO_SCREEN = "pomodoro"
    const val STATISTICS_SCREEN = "statistics" // Add statistics route
    const val BACKUP_SCREEN = "backup" // Add backup route
    const val RESTORE_SCREEN = "restore" // Add restore route
}

// Define custom colors
val DarkBlue = Color(0xFF00008B) // Example Dark Blue
val DarkRed = Color(0xFFB00020)  // Example Dark Red (similar to Material error)
val LightBlue = Color(0xFFBEDCE8) // Example Light Blue

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
        completed = this.completed
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get database instance
        val database = AppDatabase.getDatabase(applicationContext)
        val listNameDao = database.listNameDao()
        val taskDao = database.taskDao()

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
                        completed = entity.completed  // Add completed flag
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
                            tasks = tasks
                        )
                    }
                    // Add composable for the Restore screen
                    composable(AppDestinations.RESTORE_SCREEN) {
                        RestoreScreen(
                            navController = navController,
                            listNameDao = listNameDao,
                            taskDao = taskDao
                        )
                    }
                }
            }
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
        containerColor = Color.White,
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
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Create New List") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("List Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                        focusedLabelColor = DarkBlue, unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = DarkBlue, unfocusedBorderColor = Color.Gray,
                        cursorColor = DarkBlue
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
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddListDialog = false; newListName = "" },
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
                ) { Text("Cancel") }
            }
        )
    }

    // Edit List Dialog
    if (showEditDialog && listToEditIndex != null) {
        val originalName = listNames.getOrNull(listToEditIndex!!) ?: ""
        AlertDialog(
            onDismissRequest = { showEditDialog = false; listToEditIndex = null },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Edit List Name") },
            text = {
                OutlinedTextField(
                    value = editedListName,
                    onValueChange = { editedListName = it },
                    label = { Text("New List Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                        focusedLabelColor = DarkBlue, unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = DarkBlue, unfocusedBorderColor = Color.Gray,
                        cursorColor = DarkBlue
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
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false; listToEditIndex = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
                ) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && listToDeleteIndex != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false; listToDeleteIndex = null },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
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
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkRed)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false; listToDeleteIndex = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
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
            containerColor = if (isDragging) LightBlue.copy(alpha = 0.9f) else LightBlue
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
                    color = Color.Black
                )
                // Add task count text
                Text(
                    text = "$taskCount task${if (taskCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit List",
                    tint = DarkBlue
                )
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete List",
                    tint = DarkRed
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

    // Update drawer items to include Pomodoro, Statistics, Backup, and Restore
    val drawerItems = listOf(
        AppDestinations.MAIN_SCREEN to "Calendar",
        AppDestinations.LISTS_SCREEN to "Lists",
        AppDestinations.POMODORO_SCREEN to "Pomodoro",
        AppDestinations.STATISTICS_SCREEN to "Statistics",
        AppDestinations.BACKUP_SCREEN to "Backup",
        AppDestinations.RESTORE_SCREEN to "Restore" // Add Restore item
    )

    // Local state for the Add Task Dialog form
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskName by rememberSaveable { mutableStateOf("") }
    var newTaskDescription by rememberSaveable { mutableStateOf("") }
    var newTaskDate by rememberSaveable { mutableStateOf(Calendar.getInstance()) } // Keep Calendar instance
    var newTaskPriority by rememberSaveable { mutableStateOf(TaskPriority.MEDIUM) }
    var newTaskList by rememberSaveable { mutableStateOf<String?>(null) }
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

    // Filter tasks for the selected date using the passed-in state and function
    val filteredTasks = remember(tasks, selectedDate) {
        tasks.filter { task -> isSameDay(task.dueDate, selectedDate) }
            .sortedWith(compareBy<Task> { it.priority }.thenBy { it.name }) // Sort by priority then name
    }

    // Force recomposition every second when any task is being tracked
    val isAnyTaskTracking = tasks.any { it.isTracking }
    var tickerState by remember { mutableStateOf(System.currentTimeMillis()) }

    // This LaunchedEffect is crucial for updating the timer display
    LaunchedEffect(isAnyTaskTracking) {
        if (isAnyTaskTracking) {
            while (true) {
                delay(50) // Update 20 times per second for very smooth updates
                tickerState = System.currentTimeMillis() // Trigger recomposition
            }
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
            ModalDrawerSheet(drawerContainerColor = Color.White) {
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
                            unselectedTextColor = Color.Black,
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
            containerColor = Color.White,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "TIJA",
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
                // Pass the selected date and update callback to WeekCalendar
                WeekCalendar(
                    selectedDate = selectedDate, // Use passed-in selectedDate
                    onDateSelected = onDateSelected, // Use passed-in callback
                    tasks = tasks // Pass all tasks to the calendar
                )

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
                    color = Color.Blue,
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
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp)) // Add space between text and GIF
                            // Add the bun.gif using the existing GifImage composable
                            GifImage(
                                modifier = Modifier.size(150.dp), // Adjust size as needed
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
                                currentTimeMillis = tickerState // Pass current time for live updates
                            )
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
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Create New Task") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
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
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = DarkBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = DarkBlue
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
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = DarkBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = DarkBlue
                        )
                    )

                    // Date Picker Trigger
                    val dateFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date: ", color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormatter.format(newTaskDate.time),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true }
                                .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(4.dp)) // Add border
                                .padding(vertical = 8.dp, horizontal = 12.dp), // Add padding
                            color = DarkBlue
                        )
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = DarkBlue)
                        }
                    }

                    // Priority Selection
                    Text("Priority:", color = Color.Black)
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
                    Text("List:", color = Color.Black)
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
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedLabelColor = DarkBlue,
                                unfocusedLabelColor = Color.Gray,
                                focusedBorderColor = DarkBlue,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = DarkBlue,
                                // Make it look read-only
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray,
                                disabledTrailingIconColor = DarkBlue
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
                                            color = Color.Gray,
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
                                list = newTaskList
                            )
                            onAddTask(newTask) // Call the callback to handle DB insertion
                            showAddTaskDialog = false
                            // Reset fields (optional, happens on next FAB click anyway)
                            // newTaskName = "" ... etc.
                        }
                    },
                    enabled = newTaskName.isNotBlank(), // Basic validation
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddTaskDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
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

        AlertDialog(
            onDismissRequest = {
                showEditTaskDialog = false
                taskToEdit = null
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Edit Task") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
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
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = DarkBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = DarkBlue
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
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = DarkBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = DarkBlue
                        )
                    )

                    // Date Picker Trigger
                    val dateFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date: ", color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormatter.format(newTaskDate.time),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true }
                                .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(4.dp)) // Add border
                                .padding(vertical = 8.dp, horizontal = 12.dp), // Add padding
                            color = DarkBlue
                        )
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = DarkBlue)
                        }
                    }

                    // Priority Selection
                    Text("Priority:", color = Color.Black)
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
                    Text("List:", color = Color.Black)
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
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedLabelColor = DarkBlue,
                                unfocusedLabelColor = Color.Gray,
                                focusedBorderColor = DarkBlue,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = DarkBlue,
                                // Make it look read-only
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray,
                                disabledTrailingIconColor = DarkBlue
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
                                            color = Color.Gray,
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

                    // After list selection dropdown, add time editor
                    Text("Tracked Time:", color = Color.Black)
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
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = DarkBlue,
                            unfocusedLabelColor = Color.Gray,
                            focusedBorderColor = DarkBlue,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = DarkBlue
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
                                trackingStartTime = taskToEdit!!.trackingStartTime
                            )

                            // Delete the old task and add the updated one
                            onDeleteTask(taskToEdit!!)
                            onAddTask(updatedTask)

                            showEditTaskDialog = false
                            taskToEdit = null
                        }
                    },
                    enabled = newTaskName.isNotBlank() && timeError == null,
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditTaskDialog = false
                        taskToEdit = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
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
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
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
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkRed)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
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
            containerColor = Color.White,
            title = {
                Text(
                    "Well Done!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
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
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
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

                    // Get Jalali date (DD/MM format only)
                    val jalaliDateString = getJalaliDateString(selectedDate)

                    // Combined Date Text
                    Text(
                        text = "$gregorianDateString ($jalaliDateString)", // Display both dates
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
                            color = Color.Gray
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
            containerColor = Color.White,
            title = {
                Text(
                    "Well Done!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
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
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
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
        PomodoroMode.Work -> DarkRed
        PomodoroMode.ShortBreak, PomodoroMode.LongBreak -> DarkBlue
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
                        tint = DarkBlue, // Use blue color for pause icon
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
            color = Color.Black
        )
        Text(
            text = "Cycle ${cycleCount + 1}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
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
            Text("Reset", color = if (hasSelectedTask) DarkBlue else Color.Gray)
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
                    .background(Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Start Timer (Disabled)",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Skip Button
        TextButton(onClick = onSkip, enabled = hasSelectedTask && !timerRunning) {
            Text("Skip", color = if (hasSelectedTask && !timerRunning) DarkBlue else Color.Gray)
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
    val completed: Boolean = false  // Add completed flag
)

// Task priority enum
enum class TaskPriority(val color: Color, val label: String) {
    HIGH(Color.Red, "High"),
    MEDIUM(Color(0xFFFF9800), "Medium"),  // Orange
    LOW(DarkBlue, "Low"),
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
            color = if (selected) priority.color else Color.Black,
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
    onCompletedChange: (Boolean) -> Unit = {}, // Add callback for checkbox
    currentTimeMillis: Long = 0  // This parameter forces recomposition
) {
    val dateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

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
                tint = DarkBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayTime,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DarkBlue
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed)
                Color.Gray.copy(alpha = 0.2f)
            else
                task.priority.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    color = if (task.completed) Color.Gray else Color.Black,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
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
                    // Date
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Due Date",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = dateFormatter.format(task.dueDate.time),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    // Show list name if available
                    task.list?.let { listName ->
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = listName,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkBlue,
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
                            color = if (task.isTracking) DarkBlue.copy(alpha = 0.2f) else Color.Transparent,
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
                        tint = DarkBlue
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Task",
                        tint = DarkRed
                    )
                }
            }
        }
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
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black,
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
                            color = Color.Gray,
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
                                            else -> Color.Black
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
                colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
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
                        color = Color.Gray,
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

                    val jalaliDateStr = getJalaliDateString(dayCalendar)

                    val isToday = today.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            today.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                            today.get(Calendar.DAY_OF_MONTH) == day

                    val isSelected = selectedDate.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            selectedDate.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                            selectedDate.get(Calendar.DAY_OF_MONTH) == day

                    // Check if this day has tasks
                    val hasTasks = hasTasksOnDate(dayCalendar)

                    val backgroundColor = when {
                        isSelected -> PinkHighlight
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isSelected -> Color.Black
                        isToday -> MaterialTheme.colorScheme.onPrimary
                        else -> Color.Black
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
                            Text(
                                text = day.toString(),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = textColor,
                                modifier = if (hasTasks) {
                                    // Add blue underline for days with tasks using drawBehind
                                    Modifier.drawBehind {
                                        // Draw a blue line under the text
                                        drawLine(
                                            color = DarkBlue,
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 2f
                                        )
                                    }
                                } else Modifier
                            )
                            Text(
                                text = jalaliDateStr,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = if (isSelected || isToday) textColor.copy(alpha = 0.7f) else Color.Gray
                            )
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
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black,
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
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Year", tint = Color.Black)
                    }
                    Text(selectedYear.toString(), fontWeight = FontWeight.Bold, color = Color.Black)
                    IconButton(onClick = {
                        if (selectedYear < yearRange.last) selectedYear++
                    }, enabled = selectedYear < yearRange.last) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Year", tint = Color.Black)
                    }
                }

                Text("Month:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp), color = Color.Black)
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
                                    contentColor = if (isSelected) DarkBlue else Color.Black
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
                colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = DarkBlue)
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
    tasks: List<Task>
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Create launcher for document creation
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
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
                        put("list", task.list ?: JSONObject.NULL)
                        put("trackedTimeMillis", task.trackedTimeMillis)
                        put("isTracking", task.isTracking)
                        put("trackingStartTime", task.trackingStartTime)
                        put("completed", task.completed)
                    })
                }
                put("tasks", tasksArray)
            }
            
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(backupData.toString(2).toByteArray())
                }
                exportMessage = "Backup successfully saved!"
            } catch (e: Exception) {
                Log.e("BackupScreen", "Error exporting data", e)
                exportMessage = "Error: ${e.localizedMessage}"
            } finally {
                isExporting = false
                showExportDialog = true
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
            // Add space between app bar and description text
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Create a backup of all your lists and tasks. The backup will be saved as a JSON file that you can use to restore your data later.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = LightBlue
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${listNameEntities.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Lists")
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${tasks.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Tasks")
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${tasks.count { it.completed }}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Completed")
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
                    createDocumentLauncher.launch("JITA_Backup_$timestamp.json")
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
            Spacer(modifier = Modifier.height(24.dp))
            
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

// Add the RestoreScreen composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreScreen(
    navController: NavHostController,
    listNameDao: ListNameDao,
    taskDao: TaskDao
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isImporting by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importStats by remember { mutableStateOf(Triple(0, 0, 0)) } // lists, tasks, completed tasks
    
    // Create launcher for document selection
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isImporting = true
            importMessage = null
            
            try {
                // Read the JSON file
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: throw Exception("Could not read file")
                
                val jsonObject = JSONObject(jsonString)
                
                // Parse lists
                val listsArray = jsonObject.getJSONArray("lists")
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
                val tasksArray = jsonObject.getJSONArray("tasks")
                val taskEntities = mutableListOf<TaskEntity>()
                var completedTaskCount = 0
                
                for (i in 0 until tasksArray.length()) {
                    val taskObj = tasksArray.getJSONObject(i)
                    val list = if (taskObj.has("list") && !taskObj.isNull("list")) 
                        taskObj.getString("list") else null
                    
                    val isCompleted = if (taskObj.has("completed")) 
                        taskObj.getBoolean("completed") else false
                    
                    if (isCompleted) completedTaskCount++
                    
                    taskEntities.add(
                        TaskEntity(
                            id = if (taskObj.has("id")) taskObj.getInt("id") else 0,
                            name = taskObj.getString("name"),
                            description = taskObj.getString("description"),
                            dueDate = taskObj.getLong("dueDate"),
                            priority = taskObj.getString("priority"),
                            list = list,
                            trackedTimeMillis = taskObj.getLong("trackedTimeMillis"),
                            isTracking = taskObj.getBoolean("isTracking"),
                            trackingStartTime = taskObj.getLong("trackingStartTime"),
                            completed = isCompleted
                        )
                    )
                }
                
                // Store import stats before database operations
                importStats = Triple(listEntities.size, taskEntities.size, completedTaskCount)
                
                // Insert into database
                scope.launch(Dispatchers.IO) {
                    try {
                        // Clear existing data
                        taskDao.deleteAllTasks()
                        listNameDao.deleteAllListNames()
                        
                        // Insert new data
                        listEntities.forEach { listNameDao.insertListName(it) }
                        taskEntities.forEach { taskDao.insertTask(it) }
                        
                        importMessage = "Data restored successfully!"
                    } catch (e: Exception) {
                        Log.e("RestoreScreen", "Database error", e)
                        importMessage = "Database error: ${e.localizedMessage}"
                    } finally {
                        isImporting = false
                        showImportDialog = true
                    }
                }
                
            } catch (e: Exception) {
                Log.e("RestoreScreen", "Error importing data", e)
                importMessage = "Error: ${e.localizedMessage}"
                isImporting = false
                showImportDialog = true
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
                text = "Restore your lists and tasks from a previously created backup file. This will replace all current data with the data from the backup file.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Warning Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DarkRed.copy(alpha = 0.1f)
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
                        color = DarkRed
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Restoring data will replace all your current lists and tasks. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Import button
            Button(
                onClick = {
                    openDocumentLauncher.launch(arrayOf("application/json"))
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
            
            // Add extra space between button and GIF
            Spacer(modifier = Modifier.height(16.dp))
            
            // GIF image
            GifImage(
                modifier = Modifier.size(200.dp),
                drawableResId = R.drawable.cat // Changed from crowi to cat
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
                        Text("Lists: ${importStats.first}")
                        Text("Tasks: ${importStats.second}")
                        Text("Completed tasks: ${importStats.third}")
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