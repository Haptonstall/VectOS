package com.lz.vectos.app.platform

import com.lz.runtime.screen.api.ScreenProvider
import java.util.concurrent.ConcurrentHashMap

object AndroidScreenRegistry {

    private val providers =
        ConcurrentHashMap<String, AndroidScreenProvider>()

    fun register(

        provider: AndroidScreenProvider

    ) {

        provider.destinations()

            .forEach {

                providers[it.id] = provider

            }

    }

    fun providerFor(

        destinationId: String

    ): AndroidScreenProvider? {

        return providers[destinationId]

    }

    fun clear() {

        providers.clear()

    }
}