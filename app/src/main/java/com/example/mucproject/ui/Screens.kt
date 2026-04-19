package com.example.mucproject.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mucproject.data.MealType
import com.example.mucproject.data.Status
import com.example.mucproject.data.UserRole
import java.util.*

@Composable
fun LoginScreen(viewModel: SmartDoseViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.ELDER) }
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("SmartDose", fontSize = 40.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email, 
            onValueChange = { email = it }, 
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password, 
            onValueChange = { password = it }, 
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        Text("Select Role", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = role == UserRole.ELDER, onClick = { role = UserRole.ELDER })
            Text("Elder")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = role == UserRole.CAREGIVER, onClick = { role = UserRole.CAREGIVER })
            Text("Caregiver")
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Remember Me")
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { viewModel.login(email, password, role, rememberMe) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("LOGIN")
        }
    }
}

@Composable
fun ElderScreen(viewModel: SmartDoseViewModel, onNavigateToSettings: () -> Unit) {
    val allRecords by viewModel.allRecords.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val isConnected by viewModel.connectionStatus.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    val currentDay by viewModel.currentDay.collectAsState()
    val currentMeal by viewModel.currentMeal.collectAsState()
    val isFillMode by viewModel.isFillMode.collectAsState()
    val isTestMode by viewModel.isTestMode.collectAsState()

    val baseFontSize = if (profile?.largeTextMode == true) 28 else 24
    val baseSubFontSize = if (profile?.largeTextMode == true) 20 else 16
    
    val fontSize = baseFontSize.sp
    val titleFontSize = (baseFontSize + 4).sp
    val subFontSize = baseSubFontSize.sp
    
    val bgColor = if (profile?.highContrastMode == true) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (profile?.highContrastMode == true) Color.White else MaterialTheme.colorScheme.onBackground

    val infiniteTransition = rememberInfiniteTransition(label = "refresh")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation"
    )

    if (isFillMode) {
        FillModeDialog(viewModel)
    }

    Column(modifier = Modifier.fillMaxSize().background(bgColor).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Good Day!", fontSize = fontSize, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { viewModel.refreshStatus() }, enabled = !isRefreshing) {
                Icon(Icons.Default.Refresh, "Refresh", modifier = if (isRefreshing) Modifier.rotate(rotation) else Modifier, tint = MaterialTheme.colorScheme.primary)
            }
            ConnectionStatusBadge(isConnected)
        }
        
        // TEST MODE ROW
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Test Mode", fontSize = 12.sp, color = textColor.copy(alpha = 0.6f))
            Switch(checked = isTestMode, onCheckedChange = { viewModel.toggleTestMode() }, modifier = Modifier.scale(0.7f))
            if (isTestMode) {
                Spacer(Modifier.width(8.dp))
                Button(onClick = { viewModel.fastForwardMeal() }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Fast Forward", fontSize = 10.sp)
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { viewModel.clearAllData() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Clear All", fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (profile?.highContrastMode == true) Color.White else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (profile?.highContrastMode == true) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Your Next Dose:", fontSize = subFontSize)
                Text("${currentMeal.name} for Day ${currentDay + 1}", fontSize = titleFontSize, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text("Take medicine from the HIGHLIGHTED compartment", textAlign = TextAlign.Center, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        PillboxGrid(allRecords, currentDay, currentMeal, isElder = true, highContrast = profile?.highContrastMode == true) { d, m -> 
            viewModel.onCompartmentOpened(d, m) 
        }
        
        Spacer(Modifier.weight(1f))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.toggleFillMode() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                Text("FILL PILLBOX")
            }
            Button(onClick = onNavigateToSettings, modifier = Modifier.weight(1f)) {
                Text("SETTINGS")
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.logout() }, modifier = Modifier.fillMaxWidth()) {
            Text("LOGOUT")
        }
    }
}

