package com.example.jita

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.jita.data.NoteDao
import com.example.jita.data.NoteEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    navController: NavHostController,
    noteDao: NoteDao,
    noteId: Int = -1,
    folderId: Int? = null
) {
    val scope = rememberCoroutineScope()
    val titleFocusRequester = remember { FocusRequester() }
    
    // State for note fields
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    
    // Format current date for display
    val dateFormat = SimpleDateFormat("dd/MM, HH:mm", Locale.getDefault())
    val currentDateTime = dateFormat.format(Date())
    
    // If noteId exists, load it from database
    LaunchedEffect(noteId) {
        if (noteId > 0) {
            noteDao.getNoteById(noteId).collect { noteEntity ->
                noteEntity?.let {
                    title = it.title
                    content = it.content
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    
                    // More options menu
                    IconButton(onClick = { /* Show more options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentColor = Color.Black,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text formatting button (Aa)
                    IconButton(onClick = { /* Text formatting */ }) {
                        Text(
                            text = "Aa",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        )
                    }
                    
                    // Checkbox button
                    IconButton(onClick = { /* Insert checkbox */ }) {
                        Icon(
                            imageVector = Icons.Default.CheckBox,
                            contentDescription = "Insert Checkbox",
                            tint = Color.DarkGray
                        )
                    }
                    
                    // Voice input button
                    IconButton(onClick = { /* Voice input */ }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = Color.DarkGray
                        )
                    }
                    
                    // Drawing/pen button
                    IconButton(onClick = { /* Drawing */ }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Drawing",
                            tint = Color.DarkGray
                        )
                    }
                    
                    // Image button
                    IconButton(onClick = { /* Insert image */ }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Insert Image",
                            tint = Color.DarkGray
                        )
                    }
                    
                    // Emoji button
                    IconButton(onClick = { /* Insert emoji */ }) {
                        Icon(
                            imageVector = Icons.Default.EmojiEmotions,
                            contentDescription = "Insert Emoji",
                            tint = Color.DarkGray
                        )
                    }
                    
                    // Formatting/palette button
                    IconButton(onClick = { /* Formatting options */ }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Formatting Options",
                            tint = Color.DarkGray
                        )
                    }
                    
                    // List options button
                    IconButton(onClick = { /* List options */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = "List Options",
                            tint = Color.DarkGray
                        )
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .background(Color.White)
        ) {
            // Date and time display with edit icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = currentDateTime,
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                IconButton(
                    onClick = { /* Edit date */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Date",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Title field using BasicTextField for more minimal appearance
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.DarkGray
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) {
                            Text(
                                text = "Title",
                                color = Color.LightGray,
                                fontSize = 24.sp
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .focusRequester(titleFocusRequester),
                singleLine = true
            )
            
            // Content field using BasicTextField for more minimal appearance
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.DarkGray
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (content.isEmpty()) {
                            Text(
                                text = "Note here",
                                color = Color.LightGray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
    
    // Save note when back button is pressed
    BackHandler {
        scope.launch {
            // Only save if there's content
            if (title.isNotBlank() || content.isNotBlank()) {
                val resolvedFolderId = folderId ?: 1 // Default to first folder if null
                val note = if (noteId > 0) {
                    NoteEntity(
                        id = noteId,
                        title = title.ifBlank { "Untitled" },
                        content = content,
                        folderId = resolvedFolderId,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    NoteEntity(
                        title = title.ifBlank { "Untitled" },
                        content = content,
                        folderId = resolvedFolderId
                    )
                }
                noteDao.insertNote(note)
            }
            navController.navigateUp()
        }
    }
    
    // Set focus to title when screen is first displayed if creating a new note
    LaunchedEffect(Unit) {
        if (noteId <= 0) {
            titleFocusRequester.requestFocus()
        }
    }
} 