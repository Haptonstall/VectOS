package com.lz.runtime.screen.internal

import com.lz.runtime.api.NavigationDestination
import com.lz.runtime.screen.api.ScreenProvider
import com.lz.runtime.screen.api.ScreenRegistry
import java.util.concurrent.ConcurrentHashMap

class DefaultScreenRegistry : ScreenRegistry {

    private val registrations =
        ConcurrentHashMap<String, ScreenRegistration>()

    override fun register(
        provider: ScreenProvider
    ) {

        provider.destinations().forEach { destination ->

            registrations[destination.id] =
                ScreenRegistration(
                    destination = destination,
                    provider = provider
                )
        }
    }

    override fun unregister(
        runtimeModuleId: String
    ) {

        registrations.entries.removeIf {

            it.value.provider.runtimeModuleId == runtimeModuleId

        }
    }

    override fun providerFor(
        destinationId: String
    ): ScreenProvider? {

        return registrations[destinationId]?.provider
    }

    override fun destinations(): List<NavigationDestination> {

        return registrations.values
            .map { it.destination }
            .sortedBy { it.title }
    }

    override fun clear() {

        registrations.clear()
    }
}