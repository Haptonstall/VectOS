package com.lz.ui.boundary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lz.model.structural.ConstraintType
import com.lz.model.structural.NodeBoundaryCondition

@Composable
fun DofConstraintEditor(
    condition: NodeBoundaryCondition,
    config: DofEditorConfig,
    onConditionChanged: (NodeBoundaryCondition) -> Unit
) {
    Column {

        Text(
            text = "Degrees of Freedom",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        config.editableDofs.forEach { dof ->

            val constraint = condition[dof]

            ListItem(
                headlineContent = {
                    Text(dof.displayName)
                },
                supportingContent = {
                    Text(constraint.type.name)
                },
                trailingContent = {
                    ConstraintTypeDropdown(
                        selectedType = constraint.type,
                        allowedTypes = config.allowedConstraintTypes,
                        onSelected = { selected ->

                            val updated =
                                constraint.copy(
                                    type = selected,
                                    stiffness =
                                        if (selected == ConstraintType.SPRING)
                                            constraint.stiffness ?: 1000.0
                                        else
                                            null
                                )

                            onConditionChanged(
                                condition.withConstraint(
                                    dof,
                                    updated
                                )
                            )
                        }
                    )
                }
            )

            if (constraint.type == ConstraintType.SPRING) {
                SpringConstraintEditor(
                    stiffness = constraint.stiffness ?: 1000.0,
                    onStiffnessChanged = { stiffness ->
                        onConditionChanged(
                            condition.withConstraint(
                                dof,
                                constraint.copy(
                                    stiffness = stiffness
                                )
                            )
                        )
                    }
                )
            }
        }
    }
}