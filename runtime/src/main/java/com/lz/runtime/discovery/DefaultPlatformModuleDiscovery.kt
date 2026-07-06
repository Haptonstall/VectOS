package com.lz.runtime.discovery

import com.lz.runtime.api.RuntimeModuleProvider

/**
 * Default implementation used by tests.
 *
 * Android replaces this with AndroidPlatformModuleDiscovery.
 */
class DefaultPlatformModuleDiscovery(

    private val providers: List<RuntimeModuleProvider> = emptyList()

) : PlatformModuleDiscovery {

    override fun discoverProviders(): List<RuntimeModuleProvider> =
        providers
}