package com.lz.runtime.api.capabilities

import com.lz.runtime.api.NavigationDestination

/**
 * Capability contributed by a calculator module.
 */
interface CalculatorCapability : ModuleCapability {

    /**
     * Supports Quick Calc mode.
     */
    val supportsQuickCalc: Boolean

    /**
     * Logical destination.
     */
    val destination: NavigationDestination
}