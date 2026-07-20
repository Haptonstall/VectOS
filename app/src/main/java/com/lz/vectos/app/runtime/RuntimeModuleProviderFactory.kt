package com.lz.vectos.app.runtime

import com.lz.runtime.api.RuntimeModuleProvider
import com.lz.runtime.api.marketplace.InstalledModule

/**
 * Creates RuntimeModuleProvider instances from installed module metadata.
 *
 * Android is responsible for locating provider implementations.
 */
interface RuntimeModuleProviderFactory {

    fun create(

        installedModule: InstalledModule

    ): RuntimeModuleProvider

}
