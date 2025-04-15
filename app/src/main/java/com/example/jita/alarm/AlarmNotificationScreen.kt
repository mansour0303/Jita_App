package com.example.jita.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.jita.Task
import com.example.jita.TaskPriority
import com.example.jita.model.Reminder
import java.text.SimpleDateFormat
import java.util.*

// Helper function to determine if the current theme is in light mode
@Composable
fun isLightTheme(): Boolean {
    val background = MaterialTheme.colorScheme.background
    // Calculate luminance - lighter colors have higher values
    val luminance = (0.299 * background.red + 0.587 * background.green + 0.114 * background.blue)
    return luminance > 0.5
}

@Composable
fun AlarmNotificationScreen(
    reminder: Reminder,
    attachedTasks: List<Task>,
    onDeleteClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    // Determine if we're in light or dark theme
    val isLight = isLightTheme()
    
    // Define priority colors that work well in both light and dark mode
    val highPriorityColor = if (isLight) Color(0xFFE53935) else Color(0xFFFF5252)
    val mediumPriorityColor = if (isLight) Color(0xFFFFA000) else Color(0xFFFFD54F)
    val lowPriorityColor = if (isLight) Color(0xFF43A047) else Color(0xFF81C784)

    Dialog(
        onDismissRequest = { /* Do nothing to prevent accidental dismissal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with alarm icon
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(top = 16.dp, bottom = 8.dp)
                )
                
                // Alarm name (Title)
                Text(
                    text = reminder.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Current time
                val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                Text(
                    text = timeFormatter.format(Calendar.getInstance().time),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Alarm message
                if (reminder.message.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = reminder.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Attached tasks section
                if (attachedTasks.isNotEmpty()) {
                    Text(
                        text = "Attached Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 16.dp, bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Take available space
                    ) {
                        items(attachedTasks) { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    // Task name with priority indicator
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Priority indicator with adaptive colors
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = when (task.priority) {
                                                        TaskPriority.HIGH -> highPriorityColor
                                                        TaskPriority.MEDIUM -> mediumPriorityColor
                                                        TaskPriority.LOW -> lowPriorityColor
                                                        else -> MaterialTheme.colorScheme.primary
                                                    },
                                                    shape = CircleShape
                                                )
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = task.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    // Task description if available
                                    if (task.description.isNotBlank()) {
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 20.dp, top = 4.dp),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    // Due date and list info
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 20.dp, top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = dateFormat.format(task.dueDate.time),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        if (!task.list.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Icon(
                                                imageVector = Icons.Default.List,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = task.list!!,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f)) // Take remaining space if no tasks
                }
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Delete")
                    }
                    
                    Button(
                        onClick = onDismissClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
} 