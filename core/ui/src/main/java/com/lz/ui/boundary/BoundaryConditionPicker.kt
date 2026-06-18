package com.lz.ui.boundary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lz.domain.structural.BoundaryConditionFactory
import com.lz.domain.structural.BoundaryConditionPreset
import com.lz.domain.structural.NodeBoundaryCondition
import com.lz.model.structural.SupportCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoundaryConditionPicker(
    config: BoundaryConditionPickerConfig,
    currentCondition: NodeBoundaryCondition,
    onDismiss: () -> Unit,
    onConfirmed: (NodeBoundaryCondition) -> Unit
) {
    val dofEditorConfig: DofEditorConfig

    var selectedPreset by remember {
        mutableStateOf<BoundaryConditionPreset?>(null)
    }

    var condition by remember {
        mutableStateOf(currentCondition)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = config.title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                config.presetOptions.forEach { option ->

                    BoundaryOptionItem(
                        label = option.label,
                        icon = option.icon,
                        isSelected = selectedPreset == option.preset,
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedPreset = option.preset

                        condition =
                            BoundaryConditionFactory
                                .fromPreset(option.preset)
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            if (config.allowCustomDofs) {
                DofConstraintEditor(
                    condition = condition,
                    config = config.dofEditorConfig,
                    onConditionChanged = {
                        condition = it
                        selectedPreset =
                            BoundaryConditionPreset.CUSTOM
                    }
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onConfirmed(condition)
                }
            ) {
                Text("Apply Condition")
            }
        }
    }
}