package com.lz.runtime.registry

import com.lz.runtime.api.NavigationDestination
import com.lz.runtime.api.NavigationRegistry
import com.lz.runtime.core.AbstractRuntimeService
import java.util.concurrent.ConcurrentHashMap

class DefaultNavigationRegistry :
    AbstractRuntimeService(
        "runtimeEnvironment.navigationRegistry"
    ),
    NavigationRegistry {

    private val destinations =
        ConcurrentHashMap<String, NavigationDestination>()

    override fun onShutdown() {

        destinations.clear()
    }

    override fun register(
        destination: NavigationDestination
    ) {

        destinations[destination.id] = destination
    }

    override fun unregister(
        destinationId: String
    ) {

        destinations.remove(destinationId)
    }

    override fun get(
        destinationId: String
    ): NavigationDestination? {

        return destinations[destinationId]
    }

    override fun destinations(): List<NavigationDestination> {

        return destinations.values.toList()
    }

    override fun destinations(
        moduleId: String
    ): List<NavigationDestination> {

        return destinations.values.filter {
            it.moduleId == moduleId
        }
    }
}