package com.lz.vectos.app.runtime

import androidx.navigation.NavHostController
import com.lz.runtime.compose.navigation.NavigationDestination

/**
 * Central navigation entry point for Runtime modules.
 */
object RuntimeDestinationLauncher {

    fun launch(

        navController: NavHostController,

        destination: NavigationDestination

    ) {

        navController.navigate(

            destination.id

        )

    }

}