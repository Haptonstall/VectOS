package com.lz.vectos.app.platform

import androidx.navigation.NavHostController
import com.lz.runtime.api.NavigationDestination

/**
 * Converts Runtime destinations into
 * Navigation routes.
 */
class RuntimeLauncher(

    private val navController: NavHostController

) {

    fun launch(

        destination: NavigationDestination

    ) {

        navController.navigate(
            destination.id
        )
    }
}