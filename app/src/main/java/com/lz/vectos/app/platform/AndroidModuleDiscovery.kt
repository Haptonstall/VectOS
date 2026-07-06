package com.lz.vectos.app.platform

import android.content.Context
import com.lz.runtime.discovery.PlatformModuleDiscovery
import com.lz.runtime.api.RuntimeModuleProvider

/**
 * Android implementation of Runtime module discovery.
 *
 * This implementation discovers providers registered through
 * Android Manifest metadata.
 */
class AndroidModuleDiscovery(

    private val context: Context

) : PlatformModuleDiscovery {

    override fun discoverProviders():
            List<RuntimeModuleProvider> {

        // Placeholder until Manifest discovery is added.
        return emptyList()
    }
}