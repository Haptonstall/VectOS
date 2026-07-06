package com.lz.runtime.api

/**
 * Registry of navigation destinations exposed by installed modules.
 *
 * The RuntimeEnvironment remains UI-agnostic. The App Shell maps these logical
 * destinations to Compose navigation destinations.
 */
interface NavigationRegistry : RuntimeService {

    /**
     * Registers a destination.
     */
    fun register(destination: NavigationDestination)

    /**
     * Removes a destination.
     */
    fun unregister(destinationId: String)

    /**
     * Returns a destination.
     */
    fun get(destinationId: String): NavigationDestination?

    /**
     * Returns every destination.
     */
    fun destinations(): List<NavigationDestination>

    /**
     * Returns all destinations contributed by a module.
     */
    fun destinations(
        moduleId: String
    ): List<NavigationDestination>
}