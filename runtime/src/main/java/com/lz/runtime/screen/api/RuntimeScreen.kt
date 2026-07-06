package com.lz.runtime.screen.api

import androidx.compose.runtime.Composable

/**
 * Represents a screen contributed by a RuntimeModule.
 *
 * Runtime never knows what the screen actually displays.
 */
interface RuntimeScreen {

    /**
     * Unique navigation route.
     */
    val route: String

    /**
     * Composable content.
     */
    @Composable
    fun Content()

}