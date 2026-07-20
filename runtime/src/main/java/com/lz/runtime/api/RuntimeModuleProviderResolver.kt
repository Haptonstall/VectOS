package com.lz.runtime.api

import com.lz.runtime.api.marketplace.InstalledModule

/**
 * Resolves an installed module manifest into a RuntimeModuleProvider.
 *
 * Runtime intentionally knows nothing about reflection,
 * Android class loading, Play Feature Delivery, etc.
 */
interface RuntimeModuleProviderResolver {

    fun resolve(
        module: InstalledModule
    ): RuntimeModuleProvider

}