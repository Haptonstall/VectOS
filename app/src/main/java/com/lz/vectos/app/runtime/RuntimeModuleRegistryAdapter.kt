package com.lz.vectos.app.runtime

import com.lz.domain.module.ModuleDescriptor
import com.lz.domain.module.ModuleRegistry
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.common.ModuleType
import com.lz.runtime.api.CapabilityType
import com.lz.runtime.api.RuntimeModuleDescriptor
import com.lz.runtime.api.capabilities.CalculatorCapability
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuntimeModuleRegistryAdapter @Inject constructor(

    private val runtime: RuntimeEnvironment

) : ModuleRegistry {

    override fun register(
        descriptor: ModuleDescriptor
    ) {
        // Runtime owns registration.
    }

    override fun unregister(
        moduleId: String
    ) {
        // Runtime owns registration.
    }

    override fun getModule(
        moduleId: String
    ): ModuleDescriptor? {

        return getModules()
            .firstOrNull {
                it.id == moduleId
            }

    }

    override fun getModules(): List<ModuleDescriptor> {
        println(
            "Capabilities = ${
                runtime.context.capabilityRegistry.capabilities().size
            }"
        )
        println(
            "Modules = ${
                runtime.context.runtimeModuleRegistry.modules().size
            }"
        )

        return runtime.context
            .capabilityRegistry
            .capabilities(CapabilityType.CALCULATOR)
            .filterIsInstance<CalculatorCapability>()
            .mapNotNull { capability ->

                runtime.context
                    .runtimeModuleRegistry
                    .get(capability.runtimeModuleId)
                    ?.descriptor
                    ?.toDomain(capability)

            }

    }
}

private fun RuntimeModuleDescriptor.toDomain(
    capability: CalculatorCapability
) =

    ModuleDescriptor(

        id = id,

        displayName = capability.displayName,

        description = description,

        version = version,

        dynamicFeatureName = id,

        moduleType = ModuleType.CALCULATION,

        supportsProjectMode = capability.supportsProjectMode,

        supportsQuickCalcMode = capability.supportsQuickCalcMode

    )
