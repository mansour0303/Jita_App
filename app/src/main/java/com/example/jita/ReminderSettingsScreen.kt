package com.example.jita

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    navController: NavHostController
) {
    // State for the reminder settings
    var selectedHour by remember { mutableStateOf(6) }
    var selectedMinute by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().apply { 
        add(Calendar.DAY_OF_YEAR, 1) // Default to tomorrow
    }) }
    var reminderName by remember { mutableStateOf("") }
    var alarmSoundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var snoozeEnabled by remember { mutableStateOf(true) }
    
    // Selected days of week (Monday to Sunday, 0-6)
    val selectedDays = remember { mutableStateListOf(false, true, false, false, false, false, false) }
    
    // Date formatter
    val dateFormatter = SimpleDateFormat("E, dd MMM", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Set Reminder",
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time picker section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Time picker UI - simplified for this example
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hour picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%02d", (selectedHour + 11) % 12 + 1),
                            fontSize = 24.sp,
                            color = Color.Gray,
                            modifier = Modifier.alpha(0.5f)
                        )
                        Text(
                            text = String.format("%02d", selectedHour),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%02d", (selectedHour + 1) % 24),
                            fontSize = 24.sp,
                            color = Color.Gray,
                            modifier = Modifier.alpha(0.5f)
                        )
                    }
                    
                    // Colon
                    Text(
                        text = ":",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Minute picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%02d", (selectedMinute + 59) % 60),
                            fontSize = 24.sp,
                            color = Color.Gray,
                            modifier = Modifier.alpha(0.5f)
                        )
                        Text(
                            text = String.format("%02d", selectedMinute),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%02d", (selectedMinute + 1) % 60),
                            fontSize = 24.sp,
                            color = Color.Gray,
                            modifier = Modifier.alpha(0.5f)
                        )
                    }
                }
            }
            
            // Date section
            Text(
                text = "Tomorrow-${dateFormatter.format(selectedDate.time)}",
                modifier = Modifier.padding(vertical = 16.dp),
                fontSize = 18.sp
            )
            
            // Day picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEachIndexed { index, day ->
                    val isSelected = selectedDays[index]
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedDays[index] = !selectedDays[index]
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else 
                                    if (index == 6) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Reminder name
            OutlinedTextField(
                value = reminderName,
                onValueChange = { reminderName = it },
                label = { Text("Alarm name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Alarm sound
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Alarm sound",
                        fontSize = 16.sp
                    )
                    Text(
                        text = "The Voyage",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Switch(
                    checked = alarmSoundEnabled,
                    onCheckedChange = { alarmSoundEnabled = it }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Vibration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vibration",
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Basic call",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Snooze
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Snooze",
                        fontSize = 16.sp
                    )
                    Text(
                        text = "5 minutes, 3 times",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Switch(
                    checked = snoozeEnabled,
                    onCheckedChange = { snoozeEnabled = it }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Cancel and Save buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancel")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = { 
                        // Save reminder logic would go here
                        navController.navigateUp() 
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
} 