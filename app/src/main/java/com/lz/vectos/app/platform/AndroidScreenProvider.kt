package com.lz.vectos.app.platform

import androidx.compose.runtime.Composable
import com.lz.runtime.compose.navigation.NavigationDestination
import com.lz.runtime.compose.screen.api.ScreenProvider

/**
 * Android implementation of a Runtime ScreenProvider.
 *
 * Implemented inside feature modules.
 */
interface AndroidScreenProvider :
    ScreenProvider {

    /**
     * Returns the Compose implementation for
     * the requested destination.
     */
    @Composable
    override fun Content(

        destination: NavigationDestination

    )
}