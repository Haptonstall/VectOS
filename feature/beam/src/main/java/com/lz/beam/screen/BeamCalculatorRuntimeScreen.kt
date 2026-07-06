package com.lz.beam.screen

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.lz.beam.presentation.BeamViewModel
import com.lz.beam.ui.BeamCalculatorScreen
import com.lz.domain.project.Project
import com.lz.runtime.screen.api.RuntimeScreen
import java.time.LocalDateTime

class BeamCalculatorRuntimeScreen : RuntimeScreen {

    override val route = "beam/calculator"

    @Composable
    override fun Content() {
        val viewModel: BeamViewModel = hiltViewModel()
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        // Use the project from the calculation if available, or a default
        val activeProject = viewModel.calculationResult?.project ?: Project(
            name = "Beam Calculation",
            createdAt = LocalDateTime.now()
        )

        BeamCalculatorScreen(
            activeProject = activeProject,
            viewModel = viewModel,
            onBack = { backDispatcher?.onBackPressed() }
        )
    }
}
