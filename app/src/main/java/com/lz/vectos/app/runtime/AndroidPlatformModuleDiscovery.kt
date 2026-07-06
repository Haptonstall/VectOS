package com.lz.vectos.app.runtime

import com.lz.runtime.api.RuntimeModuleProvider
import com.lz.runtime.discovery.PlatformModuleDiscovery
import com.lz.runtime.repository.InstalledModuleRepository

/**
 * Android implementation of Runtime module discovery.
 *
 * Reads installed module manifests and creates providers.
 */
class AndroidPlatformModuleDiscovery(

    private val repository: InstalledModuleRepository,

    private val providerFactory: RuntimeModuleProviderFactory

) : PlatformModuleDiscovery {

    override fun discoverProviders(): List<RuntimeModuleProvider> {

        return repository
            .installedModules()
            .asSequence()
            .filter { it.enabled }
            .map {

                providerFactory.create(
                    it.providerClass
                )

            }
            .toList()

    }

}