package com.lz.vectos.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lz.runtime.screen.api.RuntimeScreen
import com.lz.runtime.screen.api.ScreenRegistry

/**
 * Registers every Runtime supplied screen with Compose Navigation.
 */
class RuntimeDestinationHost(

    private val screenRegistry: ScreenRegistry

) {

    fun register(

        graph: NavGraphBuilder

    ) {

        screenRegistry
            .screens()
            .forEach { screen ->

                registerScreen(

                    graph = graph,

                    screen = screen

                )

            }

    }

    private fun registerScreen(

        graph: NavGraphBuilder,

        screen: RuntimeScreen

    ) {

        graph.composable(

            route = screen.route

        ) {

            screen.Content()

        }

    }

}