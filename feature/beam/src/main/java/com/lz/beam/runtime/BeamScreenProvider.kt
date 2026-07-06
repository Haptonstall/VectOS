package com.lz.beam.runtime

import com.lz.beam.screen.BeamCalculatorRuntimeScreen
import com.lz.runtime.api.NavigationDestination
import com.lz.runtime.screen.api.ScreenProvider
import com.lz.runtime.screen.api.RuntimeScreen

class BeamScreenProvider : ScreenProvider {

    override val runtimeModuleId: String =
        BeamDescriptor.id

    override fun destinations(): List<NavigationDestination> {

        return listOf(

            BeamNavigationDestination

        )
    }

    override fun screens(): List<RuntimeScreen> {

        return listOf(

            BeamCalculatorRuntimeScreen()

        )

    }
}
