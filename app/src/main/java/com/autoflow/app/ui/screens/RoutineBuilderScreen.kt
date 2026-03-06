package com.autoflow.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autoflow.app.data.database.entities.Action
import com.autoflow.app.data.database.entities.Condition
import com.autoflow.app.data.database.entities.Routine
import com.autoflow.app.data.database.entities.Trigger
import com.autoflow.app.ui.components.ActionSelector
import com.autoflow.app.ui.components.ConditionSelector
import com.autoflow.app.ui.components.TriggerSelector
import com.autoflow.app.ui.viewmodel.RoutineViewModel

data class TriggerConfig(
    val type: String = "",
    val value: String = ""
)

data class ConditionConfig(
    val type: String = "",
    val value: String = ""
)

data class ActionConfig(
    val type: String = "",
    val value: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineBuilderScreen(
    viewModel: RoutineViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var routineName by remember { mutableStateOf("") }
    val triggers = remember { mutableStateListOf<TriggerConfig>() }
    val conditions = remember { mutableStateListOf<ConditionConfig>() }
    val actions = remember { mutableStateListOf<ActionConfig>() }

    val stepTitles = listOf("Trigger", "Conditions", "Actions", "Save")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Routine - ${stepTitles[currentStep]}",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) currentStep-- else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Progress indicator
            @Suppress("DEPRECATION")
            LinearProgressIndicator(
                progress = (currentStep + 1) / 4f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Step ${currentStep + 1} of 4",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step content
            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier.weight(1f),
                label = "stepContent"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (step) {
                        0 -> TriggerStepContent(triggers)
                        1 -> ConditionStepContent(conditions)
                        2 -> ActionStepContent(actions)
                        3 -> SaveStepContent(
                            routineName = routineName,
                            onNameChange = { routineName = it },
                            triggers = triggers,
                            conditions = conditions,
                            actions = actions
                        )
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    OutlinedButton(onClick = { currentStep-- }) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier)
                }

                if (currentStep < 3) {
                    Button(
                        onClick = { currentStep++ },
                        enabled = when (currentStep) {
                            0 -> triggers.isNotEmpty() && triggers.all { it.type.isNotEmpty() && it.value.isNotEmpty() }
                            1 -> true // Conditions are optional
                            2 -> actions.isNotEmpty() && actions.all { it.type.isNotEmpty() && it.value.isNotEmpty() }
                            else -> true
                        }
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.saveRoutine(
                                routine = Routine(name = routineName),
                                triggers = triggers.map { Trigger(routineId = 0, type = it.type, value = it.value) },
                                conditions = conditions.filter { it.type.isNotEmpty() && it.value.isNotEmpty() }
                                    .map { Condition(routineId = 0, type = it.type, value = it.value) },
                                actions = actions.map { Action(routineId = 0, type = it.type, value = it.value) }
                            )
                            onSaved()
                        },
                        enabled = routineName.isNotBlank() && triggers.isNotEmpty() && actions.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Text("Save Routine", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TriggerStepContent(triggers: MutableList<TriggerConfig>) {
    Column {
        Text(
            text = "Select Trigger",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Choose what starts this routine",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (triggers.isEmpty()) {
            triggers.add(TriggerConfig())
        }

        triggers.forEachIndexed { index, trigger ->
            TriggerSelector(
                selectedType = trigger.type,
                value = trigger.value,
                onTypeSelected = { type ->
                    triggers[index] = trigger.copy(type = type)
                },
                onValueChanged = { value ->
                    triggers[index] = trigger.copy(value = value)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ConditionStepContent(conditions: MutableList<ConditionConfig>) {
    Column {
        Text(
            text = "Add Conditions (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Add extra conditions that must be met",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        conditions.forEachIndexed { index, condition ->
            ConditionSelector(
                selectedType = condition.type,
                value = condition.value,
                onTypeSelected = { type ->
                    conditions[index] = condition.copy(type = type)
                },
                onValueChanged = { value ->
                    conditions[index] = condition.copy(value = value)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = { conditions.add(ConditionConfig()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add Condition")
        }
    }
}

@Composable
fun ActionStepContent(actions: MutableList<ActionConfig>) {
    Column {
        Text(
            text = "Select Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Choose what happens when triggered",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (actions.isEmpty()) {
            actions.add(ActionConfig())
        }

        actions.forEachIndexed { index, action ->
            ActionSelector(
                selectedType = action.type,
                value = action.value,
                onTypeSelected = { type ->
                    actions[index] = action.copy(type = type)
                },
                onValueChanged = { value ->
                    actions[index] = action.copy(value = value)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = { actions.add(ActionConfig()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add Action")
        }
    }
}

@Composable
fun SaveStepContent(
    routineName: String,
    onNameChange: (String) -> Unit,
    triggers: List<TriggerConfig>,
    conditions: List<ConditionConfig>,
    actions: List<ActionConfig>
) {
    Column {
        Text(
            text = "Review & Save",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = routineName,
            onValueChange = onNameChange,
            label = { Text("Routine Name") },
            placeholder = { Text("e.g., Night Mode") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Summary
        SummarySection("Triggers", triggers.map { "${it.type}: ${it.value}" })
        Spacer(modifier = Modifier.height(12.dp))
        SummarySection("Conditions", conditions.filter { it.type.isNotEmpty() }.map { "${it.type}: ${it.value}" })
        Spacer(modifier = Modifier.height(12.dp))
        SummarySection("Actions", actions.map { "${it.type}: ${it.value}" })
    }
}

@Composable
fun SummarySection(title: String, items: List<String>) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (items.isEmpty() || items.all { it == ": " }) {
            Text(
                text = "None",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            items.filter { it != ": " }.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }
    }
}
