package com.lz.vectos.app.platform

import androidx.compose.runtime.Composable
import com.lz.runtime.api.NavigationDestination
import com.lz.runtime.screen.api.ScreenProvider

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
    fun Content(

        destination: NavigationDestination

    )
}