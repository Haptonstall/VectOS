package com.lz.runtime.api.capabilities

import com.lz.runtime.api.CapabilityType

/**
 * Base contract implemented by every capability contributed by a RuntimeModule.
 *
 * Examples:
 *
 * Beam Calculator
 * Beam Reports
 * Steel Materials
 * AISC Design Codes
 */
interface ModuleCapability {

    /**
     * Unique identifier.
     */
    val id: String

    /**
     * Display name.
     */
    val displayName: String

    /**
     * Category implemented by this capability.
     */
    val capabilityType: CapabilityType

    /**
     * RuntimeModule providing this capability.
     */
    val runtimeModuleId: String
}