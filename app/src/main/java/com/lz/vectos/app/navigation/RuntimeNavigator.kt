package com.lz.vectos.app.navigation

import androidx.navigation.NavHostController
import com.lz.runtime.compose.navigation.NavigationDestination

class RuntimeNavigator(

    private val navController: NavHostController

) {

    fun navigate(

        destination: NavigationDestination

    ) {

        navController.navigate(
            destination.id
        )
    }

    fun back() {

        navController.popBackStack()

    }
}