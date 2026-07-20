package com.lz.runtime.api.marketplace

import com.lz.common.Version

data class InstalledModule(

    val moduleId: String,

    val displayName: String,

    val version: Version,

    val installState: InstallState,

    val enabled: Boolean,

    val source: ModuleSource,

    val featureName: String?,

    val entryPoint: ModuleEntryPoint,

    val signature: String?
)

enum class InstallState {

    NOT_INSTALLED,

    INSTALLED,

    UPDATE_AVAILABLE,

    INSTALLING,

    FAILED
}

enum class ModuleSource {

    BUNDLED,

    MARKETPLACE,

    DYNAMIC_FEATURE,

    ENTERPRISE,

    LOCAL
}

data class ModuleEntryPoint(

    val value: String
)
