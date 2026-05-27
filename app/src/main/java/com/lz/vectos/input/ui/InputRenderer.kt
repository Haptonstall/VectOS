package com.lz.vectos.input.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lz.vectos.input.model.InputKeyboardType
import com.lz.vectos.input.model.InputVisibility
import com.lz.vectos.input.state.CalculationInputState
import com.lz.vectos.input.state.InputEvent

/**
 * GENERIC INPUT RENDERER (COMPOSE)
 * Stateless and generic. Emits events for any changes.
 * No knowledge of calculation logic.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationInput(
    state: CalculationInputState,
    onEvent: (InputEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.spec.visibility == InputVisibility.ADVANCED) {
        // Architecture supports advanced fields, e.g., could be collapsed by default
        // For now, we render if requested, but this is the hook for progressive disclosure.
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.rawValue,
                onValueChange = { onEvent(InputEvent.ValueChanged(state.spec.id, it)) },
                label = { Text(state.spec.label) },
                isError = !state.isValid,
                supportingText = {
                    if (!state.isValid) {
                        Text(state.errorMessage ?: "Invalid input")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (state.spec.keyboardType) {
                        InputKeyboardType.DECIMAL -> KeyboardType.Decimal
                        InputKeyboardType.NUMBER -> KeyboardType.Number
                        InputKeyboardType.TEXT -> KeyboardType.Text
                    }
                ),
                modifier = Modifier.weight(1f)
            )

            if (state.spec.unitOptions.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.width(100.dp)
                ) {
                    OutlinedTextField(
                        value = state.selectedUnit?.label ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        state.spec.unitOptions.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.label) },
                                onClick = {
                                    onEvent(InputEvent.UnitChanged(state.spec.id, unit))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InputList(
    inputs: List<CalculationInputState>,
    onEvent: (InputEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progressive Disclosure: Group inputs by visibility if needed
        val required = inputs.filter { it.spec.visibility == InputVisibility.REQUIRED }
        val advanced = inputs.filter { it.spec.visibility == InputVisibility.ADVANCED }

        required.forEach { inputState ->
            CalculationInput(state = inputState, onEvent = onEvent)
        }

        if (advanced.isNotEmpty()) {
            var showAdvanced by remember { mutableStateOf(false) }
            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "Hide Advanced Settings" else "Show Advanced Settings")
            }
            if (showAdvanced) {
                advanced.forEach { inputState ->
                    CalculationInput(state = inputState, onEvent = onEvent)
                }
            }
        }
    }
}
