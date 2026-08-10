package com.lz.runtime.loader

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.api.RuntimeModuleLoader
import com.lz.runtime.api.RuntimeService
import com.lz.runtime.api.marketplace.InstallState
import com.lz.runtime.repository.InstalledModuleRepository

class RuntimeModuleInstaller(

    private val repository: InstalledModuleRepository,

    private val moduleLoader: RuntimeModuleLoader

) : RuntimeService {

    override val id = "runtime.module.installer"

    private lateinit var context: RuntimeContext

    override fun initialize(
        context: RuntimeContext
    ) {
        this.context = context
    }

    override fun shutdown(
        context: RuntimeContext
    ) {
        // Nothing required.
    }

    fun installAll() {

        println("Runtime: installAll()")

        repository
            .installedModules()
            .asSequence()
            .filter {
                it.enabled &&
                        it.installState == InstallState.INSTALLED
            }
            .forEach { module ->
                println("Runtime: ${module.moduleId}")
                try {

                    val runtimeModule =
                        moduleLoader.load(module)

                    install(runtimeModule)
                    println("Provider resolved")
                } catch (error: Throwable) {

                    throw error

                }
            }

    }

    fun uninstallAll() {
        println("Runtime: uninstallAll()")

        context
            .runtimeModuleRegistry
            .modules()
            .reversed()
            .forEach {

                uninstall(it)

            }

    }

    fun install(
        module: RuntimeModule
    ) {

        println("Initializing ${module.descriptor.id}")

        module.initialize(
            context.runtimeEnvironment
        )

        println("Registring module ${module.descriptor.id}")

        context.runtimeModuleRegistry.register(
            module
        )

        module
            .capabilities()
            .forEach { capability ->
                println("Register capability ${capability.id}")
                context
                    .capabilityRegistry
                    .register(capability)
                println("Registered capability: ${capability.id}")
            }

        println(
            "Capability count = ${
                context.capabilityRegistry.capabilities().size
            }"
        )
    }

    fun uninstall(
        module: RuntimeModule
    ) {

        module
            .capabilities()
            .forEach { capability ->
                context
                    .capabilityRegistry
                    .unregister(
                        capability.id
                    )
            }

        module.shutdown(
            context.runtimeEnvironment
        )

        context
            .runtimeModuleRegistry
            .unregister(
            module.descriptor.id
            )
    }

}