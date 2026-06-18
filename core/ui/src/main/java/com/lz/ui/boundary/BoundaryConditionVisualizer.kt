package com.lz.ui.boundary

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.lz.domain.structural.NodeBoundaryCondition

@Composable
fun BoundaryConditionVisualizer(
    condition: NodeBoundaryCondition
) {
    Text(
        text = condition.toString()
    )
}
