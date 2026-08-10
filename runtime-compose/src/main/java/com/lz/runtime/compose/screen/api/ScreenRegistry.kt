package com.lz.runtime.compose.screen.api

import com.lz.runtime.api.RuntimeService

interface ScreenRegistry :
    RuntimeService {

    fun register(
        provider: ScreenProvider
    )

    fun unregister(
        route: String
    )

    fun get(
        route: String
    ): ScreenProvider?

    fun screenProviders(): List<ScreenProvider>

}