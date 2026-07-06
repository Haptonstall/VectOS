package com.lz.runtime.screen.api

import com.lz.runtime.api.NavigationDestination

interface ScreenRegistry {

    /**
     * Registers every destination exposed by a provider.
     */
    fun register(
        provider: ScreenProvider
    )

    /**
     * Removes all destinations belonging to a RuntimeModule.
     */
    fun unregister(
        runtimeModuleId: String
    )

    /**
     * Returns the provider responsible for the destination.
     */
    fun providerFor(
        destinationId: String
    ): ScreenProvider?

    /**
     * Returns every registered destination.
     */
    fun destinations(): List<NavigationDestination>

    /**
     * Removes all registrations.
     */
    fun clear()
}