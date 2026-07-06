package com.lz.runtime.api.screen

import com.lz.runtime.api.NavigationDestination

/**
 * Creates a UI screen for a Runtime destination.
 *
 * The Runtime knows only about this contract.
 *
 * The Android application provides the implementation.
 */
interface RuntimeScreenFactory {

    /**
     * Destination supported by this factory.
     */
    val destination: NavigationDestination
}