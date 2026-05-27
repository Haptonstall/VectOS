package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.structural.Load
import com.lz.vectos.domain.structural.LoadDirection

@Composable
fun LoadEditor(
    loads: List<Load>,
    activeCaseId: String,
    memberLength: Double,
    onAddLoad: (Load) -> Unit,
    onDeleteLoad: (Load) -> Unit,
    onLoadSelected: (Load?) -> Unit,
    selectedLoad: Load?,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Loads in $activeCaseId",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Load")
            }
        }

        if (loads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No loads defined for this case", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            loads.forEach { load ->
                LoadItem(
                    load = load,
                    isSelected = selectedLoad == load,
                    onSelect = { onLoadSelected(if (selectedLoad == load) null else load) },
                    onDelete = { onDeleteLoad(load) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showAddDialog) {
        AddLoadDialog(
            memberLength = memberLength,
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                onAddLoad(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LoadItem(
    load: Load,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (load) {
                is Load.PointLoad -> Icons.Default.VerticalAlignBottom
                is Load.UniformDistributedLoad -> Icons.Default.ViewStream
                is Load.TrapezoidalLoad -> Icons.Default.SignalCellularAlt
            }
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                val title = when (load) {
                    is Load.PointLoad -> "Point Load"
                    is Load.UniformDistributedLoad -> "UDL"
                    is Load.TrapezoidalLoad -> "Trapezoidal"
                }
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                
                val details = when (load) {
                    is Load.PointLoad -> "${load.value} N @ ${load.locationStart} m"
                    is Load.UniformDistributedLoad -> "${load.value} N/m [${load.locationStart} - ${load.locationEnd}] m"
                    is Load.TrapezoidalLoad -> "${load.valueStart} to ${load.valueEnd} N/m [${load.locationStart} - ${load.locationEnd}] m"
                }
                Text(details, style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoadDialog(
    memberLength: Double,
    onDismiss: () -> Unit,
    onConfirm: (Load) -> Unit
) {
    var loadType by remember { mutableStateOf(0) } // 0: Point, 1: UDL
    var value1 by remember { mutableStateOf("") }
    var pos1 by remember { mutableStateOf("") }
    var pos2 by remember { mutableStateOf(memberLength.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Load") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = loadType == 0,
                        onClick = { loadType = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Point") }
                    SegmentedButton(
                        selected = loadType == 1,
                        onClick = { loadType = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("UDL") }
                }

                OutlinedTextField(
                    value = value1,
                    onValueChange = { value1 = it },
                    label = { Text(if (loadType == 0) "Magnitude (N)" else "Magnitude (N/m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pos1,
                    onValueChange = { pos1 = it },
                    label = { Text(if (loadType == 0) "Location (m)" else "Start Position (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                if (loadType == 1) {
                    OutlinedTextField(
                        value = pos2,
                        onValueChange = { pos2 = it },
                        label = { Text("End Position (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v1 = value1.toDoubleOrNull() ?: 0.0
                    val p1 = pos1.toDoubleOrNull() ?: 0.0
                    val p2 = pos2.toDoubleOrNull() ?: memberLength
                    
                    val load = if (loadType == 0) {
                        Load.PointLoad(v1, p1)
                    } else {
                        Load.UniformDistributedLoad(v1, p1, p2)
                    }
                    onConfirm(load)
                },
                enabled = value1.isNotEmpty() && pos1.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
