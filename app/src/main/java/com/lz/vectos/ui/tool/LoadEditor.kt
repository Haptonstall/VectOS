package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.structural.Load
import com.lz.vectos.domain.structural.LoadCategory
import com.lz.vectos.domain.structural.LoadDirection
import com.lz.vectos.domain.structural.SpanGeometry
import com.lz.vectos.domain.units.*
import java.util.UUID

@Composable
fun LoadEditor(
    loads: List<Load>,
    activeCaseId: String,
    memberLength: Double,
    onAddLoad: (Load) -> Unit,
    onDeleteLoad: (Load) -> Unit,
    onLoadSelected: (Load?) -> Unit,
    selectedLoad: Load?,
    modifier: Modifier = Modifier,
    unitSystem: UnitSystem = UnitSystem.IMPERIAL,
    spans: List<SpanGeometry> = emptyList()
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val formatter = EngineeringUnitFormatter(unitSystem)

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
                    formatter = formatter,
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
            unitSystem = unitSystem,
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                onAddLoad(it)
                showAddDialog = false
            },
            spans = spans
        )
    }
}

@Composable
fun LoadItem(
    load: Load,
    isSelected: Boolean,
    formatter: UnitFormatter,
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
                is Load.PointMoment, is Load.PointTorque -> Icons.Default.RotateRight
                is Load.AxialLoad -> Icons.Default.Compress
                else -> Icons.Default.ViewStream
            }
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                val title = when (load) {
                    is Load.PointLoad -> "Point Load"
                    is Load.UniformDistributedLoad -> "UDL"
                    is Load.TrapezoidalLoad -> "Trapezoidal"
                    is Load.AxialLoad -> "Axial Load"
                    is Load.PointMoment -> "Moment"
                    is Load.PointTorque -> "Torsion"
                    else -> "Load"
                }
                Text("$title (${load.direction.name.replace("_", " ")})", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                
                val details = when (load) {
                    is Load.PointLoad -> "${formatter.force(load.value.pounds)} @ ${formatter.length(load.locationStart.inches)}"
                    is Load.UniformDistributedLoad -> "${formatter.distributedLoad(load.value.pli)} [${formatter.length(load.locationStart.inches)} - ${formatter.length(load.locationEnd.inches)}]"
                    is Load.TrapezoidalLoad -> "${formatter.distributedLoad(load.valueStart.pli)} to ${formatter.distributedLoad(load.valueEnd.pli)} [${formatter.length(load.locationStart.inches)} - ${formatter.length(load.locationEnd.inches)}]"
                    is Load.AxialLoad -> formatter.force(load.value.pounds)
                    is Load.PointMoment -> "${formatter.moment(load.value.lbIn)} @ ${formatter.length(load.locationStart.inches)}"
                    is Load.PointTorque -> "${formatter.moment(load.value.lbIn)} @ ${formatter.length(load.locationStart.inches)}"
                    else -> ""
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
    unitSystem: UnitSystem,
    onDismiss: () -> Unit,
    onConfirm: (Load) -> Unit,
    spans: List<SpanGeometry> = emptyList()
) {
    var loadType by remember { mutableStateOf(0) } // 0: Point, 1: UDL, 2: Moment, 3: Axial
    var direction by remember { mutableStateOf(LoadDirection.VERTICAL_DOWN) }
    var value1 by remember { mutableStateOf("") }
    var value2 by remember { mutableStateOf("") }
    var pos1 by remember { mutableStateOf("") }
    var pos2 by remember { mutableStateOf((memberLength / 12.0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Load") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type selection - Simplified for space, could be a Dropdown
                val types = listOf("Point", "UDL", "Moment", "Axial")
                ScrollableTabRow(selectedTabIndex = loadType, edgePadding = 0.dp) {
                    types.forEachIndexed { index, title ->
                        Tab(selected = loadType == index, onClick = { 
                            loadType = index 
                            // Set default direction based on type
                            direction = when(index) {
                                2 -> LoadDirection.MOMENT_CLOCKWISE
                                3 -> LoadDirection.AXIAL_COMPRESSION
                                else -> LoadDirection.VERTICAL_DOWN
                            }
                        }) {
                            Text(title, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }

                // Direction Dropdown (Mocked for brevity, but should list relevant directions)
                Text("Direction: ${direction.name.replace("_", " ")}", style = MaterialTheme.typography.bodySmall)

                val magnitudeLabel = when (loadType) {
                    0 -> if (unitSystem == UnitSystem.METRIC) "Magnitude (kN)" else "Magnitude (lb)"
                    1 -> if (unitSystem == UnitSystem.METRIC) "Magnitude (N/m)" else "Magnitude (lb/ft)"
                    2 -> if (unitSystem == UnitSystem.METRIC) "Moment (kN-m)" else "Moment (lb-ft)"
                    3 -> if (unitSystem == UnitSystem.METRIC) "Force (kN)" else "Force (lb)"
                    else -> "Value"
                }

                OutlinedTextField(
                    value = value1,
                    onValueChange = { value1 = it },
                    label = { Text(magnitudeLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                if (loadType != 3) {
                    val posLabel = if (loadType == 1) {
                        if (unitSystem == UnitSystem.METRIC) "Start Position (m)" else "Start Position (ft)"
                    } else {
                        if (unitSystem == UnitSystem.METRIC) "Location (m)" else "Location (ft)"
                    }
                    OutlinedTextField(
                        value = pos1,
                        onValueChange = { pos1 = it },
                        label = { Text(posLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (loadType == 1) {
                    val endPosLabel = if (unitSystem == UnitSystem.METRIC) "End Position (m)" else "End Position (ft)"
                    OutlinedTextField(
                        value = pos2,
                        onValueChange = { pos2 = it },
                        label = { Text(endPosLabel) },
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
                    val p1raw = pos1.toDoubleOrNull() ?: 0.0
                    val p2raw = pos2.toDoubleOrNull() ?: (memberLength / 12.0)
                    
                    val p1 = if (unitSystem == UnitSystem.METRIC) p1raw.meters else p1raw.feet
                    val p2 = if (unitSystem == UnitSystem.METRIC) p2raw.meters else p2raw.feet

                    // Use the first span as default if available
                    val targetSpanId = spans.firstOrNull()?.id ?: UUID.randomUUID()

                    val load = when (loadType) {
                        0 -> Load.PointLoad(
                            if (unitSystem == UnitSystem.METRIC) v1.kiloNewtons else v1.poundsForce,
                            targetSpanId, p1, direction = direction
                        )
                        1 -> Load.UniformDistributedLoad(
                            if (unitSystem == UnitSystem.METRIC) (v1 / 4.4482216).lbPerIn else v1.lbPerFt,
                            targetSpanId, p1, p2, direction = direction
                        )
                        2 -> Load.PointMoment(
                            if (unitSystem == UnitSystem.METRIC) (v1 * 8.8507).lbIn else v1.lbFt,
                            targetSpanId, p1, direction = direction
                        )
                        3 -> Load.AxialLoad(
                            if (unitSystem == UnitSystem.METRIC) v1.kiloNewtons else v1.poundsForce,
                            targetSpanId, direction = direction
                        )
                        else -> Load.PointLoad(v1.poundsForce, targetSpanId, p1)
                    }
                    onConfirm(load)
                },
                enabled = value1.isNotEmpty() && (loadType == 3 || pos1.isNotEmpty())
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

