package com.lz.runtime.api

import com.lz.runtime.api.marketplace.InstalledModule

/**
 * Loads RuntimeModules from installed module metadata.
 */
interface RuntimeModuleLoader {

    fun load(
        module: InstalledModule
    ): RuntimeModule

}