package com.lz.vectos.app.runtime

import com.lz.runtime.api.RuntimeModuleProvider
import com.lz.runtime.api.RuntimeModuleProviderResolver
import com.lz.runtime.api.marketplace.InstalledModule

class AndroidRuntimeModuleProviderResolver :
    RuntimeModuleProviderResolver {

    override fun resolve(
        module: InstalledModule
    ): RuntimeModuleProvider {

        println("Loading provider ${module.entryPoint.value}")

        val clazz =
            Class.forName(module.entryPoint.value)

        println("Loaded ${clazz.name}")

        return clazz
            .getDeclaredConstructor()
            .newInstance() as RuntimeModuleProvider
    }
}
