package com.lz.runtime.api.capabilities

interface SettingsCapability : ModuleCapability {

    /**
     * Indicates whether this module contributes
     * configurable application settings.
     */
    val hasSettings: Boolean
}