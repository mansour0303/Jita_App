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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.graphics.toArgb
import org.json.JSONArray
import org.json.JSONObject

// Define data classes for tracking rich text without Gson dependency
data class TextStyleInfo(
    val start: Int,
    val end: Int,
    val isBold: Boolean = false,
    val isUnderlined: Boolean = false,
    val isStrikethrough: Boolean = false,
    val fontSize: Int? = null,
    val textColor: String? = null,
    val backgroundColor: String? = null
)

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
    
    // Use TextFieldValue to track both content and selection
    var textFieldValue by remember { 
        mutableStateOf(TextFieldValue(text = "")) 
    }
    
    // Track if text is currently selected
    val isTextSelected = textFieldValue.selection.start != textFieldValue.selection.end

    // State for formatting toolbar
    var showFormattingToolbar by remember { mutableStateOf(false) }
    var currentFontSize by remember { mutableStateOf(16) }

    // States for text formatting
    var textColor by remember { mutableStateOf(Color.Black) }
    var highlightColor by remember { mutableStateOf(Color.Transparent) }

    // Track if we've created a default folder
    var defaultFolderId by remember { mutableStateOf<Int?>(null) }

    // State for custom date/time
    var noteTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    // Format current date for display
    val dateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
    val formattedDateTime = dateFormat.format(Date(noteTimestamp))

    // Keep track of all applied styles
    var appliedStyles by remember { mutableStateOf(listOf<TextStyleInfo>()) }

    // Function to regenerate text with all applied styles
    fun regenerateStyledText(text: String): AnnotatedString {
        return buildAnnotatedString {
            append(text)
            
            // Apply all stored styles
            for (style in appliedStyles) {
                if (style.start < text.length && style.end <= text.length) {
                    try {
                        // Create SpanStyle with only the needed properties
                        val spanStyle = SpanStyle()
                        
                        // Apply individual properties as needed
                        val finalSpanStyle = spanStyle.copy(
                            fontWeight = if (style.isBold) FontWeight.Bold else null,
                            textDecoration = when {
                                style.isUnderlined && style.isStrikethrough -> 
                                    TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                                style.isUnderlined -> TextDecoration.Underline
                                style.isStrikethrough -> TextDecoration.LineThrough
                                else -> null
                            }
                        )
                        
                        // Apply fontSize separately if it exists
                        val withFontSize = if (style.fontSize != null) {
                            finalSpanStyle.copy(fontSize = style.fontSize.sp)
                        } else {
                            finalSpanStyle
                        }
                        
                        // Apply text color separately if it exists
                        val withTextColor = if (style.textColor != null) {
                            try {
                                withFontSize.copy(color = Color(android.graphics.Color.parseColor(style.textColor)))
                            } catch (e: Exception) {
                                withFontSize
                            }
                        } else {
                            withFontSize
                        }
                        
                        // Apply background separately if it exists
                        val withBackground = if (style.backgroundColor != null) {
                            try {
                                withTextColor.copy(background = Color(android.graphics.Color.parseColor(style.backgroundColor)))
                            } catch (e: Exception) {
                                withTextColor
                            }
                        } else {
                            withTextColor
                        }
                        
                        // Add the final style
                        addStyle(
                            withBackground,
                            start = style.start,
                            end = style.end
                        )
                    } catch (e: Exception) {
                        // If there's any error applying a style, skip it
                        continue
                    }
                }
            }
        }
    }

    // If noteId exists, load it from database
    LaunchedEffect(noteId) {
        if (noteId > 0) {
            noteDao.getNoteById(noteId).collect { noteEntity ->
                noteEntity?.let {
                    title = it.title
                    textFieldValue = TextFieldValue(it.content)
                    // Use the note's timestamp if available
                    noteTimestamp = it.updatedAt
                }
            }
        }
    }

    // Update save function to save text content
    fun saveNoteAndNavigate(navigateUp: Boolean = false) {
        scope.launch {
            // Only save if there's content
            if (title.isNotBlank() || textFieldValue.text.isNotBlank()) {
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
                        content = textFieldValue.text,
                        folderId = finalFolderId,
                        updatedAt = noteTimestamp // Use custom timestamp
                    )
                } else {
                    NoteEntity(
                        title = title.ifBlank { "Untitled" },
                        content = textFieldValue.text,
                        folderId = finalFolderId,
                        createdAt = noteTimestamp, // Use custom timestamp for creation
                        updatedAt = noteTimestamp  // Use custom timestamp for update
                    )
                }
                noteDao.insertNote(note)
            }

            if (navigateUp) {
                navController.navigateUp()
            }
        }
    }

    // Function to record applied styles
    fun recordStyle(start: Int, end: Int, styleUpdate: (TextStyleInfo) -> TextStyleInfo) {
        // Find if there's an existing style for this range
        val existingStyleIndex = appliedStyles.indexOfFirst { style ->
            style.start == start && style.end == end
        }

        if (existingStyleIndex >= 0) {
            // Update existing style
            val updatedStyles = appliedStyles.toMutableList()
            val updatedStyle = styleUpdate(updatedStyles[existingStyleIndex])
            
            // If all style attributes are false/null/default, remove the style completely
            if (!updatedStyle.isBold && 
                !updatedStyle.isUnderlined && 
                !updatedStyle.isStrikethrough && 
                updatedStyle.fontSize == null && 
                updatedStyle.textColor == null && 
                updatedStyle.backgroundColor == null) {
                updatedStyles.removeAt(existingStyleIndex)
            } else {
                updatedStyles[existingStyleIndex] = updatedStyle
            }
            appliedStyles = updatedStyles
        } else {
            // Create new style
            val newStyle = TextStyleInfo(start, end)
            appliedStyles = appliedStyles + styleUpdate(newStyle)
        }
        
        // Reapply all styles to ensure they persist
        val annotatedString = regenerateStyledText(textFieldValue.text)
        
        // Preserve selection
        textFieldValue = TextFieldValue(
            annotatedString,
            selection = textFieldValue.selection
        )
    }

    // Functions to apply formatting only to selected text
    fun applyBold() {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == selectionStart && it.end == selectionEnd }
        val shouldToggleOff = existingStyle?.isBold == true

        // Record the applied style with toggle behavior
        recordStyle(selectionStart, selectionEnd) { styleInfo ->
            styleInfo.copy(isBold = !shouldToggleOff)
        }
    }
    
    // Rest of the styling functions
    fun applyUnderline() {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == selectionStart && it.end == selectionEnd }
        val shouldToggleOff = existingStyle?.isUnderlined == true

        // Record the applied style with toggle behavior
        recordStyle(selectionStart, selectionEnd) { styleInfo ->
            styleInfo.copy(isUnderlined = !shouldToggleOff)
        }
    }

    fun applyStrikethrough() {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == selectionStart && it.end == selectionEnd }
        val shouldToggleOff = existingStyle?.isStrikethrough == true

        // Record the applied style with toggle behavior
        recordStyle(selectionStart, selectionEnd) { styleInfo ->
            styleInfo.copy(isStrikethrough = !shouldToggleOff)
        }
    }

    fun applyFontSize(fontSize: Int) {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == selectionStart && it.end == selectionEnd }
        val shouldToggleOff = existingStyle?.fontSize == fontSize

        // Record the applied style with toggle behavior
        recordStyle(selectionStart, selectionEnd) { styleInfo ->
            styleInfo.copy(fontSize = if (shouldToggleOff) null else fontSize)
        }

        currentFontSize = fontSize
    }

    fun applyTextColor(color: Color) {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end
        
        // Convert color to hex format
        val colorHex = String.format("#%08X", color.toArgb())

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == selectionStart && it.end == selectionEnd }
        val shouldToggleOff = existingStyle?.textColor == colorHex

        // Record the applied style with toggle behavior
        recordStyle(selectionStart, selectionEnd) { styleInfo ->
            styleInfo.copy(textColor = if (shouldToggleOff) null else colorHex)
        }

        textColor = color
    }

    fun applyHighlight(color: Color) {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end
        
        // Convert color to hex format (with alpha)
        val colorHex = String.format("#%08X", color.toArgb())

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == selectionStart && it.end == selectionEnd }
        val shouldToggleOff = existingStyle?.backgroundColor == colorHex

        // Record the applied style with toggle behavior
        recordStyle(selectionStart, selectionEnd) { styleInfo ->
            styleInfo.copy(backgroundColor = if (shouldToggleOff) null else colorHex)
        }

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
                        saveNoteAndNavigate(navigateUp = true)
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
                    isTextSelected = isTextSelected,
                    onBoldClick = { applyBold() },
                    onUnderlineClick = { applyUnderline() },
                    onStrikethroughClick = { applyStrikethrough() },
                    onFontSizeChanged = { applyFontSize(it) },
                    onTextColorChanged = { applyTextColor(it) },
                    onHighlightColorChanged = { applyHighlight(it) }
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

            // Content field with rich text support
            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                SelectionContainer {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            // When content or selection changes, preserve styles
                            val newText = newValue.text
                            val newAnnotatedString = regenerateStyledText(newText)
                            textFieldValue = TextFieldValue(
                                newAnnotatedString,
                                selection = newValue.selection
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        ),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (textFieldValue.text.isEmpty()) {
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

    // Update BackHandler to use the saveNote function
    BackHandler {
        saveNoteAndNavigate(navigateUp = true)
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