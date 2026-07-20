package com.lz.runtime.loader

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.api.RuntimeModuleLoadFailedEvent
import com.lz.runtime.api.RuntimeModuleProviderResolver
import com.lz.runtime.api.RuntimeService
import com.lz.runtime.api.marketplace.InstallState
import com.lz.runtime.repository.InstalledModuleRepository

class RuntimeModuleInstaller(

    private val repository: InstalledModuleRepository,

    private val providerResolver: RuntimeModuleProviderResolver

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
                        providerResolver
                            .resolve(module)
                            .createModule()

                    install(runtimeModule)
                    println("Provider resolved")
                } catch (error: Throwable) {

                    context.eventBus.publish(
                        RuntimeModuleLoadFailedEvent(
                            providerName = module.entryPoint.value,
                            message = error.message
                        )
                    )

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

        module.initialize(
            context.runtimeEnvironment
        )

        context.runtimeModuleRegistry.register(
            module
        )

        module
            .capabilities()
            .forEach { capability ->
                context
                    .capabilityRegistry
                    .register(capability)
            }
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