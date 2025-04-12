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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.graphics.toArgb
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.ui.res.fontResource
import com.example.jita.R

// Define data class for checkbox items
data class CheckboxItem(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "Checkbox item",
    var isChecked: Boolean = false
)

// Define data classes for tracking rich text without Gson dependency
data class TextStyleInfo(
    val start: Int,
    val end: Int,
    val isBold: Boolean = false,
    val isUnderlined: Boolean = false,
    val isStrikethrough: Boolean = false,
    val fontSize: Int? = null,
    val textColor: String? = null,
    val backgroundColor: String? = null,
    val fontName: String? = null
)

// Define custom fonts
val TimesRomanFontFamily = FontFamily(
    Font(R.font.times),
    Font(R.font.timesbd, FontWeight.Bold),
    //Font(R.font.timesi, FontStyle.Italic),
    Font(R.font.timesbi, FontWeight.Bold, FontStyle.Italic)
)

val SegoePrintFontFamily = FontFamily(
    Font(R.font.segoepr),
    Font(R.font.segoeprb, FontWeight.Bold)
)

// Add a debug helper to safely handle checkbox data even if it's missing
private fun JSONArray?.toCheckboxList(): List<CheckboxItem> {
    if (this == null) return emptyList()
    
    return try {
        val items = mutableListOf<CheckboxItem>()
        for (i in 0 until this.length()) {
            val item = this.getJSONObject(i)
            items.add(
                CheckboxItem(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    text = item.optString("text", "Checkbox item"),
                    isChecked = item.optBoolean("isChecked", false)
                )
            )
        }
        items
    } catch (e: Exception) {
        android.util.Log.e("NoteEditor", "Error parsing checkbox items: ${e.message}")
        emptyList()
    }
}

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
    var isTextSelected by remember { 
        mutableStateOf(false) 
    }

    // Add state for checkbox widget
    var showCheckboxWidget by remember { mutableStateOf(false) }
    // Replace single checkbox with a list of checkbox items
    var checkboxItems by remember { mutableStateOf(listOf<CheckboxItem>()) }

    // Update selection tracking based on current text field selection
    LaunchedEffect(textFieldValue.selection) {
        isTextSelected = textFieldValue.selection.start != textFieldValue.selection.end
    }

    // State for formatting toolbar
    var showFormattingToolbar by remember { mutableStateOf(false) }
    var currentFontSize by remember { mutableStateOf(16) }

    // States for text formatting
    var textColor by remember { mutableStateOf(Color.Black) }
    var highlightColor by remember { mutableStateOf(Color.Transparent) }
    var currentFontName by remember { mutableStateOf<String?>(null) }

    // Track if we've created a default folder
    var defaultFolderId by remember { mutableStateOf<Int?>(null) }

    // State for custom date/time
    var noteTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    // Format current date for display
    val dateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
    val formattedDateTime = dateFormat.format(Date(noteTimestamp))

    // Keep track of all applied styles
    var appliedStyles by remember { mutableStateOf(listOf<TextStyleInfo>()) }
    
    // Track note's folder ID
    var noteFolderId by remember { mutableStateOf(folderId) }

    // Function to regenerate text with all applied styles
    val regenerateStyledText = remember { { text: String ->
        buildAnnotatedString {
            append(text)
            
            // Apply all stored styles
            for (style in appliedStyles) {
                if (style.start < text.length && style.end <= text.length && style.start < style.end) {
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
                        
                        // Apply font family if it exists
                        val withFontFamily = if (style.fontName != null) {
                            try {
                                // Apply custom fonts based on the fontName
                                val fontFamily = when(style.fontName) {
                                    "Times" -> TimesRomanFontFamily
                                    "Segoe Print" -> SegoePrintFontFamily
                                    "System Sans-serif" -> FontFamily.SansSerif
                                    "System Serif" -> FontFamily.Serif
                                    "System Monospace" -> FontFamily.Monospace
                                    else -> null
                                }
                                
                                if (fontFamily != null) {
                                    withFontSize.copy(fontFamily = fontFamily)
                                } else {
                                    withFontSize
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("NoteEditor", "Error applying font family: ${e.message}")
                                withFontSize
                            }
                        } else {
                            withFontSize
                        }
                        
                        // Apply text color separately if it exists
                        val withTextColor = if (style.textColor != null) {
                            try {
                                withFontFamily.copy(color = Color(android.graphics.Color.parseColor(style.textColor)))
                            } catch (e: Exception) {
                                // If color parsing fails, continue with other styles
                                android.util.Log.e("NoteEditor", "Error parsing text color: ${e.message}")
                                withFontFamily
                            }
                        } else {
                            withFontFamily
                        }
                        
                        // Apply background separately if it exists
                        val withBackground = if (style.backgroundColor != null) {
                            try {
                                withTextColor.copy(background = Color(android.graphics.Color.parseColor(style.backgroundColor)))
                            } catch (e: Exception) {
                                // If color parsing fails, continue with other styles
                                android.util.Log.e("NoteEditor", "Error parsing background color: ${e.message}")
                                withTextColor
                            }
                        } else {
                            withTextColor
                        }
                        
                        // Add the final style - ensure start is not greater than end
                        if (style.start < style.end) {
                            addStyle(
                                withBackground,
                                start = style.start,
                                end = style.end
                            )
                        }
                    } catch (e: Exception) {
                        // If there's any error applying a style, skip it
                        android.util.Log.e("NoteEditor", "Error applying style: ${e.message}")
                        continue
                    }
                }
            }
        }
    }}

    // If noteId exists, load it from database
    LaunchedEffect(noteId) {
        if (noteId > 0) {
            noteDao.getNoteById(noteId).collect { noteEntity ->
                noteEntity?.let {
                    title = it.title
                    noteTimestamp = it.updatedAt
                    // Store the note's folder ID 
                    noteFolderId = it.folderId
                    
                    // Load checkbox items if they exist with safer parsing
                    try {
                        it.checkboxItems?.let { checkboxJson ->
                            val checkboxArray = JSONArray(checkboxJson)
                            val loadedItems = checkboxArray.toCheckboxList()
                            
                            checkboxItems = loadedItems
                            // Show checkbox widget if we have items
                            if (loadedItems.isNotEmpty()) {
                                showCheckboxWidget = true
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NoteEditor", "Error loading checkbox items: ${e.message}")
                    }
                    
                    appliedStyles = it.styles?.let { jsonString ->
                        try {
                            JSONArray(jsonString).toList().map { item ->
                                val json = item as JSONObject
                                TextStyleInfo(
                                    start = json.getInt("start"),
                                    end = json.getInt("end"),
                                    isBold = json.optBoolean("isBold"),
                                    isUnderlined = json.optBoolean("isUnderlined"),
                                    isStrikethrough = json.optBoolean("isStrikethrough"),
                                    fontSize = json.optInt("fontSize").takeIf { it != 0 },
                                    textColor = json.optString("textColor").takeIf { it.isNotEmpty() },
                                    backgroundColor = json.optString("backgroundColor").takeIf { it.isNotEmpty() },
                                    fontName = json.optString("fontName").takeIf { it.isNotEmpty() }
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("NoteEditor", "Error parsing styles: ${e.message}")
                            emptyList()
                        }
                    } ?: emptyList()

                    // Create annotated text with styles
                    val annotatedContent = regenerateStyledText(it.content)
                    textFieldValue = TextFieldValue(
                        annotatedContent,
                        selection = TextRange(0) // Reset selection to start
                    )
                }
            }
        }
    }

    // Update save function to save text content with null check for folderId
    fun saveNoteAndNavigate(navigateUp: Boolean = false) {
        scope.launch {
            // Only save if there's content
            if (title.isNotBlank() || textFieldValue.text.isNotBlank() || checkboxItems.isNotEmpty()) {
                try {
                    // Create a copy of styles to ensure we capture current state
                    val stylesToSave = JSONArray(appliedStyles.map { style ->
                        JSONObject().apply {
                            put("start", style.start)
                            put("end", style.end)
                            put("isBold", style.isBold)
                            put("isUnderlined", style.isUnderlined)
                            put("isStrikethrough", style.isStrikethrough)
                            put("fontSize", style.fontSize)
                            put("textColor", style.textColor)
                            put("backgroundColor", style.backgroundColor)
                            put("fontName", style.fontName)
                        }
                    }).toString()
                    
                    // Save checkbox items as JSON
                    val checkboxItemsJson = if (checkboxItems.isNotEmpty()) {
                        JSONArray(checkboxItems.map { item ->
                            JSONObject().apply {
                                put("id", item.id)
                                put("text", item.text)
                                put("isChecked", item.isChecked)
                            }
                        }).toString()
                    } else {
                        null
                    }

                    val note = if (noteId > 0) {
                        NoteEntity(
                            id = noteId,
                            title = title.ifBlank { "Untitled" },
                            content = textFieldValue.text,
                            folderId = noteFolderId,
                            updatedAt = noteTimestamp,
                            styles = stylesToSave,
                            checkboxItems = checkboxItemsJson
                        )
                    } else {
                        NoteEntity(
                            title = title.ifBlank { "Untitled" },
                            content = textFieldValue.text,
                            folderId = noteFolderId,
                            createdAt = noteTimestamp,
                            updatedAt = noteTimestamp,
                            styles = stylesToSave,
                            checkboxItems = checkboxItemsJson
                        )
                    }
                    
                    // Ensure the insert completes before navigation
                    noteDao.insertNote(note)
                } catch (e: Exception) {
                    // Log the error but don't crash the app
                    android.util.Log.e("NoteEditor", "Error saving note: ${e.message}")
                }
            }

            // Move navigation inside the coroutine after save completes
            if (navigateUp) {
                navController.navigateUp()
            }
        }
    }

    // Function to record applied styles
    fun recordStyle(start: Int, end: Int, styleUpdate: (TextStyleInfo) -> TextStyleInfo) {
        // Make sure selection range is valid and not reversed
        if (start >= end || start < 0 || end > textFieldValue.text.length) {
            // Don't attempt to apply style to invalid range
            return
        }

        // Find if there's an existing style for this range
        val existingStyleIndex = appliedStyles.indexOfFirst { style ->
            style.start == start && style.end == end
        }

        // Track if styles actually changed to avoid unnecessary regeneration
        var stylesChanged = false

        if (existingStyleIndex >= 0) {
            // Update existing style
            val updatedStyles = appliedStyles.toMutableList()
            val oldStyle = updatedStyles[existingStyleIndex]
            val updatedStyle = styleUpdate(oldStyle)
            
            // Only update if the style actually changed
            if (oldStyle != updatedStyle) {
                stylesChanged = true
                
                // If all style attributes are false/null/default, remove the style completely
                if (!updatedStyle.isBold && 
                    !updatedStyle.isUnderlined && 
                    !updatedStyle.isStrikethrough && 
                    updatedStyle.fontSize == null && 
                    updatedStyle.textColor == null && 
                    updatedStyle.backgroundColor == null &&
                    updatedStyle.fontName == null) {
                    updatedStyles.removeAt(existingStyleIndex)
                } else {
                    updatedStyles[existingStyleIndex] = updatedStyle
                }
                appliedStyles = updatedStyles
            }
        } else {
            // Create new style
            val newStyle = TextStyleInfo(start, end)
            val updatedStyle = styleUpdate(newStyle)
            
            // Only add if style has actual formatting
            if (updatedStyle.isBold || 
                updatedStyle.isUnderlined || 
                updatedStyle.isStrikethrough || 
                updatedStyle.fontSize != null || 
                updatedStyle.textColor != null || 
                updatedStyle.backgroundColor != null ||
                updatedStyle.fontName != null) {
                appliedStyles = appliedStyles + updatedStyle
                stylesChanged = true
            }
        }
        
        // Only regenerate text if styles actually changed
        if (stylesChanged) {
            // Reapply all styles to ensure they persist
            val annotatedString = regenerateStyledText(textFieldValue.text)
            
            // Preserve selection
            textFieldValue = TextFieldValue(
                annotatedString,
                selection = textFieldValue.selection
            )
        }
    }

    // Functions to apply formatting only to selected text
    fun applyBold() {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end

        // Sort selection if needed (in case user selected from end to start)
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)

        // Make sure selection is valid
        if (start < 0 || end > textFieldValue.text.length || start >= end) {
            return
        }

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == start && it.end == end }
        val shouldToggleOff = existingStyle?.isBold == true

        // Record the applied style with toggle behavior
        recordStyle(start, end) { styleInfo ->
            styleInfo.copy(isBold = !shouldToggleOff)
        }
    }
    
    // Rest of the styling functions
    fun applyUnderline() {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end

        // Sort selection if needed
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == start && it.end == end }
        val shouldToggleOff = existingStyle?.isUnderlined == true

        // Record the applied style with toggle behavior
        recordStyle(start, end) { styleInfo ->
            styleInfo.copy(isUnderlined = !shouldToggleOff)
        }
    }

    fun applyStrikethrough() {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end

        // Sort selection if needed
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == start && it.end == end }
        val shouldToggleOff = existingStyle?.isStrikethrough == true

        // Record the applied style with toggle behavior
        recordStyle(start, end) { styleInfo ->
            styleInfo.copy(isStrikethrough = !shouldToggleOff)
        }
    }

    fun applyFontSize(fontSize: Int) {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end

        // Sort selection if needed
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == start && it.end == end }
        val shouldToggleOff = existingStyle?.fontSize == fontSize

        // Record the applied style with toggle behavior
        recordStyle(start, end) { styleInfo ->
            styleInfo.copy(fontSize = if (shouldToggleOff) null else fontSize)
        }

        currentFontSize = fontSize
    }

    fun applyTextColor(color: Color) {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end
        
        // Sort selection if needed
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)
        
        try {
            // Convert color to hex format
            val colorHex = String.format("#%08X", color.toArgb())

            // Check if we need to toggle on or off
            val existingStyle = appliedStyles.find { it.start == start && it.end == end }
            val shouldToggleOff = existingStyle?.textColor == colorHex

            // Record the applied style with toggle behavior
            recordStyle(start, end) { styleInfo ->
                styleInfo.copy(textColor = if (shouldToggleOff) null else colorHex)
            }

            textColor = color
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.e("NoteEditor", "Error applying text color: ${e.message}")
        }
    }

    fun applyHighlight(color: Color) {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end
        
        // Sort selection if needed
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)
        
        try {
            // Convert color to hex format (with alpha)
            val colorHex = String.format("#%08X", color.toArgb())

            // Check if we need to toggle on or off
            val existingStyle = appliedStyles.find { it.start == start && it.end == end }
            val shouldToggleOff = existingStyle?.backgroundColor == colorHex

            // Record the applied style with toggle behavior
            recordStyle(start, end) { styleInfo ->
                styleInfo.copy(backgroundColor = if (shouldToggleOff) null else colorHex)
            }

            highlightColor = color
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.e("NoteEditor", "Error applying highlight color: ${e.message}")
        }
    }

    // Add function to apply font change
    fun applyFont(fontName: String) {
        if (!isTextSelected) return

        val selectionStart = textFieldValue.selection.start
        val selectionEnd = textFieldValue.selection.end

        // Sort selection if needed
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)

        // Check if we need to toggle on or off
        val existingStyle = appliedStyles.find { it.start == start && it.end == end }
        val shouldToggleOff = existingStyle?.fontName == fontName

        // Record the applied style with toggle behavior
        recordStyle(start, end) { styleInfo ->
            styleInfo.copy(fontName = if (shouldToggleOff) null else fontName)
        }

        currentFontName = if (shouldToggleOff) null else fontName
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
                    IconButton(onClick = { 
                        // Toggle checkbox widget
                        showCheckboxWidget = !showCheckboxWidget
                    }) {
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
                    currentFontName = currentFontName,
                    isTextSelected = isTextSelected,
                    onBoldClick = { applyBold() },
                    onUnderlineClick = { applyUnderline() },
                    onStrikethroughClick = { applyStrikethrough() },
                    onFontSizeChanged = { applyFontSize(it) },
                    onTextColorChanged = { applyTextColor(it) },
                    onHighlightColorChanged = { applyHighlight(it) },
                    onFontChanged = { applyFont(it) }
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

            // Checkbox widget - displayed below title when activated
            AnimatedVisibility(
                visible = showCheckboxWidget,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Display all checkbox items
                    checkboxItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { checked ->
                                    // Update the checked state of this specific item
                                    checkboxItems = checkboxItems.map {
                                        if (it.id == item.id) it.copy(isChecked = checked) else it
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = Color.Gray
                                )
                            )
                            
                            // Editable text field for each checkbox item
                            BasicTextField(
                                value = item.text,
                                onValueChange = { newText ->
                                    // Update text for this specific checkbox item
                                    checkboxItems = checkboxItems.map {
                                        if (it.id == item.id) it.copy(text = newText) else it
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.DarkGray
                                ),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp)
                                    ) {
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Delete button for this checkbox item
                            IconButton(
                                onClick = {
                                    checkboxItems = checkboxItems.filter { it.id != item.id }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete checkbox item",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    // Button to add a new checkbox item
                    TextButton(
                        onClick = {
                            checkboxItems = checkboxItems + CheckboxItem()
                        },
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add checkbox",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Text(
                            text = "Add item",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Content field with rich text support
            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                SelectionContainer {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            // Store current selection
                            val currentSelection = newValue.selection
                            
                            // Validate the selection range
                            val validSelection = if (currentSelection.start > currentSelection.end) {
                                TextRange(currentSelection.end, currentSelection.start)
                            } else {
                                currentSelection
                            }
                            
                            // When content changes, we need to adjust style ranges if needed
                            if (newValue.text.length != textFieldValue.text.length) {
                                val oldLength = textFieldValue.text.length
                                val newLength = newValue.text.length
                                val diff = newLength - oldLength
                                
                                // Only adjust styles if text length changed
                                if (diff != 0) {
                                    // Find position where text changed
                                    val oldCursorPos = textFieldValue.selection.start
                                    val newCursorPos = validSelection.start
                                    val changePos = (newCursorPos - diff).coerceAtLeast(0)
                                    
                                    // Adjust style ranges that come after the change position
                                    appliedStyles = appliedStyles.map { style ->
                                        when {
                                            // Style ends before change, keep as is
                                            style.end <= changePos -> style
                                            // Style starts after change, shift both start and end
                                            style.start >= changePos -> style.copy(
                                                start = (style.start + diff).coerceAtLeast(0),
                                                end = (style.end + diff).coerceAtLeast(0)
                                            )
                                            // Style spans across change, only adjust end
                                            else -> style.copy(
                                                end = (style.end + diff).coerceAtLeast(style.start)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Only regenerate styled text if needed
                            val shouldRegenerate = textFieldValue.text != newValue.text || 
                                                 (isTextSelected != (validSelection.start != validSelection.end))
                                                 
                            if (shouldRegenerate) {
                                // Update if text is selected
                                isTextSelected = validSelection.start != validSelection.end
                                
                                // Regenerate styled text
                                val newAnnotatedString = regenerateStyledText(newValue.text)
                                
                                // Update text field value with styles and use valid selection
                                textFieldValue = TextFieldValue(
                                    newAnnotatedString,
                                    selection = validSelection
                                )
                            } else {
                                // Just update the selection without regenerating styles
                                textFieldValue = textFieldValue.copy(selection = validSelection)
                            }
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
    currentFontName: String?,
    isTextSelected: Boolean,
    onBoldClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onFontSizeChanged: (Int) -> Unit,
    onTextColorChanged: (Color) -> Unit,
    onHighlightColorChanged: (Color) -> Unit,
    onFontChanged: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    val colorOptions = listOf(
        Color.Black, Color.Red, Color.Blue, Color.Green,
        Color.Magenta, Color.Cyan, Color.Yellow, Color.Gray
    )

    // Font options - these should match the font files in your app/src/main/res/font directory
    val fontOptions = listOf(
        "Times" to "Times",
        "Segoe Print" to "Segoe Print",
        "System Sans-serif" to "System Sans-serif",
        "System Serif" to "System Serif", 
        "System Monospace" to "System Monospace"
    )

    var showTextColorPicker by remember { mutableStateOf(false) }
    var showHighlightColorPicker by remember { mutableStateOf(false) }
    var showFontPicker by remember { mutableStateOf(false) }

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
            
            // Font selection button (new)
            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            
            // Font selector
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display current font name or "Font"
                Text(
                    text = currentFontName ?: "Font",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isTextSelected) Color.Black else Color.Gray,
                    maxLines = 1,
                    fontFamily = when(currentFontName) {
                        "Times" -> TimesRomanFontFamily
                        "Segoe Print" -> SegoePrintFontFamily
                        "System Sans-serif" -> FontFamily.SansSerif
                        "System Serif" -> FontFamily.Serif
                        "System Monospace" -> FontFamily.Monospace
                        else -> null
                    },
                    modifier = Modifier.width(80.dp)
                )
                
                // Font selection dropdown button
                IconButton(
                    onClick = { showFontPicker = !showFontPicker },
                    modifier = Modifier.size(36.dp),
                    enabled = isTextSelected
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Font",
                        tint = if (isTextSelected) Color.Black else Color.Gray
                    )
                }
            }
            
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
        
        // Font picker dropdown
        AnimatedVisibility(
            visible = showFontPicker,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                fontOptions.forEach { (displayName, fontName) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isTextSelected) {
                                if (fontName != null) {
                                    onFontChanged(fontName)
                                }
                                showFontPicker = false
                            }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isSelected = currentFontName == fontName
                            
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(32.dp))
                        }
                        
                        Text(
                            text = displayName,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                            fontFamily = when(fontName) {
                                "Times" -> TimesRomanFontFamily
                                "Segoe Print" -> SegoePrintFontFamily
                                "System Sans-serif" -> FontFamily.SansSerif
                                "System Serif" -> FontFamily.Serif
                                "System Monospace" -> FontFamily.Monospace
                                else -> null
                            }
                        )
                    }
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