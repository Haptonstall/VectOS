package com.lz.ui.serviceability

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lz.model.presentation.ServiceabilityCategory
import com.lz.model.presentation.ServiceabilityLimits

@Composable
fun ServiceabilityPickerDialog(
    currentLimits: ServiceabilityLimits,
    onDismiss: () -> Unit,
    onConfirmed: (ServiceabilityLimits) -> Unit
) {
    var state by remember { mutableStateOf(currentLimits) }

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
                Text("Serviceability Limits", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Select a standard IBC category or define custom deflection limits for this span.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ServiceabilityCategory.entries) { category ->
                        val isSelected = state.category == category
                        Surface(
                            onClick = { state = state.copy(category = category) },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            ),
                            border = if (isSelected) BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.primary
                            ) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    category.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    getCategoryDescription(category),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (state.category == ServiceabilityCategory.CUSTOM) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.customLiveDenominator?.toString() ?: "",
                            onValueChange = {
                                state = state.copy(customLiveDenominator = it.toDoubleOrNull())
                            },
                            label = { Text("Live L/") },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = state.customTotalDenominator?.toString() ?: "",
                            onValueChange = {
                                state = state.copy(customTotalDenominator = it.toDoubleOrNull())
                            },
                            label = { Text("Total L/") },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirmed(state) }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

private fun getCategoryDescription(category: ServiceabilityCategory): String = when (category) {
    ServiceabilityCategory.FLOOR -> "Standard Floor: L/360 Live, L/240 Total (IBC Table 1604.3)"
    ServiceabilityCategory.ROOF_SUPPORTING_PLASTER -> "Roof w/ Plaster Ceiling: L/360 Live, L/240 Total"
    ServiceabilityCategory.ROOF_SUPPORTING_NONPLASTER -> "Roof w/ Non-Plaster Ceiling: L/240 Live, L/180 Total"
    ServiceabilityCategory.ROOF_NO_CEILING -> "Roof w/o Ceiling: L/180 Live, L/120 Total"
    ServiceabilityCategory.EXTERIOR_WALL_BRITTLE -> "Exterior Wall (Brittle Finish): L/240"
    ServiceabilityCategory.EXTERIOR_WALL_FLEXIBLE -> "Exterior Wall (Flexible Finish): L/120"
    ServiceabilityCategory.INTERIOR_PARTITION -> "Interior Partition: L/120"
    ServiceabilityCategory.CUSTOM -> "User-defined deflection denominators"
}