package com.lz.runtime.compose.screen

import com.lz.runtime.compose.screen.api.ScreenProvider
import com.lz.runtime.compose.screen.api.ScreenRegistry
import com.lz.runtime.core.AbstractRuntimeService

class DefaultScreenRegistry :
    AbstractRuntimeService(
        "runtime.compose.screen.registry"
    ),
    ScreenRegistry {

    private val providers =
        LinkedHashMap<String, ScreenProvider>()

    override fun register(
        provider: ScreenProvider
    ) {

        provider.destinations().forEach {

            providers[it.id] = provider

        }

    }

    override fun unregister(
        route: String
    ) {

        providers.remove(route)

    }

    override fun get(
        route: String
    ): ScreenProvider? =

        providers[route]

    override fun screenProviders(): List<ScreenProvider> =

        providers.values
            .distinct()

    override fun onShutdown() {

        providers.clear()

    }

}