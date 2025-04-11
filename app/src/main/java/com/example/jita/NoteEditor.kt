package com.example.jita

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.jita.data.NoteDao
import com.example.jita.data.NoteEntity
import com.example.jita.data.FolderEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle

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
    val context = LocalContext.current

    // State for note fields
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

    // State for formatting toolbar
    var showFormattingToolbar by remember { mutableStateOf(false) }
    var currentFontSize by remember { mutableStateOf(16) }

    // States for text formatting
    var textColor by remember { mutableStateOf(Color.Black) }
    var highlightColor by remember { mutableStateOf(Color.Transparent) }

    // Content text style - explicitly set initial FontStyle to Normal
    var contentTextStyle by remember {
        mutableStateOf(
            TextStyle(
                fontSize = 16.sp,
                color = Color.DarkGray,
                fontStyle = FontStyle.Normal
            )
        )
    }

    // Track text selection (simplified for this version)
    var selectedText by remember { mutableStateOf("") }
    var isTextSelected by remember { mutableStateOf(false) }

    // Track if we've created a default folder
    var defaultFolderId by remember { mutableStateOf<Int?>(null) }

    // State for custom date/time
    var noteTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    // Format current date for display
    val dateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
    val formattedDateTime = dateFormat.format(Date(noteTimestamp))

    // If noteId exists, load it from database
    LaunchedEffect(noteId) {
        if (noteId > 0) {
            noteDao.getNoteById(noteId).collect { noteEntity ->
                noteEntity?.let {
                    title = it.title
                    content = it.content
                    // Use the note's timestamp if available
                    noteTimestamp = it.updatedAt
                }
            }
        }
    }

    // Simplified text formatting functions that update the TextStyle
    fun applyBold() {
        contentTextStyle = contentTextStyle.copy(
            fontWeight = if (contentTextStyle.fontWeight == FontWeight.Bold)
                FontWeight.Normal else FontWeight.Bold
        )
    }

    fun applyItalic() {
        // Get current font style or default to Normal if null
        val currentFontStyle = contentTextStyle.fontStyle ?: FontStyle.Normal

        // Toggle between Italic and Normal
        val newFontStyle = if (currentFontStyle == FontStyle.Italic) {
            FontStyle.Normal
        } else {
            FontStyle.Italic
        }

        // Apply the new style
        contentTextStyle = contentTextStyle.copy(
            fontStyle = newFontStyle
        )
    }
    
    fun applyUnderline() {
        val currentDecoration = contentTextStyle.textDecoration
        contentTextStyle = contentTextStyle.copy(
            textDecoration = if (currentDecoration != null &&
                currentDecoration.contains(TextDecoration.Underline)) {
                // Remove underline - create a new decoration without underline
                when {
                    currentDecoration.contains(TextDecoration.LineThrough) -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }
            } else {
                // Add underline, preserving other decorations if they exist
                when {
                    currentDecoration == null || currentDecoration == TextDecoration.None -> TextDecoration.Underline
                    currentDecoration.contains(TextDecoration.LineThrough) -> TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                    else -> TextDecoration.Underline
                }
            }
        )
    }

    fun applyStrikethrough() {
        val currentDecoration = contentTextStyle.textDecoration
        contentTextStyle = contentTextStyle.copy(
            textDecoration = if (currentDecoration != null &&
                currentDecoration.contains(TextDecoration.LineThrough)) {
                // Remove strikethrough - create a new decoration without strikethrough
                when {
                    currentDecoration.contains(TextDecoration.Underline) -> TextDecoration.Underline
                    else -> TextDecoration.None
                }
            } else {
                // Add strikethrough, preserving other decorations if they exist
                when {
                    currentDecoration == null || currentDecoration == TextDecoration.None -> TextDecoration.LineThrough
                    currentDecoration.contains(TextDecoration.Underline) -> TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                    else -> TextDecoration.LineThrough
                }
            }
        )
    }

    fun applyFontSize(fontSize: Int) {
        contentTextStyle = contentTextStyle.copy(
            fontSize = fontSize.sp
        )
    }

    fun applyTextColor(color: Color) {
        contentTextStyle = contentTextStyle.copy(
            color = color
        )
        textColor = color
    }

    fun applyHighlight(color: Color) {
        contentTextStyle = contentTextStyle.copy(
            background = color
        )
        highlightColor = color
    }

    // Custom colors for text selection
    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        // Save note when back arrow is clicked
                        scope.launch {
                            // Only save if there's content
                            if (title.isNotBlank() || content.isNotBlank()) {
                                // Make sure we have a valid folder ID
                                val finalFolderId: Int = when {
                                    folderId != null -> folderId
                                    defaultFolderId != null -> defaultFolderId as Int
                                    else -> {
                                        // Check if the default folder exists
                                        val folders = noteDao.getAllFolders()
                                        if (folders.isNotEmpty()) {
                                            folders.first().id
                                        } else {
                                            // Create a default folder if none exist
                                            val defaultFolder = FolderEntity(name = "Notes")
                                            val newFolderId = noteDao.insertFolder(defaultFolder).toInt()
                                            defaultFolderId = newFolderId
                                            newFolderId
                                        }
                                    }
                                }

                                val note = if (noteId > 0) {
                                    NoteEntity(
                                        id = noteId,
                                        title = title.ifBlank { "Untitled" },
                                        content = content,
                                        folderId = finalFolderId,
                                        updatedAt = noteTimestamp // Use custom timestamp
                                    )
                                } else {
                                    NoteEntity(
                                        title = title.ifBlank { "Untitled" },
                                        content = content,
                                        folderId = finalFolderId,
                                        createdAt = noteTimestamp, // Use custom timestamp for creation
                                        updatedAt = noteTimestamp  // Use custom timestamp for update
                                    )
                                }
                                noteDao.insertNote(note)
                            }
                            navController.navigateUp()
                        }
                    }) {
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
                    IconButton(onClick = {
                        // Toggle formatting toolbar
                        showFormattingToolbar = !showFormattingToolbar
                    }) {
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
                    text = formattedDateTime,
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                IconButton(
                    onClick = {
                        // Get current date from timestamp
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = noteTimestamp
                        }

                        // Show date picker
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                // Update calendar with selected date
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                // Show time picker after date is selected
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        // Update calendar with selected time
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        calendar.set(Calendar.MINUTE, minute)
                                        calendar.set(Calendar.SECOND, 0)

                                        // Update timestamp state
                                        noteTimestamp = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true // 24-hour format
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
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

            // Text formatting toolbar
            AnimatedVisibility(
                visible = showFormattingToolbar,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                TextFormattingToolbar(
                    currentFontSize = currentFontSize,
                    textColor = textColor,
                    highlightColor = highlightColor,
                    isTextSelected = true, // We'll enable all controls since we're applying to all text now
                    onBoldClick = { applyBold() },
                    onItalicClick = { applyItalic() },
                    onUnderlineClick = { applyUnderline() },
                    onStrikethroughClick = { applyStrikethrough() },
                    onFontSizeChanged = {
                        currentFontSize = it
                        applyFontSize(it)
                    },
                    onTextColorChanged = { newColor ->
                        applyTextColor(newColor)
                    },
                    onHighlightColorChanged = { newColor ->
                        applyHighlight(newColor)
                    }
                )
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

            // Content field with applied styling
            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                SelectionContainer {
                    BasicTextField(
                        value = content,
                        onValueChange = { newValue ->
                            content = newValue

                            // For simplicity, we're applying styles to all text
                            // No need to track selection in this approach
                        },
                        textStyle = contentTextStyle,
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
        }
    }

    // Save note when back button is pressed
    BackHandler {
        scope.launch {
            // Only save if there's content
            if (title.isNotBlank() || content.isNotBlank()) {
                // Make sure we have a valid folder ID
                val finalFolderId: Int = when {
                    folderId != null -> folderId
                    defaultFolderId != null -> defaultFolderId as Int
                    else -> {
                        // Check if the default folder exists
                        val folders = noteDao.getAllFolders()
                        if (folders.isNotEmpty()) {
                            folders.first().id
                        } else {
                            // Create a default folder if none exist
                            val defaultFolder = FolderEntity(name = "Notes")
                            val newFolderId = noteDao.insertFolder(defaultFolder).toInt()
                            defaultFolderId = newFolderId
                            newFolderId
                        }
                    }
                }

                val note = if (noteId > 0) {
                    NoteEntity(
                        id = noteId,
                        title = title.ifBlank { "Untitled" },
                        content = content,
                        folderId = finalFolderId,
                        updatedAt = noteTimestamp // Use custom timestamp
                    )
                } else {
                    NoteEntity(
                        title = title.ifBlank { "Untitled" },
                        content = content,
                        folderId = finalFolderId,
                        createdAt = noteTimestamp, // Use custom timestamp for creation
                        updatedAt = noteTimestamp  // Use custom timestamp for update
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

@Composable
private fun TextFormattingToolbar(
    currentFontSize: Int,
    textColor: Color,
    highlightColor: Color,
    isTextSelected: Boolean,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onFontSizeChanged: (Int) -> Unit,
    onTextColorChanged: (Color) -> Unit,
    onHighlightColorChanged: (Color) -> Unit
) {
    val scrollState = rememberScrollState()

    val colorOptions = listOf(
        Color.Black, Color.Red, Color.Blue, Color.Green,
        Color.Magenta, Color.Cyan, Color.Yellow, Color.Gray
    )

    var showTextColorPicker by remember { mutableStateOf(false) }
    var showHighlightColorPicker by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bold
            IconButton(
                onClick = { if (isTextSelected) onBoldClick() },
                modifier = Modifier.size(36.dp),
                enabled = isTextSelected
            ) {
                Text(
                    text = "B",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTextSelected) Color.Black else Color.Gray
                    )
                )
            }

            // Underline
            IconButton(
                onClick = { if (isTextSelected) onUnderlineClick() },
                modifier = Modifier.size(36.dp),
                enabled = isTextSelected
            ) {
                Text(
                    text = "U",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTextSelected) Color.Black else Color.Gray,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }

            // Strikethrough
            IconButton(
                onClick = { if (isTextSelected) onStrikethroughClick() },
                modifier = Modifier.size(36.dp),
                enabled = isTextSelected
            ) {
                Text(
                    text = "S",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTextSelected) Color.Black else Color.Gray,
                        textDecoration = TextDecoration.LineThrough
                    )
                )
            }
            
            // Divider
            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            
            // Font size label with decrease and increase buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Decrease font size
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { 
                            if (currentFontSize > 8 && isTextSelected) {
                                onFontSizeChanged(currentFontSize - 2)
                            }
                        },
                        enabled = isTextSelected && currentFontSize > 8
                    ) {
                        Text(
                            text = "–",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTextSelected && currentFontSize > 8) Color.Black else Color.Gray
                        )
                    }
                }
                
                // Current font size
                Text(
                    text = "$currentFontSize",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTextSelected) Color.Black else Color.Gray
                )
                
                // Increase font size
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { 
                            if (currentFontSize < 32 && isTextSelected) {
                                onFontSizeChanged(currentFontSize + 2)
                            }
                        },
                        enabled = isTextSelected && currentFontSize < 32
                    ) {
                        Text(
                            text = "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTextSelected && currentFontSize < 32) Color.Black else Color.Gray
                        )
                    }
                }
            }
            
            // Divider
            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            
            // Text highlight color
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(highlightColor, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = "A",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // Color picker button
                IconButton(
                    onClick = { showHighlightColorPicker = !showHighlightColorPicker },
                    modifier = Modifier.size(36.dp),
                    enabled = isTextSelected
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color.Red, Color.Yellow, Color.Green,
                                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                    )
                                ),
                                shape = CircleShape
                            )
                            .alpha(if (isTextSelected) 1f else 0.5f)
                    )
                }
            }
            
            // Divider
            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            
            // Text color
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(end = 4.dp)
                )
                
                // Color picker button
                IconButton(
                    onClick = { showTextColorPicker = !showTextColorPicker },
                    modifier = Modifier.size(36.dp),
                    enabled = isTextSelected
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color.Red, Color.Yellow, Color.Green,
                                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                    )
                                ),
                                shape = CircleShape
                            )
                            .alpha(if (isTextSelected) 1f else 0.5f)
                    )
                }
            }
        }
        
        // Text color picker
        AnimatedVisibility(
            visible = showTextColorPicker,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colorOptions.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .border(
                                width = 2.dp,
                                color = if (color == textColor) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable(enabled = isTextSelected) {
                                onTextColorChanged(color)
                                showTextColorPicker = false
                            }
                    )
                }
            }
        }
        
        // Highlight color picker
        AnimatedVisibility(
            visible = showHighlightColorPicker,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add transparent (no highlight) option
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, CircleShape)
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = CircleShape
                        )
                        .border(
                            width = if (highlightColor == Color.Transparent) 2.dp else 0.dp,
                            color = if (highlightColor == Color.Transparent) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable(enabled = isTextSelected) {
                            onHighlightColorChanged(Color.Transparent)
                            showHighlightColorPicker = false
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "No highlight",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Center)
                    )
                }
                
                // Pastel highlight options
                listOf(
                    Color(0xFFFFF9C4), // Light yellow
                    Color(0xFFFFCCBC), // Light orange
                    Color(0xFFBBDEFB), // Light blue
                    Color(0xFFDCEDC8), // Light green
                    Color(0xFFF8BBD0), // Light pink
                    Color(0xFFE1BEE7)  // Light purple
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .border(
                                width = 2.dp,
                                color = if (color == highlightColor) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable(enabled = isTextSelected) {
                                onHighlightColorChanged(color)
                                showHighlightColorPicker = false
                            }
                    )
                }
            }
        }
    }
} 