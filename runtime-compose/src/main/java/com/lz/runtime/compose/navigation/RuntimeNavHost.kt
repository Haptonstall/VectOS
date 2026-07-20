package com.lz.runtime.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.compose.api.ComposeRuntimeModule

@Composable
fun RuntimeNavHost(
    runtime: RuntimeEnvironment,
    navController: NavHostController,
    startDestination: String
) {

    val providers = remember(runtime) {

        runtime.context
            .runtimeModuleRegistry
            .modules()
            .filterIsInstance<ComposeRuntimeModule>()
            .flatMap {
                it.screenProviders()
            }

    }

    val destinations = remember(providers) {

        providers.flatMap {
            it.destinations()
        }

    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        destinations.forEach { destination ->

            composable(destination.id) {

                RuntimeDestinationHost(
                    destination = destination,
                    providers = providers
                )

            }

        }

    }

}