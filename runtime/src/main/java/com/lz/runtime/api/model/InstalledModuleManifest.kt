package com.lz.runtime.model

/**
 * Represents one installed engineering module.
 *
 * This model is intentionally lightweight. Future marketplace metadata
 * (license, purchase date, etc.) can be added without changing the Runtime.
 */
data class InstalledModuleManifest(

    /**
     * Unique module id.
     */
    val moduleId: String,

    /**
     * Provider implementation class.
     */
    val providerClass: String,

    /**
     * True when the module is enabled.
     */
    val enabled: Boolean = true
)