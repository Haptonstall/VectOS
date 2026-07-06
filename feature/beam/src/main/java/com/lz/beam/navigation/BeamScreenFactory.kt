package com.lz.beam.navigation

import androidx.compose.runtime.Composable
import com.lz.beam.presentation.BeamViewModel
import com.lz.beam.runtime.BeamNavigationDestination
import com.lz.beam.ui.BeamCalculatorScreen
import com.lz.runtime.api.NavigationDestination
import com.lz.vectos.app.navigation.ComposeScreenFactory
import com.lz.vectos.ui.beam.BeamCalculatorScreen

class BeamScreenFactory(

    private val viewModel: BeamViewModel

) : ComposeScreenFactory {

    override val destination: NavigationDestination =
        BeamNavigationDestination

    @Composable
    override fun Content() {

        BeamCalculatorScreen(
            viewModel = viewModel,
            onBack = { }
        )
    }
}