package com.autoflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Represents a node in the flowchart.
 */
data class FlowchartNode(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: NodeType,
    val label: String,
    val value: String = ""
)

enum class NodeType {
    TRIGGER,
    CONDITION,
    ACTION
}

/**
 * Visual Flowchart Routine Builder.
 * Displays automation flow as connected nodes that can be added, edited, and deleted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowchartBuilderScreen(
    onBack: () -> Unit,
    onSave: (List<FlowchartNode>) -> Unit
) {
    val nodes = remember { mutableStateListOf<FlowchartNode>() }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingNodeIndex by remember { mutableStateOf(-1) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Flowchart Builder",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (nodes.isNotEmpty()) {
                        IconButton(onClick = { onSave(nodes.toList()) }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Node")
            }
        }
    ) { paddingValues ->
        if (nodes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Build Your Automation Flow",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add trigger, condition, or action nodes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                itemsIndexed(nodes) { index, node ->
                    FlowchartNodeCard(
                        node = node,
                        onEdit = {
                            editingNodeIndex = index
                            showAddSheet = true
                        },
                        onDelete = { nodes.removeAt(index) }
                    )

                    // Arrow connector between nodes
                    if (index < nodes.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Add/Edit Node Bottom Sheet
        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddSheet = false
                    editingNodeIndex = -1
                },
                sheetState = sheetState
            ) {
                AddNodeContent(
                    editingNode = if (editingNodeIndex >= 0) nodes[editingNodeIndex] else null,
                    onAddNode = { node ->
                        if (editingNodeIndex >= 0) {
                            nodes[editingNodeIndex] = node
                            editingNodeIndex = -1
                        } else {
                            nodes.add(node)
                        }
                        showAddSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FlowchartNodeCard(
    node: FlowchartNode,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (bgColor, borderColor, label) = when (node.type) {
        NodeType.TRIGGER -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            "TRIGGER"
        )
        NodeType.CONDITION -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondary,
            "CONDITION"
        )
        NodeType.ACTION -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.tertiary,
            "ACTION"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Node type badge
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = borderColor,
                    modifier = Modifier
                        .background(
                            color = borderColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = node.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            if (node.value.isNotEmpty()) {
                Text(
                    text = node.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddNodeContent(
    editingNode: FlowchartNode?,
    onAddNode: (FlowchartNode) -> Unit
) {
    var selectedType by remember { mutableStateOf(editingNode?.type) }
    var label by remember { mutableStateOf(editingNode?.label ?: "") }
    var value by remember { mutableStateOf(editingNode?.value ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (editingNode != null) "Edit Node" else "Add Node",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Node type selector
        Text(
            text = "Node Type",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NodeType.values().forEach { type ->
                val isSelected = selectedType == type
                val color = when (type) {
                    NodeType.TRIGGER -> MaterialTheme.colorScheme.primary
                    NodeType.CONDITION -> MaterialTheme.colorScheme.secondary
                    NodeType.ACTION -> MaterialTheme.colorScheme.tertiary
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) color else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedType = type }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Node Label") },
            placeholder = { Text("e.g., Battery Below 20%") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Value (optional)") },
            placeholder = { Text("e.g., 20") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                if (selectedType != null && label.isNotBlank()) {
                    onAddNode(
                        FlowchartNode(
                            id = editingNode?.id ?: java.util.UUID.randomUUID().toString(),
                            type = selectedType!!,
                            label = label,
                            value = value
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedType != null && label.isNotBlank()
        ) {
            Text(if (editingNode != null) "Update Node" else "Add Node")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
