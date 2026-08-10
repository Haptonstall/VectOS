package com.lz.runtime.compose.api

import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.compose.screen.api.ScreenProvider

/**
 * Optional interface implemented by RuntimeModules that contribute
 * Compose UI to the application.
 *
 * Runtime itself never depends on this interface.
 */
interface ComposeRuntimeModule :
    RuntimeModule {

    /**
     * Returns every ScreenProvider contributed by this module.
     */
    fun screenProviders(): List<ScreenProvider>
}