package com.lz.runtime.loader

import com.lz.runtime.api.NavigationDestination
import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.api.RuntimeEnvironment

class RuntimeModuleInstaller(

    private val context: RuntimeContext

) {

    fun install(
        module: RuntimeModule
    ) {

        //----------------------------------------------------
        // Register RuntimeModule
        //----------------------------------------------------

        context.runtimeModuleRegistry.register(module)

        //----------------------------------------------------
        // Register capabilities
        //----------------------------------------------------

        module.capabilities()

            .forEach {

                context.capabilityRegistry.register(it)

            }

        //----------------------------------------------------
        // Register navigation
        //----------------------------------------------------

        module.capabilities()

            .filterIsInstance<NavigationDestination>()

            .forEach {

                context.navigationRegistry.register(it)

            }

        //----------------------------------------------------
        // Register screens
        //----------------------------------------------------

        module.screenProviders()

            .forEach {

                context.screenRegistry.register(it)

            }

        //----------------------------------------------------
        // Initialize RuntimeModule
        //----------------------------------------------------

        module.initialize(

            context.runtimeEnvironment

        )

    }

    fun uninstall(
        runtimeModule: RuntimeModule
    ) {

        //----------------------------------------------------
        // Shutdown
        //----------------------------------------------------

        runtimeModule.shutdown(

            context.runtimeEnvironment

        )

        //----------------------------------------------------
        // Remove screens
        //----------------------------------------------------

        runtimeModule.screenProviders()

            .forEach {

                context.screenRegistry.unregister(

                    it.runtimeModuleId

                )

            }

        //----------------------------------------------------
        // Remove navigation
        //----------------------------------------------------

        runtimeModule.capabilities()

            .filterIsInstance<NavigationDestination>()

            .forEach {

                context.navigationRegistry.unregister(

                    it.id

                )

            }

        //----------------------------------------------------
        // Remove capabilities
        //----------------------------------------------------

        runtimeModule.capabilities()

            .forEach {

                context.capabilityRegistry.unregister(

                    it.id

                )

            }

        //----------------------------------------------------
        // Remove RuntimeModule
        //----------------------------------------------------

        context.runtimeModuleRegistry.unregister(

            runtimeModule.descriptor.id

        )

    }
}