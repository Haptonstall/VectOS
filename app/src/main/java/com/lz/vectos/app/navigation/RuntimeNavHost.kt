package com.lz.vectos.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lz.runtime.api.RuntimeEnvironment

@Composable
fun RuntimeNavHost(

    runtime: RuntimeEnvironment,

    navController: NavHostController,

    startDestination: String

) {

    NavHost(

        navController = navController,

        startDestination = startDestination

    ) {

        runtime.context
            .screenRegistry
            .destinations()
            .forEach { destination ->

                composable(

                    route = destination.id

                ) {

                    RuntimeDestinationHost(

                        destination = destination

                    )

                }
            }
    }
}