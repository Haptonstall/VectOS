package com.lz.runtime.api.capabilities

/**
 * Capability contributed by a calculator module.
 */
interface CalculatorCapability : ModuleCapability {

    val description: String

    val destination: String

    val supportsProjectMode: Boolean

    val supportsQuickCalcMode: Boolean

}
