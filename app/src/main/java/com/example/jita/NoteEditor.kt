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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.jita.data.NoteDao
import com.example.jita.data.NoteEntity
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
import androidx.compose.ui.graphics.toArgb
import org.json.JSONArray
import org.json.JSONObject
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.unit.DpSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars

// Add this sealed class for handling image URIs safely
sealed class ImageUriState {
    data class Success(val uri: android.net.Uri) : ImageUriState()
    object Error : ImageUriState()
    object NotFound : ImageUriState()
}

// Define a consistent blue color for collapsible headers
val collapsibleBlue = Color(0xFF2196F3) // Material Blue

// Define data class for checkbox items
data class CheckboxItem(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "Checkbox item",
    var isChecked: Boolean = false
)

// Define data class for voice recordings
data class VoiceRecording(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val durationMs: Long,
    val recordedAt: Long = System.currentTimeMillis()
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

// Define data class for file attachments
data class FileAttachment(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val attachedAt: Long = System.currentTimeMillis()
)

// Define data class for image attachments
data class ImageAttachment(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val attachedAt: Long = System.currentTimeMillis()
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

// Convert JSON array to voice recordings list
private fun JSONArray?.toVoiceRecordingsList(): List<VoiceRecording> {
    if (this == null) return emptyList()
    
    return try {
        val recordings = mutableListOf<VoiceRecording>()
        for (i in 0 until this.length()) {
            val item = this.getJSONObject(i)
            recordings.add(
                VoiceRecording(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    fileName = item.optString("fileName", "Recording"),
                    filePath = item.optString("filePath", ""),
                    durationMs = item.optLong("durationMs", 0),
                    recordedAt = item.optLong("recordedAt", System.currentTimeMillis())
                )
            )
        }
        recordings
    } catch (e: Exception) {
        android.util.Log.e("NoteEditor", "Error parsing voice recordings: ${e.message}")
        emptyList()
    }
}

// Helper function to create necessary directories for voice recordings
private fun createVoiceRecordingDirectory(context: android.content.Context): File {
    val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "jita_notes")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory
}

// Generate unique filename for voice recording
private fun generateVoiceRecordingFilename(): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return "voice_recording_${timestamp}.mp3"
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

    // Add state for file attachments
    var fileAttachments by remember { mutableStateOf(listOf<FileAttachment>()) }
    var showAttachmentSection by remember { mutableStateOf(true) }

    // Add state for image attachments
    var imageAttachments by remember { mutableStateOf(listOf<ImageAttachment>()) }
    var showImageSection by remember { mutableStateOf(true) }

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
    
    // Note background color state
    var noteBackgroundColor by remember { mutableStateOf(Color.White) }
    var showNoteBackgroundColorPicker by remember { mutableStateOf(false) }

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

    // Add state for voice recordings
    var voiceRecordings by remember { mutableStateOf(listOf<VoiceRecording>()) }
    var showVoiceRecordingDialog by remember { mutableStateOf(false) }

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
                    
                    // Load note background color if it exists
                    try {
                        it.color?.let { colorHex ->
                            noteBackgroundColor = Color(android.graphics.Color.parseColor(colorHex))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NoteEditor", "Error loading note color: ${e.message}")
                    }
                    
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
                    
                    // Load voice recordings if they exist
                    try {
                        it.voiceRecordings?.let { recordingsJson ->
                            val recordingsArray = JSONArray(recordingsJson)
                            voiceRecordings = recordingsArray.toVoiceRecordingsList()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NoteEditor", "Error loading voice recordings: ${e.message}")
                    }
                    
                    // Load file attachments if they exist
                    try {
                        it.fileAttachments?.let { attachmentsJson ->
                            val attachmentsArray = JSONArray(attachmentsJson)
                            fileAttachments = attachmentsArray.toFileAttachmentsList()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NoteEditor", "Error loading file attachments: ${e.message}")
                    }
                    
                    // Load image attachments if they exist
                    try {
                        it.imageAttachments?.let { imagesJson ->
                            val imagesArray = JSONArray(imagesJson)
                            imageAttachments = imagesArray.toImageAttachmentsList()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NoteEditor", "Error loading image attachments: ${e.message}")
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

    // Update save function to save with file attachments
    fun saveNoteAndNavigate(navigateUp: Boolean = false) {
        scope.launch {
            // Only save if there's content
            if (title.isNotBlank() || textFieldValue.text.isNotBlank() || checkboxItems.isNotEmpty() || fileAttachments.isNotEmpty() || imageAttachments.isNotEmpty()) {
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
                    
                    // Save voice recordings as JSON
                    val voiceRecordingsJson = if (voiceRecordings.isNotEmpty()) {
                        JSONArray(voiceRecordings.map { recording ->
                            JSONObject().apply {
                                put("id", recording.id)
                                put("fileName", recording.fileName)
                                put("filePath", recording.filePath)
                                put("durationMs", recording.durationMs)
                                put("recordedAt", recording.recordedAt)
                            }
                        }).toString()
                    } else {
                        null
                    }
                    
                    // Save file attachments as JSON
                    val fileAttachmentsJson = if (fileAttachments.isNotEmpty()) {
                        JSONArray(fileAttachments.map { attachment ->
                            JSONObject().apply {
                                put("id", attachment.id)
                                put("fileName", attachment.fileName)
                                put("filePath", attachment.filePath)
                                put("fileSizeBytes", attachment.fileSizeBytes)
                                put("mimeType", attachment.mimeType)
                                put("attachedAt", attachment.attachedAt)
                            }
                        }).toString()
                    } else {
                        null
                    }

                    // Save image attachments as JSON
                    val imageAttachmentsJson = if (imageAttachments.isNotEmpty()) {
                        JSONArray(imageAttachments.map { image ->
                            JSONObject().apply {
                                put("id", image.id)
                                put("fileName", image.fileName)
                                put("filePath", image.filePath)
                                put("fileSizeBytes", image.fileSizeBytes)
                                put("attachedAt", image.attachedAt)
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
                            checkboxItems = checkboxItemsJson,
                            voiceRecordings = voiceRecordingsJson,
                            fileAttachments = fileAttachmentsJson,
                            imageAttachments = imageAttachmentsJson,
                            color = String.format("#%08X", noteBackgroundColor.toArgb())
                        )
                    } else {
                        NoteEntity(
                            title = title.ifBlank { "Untitled" },
                            content = textFieldValue.text,
                            folderId = noteFolderId,
                            createdAt = noteTimestamp,
                            updatedAt = noteTimestamp,
                            styles = stylesToSave,
                            checkboxItems = checkboxItemsJson,
                            voiceRecordings = voiceRecordingsJson,
                            fileAttachments = fileAttachmentsJson,
                            imageAttachments = imageAttachmentsJson,
                            color = String.format("#%08X", noteBackgroundColor.toArgb())
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

    // Add file picker launcher for selecting files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val newAttachments = uris.mapNotNull { uri ->
                    try {
                        // Create directory for attachments
                        val attachmentDir = createAttachmentDirectory(context)
                        
                        // Get file details
                        val fileName = getFileName(context, uri) ?: "unknown_file"
                        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                        
                        // Create a unique file name to avoid collisions
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val uniqueFileName = "${timestamp}_${fileName}"
                        val targetFile = File(attachmentDir, uniqueFileName)
                        
                        // Copy the file to our directory
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            targetFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        
                        // Create attachment object
                        FileAttachment(
                            fileName = fileName,
                            filePath = targetFile.absolutePath,
                            fileSizeBytes = targetFile.length(),
                            mimeType = mimeType
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("NoteEditor", "Error adding attachment: ${e.message}")
                        null
                    }
                }
                
                // Add new attachments to the list
                if (newAttachments.isNotEmpty()) {
                    fileAttachments = fileAttachments + newAttachments
                }
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val newImages = uris.mapNotNull { uri ->
                    try {
                        // Create directory for images
                        val imageDir = createImageDirectory(context)
                        
                        // Get file details
                        val fileName = getFileName(context, uri) ?: "unknown_image"
                        val mimeType = context.contentResolver.getType(uri) ?: "image/*"
                        
                        // Check if it's an image
                        if (!mimeType.startsWith("image/")) {
                            android.widget.Toast.makeText(
                                context,
                                "Only image files are supported",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            return@mapNotNull null
                        }
                        
                        // Create a unique file name to avoid collisions
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val uniqueFileName = "${timestamp}_${fileName}"
                        val targetFile = File(imageDir, uniqueFileName)
                        
                        // Copy the file to our directory
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            targetFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        
                        // Create image attachment object
                        ImageAttachment(
                            fileName = fileName,
                            filePath = targetFile.absolutePath,
                            fileSizeBytes = targetFile.length()
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("NoteEditor", "Error adding image: ${e.message}")
                        null
                    }
                }
                
                // Add new images to the list
                if (newImages.isNotEmpty()) {
                    imageAttachments = imageAttachments + newImages
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = noteBackgroundColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        // Save note before navigating back, but avoid double navigation
                        scope.launch {
                            saveNoteAndNavigate(navigateUp = true)
                            // Removed navController.popBackStack() to avoid double navigation
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = noteBackgroundColor,
                    titleContentColor = contentColorFor(noteBackgroundColor),
                    navigationIconContentColor = contentColorFor(noteBackgroundColor),
                    actionIconContentColor = contentColorFor(noteBackgroundColor)
                ),
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
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = noteBackgroundColor,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .horizontalScroll(rememberScrollState()),
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
                    IconButton(onClick = { 
                        showVoiceRecordingDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Recording",
                            tint = Color.DarkGray
                        )
                    }

                    // Link button for attaching files - Updated to use file picker
                    IconButton(onClick = { 
                        filePickerLauncher.launch("*/*") 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Attach Files",
                            tint = Color.DarkGray
                        )
                    }

                    // Image button - update to use image picker
                    IconButton(onClick = { 
                        imagePickerLauncher.launch("image/*") 
                    }) {
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

                    // Note background color button
                    IconButton(onClick = { showNoteBackgroundColorPicker = !showNoteBackgroundColorPicker }) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Note Background Color",
                            tint = Color.DarkGray
                        )
                    }



                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
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
                    onFontChanged = { applyFont(it) },
                    backgroundColor = noteBackgroundColor
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

            // Voice Recordings section - display if there are any recordings
            AnimatedVisibility(
                visible = voiceRecordings.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    // Add collapsible header with state
                    var isVoiceRecordingsSectionExpanded by remember { mutableStateOf(false) }
                    
                    // Header row with toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isVoiceRecordingsSectionExpanded = !isVoiceRecordingsSectionExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Voice Recordings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = collapsibleBlue
                        )
                        
                        // Arrow icon that rotates based on expanded state
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isVoiceRecordingsSectionExpanded) 
                                "Collapse" else "Expand",
                            modifier = Modifier
                                .rotate(if (isVoiceRecordingsSectionExpanded) 0f else 180f)
                                .size(20.dp),
                            tint = collapsibleBlue
                        )
                    }
                    
                    // Collapsible content
                    AnimatedVisibility(
                        visible = isVoiceRecordingsSectionExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            // Display voice recordings
                            voiceRecordings.forEach { recording ->
                                VoiceRecordingItem(
                                    recording = recording,
                                    voiceRecordings = voiceRecordings,
                                    onVoiceRecordingsChange = { newRecordings ->
                                        voiceRecordings = newRecordings
                                    },
                                    onDelete = {
                                        voiceRecordings = voiceRecordings.filter { it.id != recording.id }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // File Attachments section - display if there are any attachments
            AnimatedVisibility(
                visible = fileAttachments.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    // Add collapsible header with state
                    var isAttachmentSectionExpanded by remember { mutableStateOf(false) }
                    
                    // Header row with toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAttachmentSectionExpanded = !isAttachmentSectionExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Attachments",
                            style = MaterialTheme.typography.bodyMedium,
                            color = collapsibleBlue
                        )
                        
                        // Arrow icon that rotates based on expanded state
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isAttachmentSectionExpanded) 
                                "Collapse" else "Expand",
                            modifier = Modifier
                                .rotate(if (isAttachmentSectionExpanded) 0f else 180f)
                                .size(20.dp),
                            tint = collapsibleBlue
                        )
                    }
                    
                    // Collapsible content
                    AnimatedVisibility(
                        visible = isAttachmentSectionExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            // Display file attachments
                            fileAttachments.forEach { attachment ->
                                FileAttachmentItem(
                                    attachment = attachment,
                                    onDelete = {
                                        fileAttachments = fileAttachments.filter { it.id != attachment.id }
                                        // Also delete the physical file
                                        try {
                                            File(attachment.filePath).delete()
                                        } catch (e: Exception) {
                                            android.util.Log.e("NoteEditor", "Error deleting file: ${e.message}")
                                        }
                                    },
                                    onDownload = { downloadLocation ->
                                        downloadAttachmentToCustomLocation(context, attachment, downloadLocation)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Image Attachments section - display if there are any images
            AnimatedVisibility(
                visible = imageAttachments.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    // Add collapsible header with state - change default to false
                    var isImageSectionExpanded by remember { mutableStateOf(false) }
                    
                    // Header row with toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isImageSectionExpanded = !isImageSectionExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Images",
                            style = MaterialTheme.typography.bodyMedium,
                            color = collapsibleBlue
                        )
                        
                        // Arrow icon that rotates based on expanded state
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isImageSectionExpanded) 
                                "Collapse" else "Expand",
                            modifier = Modifier
                                .rotate(if (isImageSectionExpanded) 0f else 180f)
                                .size(20.dp),
                            tint = collapsibleBlue
                        )
                    }
                    
                    // Collapsible content
                    AnimatedVisibility(
                        visible = isImageSectionExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            // Replace individual image items with image slider
                            if (imageAttachments.isNotEmpty()) {
                                ImageSlider(
                                    images = imageAttachments,
                                    onDeleteImage = { image ->
                                        imageAttachments = imageAttachments.filter { it.id != image.id }
                                        // Also delete the physical file
                                        try {
                                            File(image.filePath).delete()
                                        } catch (e: Exception) {
                                            android.util.Log.e("NoteEditor", "Error deleting image: ${e.message}")
                                        }
                                    },
                                    onDownloadImage = { image, uri ->
                                        downloadImageToCustomLocation(context, image, uri)
                                    }
                                )
                            }
                        }
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

    // Display voice recording dialog when active
    if (showVoiceRecordingDialog) {
        VoiceRecordingDialog(
            onDismiss = { 
                showVoiceRecordingDialog = false 
            },
            onSaveRecording = { recording ->
                voiceRecordings = voiceRecordings + recording
                showVoiceRecordingDialog = false
            }
        )
    }

    // Set focus to title when screen is first displayed if creating a new note
    LaunchedEffect(Unit) {
        if (noteId <= 0) {
            titleFocusRequester.requestFocus()
        }
    }

    // Show note background color picker dialog if requested
    if (showNoteBackgroundColorPicker) {
        Dialog(onDismissRequest = { showNoteBackgroundColorPicker = false }) {
            // Use a slightly lighter version of the background color for the dialog
            val dialogBackgroundColor = Color.White.copy(alpha = 0.95f)
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = dialogBackgroundColor,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Select Note Background Color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Color grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(300.dp)
                    ) {
                        // Add white/default option
                        item {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (noteBackgroundColor == Color.White) 2.dp else 0.dp,
                                        color = if (noteBackgroundColor == Color.White) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        noteBackgroundColor = Color.White
                                        showNoteBackgroundColorPicker = false
                                    }
                            )
                        }
                        
                        // Pastel Colors Header
                        item(span = { GridItemSpan(5) }) {
                            Text(
                                text = "Pastel Colors",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        
                        // Add pastel color options
                        val pastelColors = listOf(
                            // Pastel Colors
                            Color(0xFFFFF9C4), // Light yellow
                            Color(0xFFFFCCBC), // Light orange
                            Color(0xFFBBDEFB), // Light blue
                            Color(0xFFDCEDC8), // Light green
                            Color(0xFFF8BBD0), // Light pink
                            Color(0xFFE1BEE7), // Light purple
                            Color(0xFFE0F7FA), // Light cyan
                            Color(0xFFF5F5F5), // Light grey
                            Color(0xFFFFE0B2), // Light amber
                            Color(0xFFD7CCC8), // Light brown
                            Color(0xFFCFD8DC),  // Light blue grey
                            Color(0xFFFCE4EC), // Lighter pink
                            Color(0xFFF3E5F5), // Lighter purple
                            Color(0xFFE8EAF6), // Indigo light
                            Color(0xFFE1F5FE), // Lighter blue
                            Color(0xFFE0F2F1), // Teal light
                            Color(0xFFF1F8E9), // Lighter green
                            Color(0xFFFFFDE7), // Lighter yellow
                            Color(0xFFFFF3E0), // Lighter orange
                            Color(0xFFEFEBE9) // Lighter brown
                        )
                        
                        items(pastelColors.size) { index ->
                            val color = pastelColors[index]
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color, RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (noteBackgroundColor.toArgb() == color.toArgb()) 2.dp else 0.dp,
                                        color = if (noteBackgroundColor.toArgb() == color.toArgb()) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        noteBackgroundColor = color
                                        showNoteBackgroundColorPicker = false
                                    }
                            )
                        }
                        
                        // Vibrant Colors Header
                        item(span = { GridItemSpan(5) }) {
                            Text(
                                text = "Vibrant Colors",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        
                        // Add vibrant color options
                        val vibrantColors = listOf(
                            // More vibrant colors
                            Color(0xFFFFEB3B), // Yellow
                            Color(0xFFFF9800), // Orange
                            Color(0xFF2196F3), // Blue
                            Color(0xFF4CAF50), // Green
                            Color(0xFFE91E63), // Pink
                            Color(0xFF9C27B0), // Purple
                            Color(0xFF00BCD4), // Cyan
                            Color(0xFF607D8B), // Blue Grey
                            Color(0xFFFF5722), // Deep Orange
                            Color(0xFF795548), // Brown
                            Color(0xFF9E9E9E), // Grey
                            Color(0xFFFF4081), // Pink accent
                            Color(0xFF536DFE), // Indigo accent
                            Color(0xFF03A9F4), // Light Blue
                            Color(0xFF009688), // Teal
                            Color(0xFF8BC34A), // Light Green
                            Color(0xFFFFC107), // Amber
                            Color(0xFF673AB7), // Deep Purple
                            Color(0xFF3F51B5), // Indigo
                            Color(0xFFCDDC39)  // Lime
                        )
                        
                        items(vibrantColors.size) { index ->
                            val color = vibrantColors[index]
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color, RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (noteBackgroundColor.toArgb() == color.toArgb()) 2.dp else 0.dp,
                                        color = if (noteBackgroundColor.toArgb() == color.toArgb()) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        noteBackgroundColor = color
                                        showNoteBackgroundColorPicker = false
                                    }
                            )
                        }
                        
                        // Dark and Muted Colors Header
                        item(span = { GridItemSpan(5) }) {
                            Text(
                                text = "Dark & Muted Colors",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        
                        // Add dark and muted color options
                        val darkMutedColors = listOf(
                            Color(0xFFEFEBE9), // Beige
                            Color(0xFFD7CCC8), // Taupe
                            Color(0xFF90A4AE), // Blue Grey 300
                            Color(0xFFBDBDBD), // Grey 400
                            Color(0xFFB0BEC5), // Blue Grey 200
                            Color(0xFF78909C), // Blue Grey 400
                            Color(0xFF546E7A), // Blue Grey 600
                            Color(0xFF455A64), // Blue Grey 700
                            Color(0xFF37474F), // Blue Grey 800
                            Color(0xFF212121), // Grey 900
                            Color(0xFF283593), // Indigo 800
                            Color(0xFF1A237E), // Indigo 900
                            Color(0xFF0D47A1), // Blue 900
                            Color(0xFF004D40), // Teal 900
                            Color(0xFF1B5E20), // Green 900
                            Color(0xFF880E4F), // Pink 900
                            Color(0xFF311B92), // Deep Purple 900
                            Color(0xFF263238), // Blue Grey 900
                            Color(0xFF3E2723), // Brown 900
                            Color(0xFF4A148C)  // Purple 900
                        )
                        
                        items(darkMutedColors.size) { index ->
                            val color = darkMutedColors[index]
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color, RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (noteBackgroundColor.toArgb() == color.toArgb()) 2.dp else 0.dp,
                                        color = if (noteBackgroundColor.toArgb() == color.toArgb()) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        noteBackgroundColor = color
                                        showNoteBackgroundColorPicker = false
                                    }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Cancel button
                    Button(
                        onClick = { showNoteBackgroundColorPicker = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
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
    onFontChanged: (String) -> Unit,
    backgroundColor: Color = Color.White
) {
    val scrollState = rememberScrollState()

    // Use a slightly darker version of the background color for the toolbar
    val toolbarBackgroundColor = backgroundColor.copy(
        red = (backgroundColor.red * 0.95f).coerceIn(0f, 1f),
        green = (backgroundColor.green * 0.95f).coerceIn(0f, 1f),
        blue = (backgroundColor.blue * 0.95f).coerceIn(0f, 1f),
        alpha = 1f
    )

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
                .background(toolbarBackgroundColor, RoundedCornerShape(8.dp))
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
                    .background(backgroundColor, RoundedCornerShape(8.dp))
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
                    Color(0xFFE1BEE7),  // Light purple
                    Color(0xFFE0F7FA), // Light cyan
                    Color(0xFFF5F5F5), // Light grey
                    Color(0xFFFFE0B2), // Light amber
                    Color(0xFFD7CCC8), // Light brown
                    Color(0xFFCFD8DC),  // Light blue grey
                    Color(0xFFFCE4EC), // Lighter pink
                    Color(0xFFF3E5F5), // Lighter purple
                    Color(0xFFE8EAF6), // Indigo light
                    Color(0xFFE1F5FE), // Lighter blue
                    Color(0xFFE0F2F1), // Teal light
                    Color(0xFFF1F8E9), // Lighter green
                    Color(0xFFFFFDE7), // Lighter yellow
                    Color(0xFFFFF3E0), // Lighter orange
                    Color(0xFFEFEBE9), // Lighter brown
                    Color(0xFFFAFAFA), // Almost white
                    Color(0xFFF0F4C3), // Lime light
                    Color(0xFFD1C4E9), // Deep purple light
                    Color(0xFFB3E5FC)  // Darker blue
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

// Voice recording dialog
@Composable
fun VoiceRecordingDialog(
    onDismiss: () -> Unit,
    onSaveRecording: (VoiceRecording) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // States for recording
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val updateInterval = 100L // Update timer every 100ms
    
    // Format duration as mm:ss
    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    // Function to stop recording - must be declared before startRecording since it's used there
    fun stopRecording(save: Boolean) {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                
                isRecording = false
                
                // Save recording if requested
                if (save && recordingFile != null) {
                    val recording = VoiceRecording(
                        fileName = recordingFile?.name ?: "voice_recording.mp3",
                        filePath = recordingFile?.absolutePath ?: "",
                        durationMs = recordingDuration
                    )
                    onSaveRecording(recording)
                } else {
                    // Delete file if not saving
                    recordingFile?.delete()
                }
            } catch (e: Exception) {
                android.util.Log.e("VoiceRecordingDialog", "Error stopping recording: ${e.message}")
                recordingFile?.delete()
            }
        }
        
        mediaRecorder = null
    }
    
    // Function to start recording
    fun startRecording() {
        try {
            // Create directory
            val recordingDir = createVoiceRecordingDirectory(context)
            val fileName = generateVoiceRecordingFilename()
            recordingFile = File(recordingDir, fileName)
            
            // Initialize media recorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recordingFile?.absolutePath)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                
                try {
                    prepare()
                    start()
                    isRecording = true
                    recordingDuration = 0L
                    
                    // Start timer
                    val startTime = System.currentTimeMillis()
                    handler.post(object : Runnable {
                        override fun run() {
                            if (isRecording) {
                                recordingDuration = System.currentTimeMillis() - startTime
                                handler.postDelayed(this, updateInterval)
                            }
                        }
                    })
                } catch (e: IOException) {
                    android.util.Log.e("VoiceRecordingDialog", "Failed to start recording: ${e.message}")
                    stopRecording(save = false)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceRecordingDialog", "Error starting recording: ${e.message}")
            onDismiss()
        }
    }
    
    // Permissions
    val hasRecordPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Start recording automatically after permission granted
            startRecording()
        } else {
            // Permission denied, dismiss dialog
            onDismiss()
        }
    }
    
    // Check and request permission when dialog opens
    LaunchedEffect(Unit) {
        if (!hasRecordPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    // Cleanup when dialog is dismissed
    DisposableEffect(Unit) {
        onDispose {
            stopRecording(save = false)
        }
    }
    
    // Start recording on first render if permission is granted
    LaunchedEffect(hasRecordPermission) {
        if (hasRecordPermission && !isRecording) {
            startRecording()
        }
    }
    
    Dialog(
        onDismissRequest = {
            stopRecording(save = false)
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Voice Recording",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Recording animation
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            if (isRecording) Color.Red.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Recording",
                        tint = if (isRecording) Color.Red else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Timer
                Text(
                    text = formatDuration(recordingDuration),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isRecording) Color.Red else Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            stopRecording(save = false)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (isRecording) {
                                stopRecording(save = true)
                            } else {
                                startRecording()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) MaterialTheme.colorScheme.primary else Color.Red
                        )
                    ) {
                        Text(if (isRecording) "Stop & Save" else "Record")
                    }
                }
            }
        }
    }
}

// Voice recording item to display in the note
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRecordingItem(
    recording: VoiceRecording,
    voiceRecordings: List<VoiceRecording>,
    onVoiceRecordingsChange: (List<VoiceRecording>) -> Unit,
    onDelete: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPosition by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(recording.durationMs.toInt()) }
    
    // Add state for delete confirmation dialog
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Add states for renaming
    var isRenaming by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf(recording.fileName) }
    
    val context = LocalContext.current
    
    // Timer to update current position during playback
    val handler = remember { Handler(Looper.getMainLooper()) }
    
    // Format duration
    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    // Format date
    val formattedDate = remember(recording.recordedAt) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(recording.recordedAt))
    }
    
    // Function to rename the physical file
    fun renameFile(newName: String): VoiceRecording {
        try {
            // Get old file
            val oldFile = File(recording.filePath)
            if (!oldFile.exists()) {
                return recording
            }
            
            // Ensure we keep the extension
            val extension = recording.fileName.substring(recording.fileName.lastIndexOf("."))
            val nameWithExtension = if (newName.endsWith(extension)) newName else "$newName$extension"
            
            // Create new file path
            val parentDir = oldFile.parentFile
            val newFile = File(parentDir, nameWithExtension)
            
            // Rename the file
            val success = oldFile.renameTo(newFile)
            
            // Return updated recording if successful
            return if (success) {
                recording.copy(
                    fileName = nameWithExtension,
                    filePath = newFile.absolutePath
                )
            } else {
                recording
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceRecordingItem", "Error renaming file: ${e.message}")
            return recording
        }
    }
    
    // Update position when playing
    LaunchedEffect(isPlaying) {
        val positionUpdater = object : Runnable {
            override fun run() {
                if (isPlaying && mediaPlayer != null) {
                    try {
                        currentPosition = mediaPlayer?.currentPosition ?: 0
                        handler.postDelayed(this, 100)
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
        }
        
        if (isPlaying) {
            handler.post(positionUpdater)
        } else {
            handler.removeCallbacksAndMessages(null)
        }
    }
    
    // Clean up media player when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            handler.removeCallbacksAndMessages(null)
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        }
    }
    
    // Function to play/pause recording
    fun togglePlayback() {
        if (isPlaying) {
            mediaPlayer?.apply {
                if (isPlaying()) {
                    pause()
                }
            }
            isPlaying = false
        } else {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(recording.filePath)
                        prepare()
                        totalDuration = duration
                        
                        setOnCompletionListener {
                            isPlaying = false
                            currentPosition = 0
                            // Don't release, allow for replay
                        }
                    }
                }
                
                mediaPlayer?.start()
                isPlaying = true
            } catch (e: Exception) {
                android.util.Log.e("VoiceRecordingItem", "Error playing recording: ${e.message}")
            }
        }
    }
    
    // Function to seek to position
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        currentPosition = position
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Recording") },
            text = { Text("Are you sure you want to delete this voice recording?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/pause button
                IconButton(
                    onClick = { togglePlayback() }
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Recording info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    if (isRenaming) {
                        // Editable text field for renaming
                        OutlinedTextField(
                            value = newFileName,
                            onValueChange = { newFileName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newFileName.isNotBlank()) {
                                        // Update the file name in the file system
                                        val updatedRecording = renameFile(newFileName)
                                        
                                        // Update the recording in the voiceRecordings list
                                        onVoiceRecordingsChange(voiceRecordings.map { 
                                            if (it.id == recording.id) updatedRecording else it 
                                        })
                                    }
                                    isRenaming = false
                                }
                            )
                        )
                    } else {
                        // Clickable text to initiate rename
                        Text(
                            text = recording.fileName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            modifier = Modifier.clickable { isRenaming = true }
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                // Delete button - updated to show confirmation dialog
                IconButton(
                    onClick = { showDeleteConfirmation = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Recording",
                        tint = Color.Gray
                    )
                }
            }
            
            // Scrubber and time display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current position
                Text(
                    text = formatDuration(currentPosition.toLong()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.width(50.dp)
                )
                
                // Scrubber/slider
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { newPosition -> 
                        seekTo(newPosition.toInt())
                    },
                    valueRange = 0f..totalDuration.toFloat(),
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.LightGray
                    ),
                    thumb = {
                        SliderDefaults.Thumb(
                            interactionSource = remember { MutableInteractionSource() },
                            thumbSize = DpSize(12.dp, 12.dp),  // Smaller thumb size (default is typically 20dp)
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                )
                
                // Total duration
                Text(
                    text = formatDuration(totalDuration.toLong()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.width(50.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

// Helper functions for file attachments
private fun createAttachmentDirectory(context: android.content.Context): File {
    val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "jita_notes/attachments")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory
}

private fun getFileName(context: android.content.Context, uri: android.net.Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    result = it.getString(nameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1 && cut != null) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

private fun downloadAttachmentToCustomLocation(context: android.content.Context, attachment: FileAttachment, downloadLocation: android.net.Uri?) {
    if (downloadLocation == null) return
    
    try {
        context.contentResolver.openOutputStream(downloadLocation)?.use { outputStream ->
            File(attachment.filePath).inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("NoteEditor", "Error downloading attachment: ${e.message}")
    }
}

// Convert JSON array to file attachments list
private fun JSONArray?.toFileAttachmentsList(): List<FileAttachment> {
    if (this == null) return emptyList()
    
    return try {
        val attachments = mutableListOf<FileAttachment>()
        for (i in 0 until this.length()) {
            val item = this.getJSONObject(i)
            attachments.add(
                FileAttachment(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    fileName = item.optString("fileName", "Unnamed file"),
                    filePath = item.optString("filePath", ""),
                    fileSizeBytes = item.optLong("fileSizeBytes", 0),
                    mimeType = item.optString("mimeType", "application/octet-stream"),
                    attachedAt = item.optLong("attachedAt", System.currentTimeMillis())
                )
            )
        }
        attachments
    } catch (e: Exception) {
        android.util.Log.e("NoteEditor", "Error parsing file attachments: ${e.message}")
        emptyList()
    }
}

// File attachment item UI component
@Composable
fun FileAttachmentItem(
    attachment: FileAttachment,
    onDelete: () -> Unit,
    onDownload: (android.net.Uri?) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Format file size
    val formattedSize = remember(attachment.fileSizeBytes) {
        when {
            attachment.fileSizeBytes < 1024 -> "${attachment.fileSizeBytes} B"
            attachment.fileSizeBytes < 1024 * 1024 -> "${attachment.fileSizeBytes / 1024} KB"
            else -> String.format("%.1f MB", attachment.fileSizeBytes / (1024.0 * 1024.0))
        }
    }
    
    // Format date
    val formattedDate = remember(attachment.attachedAt) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(attachment.attachedAt))
    }
    
    // File download launcher
    val downloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri -> 
        onDownload(uri)
    }
    
    // Function to open the file
    fun openFile() {
        try {
            val file = File(attachment.filePath)
            if (file.exists()) {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, attachment.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                // Check if there's an app that can handle this file type
                val packageManager = context.packageManager
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    // If no app can handle this file type directly, try with a more generic mime type
                    val genericIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "*/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    try {
                        context.startActivity(Intent.createChooser(
                            genericIntent, 
                            "Open ${attachment.fileName} with..."
                        ))
                    } catch (e: Exception) {
                        // Show toast if no app can open the file
                        android.widget.Toast.makeText(
                            context,
                            "No application found that can open this file type",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // Show toast if file doesn't exist
                android.widget.Toast.makeText(
                    context,
                    "File not found",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("FileAttachmentItem", "Error opening file: ${e.message}")
            // Show error toast
            android.widget.Toast.makeText(
                context,
                "Error opening file: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Attachment") },
            text = { Text("Are you sure you want to delete this attachment?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Icon based on mime type
    val fileIcon = remember(attachment.mimeType) {
        when {
            attachment.mimeType.startsWith("image/") -> Icons.Default.Image
            attachment.mimeType.startsWith("audio/") -> Icons.Default.MusicNote
            attachment.mimeType.startsWith("video/") -> Icons.Default.Videocam
            attachment.mimeType.startsWith("text/") -> Icons.Default.TextSnippet
            attachment.mimeType.contains("pdf") -> Icons.Default.PictureAsPdf
            attachment.mimeType.contains("word") || attachment.mimeType.contains("document") -> Icons.Default.Description
            attachment.mimeType.contains("excel") || attachment.mimeType.contains("sheet") -> Icons.Default.GridOn
            attachment.mimeType.contains("presentation") || attachment.mimeType.contains("powerpoint") -> Icons.Default.Preview
            else -> Icons.Default.AttachFile
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { openFile() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Icon(
                imageVector = fileIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            // File details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            // Download button
            IconButton(
                onClick = { 
                    downloadLauncher.launch(attachment.fileName)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Download",
                    tint = Color.Gray
                )
            }
            
            // Delete button
            IconButton(
                onClick = { showDeleteConfirmation = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Attachment",
                    tint = Color.Gray
                )
            }
        }
    }
}

// Helper functions for image attachments
private fun createImageDirectory(context: android.content.Context): File {
    val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "jita_notes/images")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory
}

// Convert JSON array to image attachments list
private fun JSONArray?.toImageAttachmentsList(): List<ImageAttachment> {
    if (this == null) return emptyList()
    
    return try {
        val images = mutableListOf<ImageAttachment>()
        for (i in 0 until this.length()) {
            val item = this.getJSONObject(i)
            images.add(
                ImageAttachment(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    fileName = item.optString("fileName", "Unnamed image"),
                    filePath = item.optString("filePath", ""),
                    fileSizeBytes = item.optLong("fileSizeBytes", 0),
                    attachedAt = item.optLong("attachedAt", System.currentTimeMillis())
                )
            )
        }
        images
    } catch (e: Exception) {
        android.util.Log.e("NoteEditor", "Error parsing image attachments: ${e.message}")
        emptyList()
    }
}

private fun downloadImageToCustomLocation(context: android.content.Context, image: ImageAttachment, downloadLocation: android.net.Uri?) {
    if (downloadLocation == null) return
    
    try {
        context.contentResolver.openOutputStream(downloadLocation)?.use { outputStream ->
            File(image.filePath).inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("NoteEditor", "Error downloading image: ${e.message}")
    }
}

// Image attachment item UI component
@Composable
fun ImageAttachmentItem(
    image: ImageAttachment,
    onDelete: () -> Unit,
    onDownload: (android.net.Uri?) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Format file size
    val formattedSize = remember(image.fileSizeBytes) {
        when {
            image.fileSizeBytes < 1024 -> "${image.fileSizeBytes} B"
            image.fileSizeBytes < 1024 * 1024 -> "${image.fileSizeBytes / 1024} KB"
            else -> String.format("%.1f MB", image.fileSizeBytes / (1024.0 * 1024.0))
        }
    }
    
    // Format date
    val formattedDate = remember(image.attachedAt) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(image.attachedAt))
    }
    
    // File download launcher
    val downloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/*")
    ) { uri -> 
        onDownload(uri)
    }
    
    // Function to open the image
    fun openImage() {
        try {
            val file = File(image.filePath)
            if (file.exists()) {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(intent)
            } else {
                // Show toast if file doesn't exist
                android.widget.Toast.makeText(
                    context,
                    "Image not found",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageAttachmentItem", "Error opening image: ${e.message}")
            // Show error toast
            android.widget.Toast.makeText(
                context,
                "Error opening image: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { openImage() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image thumbnail
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            ) {
                // Load image thumbnail if file exists
                val imageFile = File(image.filePath)
                if (imageFile.exists()) {
                    val imageUri = androidx.core.content.FileProvider.getUriForFile(
                        context, 
                        "${context.packageName}.fileprovider", 
                        imageFile
                    )
                    
                    // Use AsyncImage from Coil or other image loading library if available
                    // For simplicity, using an Icon here
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            
            // Image details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = image.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            // Download button
            IconButton(
                onClick = { 
                    downloadLauncher.launch(image.fileName)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Download",
                    tint = Color.Gray
                )
            }
            
            // Delete button
            IconButton(
                onClick = { showDeleteConfirmation = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Image",
                    tint = Color.Gray
                )
            }
        }
    }
}

// Image slider component
@Composable
fun ImageSlider(
    images: List<ImageAttachment>,
    onDeleteImage: (ImageAttachment) -> Unit,
    onDownloadImage: (ImageAttachment, android.net.Uri?) -> Unit
) {
    val context = LocalContext.current
    var currentImageIndex by remember { mutableStateOf(0) }
    val currentImage = images[currentImageIndex]
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // File download launcher
    val downloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/*")
    ) { uri -> 
        onDownloadImage(currentImage, uri)
    }
    
    // Function to open the image
    fun openImage(image: ImageAttachment) {
        try {
            val file = File(image.filePath)
            if (file.exists()) {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(intent)
            } else {
                // Show toast if file doesn't exist
                android.widget.Toast.makeText(
                    context,
                    "Image not found",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageSlider", "Error opening image: ${e.message}")
            // Show error toast
            android.widget.Toast.makeText(
                context,
                "Error opening image: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Format file size
    val formattedSize = remember(currentImage.fileSizeBytes) {
        when {
            currentImage.fileSizeBytes < 1024 -> "${currentImage.fileSizeBytes} B"
            currentImage.fileSizeBytes < 1024 * 1024 -> "${currentImage.fileSizeBytes / 1024} KB"
            else -> String.format("%.1f MB", currentImage.fileSizeBytes / (1024.0 * 1024.0))
        }
    }
    
    // Format date
    val formattedDate = remember(currentImage.attachedAt) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(currentImage.attachedAt))
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteImage(currentImage)
                        showDeleteConfirmation = false
                        // Update current index if needed
                        if (currentImageIndex >= images.size - 1 && currentImageIndex > 0) {
                            currentImageIndex--
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Image preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .clickable { openImage(currentImage) },
                contentAlignment = Alignment.Center
            ) {
                // Load image if file exists
                val imageFile = File(currentImage.filePath)
                
                // Use remember to handle the potentially throwing code outside the Composable execution
                val imageUriState = remember(imageFile.absolutePath) {
                    if (imageFile.exists()) {
                        try {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                imageFile
                            )
                            ImageUriState.Success(uri)
                        } catch (e: Exception) {
                            android.util.Log.e("ImageSlider", "Error creating URI: ${e.message}")
                            ImageUriState.Error
                        }
                    } else {
                        ImageUriState.NotFound
                    }
                }
                
                // Load and display the actual image bitmap if available
                val bitmap = remember(imageFile.absolutePath) {
                    if (imageFile.exists()) {
                        try {
                            val options = BitmapFactory.Options().apply {
                                inJustDecodeBounds = false
                                // Scale down large images to avoid OutOfMemoryError
                                if (outHeight > 1024 || outWidth > 1024) {
                                    val scale = maxOf(
                                        outHeight / 1024f,
                                        outWidth / 1024f
                                    ).toInt()
                                    inSampleSize = if (scale > 0) scale else 1
                                }
                            }
                            BitmapFactory.decodeFile(imageFile.absolutePath, options)?.asImageBitmap()
                        } catch (e: Exception) {
                            android.util.Log.e("ImageSlider", "Error loading image: ${e.message}")
                            null
                        }
                    } else null
                }
                
                when {
                    bitmap != null -> {
                        // Display the actual image
                        Image(
                            bitmap = bitmap,
                            contentDescription = currentImage.fileName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    imageUriState is ImageUriState.Success -> {
                        // Fallback to icon if we have URI but can't load bitmap
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    imageUriState is ImageUriState.Error -> {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    imageUriState is ImageUriState.NotFound -> {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                // Navigation buttons (left/right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button
                    IconButton(
                        onClick = {
                            if (currentImageIndex > 0) {
                                currentImageIndex--
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                        enabled = currentImageIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Image",
                            tint = Color.White
                        )
                    }
                    
                    // Spacer
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Next button
                    IconButton(
                        onClick = {
                            if (currentImageIndex < images.size - 1) {
                                currentImageIndex++
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                        enabled = currentImageIndex < images.size - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Image",
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Pagination indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                images.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .background(
                                if (index == currentImageIndex) MaterialTheme.colorScheme.primary
                                else Color.Gray.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .clickable { currentImageIndex = index }
                    )
                }
            }
            
            // Image details and actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image details
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = currentImage.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = "${currentImageIndex + 1}/${images.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                // Actions
                Row {
                    // Download button
                    IconButton(
                        onClick = { 
                            downloadLauncher.launch(currentImage.fileName)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download",
                            tint = Color.Gray
                        )
                    }
                    
                    // Delete button
                    IconButton(
                        onClick = { showDeleteConfirmation = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Image",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
} 