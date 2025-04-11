package com.example.jita
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.jita.data.AppDatabase
import com.example.jita.data.FolderDao
import com.example.jita.data.FolderEntity
import com.example.jita.data.NoteDao
import com.example.jita.data.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class to represent the note UI model
data class Note(
    val id: Int = 0,
    val title: String,
    val content: String,
    val folderId: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Data class to represent the folder UI model
data class Folder(
    val id: Int = 0,
    val name: String,
    val parentId: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// Mapper functions
fun NoteEntity.toNote(): Note = Note(id, title, content, folderId, createdAt, updatedAt)
fun Note.toNoteEntity(): NoteEntity = NoteEntity(id, title, content, folderId, createdAt, updatedAt)
fun FolderEntity.toFolder(): Folder = Folder(id, name, parentId, createdAt)
fun Folder.toFolderEntity(): FolderEntity = FolderEntity(id, name, parentId, createdAt)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavHostController,
    noteDao: NoteDao,
    folderDao: FolderDao
) {
    val scope = rememberCoroutineScope()

    // State for current folder path
    var currentFolderId by rememberSaveable { mutableStateOf<Int?>(null) }
    var currentFolderName by rememberSaveable { mutableStateOf("Notes") }
    var folderPath by rememberSaveable { mutableStateOf<List<Pair<Int?, String>>>(emptyList()) }

    // Get folders and notes from database
    val folders = if (currentFolderId == null) {
        folderDao.getRootFolders().collectAsState(initial = emptyList<FolderEntity>()).value
    } else {
        folderDao.getSubFolders(currentFolderId!!).collectAsState(initial = emptyList<FolderEntity>()).value
    }
    
    val notes = if (currentFolderId == null) {
        // For the main notes page, we don't show any notes
        // Notes should only be visible when inside a specific folder
        emptyList<NoteEntity>()
    } else {
        // Show notes specific to the current folder
        noteDao.getNotesByFolder(currentFolderId!!).collectAsState(initial = emptyList<NoteEntity>()).value
    }

    // State for dialogs
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showViewNoteDialog by remember { mutableStateOf(false) }
    var newFolderName by rememberSaveable { mutableStateOf("") }
    var newNoteTitle by rememberSaveable { mutableStateOf("") }
    var newNoteContent by rememberSaveable { mutableStateOf("") }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    
    // Add state for folder editing
    var showEditFolderDialog by remember { mutableStateOf(false) }
    var editedFolderName by rememberSaveable { mutableStateOf("") }
    var folderToEdit by remember { mutableStateOf<FolderEntity?>(null) }
    
    // Add state for delete confirmation
    var showDeleteFolderDialog by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<FolderEntity?>(null) }
    
    // Add state for note deletion confirmation
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    // Function to navigate back to parent folder
    fun navigateUp() {
        if (folderPath.isNotEmpty()) {
            val parentFolder = folderPath.last()
            folderPath = folderPath.dropLast(1)
            currentFolderId = parentFolder.first
            currentFolderName = parentFolder.second
        } else {
            currentFolderId = null
            currentFolderName = "Notes"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentFolderName,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentFolderId == null) {
                            navController.navigateUp()
                        } else {
                            navigateUp()
                        }
                    }) {
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
                ),
                actions = {
                    IconButton(onClick = { showAddFolderDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Add Folder",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Navigate to note editor screen with default values
                    val folderId = currentFolderId
                    if (folderId != null) {
                        // Set the current folder ID in the savedStateHandle
                        navController.currentBackStackEntry?.savedStateHandle?.set("currentFolderId", folderId)
                        navController.navigate(AppDestinations.createNoteEditorRoute(-1))
                    } else {
                        // Show dialog to select folder first
                        showAddNoteDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note"
                )
            }
        }
    ) { paddingValues ->
        // If there are no folders and notes, show empty state
        if (folders.isEmpty() && notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Note,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No notes or folders yet")
                    Text("Create a new note or folder to get started")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (folders.isNotEmpty()) {
                    items(folders) { folder ->
                        FolderItem(
                            folder = folder.toFolder(),
                            onClick = {
                                folderPath = folderPath + (currentFolderId to currentFolderName)
                                currentFolderId = folder.id
                                currentFolderName = folder.name
                            },
                            onEdit = {
                                folderToEdit = folder
                                editedFolderName = folder.name
                                showEditFolderDialog = true
                            },
                            onDelete = {
                                folderToDelete = folder
                                showDeleteFolderDialog = true
                            }
                        )
                    }
                }

                if (notes.isNotEmpty()) {
                    items(notes.map { it.toNote() }) { note ->
                        Spacer(modifier = Modifier.height(8.dp))
                        NoteItem(
                            note = note,
                            onClick = {
                                // Navigate directly to editor screen instead of showing dialog
                                navController.navigate(AppDestinations.createNoteEditorRoute(note.id))
                            },
                            onDelete = {
                                noteToDelete = notes.find { it.id == note.id }
                                showDeleteNoteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Add Folder Dialog
        if (showAddFolderDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddFolderDialog = false
                    newFolderName = ""
                },
                title = { Text("New Folder") },
                text = {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                scope.launch {
                                    val folderId = currentFolderId ?: run {
                                        // If we're at the root level, we need to create a root folder
                                        // or get one if it exists already
                                        val rootFolders = folderDao.getRootFolders().first() // Use first() to get the first emission
                                        if (rootFolders.isNotEmpty()) {
                                            rootFolders.first().id
                                        } else {
                                            // Create a root folder if none exists
                                            val rootFolder = FolderEntity(name = "Main")
                                            folderDao.insertFolder(rootFolder).toInt()
                                        }
                                    }
                                    
                                    val folder = FolderEntity(
                                        name = newFolderName,
                                        parentId = currentFolderId
                                    )
                                    folderDao.insertFolder(folder)
                                    showAddFolderDialog = false
                                    newFolderName = ""
                                }
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddFolderDialog = false
                            newFolderName = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Edit Folder Dialog
        if (showEditFolderDialog && folderToEdit != null) {
            AlertDialog(
                onDismissRequest = {
                    showEditFolderDialog = false
                    folderToEdit = null
                    editedFolderName = ""
                },
                title = { Text("Edit Folder") },
                text = {
                    OutlinedTextField(
                        value = editedFolderName,
                        onValueChange = { editedFolderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (editedFolderName.isNotBlank()) {
                                scope.launch {
                                    val updatedFolder = folderToEdit!!.copy(name = editedFolderName)
                                    folderDao.updateFolder(updatedFolder)
                                    
                                    // Update currentFolderName if we're editing the current folder
                                    if (folderToEdit!!.id == currentFolderId) {
                                        currentFolderName = editedFolderName
                                    }
                                    
                                    showEditFolderDialog = false
                                    folderToEdit = null
                                    editedFolderName = ""
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditFolderDialog = false
                            folderToEdit = null
                            editedFolderName = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Delete Folder Confirmation Dialog
        if (showDeleteFolderDialog && folderToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteFolderDialog = false
                    folderToDelete = null
                },
                title = { Text("Delete Folder") },
                text = { 
                    Text("Are you sure you want to delete the folder '${folderToDelete!!.name}'? All notes and subfolders will be deleted permanently.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                folderDao.deleteFolder(folderToDelete!!)
                                showDeleteFolderDialog = false
                                folderToDelete = null
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteFolderDialog = false
                            folderToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Note Confirmation Dialog
        if (showDeleteNoteDialog && noteToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteNoteDialog = false
                    noteToDelete = null
                },
                title = { Text("Delete Note") },
                text = { 
                    Text("Are you sure you want to delete the note '${noteToDelete!!.title}'? This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                noteDao.deleteNote(noteToDelete!!)
                                showDeleteNoteDialog = false
                                noteToDelete = null
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteNoteDialog = false
                            noteToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Note Dialog
        if (showAddNoteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddNoteDialog = false
                    newNoteTitle = ""
                    newNoteContent = ""
                },
                title = { Text("New Note") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newNoteTitle,
                            onValueChange = { newNoteTitle = it },
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newNoteContent,
                            onValueChange = { newNoteContent = it },
                            label = { Text("Content") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newNoteTitle.isNotBlank()) {
                                scope.launch {
                                    val folderId = currentFolderId ?: run {
                                        // If we're at the root level, we need to create a root folder
                                        // or get one if it exists already
                                        val rootFolders = folderDao.getRootFolders().first() // Use first() to get the first emission
                                        if (rootFolders.isNotEmpty()) {
                                            rootFolders.first().id
                                        } else {
                                            // Create a root folder if none exists
                                            val rootFolder = FolderEntity(name = "Main")
                                            folderDao.insertFolder(rootFolder).toInt()
                                        }
                                    }
                                    
                                    val note = NoteEntity(
                                        title = newNoteTitle,
                                        content = newNoteContent,
                                        folderId = folderId
                                    )
                                    noteDao.insertNote(note)
                                    showAddNoteDialog = false
                                    newNoteTitle = ""
                                    newNoteContent = ""
                                }
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddNoteDialog = false
                            newNoteTitle = ""
                            newNoteContent = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // View Note Dialog
        if (showViewNoteDialog && selectedNote != null) {
            AlertDialog(
                onDismissRequest = {
                    showViewNoteDialog = false
                    selectedNote = null
                },
                title = { Text(selectedNote!!.title) },
                text = {
                    Column {
                        Text(selectedNote!!.content)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Last updated: ${formatDate(selectedNote!!.updatedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showViewNoteDialog = false
                            selectedNote = null
                        }
                    ) {
                        Text("Close")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Navigate to edit screen
                            showViewNoteDialog = false
                            selectedNote?.let {
                                navController.navigate(AppDestinations.createNoteEditorRoute(it.id))
                            }
                        }
                    ) {
                        Text("Edit")
                    }
                }
            )
        }
    }
}

@Composable
fun FolderItem(
    folder: Folder, 
    onClick: () -> Unit, 
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Folder",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Folder",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Note,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDate(note.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Note",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Helper function to format date
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}