package com.lz.runtime.compose.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.compose.api.ComposeRuntimeModule

/**
 * Installs every navigation destination contributed by every loaded
 * Compose RuntimeModule into the application's NavGraph.
 *
 * The application owns exactly one NavHost.
 *
 * Runtime modules simply contribute destinations.
 */
fun NavGraphBuilder.installRuntimeRoutes(
    runtime: RuntimeEnvironment
) {

    val providers =
        runtime.context
            .runtimeModuleRegistry
            .modules()
            .filterIsInstance<ComposeRuntimeModule>()
            .flatMap { module ->

                module.screenProviders()

            }

    val destinations =
        providers.flatMap { provider ->

            provider.destinations()

        }

    destinations.forEach { destination ->

        composable(
            route = destination.id
        ) {

            RuntimeDestinationHost(

                destination = destination,

                providers = providers

            )

        }

    }

}