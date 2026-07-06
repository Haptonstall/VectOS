package com.lz.vectos.app.navigation

import com.lz.runtime.api.NavigationDestination
import java.util.concurrent.ConcurrentHashMap

object ScreenFactoryRegistry {

    private val factories =
        ConcurrentHashMap<String, ComposeScreenFactory>()

    fun register(
        factory: ComposeScreenFactory
    ) {

        factories[factory.destination.id] = factory
    }

    fun factoryFor(
        destination: NavigationDestination
    ): ComposeScreenFactory? {

        return factories[destination.id]
    }

    fun clear() {

        factories.clear()
    }
}