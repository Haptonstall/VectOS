package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lz.vectos.domain.structural.BracingType
import com.lz.vectos.domain.structural.SpanBracing
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.UnitSystem
import com.lz.vectos.domain.units.UnitConverter
import com.lz.vectos.domain.units.LengthUnit
import java.util.Locale

@Composable
fun BracingPickerDialog(
    currentBracing: SpanBracing,
    unitSystem: UnitSystem,
    onDismiss: () -> Unit,
    onConfirmed: (SpanBracing) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentBracing.type) }
    var customLocations by remember { mutableStateOf(currentBracing.customLocations) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Span Bracing", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Lateral-torsional buckling bracing for the compression flange.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                // Bracing Type Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bracing Type", style = MaterialTheme.typography.labelMedium)
                    BracingType.entries.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (selectedType == BracingType.CUSTOM) {
                    HorizontalDivider()
                    CustomLocationsEditor(
                        locations = customLocations,
                        unitSystem = unitSystem,
                        onUpdate = { customLocations = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirmed(SpanBracing(selectedType, customLocations)) }) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomLocationsEditor(
    locations: List<Length>,
    unitSystem: UnitSystem,
    onUpdate: (List<Length>) -> Unit
) {
    val targetUnit = if (unitSystem == UnitSystem.METRIC) LengthUnit.METERS else LengthUnit.FEET
    var newLocationText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Custom Locations", style = MaterialTheme.typography.labelMedium)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newLocationText,
                onValueChange = { newLocationText = it },
                label = { Text("Distance (${targetUnit.symbol})") },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            IconButton(
                onClick = {
                    newLocationText.toDoubleOrNull()?.let {
                        val newLoc = UnitConverter.toInternalBase(it, targetUnit)
                        onUpdate((locations + newLoc).sortedBy { l -> l.inches })
                        newLocationText = ""
                    }
                },
                enabled = newLocationText.toDoubleOrNull() != null
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Location")
            }
        }

        LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
            items(locations) { loc ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2f %s from start", UnitConverter.toDisplayValue(loc, targetUnit), targetUnit.symbol),
                        style = MaterialTheme.typography.bodySmall
                    )
                    IconButton(onClick = { onUpdate(locations.filter { it != loc }) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
