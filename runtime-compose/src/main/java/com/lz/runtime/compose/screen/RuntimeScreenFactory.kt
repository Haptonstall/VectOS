package com.lz.runtime.compose.screen

import com.lz.runtime.compose.navigation.NavigationDestination

/**
 * Creates UI for a Runtime destination.
 *
 * Implemented by the Android UI layer.
 */
interface RuntimeScreenFactory {

    val destination: NavigationDestination
}