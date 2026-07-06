package com.lz.vectos.app.runtime

import com.lz.runtime.api.RuntimeModuleProvider

class ReflectionRuntimeModuleProviderFactory :
    RuntimeModuleProviderFactory {

    override fun create(

        className: String

    ): RuntimeModuleProvider {

        val clazz =

            Class
                .forName(className)
        val constructor =
            clazz.getDeclaredConstructor()

        constructor.isAccessible = true

        return constructor.newInstance()

                as RuntimeModuleProvider

    }

}