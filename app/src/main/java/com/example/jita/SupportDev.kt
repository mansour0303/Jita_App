package com.example.jita

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportDeveloperScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Add state for showing info dialog and features dialog
    val showInfoDialog = remember { mutableStateOf(false) }
    val showFeaturesDialog = remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Support Developer",
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
                    // Add info button
                    IconButton(onClick = { showInfoDialog.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Donation Information",
                            tint = MaterialTheme.colorScheme.onPrimary
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Heart icon
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Heart",
                tint = Color.Red,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = "Thank You for Using Jita!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Your support helps keep this app ad-free and continuously improving. " +
                       "If you find Jita helpful in organizing your tasks and improving your productivity, " +
                       "please consider supporting the development.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // TON Keeper donation card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Donate with Tonkeeper",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "You can support development by sending TON or other cryptocurrencies to this address:",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val clipboardManager = LocalClipboardManager.current
                    val context = LocalContext.current
                    val tonAddress = "UQDDPZ03aVFWZNs1nDp7jGaM_9q__EmvIqYFY-yZ6Om8nGJz"
                    
                    // TON address with copy button
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tonAddress,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(tonAddress))
                                    Toast.makeText(context, "TON address copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy address",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Any amount is appreciated! Thank you for your support.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Add "What am I supporting?" clickable text
            TextButton(
                onClick = { showFeaturesDialog.value = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "What am I supporting?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Add info dialog
    if (showInfoDialog.value) {
        AlertDialog(
            onDismissRequest = { showInfoDialog.value = false },
            title = { Text("How to Donate via Tonkeeper") },
            text = { 
                Column {
                    Text("Follow these steps to donate using Tonkeeper app:")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("1. Install the Tonkeeper app from Google Play or App Store.")
                    Text("2. Create or restore a wallet.")
                    Text("3. Tap the 'Send' button in Tonkeeper.")
                    Text("4. Paste my TON wallet address (copy it from this page).")
                    Text("5. Enter the amount you wish to donate.")
                    Text("6. Complete the transfer.")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "TON is a cryptocurrency, and all transactions are final. Thank you for your support!",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Add features dialog
    if (showFeaturesDialog.value) {
        AlertDialog(
            onDismissRequest = { showFeaturesDialog.value = false },
            title = { 
                Text(
                    "Why Jita Is Better Than Other Apps",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Jita combines task management, time tracking, and productivity tools in one seamless app:",
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Core features
                    SectionTitle("Core Features")
                    
                    FeaturePoint("Integrated Task & Time Management", "Track time spent on tasks and analyze your productivity patterns")
                    FeaturePoint("Pomodoro Timer", "Built-in focus timer with work and break sessions")
                    FeaturePoint("Smart Reminders", "Set alarms for important tasks with attached task details")
                    FeaturePoint("Notes System", "Take detailed notes linked to your tasks")
                    FeaturePoint("Lists Organization", "Create custom lists to organize your tasks efficiently")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Advantages
                    SectionTitle("Advantages Over Competitors")
                    
                    FeaturePoint("Privacy-Focused", "Your data stays on your device - no mandatory cloud accounts")
                    FeaturePoint("No Subscription Fees", "One-time payment instead of recurring subscriptions")
                    FeaturePoint("Ad-Free", "No annoying advertisements or interruptions")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Your support helps keep Jita independently developed and focused on user needs rather than corporate profits.",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFeaturesDialog.value = false }) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun FeaturePoint(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = "• $title",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun FeatureItem(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 