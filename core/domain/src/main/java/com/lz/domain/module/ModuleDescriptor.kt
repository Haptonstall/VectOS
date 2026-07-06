package com.lz.domain.module

data class ModuleDescriptor(
    val id: String,
    val displayName: String,
    val description: String,
    val version: String,
    val moduleType: ModuleType,
    val dynamicFeatureName: String,
    val requiresSubscription: Boolean = true,
    val supportsProjectMode: Boolean,
    val supportsQuickCalcMode: Boolean
)