package com.lz.beam.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Square
import com.lz.ui.boundary.BoundaryConditionPickerConfig
import com.lz.domain.structural.BoundaryConditionPreset
import com.lz.model.structural.ConstraintType
import com.lz.model.structural.DegreeOfFreedom
import com.lz.ui.boundary.BoundaryPresetOption
import com.lz.ui.boundary.DofEditorConfig


object BeamBoundaryConditionConfig {

    val config =
        BoundaryConditionPickerConfig(
            title = "Beam Boundary Condition",
            presetOptions = listOf(
                BoundaryPresetOption(
                    BoundaryConditionPreset.PINNED,
                    "Pinned",
                    Icons.Default.ChangeHistory
                ),
                BoundaryPresetOption(
                    BoundaryConditionPreset.FIXED,
                    "Fixed",
                    Icons.Default.Square
                ),
                BoundaryPresetOption(
                    BoundaryConditionPreset.ROLLER_Y,
                    "Roller",
                    Icons.Default.RadioButtonUnchecked
                ),
                BoundaryPresetOption(
                    BoundaryConditionPreset.FREE,
                    "Free",
                    Icons.Default.Remove
                )
            ),

            dofEditorConfig =
                DofEditorConfig(
                    editableDofs = setOf(
                        DegreeOfFreedom.UY,
                        DegreeOfFreedom.RZ
                    ),
                    allowedConstraintTypes =
                        setOf(
                            ConstraintType.FREE,
                            ConstraintType.FIXED,
                            ConstraintType.SPRING
                        )

                )
        )
}