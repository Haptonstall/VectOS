package com.lz.vectos.ui.beam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.structural.BoundaryRestraint
import com.lz.vectos.domain.structural.SupportCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportConditionPicker(
    currentCondition: SupportCondition,
    currentRestraint: BoundaryRestraint,
    onDismiss: () -> Unit,
    onConfirmed: (SupportCondition, BoundaryRestraint) -> Unit
) {
    var selectedCondition by remember { mutableStateOf(currentCondition) }
    var verticalFixed by remember { mutableStateOf(currentRestraint.verticalFixed) }
    var rotationFixed by remember { mutableStateOf(currentRestraint.rotationFixed) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                "Select Support Condition",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Preset Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SupportOptionItem(
                    "Pinned", 
                    Icons.Default.ChangeHistory, 
                    selectedCondition == SupportCondition.PINNED,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedCondition = SupportCondition.PINNED
                    verticalFixed = true
                    rotationFixed = false
                }
                SupportOptionItem(
                    "Fixed", 
                    Icons.Default.Square, 
                    selectedCondition == SupportCondition.FIXED,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedCondition = SupportCondition.FIXED
                    verticalFixed = true
                    rotationFixed = true
                }
                SupportOptionItem(
                    "Roller", 
                    Icons.Default.RadioButtonUnchecked, 
                    selectedCondition == SupportCondition.ROLLER,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedCondition = SupportCondition.ROLLER
                    verticalFixed = true
                    rotationFixed = false
                }
                SupportOptionItem(
                    "Free", 
                    Icons.Default.Remove, 
                    selectedCondition == SupportCondition.FREE,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedCondition = SupportCondition.FREE
                    verticalFixed = false
                    rotationFixed = false
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Restraints
            Text("Custom Restraints", style = MaterialTheme.typography.labelLarge)
            
            ListItem(
                headlineContent = { Text("Vertical Restraint") },
                supportingContent = { Text(if (verticalFixed) "Fixed" else "Free") },
                trailingContent = {
                    Switch(
                        checked = verticalFixed,
                        onCheckedChange = { 
                            verticalFixed = it
                            selectedCondition = SupportCondition.CUSTOM
                        }
                    )
                }
            )
            ListItem(
                headlineContent = { Text("Rotational Restraint") },
                supportingContent = { Text(if (rotationFixed) "Fixed" else "Free") },
                trailingContent = {
                    Switch(
                        checked = rotationFixed,
                        onCheckedChange = { 
                            rotationFixed = it
                            selectedCondition = SupportCondition.CUSTOM
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
                    onConfirmed(selectedCondition, BoundaryRestraint(verticalFixed, rotationFixed))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Condition")
            }
        }
    }
}

@Composable
private fun SupportOptionItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(enabled = isSelected)
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
