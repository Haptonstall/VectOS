package com.lz.runtime.api.capabilities

interface ExportCapability : ModuleCapability {

    /**
     * Supported export types.
     */
    val supportedFormats: Set<ExportFormat>
}