@Composable
fun FillModeDialog(viewModel: SmartDoseViewModel) {
    val fillDay by viewModel.fillDay.collectAsState()
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Dialog(onDismissRequest = { viewModel.toggleFillMode() }) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Breakfast Fill Mode", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("Place breakfast medication for ${days[fillDay]} in the compartment and wait for the hardware to record the weight.", textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(progress = (fillDay + 1) / 7f, modifier = Modifier.size(80.dp), strokeWidth = 8.dp)
                Spacer(Modifier.height(16.dp))
                Text("Day ${fillDay + 1} of 7")
                Spacer(Modifier.height(32.dp))
                Button(onClick = { viewModel.confirmBreakfastFill() }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (fillDay < 6) "CONFIRM & NEXT DAY" else "FINISH FILLING")
                }
                TextButton(onClick = { viewModel.toggleFillMode() }) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun ConnectionStatusBadge(isConnected: Boolean) {
    Surface(color = if (isConnected) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(if (isConnected) Color.Green else Color.Red, CircleShape))
            Spacer(Modifier.width(4.dp))
            Text(if (isConnected) "Pillbox Connected" else "Pillbox Offline", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CaregiverScreen(viewModel: SmartDoseViewModel) {
    val allRecords by viewModel.allRecords.collectAsState()
    val target = viewModel.getCurrentTarget()
    val stats = viewModel.getAdherenceStats()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "refresh_caregiver")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Caregiver Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Monitoring Adherence", color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { viewModel.refreshStatus() }, enabled = !isRefreshing) {
                Icon(Icons.Default.Refresh, "Refresh", modifier = if (isRefreshing) Modifier.rotate(rotation) else Modifier, tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Weekly Adherence: ${stats["percentage"]}%", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(stats["insight"].toString(), color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(24.dp))
        PillboxGrid(allRecords, target.first, target.second, isElder = false) { _, _ -> }
        
        Spacer(Modifier.weight(1f))
        Button(onClick = { viewModel.logout() }, modifier = Modifier.fillMaxWidth()) {
            Text("LOGOUT")
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SmartDoseViewModel, onBack: () -> Unit) {
    val profile by viewModel.userProfile.collectAsState()
    var breakfast by remember { mutableStateOf(profile?.breakfastTime ?: "08:00") }
    var lunch by remember { mutableStateOf(profile?.lunchTime ?: "13:00") }
    var dinner by remember { mutableStateOf(profile?.dinnerTime ?: "19:00") }
    var largeText by remember { mutableStateOf(profile?.largeTextMode ?: false) }
    var highContrast by remember { mutableStateOf(profile?.highContrastMode ?: false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))

        Text("Meal Schedule", fontWeight = FontWeight.Bold)
        OutlinedTextField(value = breakfast, onValueChange = { breakfast = it }, label = { Text("Breakfast Time") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lunch, onValueChange = { lunch = it }, label = { Text("Lunch Time") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dinner, onValueChange = { dinner = it }, label = { Text("Dinner Time") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))
        Text("Accessibility", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = largeText, onCheckedChange = { largeText = it })
            Text("Large Text Mode")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = highContrast, onCheckedChange = { highContrast = it })
            Text("High Contrast Mode")
        }

        Spacer(Modifier.weight(1f))
        Button(onClick = { 
            viewModel.updateSettings(breakfast, lunch, dinner, largeText, highContrast)
            onBack()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("SAVE SETTINGS")
        }
    }
}

@Composable
fun PillboxGrid(records: List<com.example.mucproject.data.CompartmentRecord>, currentDay: Int, currentMeal: MealType, isElder: Boolean, highContrast: Boolean = false, onCellClick: (Int, MealType) -> Unit) {
    val meals = MealType.entries
    val daysShort = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(40.dp))
            meals.forEach { meal ->
                Text(meal.name.take(1) + meal.name.drop(1).lowercase(), Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (highContrast) Color.White else Color.Unspecified)
            }
        }

        LazyColumn(Modifier.height(400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(7) { day ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(daysShort[day], Modifier.width(40.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (highContrast) Color.White else Color.Unspecified)
                    meals.forEach { meal ->
                        val record = records.find { it.day == day && it.meal == meal }
                        val isTarget = day == currentDay && meal == currentMeal && isElder
                        Box(Modifier.weight(1f).padding(4.dp)) {
                            GridCell(status = record?.status ?: Status.PENDING, isTarget = isTarget, highContrast = highContrast, onClick = { if (isElder) onCellClick(day, meal) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridCell(status: Status, isTarget: Boolean, highContrast: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.1f, animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse), label = "scale")

    val bgColor = when {
        status == Status.TAKEN -> if (highContrast) Color(0xFF00FF00) else Color(0xFFE8F5E9)
        status == Status.DELAYED -> if (highContrast) Color(0xFFFFA500) else Color(0xFFFFF3E0)
        status == Status.MISSED -> if (highContrast) Color(0xFFFF0000) else Color(0xFFFFEBEE)
        isTarget -> if (highContrast) Color.Yellow else Color(0xFFFFF9C4)
        else -> if (highContrast) Color.DarkGray else Color(0xFFF5F5F5)
    }

    val borderColor = when {
        status == Status.TAKEN -> Color(0xFF4CAF50)
        status == Status.DELAYED -> Color(0xFFFF9800)
        status == Status.MISSED -> Color(0xFFF44336)
        isTarget -> if (highContrast) Color.Yellow else Color(0xFFFBC02D)
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier.aspectRatio(1.2f).graphicsLayer { if (isTarget) { scaleX = pulseScale; scaleY = pulseScale } }.clip(RoundedCornerShape(8.dp)).background(bgColor).border(2.dp, borderColor, RoundedCornerShape(8.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            Status.TAKEN -> Icon(Icons.Default.Check, null, tint = if (highContrast) Color.Black else Color(0xFF4CAF50))
            Status.DELAYED -> Icon(Icons.Default.Refresh, null, tint = if (highContrast) Color.Black else Color(0xFFFF9800))
            Status.MISSED -> Icon(Icons.Default.Close, null, tint = if (highContrast) Color.Black else Color(0xFFF44336))
            else -> if (isTarget) Icon(Icons.Default.Notifications, null, tint = if (highContrast) Color.Black else Color(0xFFFBC02D))
        }
    }
}
