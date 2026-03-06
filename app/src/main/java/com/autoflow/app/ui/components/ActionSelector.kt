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
import com.autoflow.app.data.database.entities.Action

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSelector(
    selectedType: String,
    value: String,
    onTypeSelected: (String) -> Unit,
    onValueChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val actionOptions = listOf(
        Action.TYPE_SET_VOLUME to "Set Volume",
        Action.TYPE_TOGGLE_WIFI to "Toggle WiFi",
        Action.TYPE_TOGGLE_BLUETOOTH to "Toggle Bluetooth",
        Action.TYPE_OPEN_APP to "Open App",
        Action.TYPE_SHOW_NOTIFICATION to "Show Notification",
        Action.TYPE_ENABLE_WIFI to "Enable WiFi",
        Action.TYPE_DISABLE_WIFI to "Disable WiFi",
        Action.TYPE_ENABLE_BLUETOOTH to "Enable Bluetooth",
        Action.TYPE_DISABLE_BLUETOOTH to "Disable Bluetooth",
        Action.TYPE_ENABLE_BATTERY_SAVER to "Enable Battery Saver",
        Action.TYPE_DISABLE_BATTERY_SAVER to "Disable Battery Saver",
        Action.TYPE_PLAY_MUSIC to "Play Music",
        Action.TYPE_PAUSE_MUSIC to "Pause Music",
        Action.TYPE_NEXT_TRACK to "Next Track",
        Action.TYPE_PREVIOUS_TRACK to "Previous Track",
        Action.TYPE_SEND_SMS to "Send SMS",
        Action.TYPE_OPEN_CAMERA to "Open Camera",
        Action.TYPE_TAKE_PHOTO to "Take Photo",
        Action.TYPE_RECORD_VIDEO to "Record Video",
        Action.TYPE_ENABLE_AIRPLANE_MODE to "Enable Airplane Mode",
        Action.TYPE_DISABLE_AIRPLANE_MODE to "Disable Airplane Mode",
        Action.TYPE_WAIT_SECONDS to "Wait (Seconds)",
        Action.TYPE_WAIT_MINUTES to "Wait (Minutes)"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = actionOptions.find { it.first == selectedType }?.second ?: "Select Action",
                onValueChange = {},
                readOnly = true,
                label = { Text("Action Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                actionOptions.forEach { (type, label) ->
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

        when (selectedType) {
            Action.TYPE_SET_VOLUME -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Volume Level") },
                    placeholder = { Text("e.g., 7 or music:7") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Level 0-15, optionally prefix with stream type") }
                )
            }
            Action.TYPE_TOGGLE_WIFI -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("WiFi State") },
                    placeholder = { Text("on or off") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Enter 'on' or 'off'") }
                )
            }
            Action.TYPE_TOGGLE_BLUETOOTH -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Bluetooth State") },
                    placeholder = { Text("on or off") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Enter 'on' or 'off'") }
                )
            }
            Action.TYPE_OPEN_APP -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("App Package Name") },
                    placeholder = { Text("e.g., com.spotify.music") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Enter the full package name") }
                )
            }
            Action.TYPE_SHOW_NOTIFICATION -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Notification (title|message)") },
                    placeholder = { Text("e.g., Alert|Battery is low") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Format: title|message") }
                )
            }
            else -> {
                if (selectedType.isEmpty()) {
                    Text(
                        text = "Select an action type above",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
