package com.lz.runtime.api.capabilities

interface ReportCapability : ModuleCapability {

    /**
     * Generates one or more reports for the owning RuntimeModule.
     */
    val supportedFormats: Set<ReportFormat>
}