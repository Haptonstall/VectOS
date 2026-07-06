package com.lz.runtime.loader

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeModuleLoader
import com.lz.runtime.api.RuntimeModuleProvider
import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.core.AbstractRuntimeService
import com.lz.runtime.discovery.PlatformModuleDiscovery

/**
 * Default RuntimeEnvironment RuntimeModuleLoader.
 *
 * This implementation delegates provider discovery to the
 * PlatformModuleDiscovery service.
 */
class DefaultRuntimeModuleLoader(

    context: RuntimeContext,
    private val discovery: PlatformModuleDiscovery

) : AbstractRuntimeService(
    "runtimeEnvironment.moduleLoader"
), RuntimeModuleLoader {

    override val id =
        "runtime.module.loader"

    override fun initialize(
        context: RuntimeContext
    ) {
        // nothing required
    }

    override fun shutdown(
        context: RuntimeContext
    ) {
        // nothing required
    }

    override fun discoverProviders(): List<RuntimeModuleProvider> {

        return discovery.discoverProviders()
    }

    override fun loadModules(): List<RuntimeModule> {

        return discoverProviders()
            .map { it.createModule() }
    }
}