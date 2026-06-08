package com.lz.vectos.ui.calculator

import androidx.compose.ui.graphics.vector.ImageVector
import com.lz.vectos.presentation.CalculationContext

interface CalculatorDefinition {
    val id: String                 // e.g. "beam_simply_supported"
    val displayName: String        // "Simply Supported Beam"
    val description: String        // Shown in tool picker
    val icon: ImageVector
    val supportsQuickCalc: Boolean

    fun createRoute(context: CalculationContext): CalculatorRoute
}
