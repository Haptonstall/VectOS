package com.lz.ui.boundary

import androidx.compose.ui.graphics.vector.ImageVector
import com.lz.domain.structural.BoundaryConditionPreset

data class BoundaryPresetOption(
    val preset: BoundaryConditionPreset,
    val label: String,
    val icon: ImageVector
)