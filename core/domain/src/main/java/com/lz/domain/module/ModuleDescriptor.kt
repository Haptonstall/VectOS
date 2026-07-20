package com.lz.domain.module

import com.lz.common.Version
import com.lz.common.ModuleType

data class ModuleDescriptor(
    val id: String,
    val displayName: String,
    val description: String,
    val version: Version,
    val moduleType: ModuleType,
    val dynamicFeatureName: String,
    val requiresSubscription: Boolean = true,
    val supportsProjectMode: Boolean,
    val supportsQuickCalcMode: Boolean
)