package com.lz.runtime.discovery

import com.lz.runtime.api.RuntimeModuleProvider

/**
 * Platform specific module discovery.
 *
 * RuntimeEnvironment Core remains platform independent.
 *
 * The Android application supplies the implementation.
 */
interface PlatformModuleDiscovery {

    fun discoverProviders(): List<RuntimeModuleProvider>
}