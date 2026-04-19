package com.example.mucproject.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeviceConnectionScreen(viewModel: SmartDoseViewModel) {
    var isScanning by remember { mutableStateOf(false) }
    val devices = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Connect Pillbox", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Searching for SmartDose hardware via Bluetooth...", color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(24.dp))

        if (isScanning) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(devices) { device ->
                ListItem(
                    headlineContent = { Text(device) },
                    trailingContent = {
                        Button(onClick = { /* Connect logic */ }) {
                            Text("Connect")
                        }
                    }
                )
                HorizontalDivider()
            }
        }

        Button(
            onClick = {
                isScanning = true
                // Simulate finding a device
                devices.clear()
                devices.add("SmartDose-ESP32 (7C:9E:BD:...)")
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isScanning
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("SCAN FOR DEVICES")
        }
    }
}
