package com.autoflow.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autoflow.app.data.database.entities.Trigger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerSelector(
    selectedType: String,
    value: String,
    onTypeSelected: (String) -> Unit,
    onValueChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val triggerOptions = listOf(
        Trigger.TYPE_BATTERY_LEVEL to "Battery Level",
        Trigger.TYPE_WIFI_CONNECTED to "WiFi Connected",
        Trigger.TYPE_BLUETOOTH_CONNECTED to "Bluetooth Connected",
        Trigger.TYPE_TIME to "Time",
        Trigger.TYPE_LOCATION to "Location",
        Trigger.TYPE_APP_OPENED to "App Opened"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = triggerOptions.find { it.first == selectedType }?.second ?: "Select Trigger",
                onValueChange = {},
                readOnly = true,
                label = { Text("Trigger Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                triggerOptions.forEach { (type, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Value input based on selected type
        when (selectedType) {
            Trigger.TYPE_BATTERY_LEVEL -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Battery Threshold (%)") },
                    placeholder = { Text("e.g., 20") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Trigger when battery drops to or below this level") }
                )
            }
            Trigger.TYPE_WIFI_CONNECTED -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("WiFi Network Name") },
                    placeholder = { Text("e.g., HomeWiFi or 'any'") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Enter SSID or 'any' for any WiFi connection") }
                )
            }
            Trigger.TYPE_BLUETOOTH_CONNECTED -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Bluetooth Device") },
                    placeholder = { Text("e.g., My Headphones or 'any'") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Enter device name or 'any' for any device") }
                )
            }
            Trigger.TYPE_TIME -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Time (HH:mm)") },
                    placeholder = { Text("e.g., 08:00") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("24-hour format") }
                )
            }
            Trigger.TYPE_LOCATION -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Location (lat,lng)") },
                    placeholder = { Text("e.g., 37.7749,-122.4194") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Enter latitude and longitude") }
                )
            }
            Trigger.TYPE_APP_OPENED -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("App Package Name") },
                    placeholder = { Text("e.g., com.spotify.music") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Enter the package name of the app") }
                )
            }
            else -> {
                if (selectedType.isEmpty()) {
                    Text(
                        text = "Please select a trigger type above",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
