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
    /**
     * Fully-qualified class name of this module's RuntimeModuleProvider
     * (e.g. "com.lz.beam.api.BeamRuntimeModuleProvider") — instantiated via
     * reflection once the dynamic feature identified by [dynamicFeatureName]
     * is confirmed installed. See SubscriptionAwareInstalledModuleRepository.
     */
    val entryPointClassName: String,
    val requiresSubscription: Boolean = true,
    val supportsProjectMode: Boolean,
    val supportsQuickCalcMode: Boolean
)