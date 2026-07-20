package com.lz.runtime.compose.screen.api

import androidx.compose.runtime.Composable
import com.lz.runtime.compose.navigation.NavigationDestination

interface ScreenProvider {

    /**
     * RuntimeModule owning these destinations.
     */
    val runtimeModuleId: String

    /**
     * Navigation destinations rendered by this provider.
     */
    fun destinations(): List<NavigationDestination>

    /**
     * Render one destination.
     */
    @Composable
    fun Content(

        destination: NavigationDestination

    )

}