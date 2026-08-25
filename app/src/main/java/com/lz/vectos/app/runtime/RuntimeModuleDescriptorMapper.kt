package com.lz.vectos.app.runtime

import com.lz.common.ModuleType
import com.lz.domain.module.ModuleDescriptor
import com.lz.runtime.api.RuntimeModuleDescriptor
import com.lz.runtime.api.capabilities.CalculatorCapability

fun RuntimeModuleDescriptor.toModuleDescriptor(
    capability: CalculatorCapability
) =

    ModuleDescriptor(

        id = id,

        displayName = capability.displayName,

        description = description,

        version = version,

        moduleType = ModuleType.CALCULATION,

        dynamicFeatureName = id,

        entryPointClassName = entryPointClassName,

        requiresSubscription = true,

        supportsProjectMode = capability.supportsProjectMode,

        supportsQuickCalcMode = capability.supportsQuickCalcMode

    )
