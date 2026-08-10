package com.lz.runtime.loader

import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.api.RuntimeModuleLoader
import com.lz.runtime.api.RuntimeModuleProviderResolver
import com.lz.runtime.api.marketplace.InstalledModule

class DefaultRuntimeModuleLoader(

    private val providerResolver: RuntimeModuleProviderResolver

) : RuntimeModuleLoader {

    override fun load(
        module: InstalledModule
    ): RuntimeModule {

        return providerResolver
            .resolve(module)
            .createModule()

    }

}