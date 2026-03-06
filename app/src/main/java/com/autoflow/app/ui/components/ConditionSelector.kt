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
import com.autoflow.app.data.database.entities.Condition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionSelector(
    selectedType: String,
    value: String,
    onTypeSelected: (String) -> Unit,
    onValueChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val conditionOptions = listOf(
        Condition.TYPE_TIME_RANGE to "Time Range",
        Condition.TYPE_BATTERY_RANGE to "Battery Range",
        Condition.TYPE_WIFI_NETWORK to "WiFi Network",
        Condition.TYPE_LOCATION_RADIUS to "Location Radius"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = conditionOptions.find { it.first == selectedType }?.second ?: "Select Condition",
                onValueChange = {},
                readOnly = true,
                label = { Text("Condition Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                conditionOptions.forEach { (type, label) ->
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
            Condition.TYPE_TIME_RANGE -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Time Range") },
                    placeholder = { Text("e.g., 09:00-17:00") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("24-hour format: start-end") }
                )
            }
            Condition.TYPE_BATTERY_RANGE -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Battery Range (%)") },
                    placeholder = { Text("e.g., 20-80") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Min-Max percentage") }
                )
            }
            Condition.TYPE_WIFI_NETWORK -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("WiFi Network Name") },
                    placeholder = { Text("e.g., HomeWiFi") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Must be connected to this network") }
                )
            }
            Condition.TYPE_LOCATION_RADIUS -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    label = { Text("Location (lat,lng,radius)") },
                    placeholder = { Text("e.g., 37.7749,-122.4194,500") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Latitude, longitude, radius in meters") }
                )
            }
            else -> {
                if (selectedType.isEmpty()) {
                    Text(
                        text = "Select a condition type above",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
