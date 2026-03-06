package com.autoflow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autoflow.app.data.database.entities.Action
import com.autoflow.app.data.database.entities.Condition
import com.autoflow.app.data.database.entities.ExecutionLog
import com.autoflow.app.data.database.entities.Routine
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.ui.viewmodel.RoutineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    viewModel: RoutineViewModel,
    routineId: Long,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    var routine by remember { mutableStateOf<Routine?>(null) }
    var triggers by remember { mutableStateOf<List<Trigger>>(emptyList()) }
    var conditions by remember { mutableStateOf<List<Condition>>(emptyList()) }
    var actions by remember { mutableStateOf<List<Action>>(emptyList()) }
    val logs by viewModel.getLogsForRoutine(routineId).collectAsState(initial = emptyList())

    LaunchedEffect(routineId) {
        viewModel.loadRoutineDetail(routineId) { r, t, c, a ->
            routine = r
            triggers = t
            conditions = c
            actions = a
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = routine?.name ?: "Routine Detail",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        routine?.let {
                            viewModel.deleteRoutine(it)
                            onDelete()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Enable/Disable toggle
            item {
                routine?.let { r ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (r.enabled) "Enabled" else "Disabled",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Switch(
                                checked = r.enabled,
                                onCheckedChange = { enabled ->
                                    viewModel.toggleRoutine(r.id, enabled)
                                    routine = r.copy(enabled = enabled)
                                }
                            )
                        }
                    }
                }
            }

            // Triggers section
            item {
                DetailSectionHeader("Triggers")
            }
            items(triggers) { trigger ->
                DetailItem(
                    label = formatTriggerType(trigger.type),
                    value = trigger.value
                )
            }

            // Conditions section
            item {
                DetailSectionHeader("Conditions")
            }
            if (conditions.isEmpty()) {
                item {
                    Text(
                        text = "No conditions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                items(conditions) { condition ->
                    DetailItem(
                        label = formatConditionType(condition.type),
                        value = condition.value
                    )
                }
            }

            // Actions section
            item {
                DetailSectionHeader("Actions")
            }
            items(actions) { action ->
                DetailItem(
                    label = formatActionType(action.type),
                    value = action.value
                )
            }

            // Execution logs section
            item {
                DetailSectionHeader("Recent Logs")
            }
            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "No execution logs yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                items(logs.take(20)) { log ->
                    LogItem(log)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun DetailSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun DetailItem(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LogItem(log: ExecutionLog) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (log.status) {
                ExecutionLog.STATUS_SUCCESS -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ExecutionLog.STATUS_FAILURE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (log.status) {
                    ExecutionLog.STATUS_SUCCESS -> Icons.Default.CheckCircle
                    ExecutionLog.STATUS_FAILURE -> Icons.Default.Close
                    else -> Icons.Default.SkipNext
                },
                contentDescription = null,
                tint = when (log.status) {
                    ExecutionLog.STATUS_SUCCESS -> MaterialTheme.colorScheme.primary
                    ExecutionLog.STATUS_FAILURE -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.status.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Trigger: ${formatTriggerType(log.triggerType)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = dateFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatTriggerType(type: String): String = when (type) {
    Trigger.TYPE_BATTERY_LEVEL -> "Battery Level"
    Trigger.TYPE_WIFI_CONNECTED -> "WiFi Connected"
    Trigger.TYPE_BLUETOOTH_CONNECTED -> "Bluetooth Connected"
    Trigger.TYPE_TIME -> "Time"
    Trigger.TYPE_LOCATION -> "Location"
    Trigger.TYPE_APP_OPENED -> "App Opened"
    else -> type
}

fun formatConditionType(type: String): String = when (type) {
    Condition.TYPE_TIME_RANGE -> "Time Range"
    Condition.TYPE_BATTERY_RANGE -> "Battery Range"
    Condition.TYPE_WIFI_NETWORK -> "WiFi Network"
    Condition.TYPE_LOCATION_RADIUS -> "Location Radius"
    else -> type
}

fun formatActionType(type: String): String = when (type) {
    Action.TYPE_SET_VOLUME -> "Set Volume"
    Action.TYPE_TOGGLE_WIFI -> "Toggle WiFi"
    Action.TYPE_TOGGLE_BLUETOOTH -> "Toggle Bluetooth"
    Action.TYPE_OPEN_APP -> "Open App"
    Action.TYPE_SHOW_NOTIFICATION -> "Show Notification"
    else -> type
}
