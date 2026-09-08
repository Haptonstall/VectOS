package com.lz.ui.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.codes.ServiceabilityCriterion
import com.lz.model.regulatory.codes.ServiceabilityLimitType

/**
 * Standard preset criteria sets. Reused by both the picker below and
 * anywhere else a sensible default is needed.
 */
object DeflectionPresets {

    val floor = listOf(
        ServiceabilityCriterion(
            limitType = ServiceabilityLimitType.LIVE_LOAD_DEFLECTION,
            name = "Live Load",
            loadCategory = LoadCategory.LIVE,
            spanDenominator = 360.0,
            description = "L/360 live load — typical floor"
        ),
        ServiceabilityCriterion(
            limitType = ServiceabilityLimitType.TOTAL_LOAD_DEFLECTION,
            name = "Total Load",
            loadCategory = null,
            spanDenominator = 240.0,
            description = "L/240 total load — typical floor"
        )
    )

    val roof = listOf(
        ServiceabilityCriterion(
            limitType = ServiceabilityLimitType.LIVE_LOAD_DEFLECTION,
            name = "Roof Live Load",
            loadCategory = LoadCategory.ROOF_LIVE,
            spanDenominator = 240.0,
            description = "L/240 roof live load — typical roof"
        ),
        ServiceabilityCriterion(
            limitType = ServiceabilityLimitType.TOTAL_LOAD_DEFLECTION,
            name = "Total Load",
            loadCategory = null,
            spanDenominator = 180.0,
            description = "L/180 total load — typical roof"
        )
    )

    /** Short label for display on the span editor's Deflect button. */
    fun labelFor(criteria: List<ServiceabilityCriterion>?): String = when {
        criteria == null -> "Standard"
        criteria == floor -> "Floor"
        criteria == roof -> "Roof"
        criteria.isEmpty() -> "Standard"
        else -> criteria.joinToString("/") { "L/${it.spanDenominator.toInt()}" }
    }
}

private enum class DeflectionPresetChoice { FLOOR, ROOF, CUSTOM, STANDARD }

@Composable
fun DeflectionCriteriaPickerDialog(
    currentCriteria: List<ServiceabilityCriterion>?,
    onDismiss: () -> Unit,
    onConfirmed: (List<ServiceabilityCriterion>?) -> Unit
) {
    val initialChoice = when {
        currentCriteria == null -> DeflectionPresetChoice.STANDARD
        currentCriteria == DeflectionPresets.floor -> DeflectionPresetChoice.FLOOR
        currentCriteria == DeflectionPresets.roof -> DeflectionPresetChoice.ROOF
        else -> DeflectionPresetChoice.CUSTOM
    }
    var choice by remember { mutableStateOf(initialChoice) }

    val existingLive = currentCriteria?.firstOrNull { it.loadCategory != null }
    val existingTotal = currentCriteria?.firstOrNull { it.loadCategory == null }
    var liveDenomText by remember {
        mutableStateOf(existingLive?.spanDenominator?.let { "%.0f".format(it) } ?: "360")
    }
    var totalDenomText by remember {
        mutableStateOf(existingTotal?.spanDenominator?.let { "%.0f".format(it) } ?: "240")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Deflection Criteria",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Override the deflection limit checked for this span only. Leave on Standard to use the active building code's criteria.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BracingOptionButton(
                        title = "Standard\n(Code)",
                        isSelected = choice == DeflectionPresetChoice.STANDARD,
                        onClick = { choice = DeflectionPresetChoice.STANDARD },
                        modifier = Modifier.weight(1f)
                    )
                    BracingOptionButton(
                        title = "Floor\n(L/360, L/240)",
                        isSelected = choice == DeflectionPresetChoice.FLOOR,
                        onClick = { choice = DeflectionPresetChoice.FLOOR },
                        modifier = Modifier.weight(1f)
                    )
                    BracingOptionButton(
                        title = "Roof\n(L/240, L/180)",
                        isSelected = choice == DeflectionPresetChoice.ROOF,
                        onClick = { choice = DeflectionPresetChoice.ROOF },
                        modifier = Modifier.weight(1f)
                    )
                    BracingOptionButton(
                        title = "Custom",
                        isSelected = choice == DeflectionPresetChoice.CUSTOM,
                        onClick = { choice = DeflectionPresetChoice.CUSTOM },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (choice == DeflectionPresetChoice.CUSTOM) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = liveDenomText,
                            onValueChange = { liveDenomText = it },
                            label = { Text("Live Load: L /") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = totalDenomText,
                            onValueChange = { totalDenomText = it },
                            label = { Text("Total Load: L /") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Text(
                        "Leave a field blank to skip that check for this span.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val result: List<ServiceabilityCriterion>? = when (choice) {
                                DeflectionPresetChoice.STANDARD -> null
                                DeflectionPresetChoice.FLOOR -> DeflectionPresets.floor
                                DeflectionPresetChoice.ROOF -> DeflectionPresets.roof
                                DeflectionPresetChoice.CUSTOM -> buildCustomCriteria(liveDenomText, totalDenomText)
                            }
                            onConfirmed(result)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.height(56.dp).padding(horizontal = 8.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun buildCustomCriteria(liveDenomText: String, totalDenomText: String): List<ServiceabilityCriterion> {
    val results = mutableListOf<ServiceabilityCriterion>()
    liveDenomText.toDoubleOrNull()?.takeIf { it > 0 }?.let { denom ->
        results += ServiceabilityCriterion(
            limitType = ServiceabilityLimitType.LIVE_LOAD_DEFLECTION,
            name = "Live Load",
            loadCategory = LoadCategory.LIVE,
            spanDenominator = denom,
            description = "L/${denom.toInt()} live load — custom"
        )
    }
    totalDenomText.toDoubleOrNull()?.takeIf { it > 0 }?.let { denom ->
        results += ServiceabilityCriterion(
            limitType = ServiceabilityLimitType.TOTAL_LOAD_DEFLECTION,
            name = "Total Load",
            loadCategory = null,
            spanDenominator = denom,
            description = "L/${denom.toInt()} total load — custom"
        )
    }
    return results
}
