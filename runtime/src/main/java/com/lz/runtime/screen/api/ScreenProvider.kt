package com.lz.runtime.screen.api

import com.lz.runtime.api.NavigationDestination

interface ScreenProvider {

    /**
     * RuntimeModule that owns these screens.
     */
    val runtimeModuleId: String

    /**
     * All destinations contributed by this provider.
     */
    fun destinations(): List<NavigationDestination>

    /**
     * Returns every screen owned by this module.
     */
    fun screens(): List<RuntimeScreen>

}