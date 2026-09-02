package com.lz.beam.runtime

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lz.beam.presentation.BeamViewModel
import com.lz.beam.presentation.BeamViewModelFactory
import com.lz.beam.ui.BeamCalculatorScreen
import com.lz.domain.project.Project
import com.lz.runtime.compose.navigation.LocalRuntimeNavigationArguments
import com.lz.runtime.compose.navigation.NavigationDestination
import com.lz.runtime.compose.screen.api.ScreenProvider
import java.time.LocalDateTime
import java.util.UUID

/**
 * Compose implementation contributed by the Beam RuntimeModule.
 *
 * A ScreenProvider owns one or more logical navigation destinations and
 * renders the appropriate Compose UI.
 */
class BeamScreenProvider : ScreenProvider {

    override val runtimeModuleId: String =
        BeamDescriptor.id

    override fun destinations(): List<NavigationDestination> =
        listOf(
            BeamNavigationDestination
        )

    @Composable
    override fun Content(
        destination: NavigationDestination
    ) {

        when (destination.id) {

            BeamNavigationDestination.id -> {

                BeamCalculator()

            }

            else -> error(
                "Unknown Beam destination '${destination.id}'."
            )

        }

    }

    @Composable
    private fun BeamCalculator() {

        val context = LocalContext.current
        val factory = remember { BeamViewModelFactory(context) }
        val viewModel: BeamViewModel =
            viewModel(factory = factory)
        val calculationId = LocalRuntimeNavigationArguments.current["calculationId"]
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        LaunchedEffect(calculationId) {
            calculationId?.let { viewModel.loadCalculation(it) }
        }

        val backDispatcher =
            LocalOnBackPressedDispatcherOwner
                .current
                ?.onBackPressedDispatcher

        val project =
            viewModel.calculationResult?.project
                ?: Project(
                    name = "Beam Calculation",
                    createdAt = LocalDateTime.now()
                )

        BeamCalculatorScreen(

            activeProject = project,

            viewModel = viewModel,

            onBack = {

                backDispatcher?.onBackPressed()

            }

        )

    }

}
