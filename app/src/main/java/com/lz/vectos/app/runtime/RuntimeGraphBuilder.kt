package com.lz.vectos.app.runtime

import androidx.navigation.NavGraphBuilder
import com.lz.runtime.screen.api.ScreenRegistry
import com.lz.vectos.app.navigation.RuntimeDestinationHost

/**
 * Registers every Runtime supplied screen into the
 * application's navigation graph.
 */
object RuntimeGraphBuilder {

    fun register(

        graph: NavGraphBuilder,

        screenRegistry: ScreenRegistry

    ) {

        RuntimeDestinationHost(

            screenRegistry

        ).register(

            graph

        )

    }

}